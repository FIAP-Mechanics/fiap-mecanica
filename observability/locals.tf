locals {
  namespace    = var.environment == "producao" ? "mecanica-production" : "mecanica-homologacao"
  cluster_name = "mecanica-${var.environment}"
  env_filter   = "`deployment.environment` = '${var.environment}'"
  k8s_filter   = "clusterName = '${local.cluster_name}' AND namespaceName = '${local.namespace}'"
}
