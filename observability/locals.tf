locals {
  namespace    = lookup({ local = "mecanica", homologacao = "mecanica-homologacao", producao = "mecanica-production" }, var.environment)
  cluster_name = "mecanica-${var.environment}"
  env_filter   = "`deployment.environment` = '${var.environment}'"
  k8s_filter   = "k8s.cluster.name = '${local.cluster_name}' AND k8s.namespace.name = '${local.namespace}'"
  k8s_cluster  = "k8s.cluster.name = '${local.cluster_name}'"
}
