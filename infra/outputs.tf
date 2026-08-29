output "cluster_name" {
  description = "Nome do cluster kind provisionado."
  value       = kind_cluster.this.name
}

output "kubeconfig" {
  description = "Kubeconfig do cluster provisionado. Também é possível obtê-lo com `kind export kubeconfig --name <cluster_name>`."
  value       = kind_cluster.this.kubeconfig
  sensitive   = true
}

output "namespace" {
  description = "Namespace onde a aplicação e o banco de dados são implantados."
  value       = kubernetes_namespace_v1.mecanica.metadata[0].name
}

output "postgres_service_dns" {
  description = "DNS interno do Service do PostgreSQL, usado em SPRING_DATASOURCE_URL nos ConfigMaps de k8s/base."
  value       = "${kubernetes_service_v1.postgres_mecanica.metadata[0].name}.${kubernetes_namespace_v1.mecanica.metadata[0].name}.svc.cluster.local"
}
