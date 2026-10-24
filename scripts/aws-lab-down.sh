#!/usr/bin/env bash
set -euo pipefail

env=${1:?uso: aws-lab-down.sh <homologacao|producao>}
region=${AWS_REGION:-us-east-1}
account=$(aws sts get-caller-identity --query Account --output text)
bucket="mecanica-tfstate-$account"
ns=mecanica-$([ "$env" = producao ] && echo production || echo homologacao)
root=$(cd "$(dirname "$0")/.." && pwd)

export TF_VAR_state_bucket=$bucket TF_VAR_master_password=x TF_VAR_db_password=x TF_VAR_jwt_secret=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

cluster=mecanica-$env
if aws eks describe-cluster --name "$cluster" --region "$region" > /dev/null 2>&1; then
  aws eks update-kubeconfig --name "$cluster" --region "$region" > /dev/null
  kubectl delete namespace "$ns" --ignore-not-found --timeout=300s
fi

destroy() {
  dir=$1; key=$2; shift 2
  (cd "$root/$dir" && terraform init -input=false -reconfigure \
      -backend-config="bucket=$bucket" -backend-config="key=$key/$env.tfstate" \
      -backend-config="region=$region" -backend-config="use_lockfile=true" > /dev/null &&
    terraform destroy -auto-approve -input=false -var-file="envs/$env.tfvars" "$@")
}

destroy functions/auth-cpf/infra auth-cpf
destroy infra-db infra-db
destroy infra/aws infra-k8s

echo "Ambiente $env destruído. O bucket $bucket (state) foi mantido."
