resource "newrelic_alert_policy" "mecanica" {
  name                = "Mecânica Oficina - ${var.environment}"
  incident_preference = "PER_CONDITION"
}

resource "newrelic_notification_destination" "email" {
  name = "Mecânica Oficina - ${var.environment} (e-mail)"
  type = "EMAIL"

  property {
    key   = "email"
    value = var.alert_email
  }
}

resource "newrelic_notification_channel" "email" {
  name           = "Mecânica Oficina - ${var.environment} (e-mail)"
  type           = "EMAIL"
  destination_id = newrelic_notification_destination.email.id
  product        = "IINT"

  property {
    key   = "subject"
    value = "[Mecânica ${var.environment}] {{ issueTitle }}"
  }
}

resource "newrelic_workflow" "alertas" {
  name                  = "Mecânica Oficina - ${var.environment}"
  muting_rules_handling = "NOTIFY_ALL_ISSUES"

  issues_filter {
    name = "Política ${var.environment}"
    type = "FILTER"

    predicate {
      attribute = "labels.policyIds"
      operator  = "EXACTLY_MATCHES"
      values    = [newrelic_alert_policy.mecanica.id]
    }
  }

  destination {
    channel_id = newrelic_notification_channel.email.id
  }
}

resource "newrelic_nrql_alert_condition" "falha_processamento_os" {
  policy_id                    = newrelic_alert_policy.mecanica.id
  type                         = "static"
  name                         = "Falha no processamento de ordens de serviço (5xx no atendimento)"
  description                  = "O serviço atendimento respondeu com erro 5xx ao processar uma ordem de serviço."
  enabled                      = true
  violation_time_limit_seconds = 86400
  aggregation_method           = "cadence"
  aggregation_window           = 60
  aggregation_delay            = 120
  fill_option                  = "static"
  fill_value                   = 0

  nrql {
    query = "SELECT count(http.server.requests) FROM Metric WHERE ${local.env_filter} AND service.name = 'atendimento-service' AND outcome = 'SERVER_ERROR'"
  }

  critical {
    operator              = "above"
    threshold             = 0
    threshold_duration    = 300
    threshold_occurrences = "at_least_once"
  }
}

resource "newrelic_nrql_alert_condition" "falha_integracao" {
  policy_id                    = newrelic_alert_policy.mecanica.id
  type                         = "static"
  name                         = "Falha na integração entre serviços (5xx ou sem resposta)"
  description                  = "O atendimento não conseguiu obter resposta de cliente, veículo, serviço ou estoque."
  enabled                      = true
  violation_time_limit_seconds = 86400
  aggregation_method           = "cadence"
  aggregation_window           = 60
  aggregation_delay            = 120
  fill_option                  = "static"
  fill_value                   = 0

  nrql {
    query = "SELECT count(http.client.requests) FROM Metric WHERE ${local.env_filter} AND outcome IN ('SERVER_ERROR', 'UNKNOWN')"
  }

  critical {
    operator              = "above"
    threshold             = 0
    threshold_duration    = 300
    threshold_occurrences = "at_least_once"
  }
}

resource "newrelic_nrql_alert_condition" "falha_notificacao" {
  policy_id                    = newrelic_alert_policy.mecanica.id
  type                         = "static"
  name                         = "Falha no envio de notificações por e-mail"
  description                  = "O atendimento não conseguiu enviar uma notificação por e-mail a cliente ou funcionário."
  enabled                      = true
  violation_time_limit_seconds = 86400
  aggregation_method           = "cadence"
  aggregation_window           = 60
  aggregation_delay            = 120
  fill_option                  = "static"
  fill_value                   = 0

  nrql {
    query = "SELECT sum(notificacao.falhas) FROM Metric WHERE ${local.env_filter}"
  }

  critical {
    operator              = "above"
    threshold             = 0
    threshold_duration    = 300
    threshold_occurrences = "at_least_once"
  }
}

resource "newrelic_nrql_alert_condition" "latencia_alta" {
  policy_id                    = newrelic_alert_policy.mecanica.id
  type                         = "static"
  name                         = "Latência p95 das APIs acima do limite"
  description                  = "A latência p95 de um serviço ficou acima de ${var.latency_p95_threshold_ms} ms."
  enabled                      = true
  violation_time_limit_seconds = 86400
  aggregation_method           = "cadence"
  aggregation_window           = 60
  aggregation_delay            = 120

  nrql {
    query = "SELECT percentile(http.server.requests, 95) FROM Metric WHERE ${local.env_filter} AND service.name LIKE '%-service' FACET service.name"
  }

  critical {
    operator              = "above"
    threshold             = var.latency_p95_threshold_ms
    threshold_duration    = 300
    threshold_occurrences = "all"
  }
}

resource "newrelic_nrql_alert_condition" "servico_sem_dados" {
  policy_id                    = newrelic_alert_policy.mecanica.id
  type                         = "static"
  name                         = "Serviço sem enviar dados (uptime)"
  description                  = "Um serviço deixou de reportar métricas, indicando que está fora do ar."
  enabled                      = true
  violation_time_limit_seconds = 86400
  aggregation_method           = "cadence"
  aggregation_window           = 60
  aggregation_delay            = 120
  fill_option                  = "static"
  fill_value                   = 0

  nrql {
    query = "SELECT count(process.uptime) FROM Metric WHERE ${local.env_filter} AND service.name LIKE '%-service' FACET service.name"
  }

  critical {
    operator              = "below"
    threshold             = 1
    threshold_duration    = 300
    threshold_occurrences = "all"
  }
}

resource "newrelic_nrql_alert_condition" "pod_nao_pronto" {
  policy_id                    = newrelic_alert_policy.mecanica.id
  type                         = "static"
  name                         = "Pod reprovado no healthcheck (não pronto)"
  description                  = "Um pod da aplicação não está pronto segundo a readiness probe do Kubernetes."
  enabled                      = true
  violation_time_limit_seconds = 86400
  aggregation_method           = "cadence"
  aggregation_window           = 60
  aggregation_delay            = 120

  nrql {
    query = "SELECT latest(isReady) FROM K8sPodSample WHERE ${local.k8s_filter} AND status = 'Running' FACET podName"
  }

  critical {
    operator              = "below"
    threshold             = 1
    threshold_duration    = 300
    threshold_occurrences = "all"
  }
}

resource "newrelic_nrql_alert_condition" "memoria_alta" {
  policy_id                    = newrelic_alert_policy.mecanica.id
  type                         = "static"
  name                         = "Memória dos contêineres próxima do limite"
  description                  = "Um contêiner usa mais de ${var.memory_utilization_threshold_percent}% do limite de memória configurado."
  enabled                      = true
  violation_time_limit_seconds = 86400
  aggregation_method           = "cadence"
  aggregation_window           = 60
  aggregation_delay            = 120

  nrql {
    query = "SELECT average(memoryWorkingSetBytes) * 100 / average(memoryLimitBytes) FROM K8sContainerSample WHERE ${local.k8s_filter} AND memoryLimitBytes > 0 FACET podName"
  }

  critical {
    operator              = "above"
    threshold             = var.memory_utilization_threshold_percent
    threshold_duration    = 300
    threshold_occurrences = "all"
  }
}
