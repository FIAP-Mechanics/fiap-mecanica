import { carregarConfig } from "./config";
import { criarClienteRepository } from "./db";
import { criarHandler } from "./handler";

export const handler = criarHandler(async () => {
  const config = await carregarConfig();
  return {
    clientes: criarClienteRepository(config.db),
    jwtSecret: config.jwtSecret,
    jwtIssuer: config.jwtIssuer,
    jwtExpirationSeconds: config.jwtExpirationSeconds,
  };
});
