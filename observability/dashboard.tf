resource "newrelic_one_dashboard" "mecanica" {
  name        = "Mecânica Oficina - ${var.environment}"
  permissions = "public_read_only"

  page {
    name = "Negócio"

    widget_billboard {
      title  = "OS abertas hoje"
      row    = 1
      column = 1
      width  = 3
      height = 3

      nrql_query {
        query = "SELECT sum(os.criadas) AS 'OS abertas' FROM Metric WHERE ${local.env_filter} SINCE today"
      }
    }

    widget_line {
      title  = "Volume diário de ordens de serviço"
      row    = 1
      column = 4
      width  = 9
      height = 3

      nrql_query {
        query = "SELECT sum(os.criadas) AS 'OS abertas' FROM Metric WHERE ${local.env_filter} SINCE 14 days ago TIMESERIES 1 day"
      }
    }

    widget_bar {
      title  = "Tempo médio por status (minutos)"
      row    = 4
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(os.status.duracao) / 60000 AS 'minutos' FROM Metric WHERE ${local.env_filter} FACET status SINCE 1 day ago"
      }
    }

    widget_table {
      title  = "Diagnóstico, execução e finalização"
      row    = 4
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(os.status.duracao) / 60000 AS 'média (min)', max(os.status.duracao) / 60000 AS 'máximo (min)', count(os.status.duracao) AS 'ordens' FROM Metric WHERE ${local.env_filter} AND status IN ('EM_DIAGNOSTICO', 'EM_EXECUCAO', 'FINALIZADA') FACET status SINCE 1 day ago"
      }
    }

    widget_bar {
      title  = "Transições de status"
      row    = 7
      column = 1
      width  = 4
      height = 3

      nrql_query {
        query = "SELECT sum(os.transicoes) FROM Metric WHERE ${local.env_filter} FACET para SINCE 1 day ago"
      }
    }

    widget_line {
      title  = "Falhas nas integrações (erro 5xx ou sem resposta)"
      row    = 7
      column = 5
      width  = 5
      height = 3

      nrql_query {
        query = "SELECT count(http.client.requests) FROM Metric WHERE ${local.env_filter} AND outcome IN ('SERVER_ERROR', 'UNKNOWN') FACET client.name TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_billboard {
      title  = "Falhas no envio de e-mail"
      row    = 7
      column = 10
      width  = 3
      height = 3

      nrql_query {
        query = "SELECT sum(notificacao.falhas) AS 'falhas' FROM Metric WHERE ${local.env_filter} SINCE 1 day ago"
      }
    }

    widget_table {
      title  = "Respostas das integrações por serviço e status HTTP"
      row    = 10
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT count(http.client.requests) AS 'chamadas', average(http.client.requests) AS 'latência média (ms)' FROM Metric WHERE ${local.env_filter} FACET client.name, status, outcome SINCE 1 day ago"
      }
    }
  }

  page {
    name = "APIs"

    widget_line {
      title  = "Latência média por serviço (ms)"
      row    = 1
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(http.server.requests) FROM Metric WHERE ${local.env_filter} AND service.name LIKE '%-service' FACET service.name TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_line {
      title  = "Latência p95 por serviço (ms)"
      row    = 1
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT percentile(http.server.requests, 95) FROM Metric WHERE ${local.env_filter} AND service.name LIKE '%-service' FACET service.name TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_line {
      title  = "Requisições por minuto"
      row    = 4
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT rate(count(http.server.requests), 1 minute) FROM Metric WHERE ${local.env_filter} AND service.name LIKE '%-service' FACET service.name TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_line {
      title  = "Taxa de erro 5xx (%)"
      row    = 4
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT filter(count(http.server.requests), WHERE outcome = 'SERVER_ERROR') * 100 / count(http.server.requests) AS 'erro 5xx (%)' FROM Metric WHERE ${local.env_filter} AND service.name LIKE '%-service' FACET service.name TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_table {
      title  = "Rotas mais lentas"
      row    = 7
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT average(http.server.requests) AS 'média (ms)', percentile(http.server.requests, 95) AS 'p95 (ms)', count(http.server.requests) AS 'chamadas' FROM Metric WHERE ${local.env_filter} AND service.name LIKE '%-service' FACET service.name, method, uri SINCE 1 hour ago LIMIT 20"
      }
    }
  }

  page {
    name = "Saúde e uptime"

    widget_table {
      title  = "Uptime por serviço (segundos)"
      row    = 1
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT latest(process.uptime) / 1000 AS 'uptime (s)' FROM Metric WHERE ${local.env_filter} FACET service.name SINCE 10 minutes ago"
      }
    }

    widget_line {
      title  = "Serviços reportando"
      row    = 1
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT uniqueCount(service.name) AS 'serviços' FROM Metric WHERE ${local.env_filter} TIMESERIES SINCE 3 hours ago"
      }
    }

    widget_table {
      title  = "Pods prontos (healthchecks do Kubernetes)"
      row    = 4
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT latest(isReady) AS 'pronto', latest(status) AS 'fase' FROM K8sPodSample WHERE ${local.k8s_filter} FACET podName SINCE 10 minutes ago LIMIT 50"
      }
    }
  }

  page {
    name = "Kubernetes"

    widget_line {
      title  = "CPU por pod (cores)"
      row    = 1
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(cpuUsedCores) FROM K8sContainerSample WHERE ${local.k8s_filter} FACET podName TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_line {
      title  = "Memória por pod (MiB)"
      row    = 1
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(memoryWorkingSetBytes) / 1048576 AS 'MiB' FROM K8sContainerSample WHERE ${local.k8s_filter} FACET podName TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_line {
      title  = "CPU dos nós (% do alocável)"
      row    = 4
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(allocatableCpuCoresUtilization) FROM K8sNodeSample WHERE clusterName = '${local.cluster_name}' FACET nodeName TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_line {
      title  = "Memória dos nós (MiB)"
      row    = 4
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(memoryWorkingSetBytes) / 1048576 AS 'MiB' FROM K8sNodeSample WHERE clusterName = '${local.cluster_name}' FACET nodeName TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_table {
      title  = "Reinícios de contêineres"
      row    = 7
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT max(restartCount) AS 'reinícios' FROM K8sContainerSample WHERE ${local.k8s_filter} FACET podName SINCE 1 hour ago LIMIT 50"
      }
    }

    widget_line {
      title  = "Réplicas do HPA (atuais x desejadas)"
      row    = 7
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT latest(currentReplicas) AS 'atuais', latest(desiredReplicas) AS 'desejadas' FROM K8sHpaSample WHERE clusterName = '${local.cluster_name}' AND namespaceName = '${local.namespace}' FACET displayName TIMESERIES SINCE 1 hour ago"
      }
    }
  }

  page {
    name = "Logs"

    widget_line {
      title  = "Volume de logs por nível"
      row    = 1
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT count(*) FROM Log WHERE ${local.env_filter} FACET level TIMESERIES SINCE 1 hour ago"
      }
    }

    widget_table {
      title  = "Requisições recentes com correlação (request_id e trace.id)"
      row    = 1
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT service.name, metodo, rota, status, duracao_ms, request_id, trace.id FROM Log WHERE ${local.env_filter} AND message = 'Requisição concluída' SINCE 30 minutes ago LIMIT 50"
      }
    }

    widget_table {
      title  = "Erros recentes"
      row    = 4
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT service.name, message, request_id, trace.id FROM Log WHERE ${local.env_filter} AND level = 'ERROR' SINCE 1 hour ago LIMIT 50"
      }
    }
  }
}
