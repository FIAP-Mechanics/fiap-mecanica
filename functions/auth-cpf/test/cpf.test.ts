import { describe, expect, it } from "vitest";
import { apenasDigitos, cpfValido, mascararCpf } from "../src/cpf";

describe("cpf", () => {
  it("deve remover tudo que não é dígito", () => {
    expect(apenasDigitos("529.982.247-25")).toBe("52998224725");
  });

  it("deve aceitar CPF com dígitos verificadores corretos", () => {
    expect(cpfValido("52998224725")).toBe(true);
  });

  it("deve rejeitar CPF com dígito verificador errado", () => {
    expect(cpfValido("52998224726")).toBe(false);
  });

  it("deve rejeitar sequências repetidas", () => {
    expect(cpfValido("11111111111")).toBe(false);
  });

  it("deve rejeitar tamanho diferente de 11 e CNPJ", () => {
    expect(cpfValido("1234567890")).toBe(false);
    expect(cpfValido("11222333000181")).toBe(false);
  });

  it("deve mascarar o CPF mantendo só o miolo", () => {
    expect(mascararCpf("52998224725")).toBe("***.982.***-**");
    expect(mascararCpf("123")).toBe("***");
  });
});
