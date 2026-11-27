package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Cartão #07: objeto de valor TipoSanguineo, testado sem Spring e sem banco. */
class TipoSanguineoTest {

    @ParameterizedTest
    @ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
    void aceitaOsOitoTiposValidos(String valor) {
        assertEquals(valor, new TipoSanguineo(valor).getValor());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "C+", "A", "AB", "o+", "A +", "ABO+"})
    void rejeitaTipoInvalido(String valor) {
        assertThrows(IllegalArgumentException.class, () -> new TipoSanguineo(valor));
    }

    @Test
    void rejeitaTipoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new TipoSanguineo(null));
    }

    @Test
    void tiposComOMesmoValorSaoIguaisETemOMesmoHashCode() {
        TipoSanguineo a = new TipoSanguineo("AB-");
        TipoSanguineo b = new TipoSanguineo("AB-");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void tiposComValoresDiferentesSaoDiferentes() {
        assertNotEquals(new TipoSanguineo("A+"), new TipoSanguineo("A-"));
    }
}
