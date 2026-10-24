output "cluster_name" {
  value = aws_eks_cluster.this.name
}

output "cluster_security_group_id" {
  description = "Security group dos nós; recebe acesso ao RDS."
  value       = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
}

output "vpc_id" {
  value = aws_vpc.this.id
}

output "vpc_cidr" {
  value = aws_vpc.this.cidr_block
}

output "private_subnet_ids" {
  value = aws_subnet.private[*].id
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}
