variable "aws_region" {
  description = "Região AWS onde a function é publicada."
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente de deploy (homologacao ou producao)."
  type        = string

  validation {
    condition     = contains(["homologacao", "producao"], var.environment)
    error_message = "environment deve ser homologacao ou producao."
  }
}

variable "image_tag" {
  description = "Tag da imagem publicada no ECR."
  type        = string
  default     = "latest"
}

variable "db_name" {
  description = "Banco do serviço cliente."
  type        = string
  default     = "mecanica_cliente"
}

variable "db_username" {
  description = "Usuário somente leitura da view v_cliente_autenticacao."
  type        = string
  default     = "auth_cpf_ro"
}

variable "db_password" {
  description = "Senha do usuário somente leitura."
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "Mesmo JWT_SECRET (>= 32 bytes) usado pelo Kong e pelos serviços."
  type        = string
  sensitive   = true
}

variable "jwt_issuer" {
  description = "Valor do claim iss; deve ser igual ao JWT_ISSUER do atendimento e da key do consumer no Kong."
  type        = string
}

variable "jwt_expiration_seconds" {
  description = "Validade do token do cliente."
  type        = number
  default     = 3600
}

variable "log_retention_days" {
  description = "Retenção dos logs no CloudWatch."
  type        = number
  default     = 14
}

variable "state_bucket" {
  description = "Bucket S3 com os states dos repositórios de infraestrutura Kubernetes e de banco."
  type        = string
}

variable "lab_role_name" {
  description = "Role pré-existente do AWS Academy (o lab não permite criar roles)."
  type        = string
  default     = "LabRole"
}
