output "ecr_repository_url" {
  value = aws_ecr_repository.this.repository_url
}

output "function_name" {
  description = "Valor de LAMBDA_FUNCTION do Kong."
  value       = aws_lambda_function.this.function_name
}
