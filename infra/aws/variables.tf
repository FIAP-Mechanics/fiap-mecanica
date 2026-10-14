variable "aws_region" {
  description = "Região AWS (o Learner Lab permite us-east-1 e us-west-2)."
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente (homologacao ou producao)."
  type        = string

  validation {
    condition     = contains(["homologacao", "producao"], var.environment)
    error_message = "environment deve ser homologacao ou producao."
  }
}

variable "vpc_cidr" {
  description = "CIDR da VPC do ambiente."
  type        = string
}

variable "kubernetes_version" {
  description = "Versão do Kubernetes do EKS."
  type        = string
  default     = "1.33"
}

variable "node_instance_type" {
  description = "Tipo de instância dos nós."
  type        = string
  default     = "t3.large"
}

variable "node_min_size" {
  type    = number
  default = 2
}

variable "node_desired_size" {
  type    = number
  default = 2
}

variable "node_max_size" {
  type    = number
  default = 4
}

variable "lab_role_name" {
  description = "Role pré-existente do AWS Academy, usada pelo cluster e pelos nós (o lab não permite criar roles)."
  type        = string
  default     = "LabRole"
}
