import { GetSecretValueCommand, SecretsManagerClient } from "@aws-sdk/client-secrets-manager";

export interface DbConfig {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
  ssl: boolean;
}

export interface Config {
  db: DbConfig;
  jwtSecret: string;
  jwtIssuer: string;
  jwtExpirationSeconds: number;
}

async function lerSecret(arn: string): Promise<Record<string, string>> {
  const client = new SecretsManagerClient({});
  const resposta = await client.send(new GetSecretValueCommand({ SecretId: arn }));
  return JSON.parse(resposta.SecretString ?? "{}");
}

function obrigatoria(env: NodeJS.ProcessEnv, nome: string): string {
  const valor = env[nome];
  if (!valor) throw new Error(`Variável de ambiente ${nome} é obrigatória.`);
  return valor;
}

export function validarJwtSecret(secret: string): string {
  if (Buffer.byteLength(secret, "utf8") < 32) {
    throw new Error("JWT_SECRET deve ter pelo menos 32 bytes.");
  }
  return secret;
}

export async function carregarConfig(env: NodeJS.ProcessEnv = process.env): Promise<Config> {
  const dbSecret = env.DB_SECRET_ARN ? await lerSecret(env.DB_SECRET_ARN) : {};
  const jwtSecret = env.JWT_SECRET_ARN ? (await lerSecret(env.JWT_SECRET_ARN)).JWT_SECRET : env.JWT_SECRET;

  return {
    db: {
      host: dbSecret.host ?? obrigatoria(env, "DB_HOST"),
      port: Number(dbSecret.port ?? env.DB_PORT ?? 5432),
      database: dbSecret.dbname ?? env.DB_NAME ?? "mecanica_cliente",
      user: dbSecret.username ?? obrigatoria(env, "DB_USER"),
      password: dbSecret.password ?? obrigatoria(env, "DB_PASSWORD"),
      ssl: (env.DB_SSL ?? "false") === "true",
    },
    jwtSecret: validarJwtSecret(jwtSecret ?? ""),
    jwtIssuer: obrigatoria(env, "JWT_ISSUER"),
    jwtExpirationSeconds: Number(env.JWT_EXPIRATION_SECONDS ?? 3600),
  };
}
