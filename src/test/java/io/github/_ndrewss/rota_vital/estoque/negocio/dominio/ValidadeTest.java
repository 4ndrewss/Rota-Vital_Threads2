package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Cartão #07: objeto de valor Validade, testado sem Spring e sem banco. */
class ValidadeTest {

    private static final LocalDate VENCIMENTO = LocalDate.of(2026, 12, 10);

    @Test
    void guardaAData() {
        assertEquals(VENCIMENTO, new Validade(VENCIMENTO).getData());
    }

    @Test
    void rejeitaDataNula() {
        assertThrows(IllegalArgumentException.class, () -> new Validade(null));
    }

    @Test
    void estaVencidaDepoisDoDiaDoVencimento() {
        assertTrue(new Validade(VENCIMENTO).estaVencida(VENCIMENTO.plusDays(1)));
    }

    /** Regra do dia exato: a bolsa ainda pode ser usada no próprio dia do vencimento. */
    @Test
    void noDiaDoVencimentoAindaNaoEstaVencida() {
        assertFalse(new Validade(VENCIMENTO).estaVencida(VENCIMENTO));
    }

    @Test
    void naoEstaVencidaAntesDoVencimento() {
        assertFalse(new Validade(VENCIMENTO).estaVencida(VENCIMENTO.minusDays(1)));
    }

    @Test
    void estaVencidaRejeitaHojeNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Validade(VENCIMENTO).estaVencida(null));
    }

    @Test
    void validadesComAMesmaDataSaoIguaisETemOMesmoHashCode() {
        Validade a = new Validade(VENCIMENTO);
        Validade b = new Validade(LocalDate.of(2026, 12, 10));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void validadesComDatasDiferentesSaoDiferentes() {
        assertNotEquals(new Validade(VENCIMENTO), new Validade(VENCIMENTO.plusDays(1)));
    }
}
