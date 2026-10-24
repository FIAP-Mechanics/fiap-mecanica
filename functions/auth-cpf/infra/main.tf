locals {
  name = "mecanica-auth-cpf-${var.environment}"
}

resource "aws_ecr_repository" "this" {
  name                 = local.name
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_secretsmanager_secret" "db" {
  name                    = "${local.name}/db"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    host     = local.db_host
    port     = local.db_port
    dbname   = var.db_name
    username = var.db_username
    password = var.db_password
  })
}

resource "aws_secretsmanager_secret" "jwt" {
  name                    = "${local.name}/jwt"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id     = aws_secretsmanager_secret.jwt.id
  secret_string = jsonencode({ JWT_SECRET = var.jwt_secret })
}

data "aws_iam_role" "lab" {
  name = var.lab_role_name
}

data "terraform_remote_state" "k8s" {
  backend = "s3"

  config = {
    bucket = var.state_bucket
    key    = "infra-k8s/${var.environment}.tfstate"
    region = var.aws_region
  }
}

data "terraform_remote_state" "db" {
  backend = "s3"

  config = {
    bucket = var.state_bucket
    key    = "infra-db/${var.environment}.tfstate"
    region = var.aws_region
  }
}

locals {
  vpc_id               = data.terraform_remote_state.k8s.outputs.vpc_id
  subnet_ids           = data.terraform_remote_state.k8s.outputs.private_subnet_ids
  db_host              = data.terraform_remote_state.db.outputs.db_host
  db_port              = data.terraform_remote_state.db.outputs.db_port
  db_security_group_id = data.terraform_remote_state.db.outputs.db_security_group_id
}

resource "aws_security_group" "lambda" {
  name        = local.name
  description = "Lambda auth-cpf"
  vpc_id      = local.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group_rule" "rds_from_lambda" {
  type                     = "ingress"
  from_port                = local.db_port
  to_port                  = local.db_port
  protocol                 = "tcp"
  security_group_id        = local.db_security_group_id
  source_security_group_id = aws_security_group.lambda.id
  description              = "auth-cpf ${var.environment}"
}

resource "aws_cloudwatch_log_group" "lambda" {
  name              = "/aws/lambda/${local.name}"
  retention_in_days = var.log_retention_days
}

resource "aws_lambda_function" "this" {
  function_name = local.name
  role          = data.aws_iam_role.lab.arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.this.repository_url}:${var.image_tag}"
  timeout       = 10
  memory_size   = 256

  vpc_config {
    subnet_ids         = local.subnet_ids
    security_group_ids = [aws_security_group.lambda.id]
  }

  environment {
    variables = {
      DB_SECRET_ARN          = aws_secretsmanager_secret.db.arn
      JWT_SECRET_ARN         = aws_secretsmanager_secret.jwt.arn
      JWT_ISSUER             = var.jwt_issuer
      JWT_EXPIRATION_SECONDS = tostring(var.jwt_expiration_seconds)
      DB_SSL                 = "true"
    }
  }

  depends_on = [
    aws_cloudwatch_log_group.lambda,
    aws_secretsmanager_secret_version.db,
    aws_secretsmanager_secret_version.jwt,
  ]
}
