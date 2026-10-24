provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "oficina-mecanica"
      Component   = "infra-k8s"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
