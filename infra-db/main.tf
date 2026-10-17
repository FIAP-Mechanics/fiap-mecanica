locals {
  name = "mecanica-${var.environment}"
}

data "terraform_remote_state" "k8s" {
  backend = "s3"

  config = {
    bucket = var.state_bucket
    key    = "infra-k8s/${var.environment}.tfstate"
    region = var.aws_region
  }
}

locals {
  vpc_id             = data.terraform_remote_state.k8s.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.k8s.outputs.private_subnet_ids
  nodes_sg_id        = data.terraform_remote_state.k8s.outputs.cluster_security_group_id
}

resource "aws_db_subnet_group" "this" {
  name       = local.name
  subnet_ids = local.private_subnet_ids
}

resource "aws_security_group" "db" {
  name        = "${local.name}-rds"
  description = "PostgreSQL gerenciado"
  vpc_id      = local.vpc_id
}

resource "aws_vpc_security_group_ingress_rule" "from_nodes" {
  security_group_id            = aws_security_group.db.id
  referenced_security_group_id = local.nodes_sg_id
  ip_protocol                  = "tcp"
  from_port                    = 5432
  to_port                      = 5432
  description                  = "Pods do EKS"
}

resource "aws_db_instance" "this" {
  identifier     = local.name
  engine         = "postgres"
  engine_version = var.postgres_version
  instance_class = var.instance_class

  allocated_storage = var.allocated_storage
  storage_type      = "gp3"
  storage_encrypted = true

  username = var.master_username
  password = var.master_password

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.db.id]
  publicly_accessible    = false
  multi_az               = false

  backup_retention_period = var.environment == "producao" ? 1 : 0
  skip_final_snapshot     = true
  deletion_protection     = false
  apply_immediately       = true
}
