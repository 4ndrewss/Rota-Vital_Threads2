package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Cartão #08: a Reserva guarda o id da Requisicao (String, o mesmo tipo de Requisicao.getId()). */
class ReservaTest {

    @Test
    void guardaORequisicaoId() {
        assertEquals("req-1", new Reserva("req-1").getRequisicaoId());
    }

    @Test
    void rejeitaRequisicaoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Reserva(null));
    }

    @Test
    void rejeitaRequisicaoIdEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> new Reserva("  "));
    }

    @Test
    void reservasDaMesmaRequisicaoSaoIguais() {
        assertEquals(new Reserva("req-1"), new Reserva("req-1"));
        assertEquals(new Reserva("req-1").hashCode(), new Reserva("req-1").hashCode());
        assertNotEquals(new Reserva("req-1"), new Reserva("req-2"));
    }
}
