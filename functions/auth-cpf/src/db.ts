import { Pool } from "pg";
import type { DbConfig } from "./config";

export interface ClienteAutenticacao {
  id: number;
  ativo: boolean;
}

export interface ClienteRepository {
  buscarPorDocumento(documento: string): Promise<ClienteAutenticacao | null>;
}

export function criarClienteRepository(config: DbConfig): ClienteRepository {
  const pool = new Pool({
    host: config.host,
    port: config.port,
    database: config.database,
    user: config.user,
    password: config.password,
    ssl: config.ssl ? { rejectUnauthorized: false } : false,
    max: 1,
    connectionTimeoutMillis: 5000,
    idleTimeoutMillis: 60000,
  });

  return {
    async buscarPorDocumento(documento) {
      const resultado = await pool.query<{ id: string; ativo: boolean }>(
        "SELECT id, ativo FROM v_cliente_autenticacao WHERE documento = $1",
        [documento],
      );
      const linha = resultado.rows[0];
      return linha ? { id: Number(linha.id), ativo: linha.ativo } : null;
    },
  };
}
