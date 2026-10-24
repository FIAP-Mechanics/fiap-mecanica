export function apenasDigitos(valor: string): string {
  return valor.replace(/\D/g, "");
}

function calcularDigito(cpf: string, tamanho: number): number {
  let soma = 0;
  for (let i = 0; i < tamanho; i++) {
    soma += Number(cpf[i]) * (tamanho + 1 - i);
  }
  const resto = soma % 11;
  return resto < 2 ? 0 : 11 - resto;
}

export function cpfValido(cpf: string): boolean {
  if (!/^\d{11}$/.test(cpf)) return false;
  if (new Set(cpf).size === 1) return false;
  return calcularDigito(cpf, 9) === Number(cpf[9]) && calcularDigito(cpf, 10) === Number(cpf[10]);
}

export function mascararCpf(cpf: string): string {
  return cpf.length === 11 ? `***.${cpf.slice(3, 6)}.***-**` : "***";
}
