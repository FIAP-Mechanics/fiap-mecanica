provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "oficina-mecanica"
      Component   = "auth-cpf"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
