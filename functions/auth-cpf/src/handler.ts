import type { APIGatewayProxyEvent, APIGatewayProxyResult } from "aws-lambda";
import { apenasDigitos, cpfValido, mascararCpf } from "./cpf";
import type { ClienteRepository } from "./db";
import { gerarTokenCliente } from "./jwt";
import { log } from "./logger";

export interface Dependencias {
  clientes: ClienteRepository;
  jwtSecret: string;
  jwtIssuer: string;
  jwtExpirationSeconds: number;
}

const CABECALHOS = { "Content-Type": "application/json" };

function erro(status: number, codigo: string, mensagem: string, correlationId: string): APIGatewayProxyResult {
  return {
    statusCode: status,
    headers: { ...CABECALHOS, "X-Request-ID": correlationId },
    body: JSON.stringify({ erros: [{ codigo, mensagem }] }),
  };
}

function lerCpf(corpo: string | null): string | null {
  if (!corpo) return null;
  try {
    const json = JSON.parse(corpo);
    return typeof json?.cpf === "string" ? json.cpf : null;
  } catch {
    return null;
  }
}

function correlationIdDe(event: APIGatewayProxyEvent): string {
  const cabecalhos = event.headers ?? {};
  const chave = Object.keys(cabecalhos).find((k) => k.toLowerCase() === "x-request-id");
  return (chave && cabecalhos[chave]) || "sem-correlation-id";
}

export function criarHandler(dependencias: Dependencias | (() => Promise<Dependencias>)) {
  let carregadas: Dependencias | undefined;

  return async (event: APIGatewayProxyEvent): Promise<APIGatewayProxyResult> => {
    const correlationId = correlationIdDe(event);
    const corpo = event.isBase64Encoded && event.body ? Buffer.from(event.body, "base64").toString("utf8") : event.body;
    const cpfInformado = lerCpf(corpo);

    if (cpfInformado === null) {
      log("WARN", "Requisição sem CPF", { correlation_id: correlationId, resultado: "requisicao-invalida" });
      return erro(400, "validacao", "Informe o CPF no corpo da requisição: {\"cpf\": \"...\"}.", correlationId);
    }

    const cpf = apenasDigitos(cpfInformado);
    if (!cpfValido(cpf)) {
      log("WARN", "CPF inválido", { correlation_id: correlationId, resultado: "cpf-invalido" });
      return erro(400, "validacao", "CPF inválido.", correlationId);
    }

    try {
      carregadas ??= typeof dependencias === "function" ? await dependencias() : dependencias;
      const cliente = await carregadas.clientes.buscarPorDocumento(cpf);

      if (!cliente || !cliente.ativo) {
        log("WARN", "Autenticação negada", {
          correlation_id: correlationId,
          cpf: mascararCpf(cpf),
          resultado: cliente ? "cliente-inativo" : "cliente-inexistente",
        });
        return erro(401, "nao-autorizado", "Não foi possível autenticar com o CPF informado.", correlationId);
      }

      const token = await gerarTokenCliente(
        cliente.id,
        carregadas.jwtSecret,
        carregadas.jwtIssuer,
        carregadas.jwtExpirationSeconds,
      );
      log("INFO", "Autenticação por CPF concluída", {
        correlation_id: correlationId,
        cpf: mascararCpf(cpf),
        cliente_id: cliente.id,
        resultado: "autenticado",
      });
      return { statusCode: 200, headers: { ...CABECALHOS, "X-Request-ID": correlationId }, body: JSON.stringify(token) };
    } catch (e) {
      log("ERROR", "Falha ao autenticar", {
        correlation_id: correlationId,
        resultado: "erro-interno",
        erro: e instanceof Error ? e.message : String(e),
      });
      return erro(500, "erro-interno", "Erro interno ao autenticar.", correlationId);
    }
  };
}
