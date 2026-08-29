# Provisiona o PostgreSQL usado pelos 6 serviços, atendendo ao requisito de "Banco de Dados" do
# bloco de IaC do PDF. Os Deployments da aplicação (k8s/overlays/local) continuam lendo as
# credenciais do Secret "mecanica-shared-secrets", gerado à parte pelo Kustomize a partir de
# k8s/overlays/local/secrets/shared.env — os valores de var.postgres_username/postgres_password
# devem ser mantidos iguais aos daquele arquivo (o default já bate com o shared.env.example).

resource "kubernetes_secret_v1" "postgres_credentials" {
  metadata {
    name      = "postgres-mecanica-credentials"
    namespace = kubernetes_namespace_v1.mecanica.metadata[0].name
    labels = {
      "app.kubernetes.io/name"    = "postgres-mecanica"
      "app.kubernetes.io/part-of" = "oficina-mecanica"
    }
  }

  data = {
    POSTGRES_USER     = var.postgres_username
    POSTGRES_PASSWORD = var.postgres_password
  }
}

resource "kubernetes_config_map_v1" "postgres_init_databases" {
  metadata {
    name      = "postgres-init-databases"
    namespace = kubernetes_namespace_v1.mecanica.metadata[0].name
    labels = {
      "app.kubernetes.io/name"    = "postgres-mecanica"
      "app.kubernetes.io/part-of" = "oficina-mecanica"
    }
  }

  data = {
    "init-databases.sql" = file("${path.module}/../docker/postgres/init-databases.sql")
  }
}

resource "kubernetes_stateful_set_v1" "postgres_mecanica" {
  metadata {
    name      = "postgres-mecanica"
    namespace = kubernetes_namespace_v1.mecanica.metadata[0].name
    labels = {
      "app.kubernetes.io/name"      = "postgres-mecanica"
      "app.kubernetes.io/component" = "database"
      "app.kubernetes.io/part-of"   = "oficina-mecanica"
    }
  }

  spec {
    service_name = kubernetes_service_v1.postgres_mecanica_headless.metadata[0].name
    replicas     = 1

    persistent_volume_claim_retention_policy {
      when_deleted = "Delete"
      when_scaled  = "Retain"
    }

    selector {
      match_labels = {
        "app.kubernetes.io/name" = "postgres-mecanica"
      }
    }

    template {
      metadata {
        labels = {
          "app.kubernetes.io/name"      = "postgres-mecanica"
          "app.kubernetes.io/component" = "database"
          "app.kubernetes.io/part-of"   = "oficina-mecanica"
        }
      }

      spec {
        automount_service_account_token  = false
        termination_grace_period_seconds = 30

        security_context {
          fs_group               = 70
          fs_group_change_policy = "OnRootMismatch"
          seccomp_profile {
            type = "RuntimeDefault"
          }
        }

        container {
          name              = "postgresql"
          image             = var.postgres_image
          image_pull_policy = "IfNotPresent"

          port {
            name           = "postgresql"
            container_port = 5432
            protocol       = "TCP"
          }

          env {
            name  = "POSTGRES_DB"
            value = "postgres"
          }

          env {
            name = "POSTGRES_USER"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.postgres_credentials.metadata[0].name
                key  = "POSTGRES_USER"
              }
            }
          }

          env {
            name = "POSTGRES_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.postgres_credentials.metadata[0].name
                key  = "POSTGRES_PASSWORD"
              }
            }
          }

          startup_probe {
            exec {
              command = ["sh", "-c", "pg_isready -U \"$POSTGRES_USER\" -d \"$POSTGRES_DB\""]
            }
            period_seconds    = 5
            timeout_seconds   = 3
            failure_threshold = 30
          }

          liveness_probe {
            exec {
              command = ["sh", "-c", "pg_isready -U \"$POSTGRES_USER\" -d \"$POSTGRES_DB\""]
            }
            period_seconds    = 10
            timeout_seconds   = 3
            failure_threshold = 3
          }

          readiness_probe {
            exec {
              command = ["sh", "-c", "pg_isready -U \"$POSTGRES_USER\" -d \"$POSTGRES_DB\""]
            }
            period_seconds    = 5
            timeout_seconds   = 3
            failure_threshold = 3
          }

          resources {
            requests = {
              cpu    = "100m"
              memory = "256Mi"
            }
            limits = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }

          security_context {
            allow_privilege_escalation = false
          }

          volume_mount {
            name       = "data"
            mount_path = "/var/lib/postgresql/data"
          }

          volume_mount {
            name       = "init-databases"
            mount_path = "/docker-entrypoint-initdb.d/init-databases.sql"
            sub_path   = "init-databases.sql"
            read_only  = true
          }
        }

        volume {
          name = "init-databases"
          config_map {
            name = kubernetes_config_map_v1.postgres_init_databases.metadata[0].name
          }
        }
      }
    }

    volume_claim_template {
      metadata {
        name = "data"
        labels = {
          "app.kubernetes.io/name"    = "postgres-mecanica"
          "app.kubernetes.io/part-of" = "oficina-mecanica"
        }
      }
      spec {
        access_modes       = ["ReadWriteOnce"]
        storage_class_name = var.postgres_storage_class != "" ? var.postgres_storage_class : null
        resources {
          requests = {
            storage = "2Gi"
          }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "postgres_mecanica" {
  metadata {
    name      = "postgres-mecanica"
    namespace = kubernetes_namespace_v1.mecanica.metadata[0].name
    labels = {
      "app.kubernetes.io/name"    = "postgres-mecanica"
      "app.kubernetes.io/part-of" = "oficina-mecanica"
    }
  }

  spec {
    type = "ClusterIP"
    selector = {
      "app.kubernetes.io/name" = "postgres-mecanica"
    }
    port {
      name        = "postgresql"
      port        = 5432
      target_port = "postgresql"
      protocol    = "TCP"
    }
  }
}

resource "kubernetes_service_v1" "postgres_mecanica_headless" {
  metadata {
    name      = "postgres-mecanica-headless"
    namespace = kubernetes_namespace_v1.mecanica.metadata[0].name
    labels = {
      "app.kubernetes.io/name"    = "postgres-mecanica"
      "app.kubernetes.io/part-of" = "oficina-mecanica"
    }
  }

  spec {
    cluster_ip                  = "None"
    publish_not_ready_addresses = true
    selector = {
      "app.kubernetes.io/name" = "postgres-mecanica"
    }
    port {
      name        = "postgresql"
      port        = 5432
      target_port = "postgresql"
      protocol    = "TCP"
    }
  }
}
