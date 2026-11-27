package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Cartão #08: exceções de negócio do Estoque, sem Spring; carregam o id da bolsa e uma mensagem. */
class ExcecoesEstoqueTest {

    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void bolsaVencidaInformaABolsa() {
        BolsaVencida e = new BolsaVencida(id);

        assertEquals(id, e.getBolsaId());
        assertTrue(e.getMessage().contains(id.toString()));
        assertTrue(e instanceof RuntimeException);
    }

    @Test
    void bolsaJaReservadaInformaABolsa() {
        BolsaJaReservada e = new BolsaJaReservada(id);

        assertEquals(id, e.getBolsaId());
        assertTrue(e.getMessage().contains(id.toString()));
        assertTrue(e instanceof RuntimeException);
    }
}
