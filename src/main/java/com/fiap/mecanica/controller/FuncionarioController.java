package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.controller.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.domain.Funcionario;
import com.fiap.mecanica.dto.FuncionarioDto;
import com.fiap.mecanica.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.fiap.mecanica.controller.mapper.FuncionarioMapper.toDto;
import static com.fiap.mecanica.controller.mapper.FuncionarioMapper.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService service;

    @GetMapping("/{id}")
    public FuncionarioDto get(@PathVariable Long id) {
        return toDto(service.buscarFuncionarioPorId(id));
    }

    @PostMapping
    public FuncionarioDto create(@Valid @RequestBody CadastrarFuncionarioRequest request) {
        Funcionario funcionario = toEntity(request);
        return toDto(service.cadastrarFuncionario(funcionario));
    }

    @PatchMapping("/{id}")
    public FuncionarioDto update(@PathVariable Long id, @RequestBody AtualizarFuncionarioRequest request) {
        FuncionarioDto funcionario = toDto(request);
        return toDto(service.atualizarFuncionario(id, funcionario));
    }

    @DeleteMapping("/{id}")
    public FuncionarioDto delete(@PathVariable Long id) {
        return toDto(service.excluirFuncionario(id));
    }

    @PutMapping("/{id}/reativar")
    public FuncionarioDto reativar(@PathVariable Long id) {
        return toDto(service.reativarFuncionario(id));
    }
}
