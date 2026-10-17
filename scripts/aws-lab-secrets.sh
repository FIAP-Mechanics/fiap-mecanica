#!/usr/bin/env bash
set -euo pipefail

env=${1:?uso: aws-lab-secrets.sh <homologacao|producao>}
region=${AWS_REGION:-us-east-1}
account=$(aws sts get-caller-identity --query Account --output text)
repo=$(gh repo view --json nameWithOwner -q .nameWithOwner)

gh api -X PUT "repos/$repo/environments/$env" > /dev/null
gh variable set TF_STATE_BUCKET --env "$env" --body "mecanica-tfstate-$account"
gh variable set AWS_REGION --env "$env" --body "$region"

eval "$(aws configure export-credentials --format env)"
gh secret set AWS_ACCESS_KEY_ID --env "$env" --body "$AWS_ACCESS_KEY_ID"
gh secret set AWS_SECRET_ACCESS_KEY --env "$env" --body "$AWS_SECRET_ACCESS_KEY"
gh secret set AWS_SESSION_TOKEN --env "$env" --body "$AWS_SESSION_TOKEN"

if [ "${RENOVAR_APENAS_CREDENCIAIS:-}" = 1 ]; then
  echo "Credenciais da sessão atualizadas em $env"
  exit 0
fi

rand() { openssl rand -hex "$1"; }
JWT_SECRET=${JWT_SECRET:-$(rand 32)}
DATABASE_PASSWORD=${DATABASE_PASSWORD:-$(rand 16)}
AUTH_CPF_DB_PASSWORD=${AUTH_CPF_DB_PASSWORD:-$(rand 16)}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-$(rand 8)}

gh secret set JWT_SECRET --env "$env" --body "$JWT_SECRET"
gh secret set DATABASE_PASSWORD --env "$env" --body "$DATABASE_PASSWORD"
gh secret set AUTH_CPF_DB_PASSWORD --env "$env" --body "$AUTH_CPF_DB_PASSWORD"
gh secret set ADMIN_PASSWORD --env "$env" --body "$ADMIN_PASSWORD"

for nome in NEW_RELIC_LICENSE_KEY NEW_RELIC_API_KEY NEW_RELIC_ACCOUNT_ID; do
  if [ -n "${!nome:-}" ]; then
    gh secret set "$nome" --env "$env" --body "${!nome}"
  fi
done
if [ -n "${ALERT_EMAIL:-}" ]; then
  gh variable set ALERT_EMAIL --env "$env" --body "$ALERT_EMAIL"
fi

echo "Secrets de $env criados. Guarde a senha do administrador: admin@mecanica.com / $ADMIN_PASSWORD"
