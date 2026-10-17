#!/usr/bin/env bash
set -euo pipefail

gw=${1:?uso: gerar-trafego.sh <url-do-gateway> <email-admin> <senha-admin> [quantidade-de-ordens] [pausa-em-segundos]}
admin_email=${2:?}
admin_password=${3:?}
total=${4:-8}
pausa=${5:-1}
json='Content-Type: application/json'

campo() { python3 -c "import sys,json; print(json.load(sys.stdin)$1)"; }
chamar() { curl -sf -X "$1" "$gw$2" -H "Authorization: Bearer $admin" -H "$json" ${3:+-d "$3"}; }
codigo() { curl -s -o /dev/null -w '%{http_code}' -X "$1" "$gw$2" -H "Authorization: Bearer $admin" -H "$json" ${3:+-d "$3"}; }
cpf_aleatorio() {
  python3 -c '
import random
n = [random.randint(0, 9) for _ in range(9)]
for _ in range(2):
    s = sum(v * (len(n) + 1 - i) for i, v in enumerate(n))
    n.append((s * 10 % 11) % 10)
print("".join(map(str, n)))'
}
placa_aleatoria() {
  python3 -c '
import random, string
print("".join(random.choices(string.ascii_uppercase, k=3)) + str(random.randint(0, 9)) + random.choice(string.ascii_uppercase) + "".join(map(str, random.choices(range(10), k=2))))'
}

admin=$(curl -sf -X POST "$gw/auth/login" -H "$json" \
  -d "{\"email\":\"$admin_email\",\"senha\":\"$admin_password\"}" | campo '["access_token"]')

existentes=$(chamar GET /templates)
criar_template() {
  if [[ "$existentes" != *"\"codigo\":\"$1\""* ]]; then
    codigo POST /templates "{\"codigo\":\"$1\",\"conteudo\":\"$2\"}" > /dev/null
  fi
}
criar_template AUTORIZAR_ORCAMENTO "Precisamos da sua autorização para iniciar os reparos."
criar_template RETIRAR_VEICULO "Seu veículo está pronto para retirada."
criar_template VEICULO_RETIRADO "Obrigado por retirar seu veículo."
criar_template REPOSICAO_ESTOQUE "Reposição de estoque necessária: %s"

servico=$(chamar POST /servicos '{"nome":"Troca de óleo","descricao":"Troca de óleo e filtro","valor":150.00}' | campo '["id"]')
insumo=$(chamar POST /estoque '{"insumo":{"nome":"Óleo 5W30","precoUnitario":45.90},"quantidade":1000}' | campo '["insumo"]["id"]')

for i in $(seq 1 "$total"); do
  cliente=$(chamar POST /cliente "{\"nome\":\"Cliente Tráfego $i\",\"documento\":\"$(cpf_aleatorio)\",\"email\":\"trafego$i@mecanica.com\",\"telefone\":\"31999999999\",\"endereco\":{\"cep\":\"30000-000\",\"estado\":\"MG\",\"cidade\":\"Belo Horizonte\",\"bairro\":\"Centro\",\"rua\":\"Rua A\",\"numero\":\"1\"}}" | campo '["id"]')
  veiculo=$(chamar POST /veiculos "{\"marca\":\"Volkswagen\",\"modelo\":\"Gol\",\"placa\":\"$(placa_aleatoria)\",\"ano\":2022}" | campo '["id"]')
  os=$(chamar POST /atendimento/iniciar "{\"cliente\":$cliente,\"veiculo\":$veiculo,\"relatoCliente\":\"Barulho na suspensão dianteira\",\"servicos\":[]}" | campo '["id"]')
  sleep "$pausa"
  chamar PATCH "/atendimento/$os/diagnostico/iniciar" > /dev/null
  sleep "$pausa"
  chamar POST "/atendimento/$os/diagnostico" "{\"observacoes\":\"Necessária troca de óleo e filtro.\",\"servicos\":[{\"servico\":$servico,\"quantidade\":1}],\"insumos\":[{\"insumo\":$insumo,\"quantidade\":2}]}" > /dev/null
  sleep "$pausa"
  if (( i % 4 == 0 )); then
    chamar POST "/atendimento/$os/cancelar" > /dev/null
    echo "OS $i cancelada ($os)"
    continue
  fi
  chamar POST "/atendimento/$os/aprovar" > /dev/null
  sleep "$pausa"
  chamar POST "/atendimento/$os/finalizar" > /dev/null
  sleep "$pausa"
  chamar POST "/atendimento/$os/entregar" > /dev/null
  echo "OS $i entregue ($os)"
done

abrir_os_com_diagnostico() {
  local marca=$1 relato=$2 insumo_id=$3 quantidade=$4
  local c v o
  c=$(chamar POST /cliente "{\"nome\":\"Cliente Falha\",\"documento\":\"$(cpf_aleatorio)\",\"email\":\"falha@mecanica.com\",\"telefone\":\"31999999999\",\"endereco\":{\"cep\":\"30000-000\",\"estado\":\"MG\",\"cidade\":\"Belo Horizonte\",\"bairro\":\"Centro\",\"rua\":\"Rua A\",\"numero\":\"1\"}}" | campo '["id"]')
  v=$(chamar POST /veiculos "{\"marca\":\"$marca\",\"modelo\":\"Uno\",\"placa\":\"$(placa_aleatoria)\",\"ano\":2020}" | campo '["id"]')
  o=$(chamar POST /atendimento/iniciar "{\"cliente\":$c,\"veiculo\":$v,\"relatoCliente\":\"$relato\",\"servicos\":[]}" | campo '["id"]')
  chamar PATCH "/atendimento/$o/diagnostico/iniciar" > /dev/null
  chamar POST "/atendimento/$o/diagnostico" "{\"observacoes\":\"$relato\",\"servicos\":[],\"insumos\":[{\"insumo\":$insumo_id,\"quantidade\":$quantidade}]}" > /dev/null
  echo "$o"
}

insumo_escasso=$(chamar POST /estoque '{"insumo":{"nome":"Fluido raro","precoUnitario":10.00},"quantidade":1}' | campo '["insumo"]["id"]')
os=$(abrir_os_com_diagnostico Fiat "Falta insumo." "$insumo_escasso" 5)
echo "Aprovação com estoque insuficiente (esperado 400): $(codigo POST "/atendimento/$os/aprovar")"
echo "Consulta de OS inexistente (esperado 404): $(codigo GET "/atendimento/00000000-0000-0000-0000-000000000000")"

if [[ -n "${PARAR_ESTOQUE_CMD:-}" ]]; then
  os=$(abrir_os_com_diagnostico Chevrolet "Integração com estoque indisponível." "$insumo" 1)
  bash -c "$PARAR_ESTOQUE_CMD"
  trap 'bash -c "${RETOMAR_ESTOQUE_CMD:-true}"' EXIT
  echo "Aprovação com o estoque fora do ar (esperado 500): $(codigo POST "/atendimento/$os/aprovar")"
fi
echo "Tráfego gerado em $gw"
