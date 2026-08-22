package com.fiap.mecanica.application.result;

import java.util.List;

public record IdentidadeAutenticadaResult(String principal, List<String> autoridades) {
}
