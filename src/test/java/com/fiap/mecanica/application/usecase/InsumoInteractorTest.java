package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.port.out.InsumoGateway;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.exception.InsumoNotFound;
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
class InsumoInteractorTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;

    @Mock
    private InsumoGateway insumoGateway;

    @InjectMocks
    private InsumoInteractor interactor;

    // ===================== buscarInsumoPorId =====================

    @Test
    void deveRetornarInsumoQuandoIdExistir() {
        Insumo insumo = criarInsumo();

        when(insumoGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(insumo));

        Insumo resultado = interactor.buscarInsumoPorId(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.getNome()).isEqualTo("Óleo de motor");
        assertThat(resultado.getPrecoUnitario()).isEqualByComparingTo(new BigDecimal("45.90"));

        verify(insumoGateway).buscarPorId(ID_EXISTENTE);
    }

    @Test
    void deveLancarInsumoNotFoundQuandoIdNaoExistir() {
        when(insumoGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarInsumoPorId(ID_INEXISTENTE))
                .isInstanceOf(InsumoNotFound.class)
                .hasMessageContaining(String.valueOf(ID_INEXISTENTE));

        verify(insumoGateway).buscarPorId(ID_INEXISTENTE);
    }

    private Insumo criarInsumo() {
        return Insumo.builder()
                .id(ID_EXISTENTE)
                .nome("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .build();
    }
}
