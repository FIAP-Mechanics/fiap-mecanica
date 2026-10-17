variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "environment" {
  description = "Ambiente (homologacao ou producao)."
  type        = string

  validation {
    condition     = contains(["homologacao", "producao"], var.environment)
    error_message = "environment deve ser homologacao ou producao."
  }
}

variable "state_bucket" {
  description = "Bucket S3 com o state do repositório de infraestrutura Kubernetes."
  type        = string
}

variable "instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "allocated_storage" {
  type    = number
  default = 20
}

variable "postgres_version" {
  type    = string
  default = "16"
}

variable "master_username" {
  description = "Usuário administrador; igual a DATABASE_USERNAME dos serviços."
  type        = string
  default     = "mecanica"
}

variable "master_password" {
  description = "Senha do administrador; igual a DATABASE_PASSWORD dos serviços."
  type        = string
  sensitive   = true
}
