import { randomBytes } from 'node:crypto';

const gateway = process.env.GATEWAY?.replace(/\/$/, '');
const adminPassword = process.env.ADMIN_PASSWORD;
if (!gateway || !adminPassword || process.env.CONFIRM_PRODUCTION_WRITES !== 'YES') {
  throw new Error('Defina GATEWAY, ADMIN_PASSWORD e CONFIRM_PRODUCTION_WRITES=YES.');
}

const results = [];
const ids = {};
let adminToken;
let clientToken;

async function request(method, path, body, token) {
  const response = await fetch(`${gateway}${path}`, {
    method,
    headers: {
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(30000),
  });
  const text = await response.text();
  let data;
  try { data = JSON.parse(text); } catch { data = undefined; }
  return { status: response.status, data };
}

function verify(label, response, expected = '2xx') {
  const passed = expected === '2xx'
    ? response.status >= 200 && response.status < 300
    : response.status === expected;
  results.push({ label, status: response.status, passed });
  console.log(`${passed ? 'OK' : 'FALHA'} ${label}: HTTP ${response.status}`);
  return passed;
}

async function test(label, method, path, body, token = adminToken, expected = '2xx') {
  try {
    const response = await request(method, path, body, token);
    verify(label, response, expected);
    return response;
  } catch (error) {
    results.push({ label, status: error.message, passed: false });
    console.log(`FALHA ${label}: ${error.message}`);
    return { status: 0 };
  }
}

function testCpf() {
  const digits = Array.from(randomBytes(9), value => value % 10);
  if (digits.every(value => value === digits[0])) digits[0] = (digits[0] + 1) % 10;
  for (let size = 9; size <= 10; size++) {
    const sum = digits.slice(0, size).reduce((total, value, index) => total + value * (size + 1 - index), 0);
    const remainder = (sum * 10) % 11;
    digits.push(remainder === 10 ? 0 : remainder);
  }
  return digits.join('');
}

const runId = `${Date.now().toString(36)}${randomBytes(2).toString('hex')}`;
const cpf = testCpf();
const plate = `QA${String.fromCharCode(65 + randomBytes(1)[0] % 26)}1A${String(randomBytes(1)[0] % 100).padStart(2, '0')}`;
const email = `qa-${runId}@example.com`;
const templateCodes = ['AUTORIZAR_ORCAMENTO', 'RETIRAR_VEICULO', 'VEICULO_RETIRADO', 'REPOSICAO_ESTOQUE'];
const createdTemplates = [];

try {
  const login = await test('Login administrador', 'POST', '/auth/login', {
    email: 'admin@mecanica.com', senha: adminPassword,
  }, undefined, 200);
  adminToken = login.data?.access_token;
  if (!adminToken) throw new Error('Login sem access_token; demais testes não podem continuar.');

  await test('Bloquear lista de clientes sem token', 'GET', '/cliente', undefined, '', 401);
  await test('Listar clientes com token admin', 'GET', '/cliente', undefined, adminToken, 200);
  for (const path of ['/veiculos', '/funcionarios', '/servicos', '/estoque']) {
    await test(`Listar ${path}`, 'GET', path, undefined, '', 200);
  }
  for (const path of ['/atendimento/abertos', '/templates']) {
    await test(`Bloquear sem token ${path}`, 'GET', path, undefined, '', 401);
  }
  await test('Bloquear escrita sem token', 'POST', '/cliente', {}, '', 401);
  await test('CPF inválido', 'POST', '/auth/cpf', { cpf: '111.111.111-11' }, '', 400);
  await test('CPF inexistente', 'POST', '/auth/cpf', { cpf: '168.995.350-09' }, '', 401);

  const client = await test('Criar cliente QA', 'POST', '/cliente', {
    nome: `Cliente QA ${runId}`, documento: cpf, email,
    telefone: '11987654321', endereco: {
      cep: '01310100', estado: 'SP', cidade: 'Sao Paulo', bairro: 'Centro',
      rua: 'Rua de Teste', numero: '100', complemento: 'QA',
    },
  });
  ids.client = client.data?.id;
  if (ids.client) {
    await test('Buscar cliente por ID', 'GET', `/cliente/${ids.client}`, undefined, adminToken, 200);
    await test('Buscar cliente por documento', 'GET', `/cliente/documento?documento=${cpf}`, undefined, adminToken, 200);
    await test('Atualizar cliente', 'PATCH', `/cliente/${ids.client}`, {
      nome: `Cliente QA atualizado ${runId}`, documento: cpf, email,
      telefone: '11999998888', endereco: {
        cep: '01310100', estado: 'SP', cidade: 'Sao Paulo', bairro: 'Centro',
        rua: 'Rua de Teste', numero: '101', complemento: 'QA',
      },
    });
    const cpfLogin = await test('Login CPF do cliente QA', 'POST', '/auth/cpf', { cpf }, '', 200);
    clientToken = cpfLogin.data?.access_token;
    if (clientToken) {
      await test('Minhas OS com token cliente', 'GET', '/atendimento/cliente/minhas', undefined, clientToken, 200);
      await test('Impedir lista administrativa ao cliente', 'GET', '/atendimento/abertos', undefined, clientToken, 403);
      await test('Impedir lista de clientes ao cliente', 'GET', '/cliente', undefined, clientToken, 403);
    }
  }

  const vehicle = await test('Criar veículo QA', 'POST', '/veiculos', {
    marca: 'Volkswagen', modelo: 'Gol QA', placa: plate, ano: 2022,
  });
  ids.vehicle = vehicle.data?.id;
  if (ids.vehicle) {
    await test('Buscar veículo por ID', 'GET', `/veiculos/${ids.vehicle}`, undefined, adminToken, 200);
    await test('Buscar veículo por placa', 'GET', `/veiculos/placa/${plate}`, undefined, adminToken, 200);
    await test('Atualizar veículo', 'PATCH', `/veiculos/${ids.vehicle}`, {
      marca: 'Volkswagen', modelo: 'Gol QA atualizado', ano: 2023,
    });
    if (ids.client) {
      await test('Vincular veículo ao cliente', 'PUT',
        `/cliente/${ids.client}/veiculo/${ids.vehicle}?placa=${plate}&marca=Volkswagen&modelo=Gol&ano=2022`);
      await test('Listar veículos do cliente', 'GET', `/cliente/${ids.client}/veiculos`, undefined, adminToken, 200);
    }
  }

  const employee = await test('Criar funcionário QA', 'POST', '/funcionarios', {
    email, nome: `Mecanico QA ${runId}`, senha: 'SenhaTeste123!', funcao: 'MECANICO',
  });
  ids.employee = employee.data?.id;
  if (ids.employee) {
    await test('Buscar funcionário por ID', 'GET', `/funcionarios/${ids.employee}`, undefined, adminToken, 200);
    await test('Buscar funcionário por e-mail', 'GET', `/funcionarios/email/${email}`, undefined, adminToken, 200);
    await test('Atualizar funcionário', 'PATCH', `/funcionarios/${ids.employee}`, {
      nome: `Mecanico QA atualizado ${runId}`, funcao: 'MECANICO',
    });
  }

  const service = await test('Criar serviço QA', 'POST', '/servicos', {
    nome: `Servico QA ${runId}`, descricao: 'Teste de endpoints', valor: 150.0,
  });
  ids.service = service.data?.id;
  if (ids.service) {
    await test('Buscar serviço por ID', 'GET', `/servicos/${ids.service}`, undefined, adminToken, 200);
    await test('Atualizar serviço', 'PATCH', `/servicos/${ids.service}`, {
      descricao: 'Teste de endpoints atualizado', valor: 175.0,
    });
  }

  const stock = await test('Criar insumo QA', 'POST', '/estoque', {
    insumo: { nome: `Insumo QA ${runId}`, precoUnitario: 45.9 }, quantidade: 20,
  });
  ids.stock = stock.data?.insumo?.id ?? stock.data?.id;
  if (ids.stock) {
    await test('Buscar insumo por ID', 'GET', `/estoque/${ids.stock}`, undefined, adminToken, 200);
    await test('Alterar quantidade', 'PUT', `/estoque/${ids.stock}`, { quantidade: 25 });
    await test('Atualizar insumo', 'PATCH', `/estoque/insumo/${ids.stock}`, {
      nome: `Insumo QA atualizado ${runId}`, precoUnitario: 49.9,
    });
    await test('Deduzir insumo', 'POST', '/estoque/deduzir', [{ insumoId: ids.stock, quantidade: 1 }]);
  }

  const existingTemplates = await test('Listar templates antes do teste', 'GET', '/templates', undefined, adminToken, 200);
  const existingCodes = new Set((existingTemplates.data ?? []).map(item => item.codigo));
  for (const code of templateCodes) {
    if (existingCodes.has(code)) continue;
    const created = await test(`Criar template ${code}`, 'POST', '/templates', {
      codigo: code, conteudo: `Mensagem QA ${runId}`,
    });
    if (created.status >= 200 && created.status < 300) createdTemplates.push(code);
  }
  const templateCode = createdTemplates[0];
  if (templateCode) {
    await test('Buscar template QA', 'GET', `/templates/${templateCode}`, undefined, adminToken, 200);
    await test('Atualizar template QA', 'PUT', `/templates/${templateCode}`, {
      conteudo: 'Mensagem de teste QA atualizada',
    });
  }

  if (ids.client && ids.vehicle && ids.service && ids.stock) {
    const order = await test('Iniciar atendimento QA', 'POST', '/atendimento/iniciar', {
      cliente: ids.client, veiculo: ids.vehicle, relatoCliente: `Teste de endpoints QA ${runId}`, servicos: [],
    });
    ids.order = order.data?.id;
    if (ids.order) {
      await test('Buscar atendimento por ID', 'GET', `/atendimento/${ids.order}`, undefined, adminToken, 200);
      await test('Consultar status da OS', 'GET', `/atendimento/${ids.order}/status`, undefined, adminToken, 200);
      await test('Listar atendimentos abertos', 'GET', '/atendimento/abertos', undefined, adminToken, 200);
      await test('Iniciar diagnóstico', 'PATCH', `/atendimento/${ids.order}/diagnostico/iniciar`);
      await test('Finalizar diagnóstico', 'POST', `/atendimento/${ids.order}/diagnostico`, {
        observacoes: 'Diagnostico de teste QA',
        servicos: [{ servico: ids.service, quantidade: 1 }],
        insumos: [{ insumo: ids.stock, quantidade: 2 }],
      });
      await test('Aprovar OS', 'POST', `/atendimento/${ids.order}/aprovar`);
      await test('Decisão externa do orçamento', 'POST', `/atendimento/${ids.order}/decisao-orcamento`, { aprovado: true });
      await test('Finalizar OS', 'POST', `/atendimento/${ids.order}/finalizar`);
      await test('Entregar veículo', 'POST', `/atendimento/${ids.order}/entregar`);
    }
  }
  await test('Relatório de tempo médio', 'GET', '/atendimento/relatorios/tempo-medio-servicos', undefined, adminToken, 200);
} finally {
  // As exclusões de cadastro são desativações lógicas. A OS fica no histórico para auditoria.
  for (const code of createdTemplates) {
    await test(`Excluir template QA ${code}`, 'DELETE', `/templates/${code}`);
  }
  for (const [name, path] of [
    ['insumo', ids.stock && `/estoque/${ids.stock}`],
    ['serviço', ids.service && `/servicos/${ids.service}`],
    ['funcionário', ids.employee && `/funcionarios/${ids.employee}`],
    ['veículo', ids.vehicle && `/veiculos/${ids.vehicle}`],
    ['cliente', ids.client && `/cliente/${ids.client}`],
  ]) {
    if (!path) continue;
    const removed = await test(`Desativar ${name} QA`, 'DELETE', path);
    if (removed.status < 200 || removed.status >= 300) continue;
    await test(`Reativar ${name} QA`, 'PUT', `${path}/reativar`);
    await test(`Desativar ${name} QA ao final`, 'DELETE', path);
  }
  const failures = results.filter(result => !result.passed);
  console.log(`RESULTADO ${results.length - failures.length}/${results.length} verificações passaram; ${failures.length} falhas. Run ID: ${runId}`);
  console.log(`IDs QA: cliente=${ids.client ?? '-'} veículo=${ids.vehicle ?? '-'} funcionário=${ids.employee ?? '-'} serviço=${ids.service ?? '-'} insumo=${ids.stock ?? '-'} OS=${ids.order ?? '-'}`);
  if (failures.length) process.exitCode = 1;
}
