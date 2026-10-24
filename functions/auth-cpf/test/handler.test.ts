import type { APIGatewayProxyEvent } from "aws-lambda";
import { jwtVerify } from "jose";
import { describe, expect, it, vi } from "vitest";
import type { ClienteRepository } from "../src/db";
import { criarHandler, type Dependencias } from "../src/handler";

const SEGREDO = "0123456789abcdef0123456789abcdef";
const CPF_VALIDO = "529.982.247-25";

function evento(corpo: string | null, headers: Record<string, string> = {}, base64 = false): APIGatewayProxyEvent {
  return { body: corpo, headers, isBase64Encoded: base64 } as unknown as APIGatewayProxyEvent;
}

function dependencias(repo: ClienteRepository): Dependencias {
  return { clientes: repo, jwtSecret: SEGREDO, jwtIssuer: "http://localhost:8086", jwtExpirationSeconds: 3600 };
}

function repo(retorno: Awaited<ReturnType<ClienteRepository["buscarPorDocumento"]>>) {
  return { buscarPorDocumento: vi.fn().mockResolvedValue(retorno) };
}

describe("handler", () => {
  it("deve devolver JWT para cliente ativo e consultar só os dígitos", async () => {
    const clientes = repo({ id: 12, ativo: true });
    const handler = criarHandler(dependencias(clientes));

    const resposta = await handler(evento(JSON.stringify({ cpf: CPF_VALIDO }), { "X-Request-ID": "abc" }));

    expect(resposta.statusCode).toBe(200);
    expect(resposta.headers?.["X-Request-ID"]).toBe("abc");
    expect(clientes.buscarPorDocumento).toHaveBeenCalledWith("52998224725");
    const corpo = JSON.parse(resposta.body);
    const { payload } = await jwtVerify(corpo.access_token, new TextEncoder().encode(SEGREDO));
    expect(payload.cliente_id).toBe(12);
  });

  it("deve aceitar corpo em base64", async () => {
    const handler = criarHandler(dependencias(repo({ id: 1, ativo: true })));
    const corpo = Buffer.from(JSON.stringify({ cpf: CPF_VALIDO })).toString("base64");

    const resposta = await handler(evento(corpo, {}, true));

    expect(resposta.statusCode).toBe(200);
    expect(resposta.headers?.["X-Request-ID"]).toBe("sem-correlation-id");
  });

  it.each([[null], ["não é json"], [JSON.stringify({ cpf: 123 })], [JSON.stringify({})]])(
    "deve responder 400 quando o corpo não traz CPF (%s)",
    async (corpo) => {
      const clientes = repo(null);
      const resposta = await criarHandler(dependencias(clientes))(evento(corpo));

      expect(resposta.statusCode).toBe(400);
      expect(clientes.buscarPorDocumento).not.toHaveBeenCalled();
    },
  );

  it("deve responder 400 para CPF inválido sem consultar o banco", async () => {
    const clientes = repo(null);
    const resposta = await criarHandler(dependencias(clientes))(evento(JSON.stringify({ cpf: "111.111.111-11" })));

    expect(resposta.statusCode).toBe(400);
    expect(JSON.parse(resposta.body).erros[0].codigo).toBe("validacao");
    expect(clientes.buscarPorDocumento).not.toHaveBeenCalled();
  });

  it("deve responder 401 genérico para cliente inexistente", async () => {
    const resposta = await criarHandler(dependencias(repo(null)))(evento(JSON.stringify({ cpf: CPF_VALIDO })));

    expect(resposta.statusCode).toBe(401);
    expect(JSON.parse(resposta.body).erros[0].codigo).toBe("nao-autorizado");
  });

  it("deve responder 401 igual para cliente inativo", async () => {
    const inexistente = await criarHandler(dependencias(repo(null)))(evento(JSON.stringify({ cpf: CPF_VALIDO })));
    const inativo = await criarHandler(dependencias(repo({ id: 3, ativo: false })))(
      evento(JSON.stringify({ cpf: CPF_VALIDO })),
    );

    expect(inativo.statusCode).toBe(401);
    expect(inativo.body).toBe(inexistente.body);
  });

  it("deve responder 500 quando o banco falha", async () => {
    const clientes = { buscarPorDocumento: vi.fn().mockRejectedValue(new Error("conexão recusada")) };
    const resposta = await criarHandler(dependencias(clientes))(evento(JSON.stringify({ cpf: CPF_VALIDO })));

    expect(resposta.statusCode).toBe(500);
    expect(JSON.parse(resposta.body).erros[0].codigo).toBe("erro-interno");
  });

  it("deve carregar as dependências uma única vez entre invocações", async () => {
    const carregar = vi.fn().mockResolvedValue(dependencias(repo({ id: 1, ativo: true })));
    const handler = criarHandler(carregar);

    await handler(evento(JSON.stringify({ cpf: CPF_VALIDO })));
    await handler(evento(JSON.stringify({ cpf: CPF_VALIDO })));

    expect(carregar).toHaveBeenCalledTimes(1);
  });
});
