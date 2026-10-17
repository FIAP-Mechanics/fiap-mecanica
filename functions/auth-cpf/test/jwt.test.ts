import { jwtVerify } from "jose";
import { describe, expect, it } from "vitest";
import { gerarTokenCliente } from "../src/jwt";

const SEGREDO = "0123456789abcdef0123456789abcdef";

describe("jwt", () => {
  it("deve gerar token HS256 compatível com os serviços", async () => {
    const token = await gerarTokenCliente(7, SEGREDO, "http://localhost:8086", 3600);

    expect(token.token_type).toBe("Bearer");
    expect(token.expires_in).toBe(3600);

    const { payload, protectedHeader } = await jwtVerify(token.access_token, new TextEncoder().encode(SEGREDO), {
      issuer: "http://localhost:8086",
    });
    expect(protectedHeader.alg).toBe("HS256");
    expect(payload.sub).toBe("7");
    expect(payload.cliente_id).toBe(7);
    expect(payload.roles).toEqual(["CLIENTE"]);
    expect(payload.exp! - payload.iat!).toBe(3600);
  });
});
