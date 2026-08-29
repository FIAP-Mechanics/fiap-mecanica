variable "cluster_name" {
  description = "Nome do cluster kind provisionado."
  type        = string
  default     = "mecanica"
}

variable "namespace" {
  description = "Namespace onde a aplicação e o banco de dados são implantados. Deve ser igual ao usado em k8s/base/namespace.yaml."
  type        = string
  default     = "mecanica"
}

variable "postgres_storage_class" {
  description = "StorageClass do PVC do PostgreSQL. Vazio usa a StorageClass padrão do cluster (a do kind serve para o ambiente local; troque aqui numa migração futura para cloud)."
  type        = string
  default     = ""
}

variable "postgres_image" {
  description = "Imagem do PostgreSQL, igual à usada em compose.app.yaml e no StatefulSet anterior."
  type        = string
  default     = "postgres:16-alpine"
}

variable "postgres_username" {
  description = "Usuário do PostgreSQL. Deve ser igual a DATABASE_USERNAME em k8s/overlays/local/secrets/shared.env."
  type        = string
  default     = "mecanica"
}

variable "postgres_password" {
  description = "Senha do PostgreSQL. Deve ser igual a DATABASE_PASSWORD em k8s/overlays/local/secrets/shared.env."
  type        = string
  sensitive   = true
  default     = "mecanica-local-only"
}
