#!/usr/bin/env bash
set -euo pipefail

gw=${1:?uso: ADMIN_PASSWORD=... smoke-test.sh <url-do-gateway> <email-admin>}
admin_email=${2:?}
: "${ADMIN_PASSWORD:?Defina ADMIN_PASSWORD no ambiente.}"
json='Content-Type: application/json'
code() { curl -s -o /dev/null -w '%{http_code}' "$@"; }
falha() { echo "FALHA: $1"; exit 1; }
campo() { python3 -c 'import sys,json; print(json.load(sys.stdin)["access_token"])'; }

admin=$(ADMIN_EMAIL="$admin_email" python3 -c \
  'import json, os; print(json.dumps({"email": os.environ["ADMIN_EMAIL"], "senha": os.environ["ADMIN_PASSWORD"]}))' \
  | curl -sf -X POST "$gw/auth/login" -H "$json" --data-binary @- | campo)

[ "$(code "$gw/cliente")" = 401 ] || falha "GET sem token de /cliente deveria dar 401"
[ "$(code "$gw/cliente/1")" = 401 ] || falha "GET sem token de /cliente/1 deveria dar 401"
[ "$(code "$gw/cliente" -H "Authorization: Bearer $admin")" = 200 ] || falha "GET de /cliente com token admin deveria dar 200"
[ "$(code -X POST "$gw/cliente" -H "$json" -d '{}')" = 401 ] || falha "POST sem token deveria dar 401"

code -X POST "$gw/cliente" -H "Authorization: Bearer $admin" -H "$json" \
  -d '{"nome":"Cliente Smoke","documento":"529.982.247-25","email":"smoke@mecanica.com","telefone":"31999999999","endereco":{"cep":"30000-000","estado":"MG","cidade":"Belo Horizonte","bairro":"Centro","rua":"Rua A","numero":"1"}}' > /dev/null

[ "$(code -X POST "$gw/auth/cpf" -H "$json" -d '{"cpf":"111.111.111-11"}')" = 400 ] || falha "CPF inválido deveria dar 400"
[ "$(code -X POST "$gw/auth/cpf" -H "$json" -d '{"cpf":"168.995.350-09"}')" = 401 ] || falha "CPF inexistente deveria dar 401"
token=$(curl -sf -X POST "$gw/auth/cpf" -H "$json" -d '{"cpf":"529.982.247-25"}' | campo)
[ "$(code "$gw/atendimento/cliente/minhas")" = 401 ] || falha "minhas sem token deveria dar 401"
[ "$(code "$gw/atendimento/cliente/minhas" -H "Authorization: Bearer $token")" = 200 ] || falha "minhas com token de cliente deveria dar 200"
[ "$(code "$gw/atendimento/abertos" -H "Authorization: Bearer $token")" = 403 ] || falha "token de cliente não deveria listar abertos"

echo "Smoke test ok em $gw"
