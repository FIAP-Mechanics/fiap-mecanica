package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.exception.InsumoNotFound;
import com.fiap.mecanica.repository.InsumoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsumoServiceTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;

    @Mock
    private InsumoRepository insumoRepository;

    @InjectMocks
    private InsumoService insumoService;

    // ===================== buscarInsumoPorId =====================

    @Test
    void deveRetornarInsumoQuandoIdExistir() {
        Insumo insumo = criarInsumo();

        when(insumoRepository.findById(ID_EXISTENTE)).thenReturn(Optional.of(insumo));

        Insumo resultado = insumoService.buscarInsumoPorId(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.getNome()).isEqualTo("Óleo de motor");
        assertThat(resultado.getPrecoUnitario()).isEqualByComparingTo(new BigDecimal("45.90"));

        verify(insumoRepository).findById(ID_EXISTENTE);
    }

    @Test
    void deveLancarInsumoNotFoundQuandoIdNaoExistir() {
        when(insumoRepository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> insumoService.buscarInsumoPorId(ID_INEXISTENTE))
                .isInstanceOf(InsumoNotFound.class)
                .hasMessageContaining(String.valueOf(ID_INEXISTENTE));

        verify(insumoRepository).findById(ID_INEXISTENTE);
    }

    private Insumo criarInsumo() {
        return Insumo.builder()
                .id(ID_EXISTENTE)
                .nome("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .build();
    }
}
