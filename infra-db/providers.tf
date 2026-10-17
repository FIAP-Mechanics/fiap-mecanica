provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "oficina-mecanica"
      Component   = "banco-de-dados"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
