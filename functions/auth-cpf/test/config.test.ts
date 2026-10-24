import { describe, expect, it } from "vitest";
import { carregarConfig, validarJwtSecret } from "../src/config";

const BASE = {
  DB_HOST: "localhost",
  DB_USER: "auth_cpf_ro",
  DB_PASSWORD: "senha",
  JWT_SECRET: "0123456789abcdef0123456789abcdef",
  JWT_ISSUER: "http://localhost:8086",
};

describe("config", () => {
  it("deve carregar da env com defaults", async () => {
    const config = await carregarConfig(BASE);

    expect(config.db).toMatchObject({ host: "localhost", port: 5432, database: "mecanica_cliente", ssl: false });
    expect(config.jwtExpirationSeconds).toBe(3600);
  });

  it("deve respeitar porta, banco, ssl e expiração informados", async () => {
    const config = await carregarConfig({
      ...BASE,
      DB_PORT: "6543",
      DB_NAME: "outro",
      DB_SSL: "true",
      JWT_EXPIRATION_SECONDS: "60",
    });

    expect(config.db).toMatchObject({ port: 6543, database: "outro", ssl: true });
    expect(config.jwtExpirationSeconds).toBe(60);
  });

  it("deve exigir variáveis obrigatórias", async () => {
    await expect(carregarConfig({ ...BASE, DB_HOST: undefined })).rejects.toThrow("DB_HOST");
  });

  it("deve rejeitar JWT_SECRET curto", async () => {
    expect(() => validarJwtSecret("curto")).toThrow("32 bytes");
    await expect(carregarConfig({ ...BASE, JWT_SECRET: "curto" })).rejects.toThrow("32 bytes");
  });
});
