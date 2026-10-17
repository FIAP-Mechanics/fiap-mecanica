import { SignJWT } from "jose";

export interface TokenCliente {
  access_token: string;
  token_type: "Bearer";
  expires_in: number;
}

export async function gerarTokenCliente(
  clienteId: number,
  segredo: string,
  issuer: string,
  expiracaoSegundos: number,
): Promise<TokenCliente> {
  const agora = Math.floor(Date.now() / 1000);
  const accessToken = await new SignJWT({ cliente_id: clienteId, roles: ["CLIENTE"] })
    .setProtectedHeader({ alg: "HS256", typ: "JWT" })
    .setIssuer(issuer)
    .setSubject(String(clienteId))
    .setIssuedAt(agora)
    .setExpirationTime(agora + expiracaoSegundos)
    .sign(new TextEncoder().encode(segredo));

  return { access_token: accessToken, token_type: "Bearer", expires_in: expiracaoSegundos };
}
