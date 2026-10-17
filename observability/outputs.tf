output "dashboard_url" {
  description = "Endereço do dashboard no New Relic."
  value       = newrelic_one_dashboard.mecanica.permalink
}

output "alert_policy_id" {
  description = "ID da política de alertas."
  value       = newrelic_alert_policy.mecanica.id
}
