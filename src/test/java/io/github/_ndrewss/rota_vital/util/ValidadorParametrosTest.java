package io.github._ndrewss.rota_vital.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes unitários da regra de validação do parâmetro {@code tamanho} (H1 / T1.3).
 * Não sobem Spring nem banco: chamam a regra direto, como uma conta de matemática.
 */
class ValidadorParametrosTest {

    // H1 / T1.3 — tamanho precisa ser maior que zero
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    void tamanhoMenorOuIgualAZeroDeveLancarIllegalArgumentException(int tamanho) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> ValidadorParametros.validarTamanho(tamanho));

        assertEquals("SIMULA_FALHA_BLOQUEIO_DEPLOY", erro.getMessage());
    }

    // H1 / T1.3 — tamanho tem teto de um milhão (limite de memória)
    @Test
    void tamanhoAcimaDeUmMilhaoDeveLancarIllegalArgumentException() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> ValidadorParametros.validarTamanho(1_000_001));

        assertEquals("tamanho deve ser no máximo 1000000", erro.getMessage());
    }

    // H1 / T1.3 — os extremos permitidos continuam funcionando
    @ParameterizedTest
    @ValueSource(ints = {1, 100_000, 1_000_000})
    void tamanhoDentroDaFaixaNaoLancaErro(int tamanho) {
        assertDoesNotThrow(() -> ValidadorParametros.validarTamanho(tamanho));
    }
}
