resource "kubernetes_namespace_v1" "mecanica" {
  metadata {
    name = var.namespace
    labels = {
      "app.kubernetes.io/name"    = "oficina-mecanica"
      "app.kubernetes.io/part-of" = "oficina-mecanica"
    }
  }

  depends_on = [kind_cluster.this]
}
