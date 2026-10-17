variable "environment" {
  description = "Ambiente (homologacao ou producao)."
  type        = string

  validation {
    condition     = contains(["homologacao", "producao"], var.environment)
    error_message = "environment deve ser homologacao ou producao."
  }
}

variable "new_relic_region" {
  description = "Região da conta New Relic (US ou EU)."
  type        = string
  default     = "US"

  validation {
    condition     = contains(["US", "EU"], var.new_relic_region)
    error_message = "new_relic_region deve ser US ou EU."
  }
}

variable "alert_email" {
  description = "E-mail que recebe os alertas."
  type        = string
}

variable "latency_p95_threshold_ms" {
  description = "Limite de latência p95 (ms) das APIs."
  type        = number
  default     = 1500
}

variable "memory_utilization_threshold_percent" {
  description = "Limite de uso de memória dos contêineres em relação ao limite configurado."
  type        = number
  default     = 90
}
