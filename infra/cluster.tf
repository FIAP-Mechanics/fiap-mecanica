resource "kind_cluster" "this" {
  name           = var.cluster_name
  wait_for_ready = true

  # node_image não é fixado: usa o padrão da versão instalada do kind (validado com kind v0.30,
  # que provisiona Kubernetes 1.34+, mesmo requisito mínimo documentado no README.md da raiz para o
  # Metrics Server v0.9.0). Numa migração futura para cloud, este é o único arquivo que muda —
  # troque o provider "kind" por um provider de cluster gerenciado (ex.: EKS).
}
