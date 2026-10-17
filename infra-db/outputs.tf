output "db_host" {
  description = "Endpoint do RDS; vira o externalName de postgres-mecanica."
  value       = aws_db_instance.this.address
}

output "db_port" {
  value = aws_db_instance.this.port
}

output "db_security_group_id" {
  description = "Security group do RDS; recebe a regra de entrada da Lambda."
  value       = aws_security_group.db.id
}
