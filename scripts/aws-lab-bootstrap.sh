#!/usr/bin/env bash
set -euo pipefail

region=${AWS_REGION:-us-east-1}
account=$(aws sts get-caller-identity --query Account --output text)
bucket="mecanica-tfstate-$account"

aws iam get-role --role-name LabRole --query Role.Arn --output text

if aws s3api head-bucket --bucket "$bucket" 2>/dev/null; then
  echo "Bucket $bucket já existe"
else
  aws s3api create-bucket --bucket "$bucket" --region "$region"
  aws s3api put-bucket-versioning --bucket "$bucket" --versioning-configuration Status=Enabled
  aws s3api put-public-access-block --bucket "$bucket" \
    --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
fi

echo "TF_STATE_BUCKET=$bucket"
