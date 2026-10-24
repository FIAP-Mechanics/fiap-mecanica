export type Nivel = "INFO" | "WARN" | "ERROR";

export function log(nivel: Nivel, mensagem: string, campos: Record<string, unknown> = {}): void {
  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: nivel,
      service: "auth-cpf",
      message: mensagem,
      ...campos,
    }),
  );
}
