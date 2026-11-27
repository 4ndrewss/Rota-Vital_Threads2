package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Cartão #09: raiz Bolsa e a regra de reserva, sem Spring e sem banco. */
class BolsaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 27);
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final TipoSanguineo O_NEG = new TipoSanguineo("O-");

    private Bolsa bolsaValidaAte(LocalDate data) {
        return new Bolsa(ID, O_NEG, new Validade(data));
    }

    @Test
    void bolsaNovaNaoEstaReservada() {
        Bolsa bolsa = bolsaValidaAte(HOJE.plusDays(10));

        assertEquals(ID, bolsa.getId());
        assertEquals(O_NEG, bolsa.getTipo());
        assertEquals(new Validade(HOJE.plusDays(10)), bolsa.getValidade());
        assertFalse(bolsa.estaReservada());
        assertEquals(Optional.empty(), bolsa.getReserva());
    }

    @Test
    void fabricaNovaGeraUmId() {
        Bolsa bolsa = Bolsa.nova(O_NEG, new Validade(HOJE));

        assertNotNull(bolsa.getId());
        assertNotEquals(bolsa.getId(), Bolsa.nova(O_NEG, new Validade(HOJE)).getId());
    }

    @Test
    void bolsaValidaReserva() {
        Bolsa bolsa = bolsaValidaAte(HOJE.plusDays(10));

        bolsa.reservar("req-1", HOJE);

        assertTrue(bolsa.estaReservada());
        assertEquals(Optional.of(new Reserva("req-1")), bolsa.getReserva());
    }

    /** Coerente com Validade: no dia do vencimento a bolsa ainda pode ser reservada. */
    @Test
    void bolsaNoDiaDoVencimentoAindaReserva() {
        Bolsa bolsa = bolsaValidaAte(HOJE);

        bolsa.reservar("req-1", HOJE);

        assertTrue(bolsa.estaReservada());
    }

    @Test
    void bolsaVencidaLancaBolsaVencidaEContinuaSemReserva() {
        Bolsa bolsa = bolsaValidaAte(HOJE.minusDays(1));

        BolsaVencida e = assertThrows(BolsaVencida.class, () -> bolsa.reservar("req-1", HOJE));

        assertEquals(ID, e.getBolsaId());
        assertFalse(bolsa.estaReservada());
    }

    @Test
    void bolsaJaReservadaLancaBolsaJaReservadaEMantemAReservaOriginal() {
        Bolsa bolsa = bolsaValidaAte(HOJE.plusDays(10));
        bolsa.reservar("req-1", HOJE);

        assertThrows(BolsaJaReservada.class, () -> bolsa.reservar("req-2", HOJE));

        assertEquals(Optional.of(new Reserva("req-1")), bolsa.getReserva());
    }

    /** Ordem de verificação: "já reservada" vem antes de "vencida". */
    @Test
    void bolsaReservadaEVencidaLancaBolsaJaReservada() {
        Bolsa bolsa = Bolsa.reconstituir(ID, O_NEG, new Validade(HOJE.minusDays(1)), new Reserva("req-1"));

        assertThrows(BolsaJaReservada.class, () -> bolsa.reservar("req-2", HOJE));
    }

    @Test
    void reservarComRequisicaoIdNuloLancaIllegalArgumentEContinuaSemReserva() {
        Bolsa bolsa = bolsaValidaAte(HOJE.plusDays(10));

        assertThrows(IllegalArgumentException.class, () -> bolsa.reservar(null, HOJE));

        assertFalse(bolsa.estaReservada());
    }

    @Test
    void construtorExigeTipo() {
        assertThrows(IllegalArgumentException.class, () -> new Bolsa(ID, null, new Validade(HOJE)));
    }

    @Test
    void construtorExigeValidade() {
        assertThrows(IllegalArgumentException.class, () -> new Bolsa(ID, O_NEG, null));
    }

    @Test
    void construtorExigeId() {
        assertThrows(IllegalArgumentException.class, () -> new Bolsa(null, O_NEG, new Validade(HOJE)));
    }

    @Test
    void reconstituirRestauraAReserva() {
        Bolsa bolsa = Bolsa.reconstituir(ID, O_NEG, new Validade(HOJE), new Reserva("req-9"));

        assertTrue(bolsa.estaReservada());
        assertEquals("req-9", bolsa.getReserva().orElseThrow().getRequisicaoId());
    }

    @Test
    void bolsasComOMesmoIdSaoIguais() {
        Bolsa a = bolsaValidaAte(HOJE);
        Bolsa b = new Bolsa(ID, new TipoSanguineo("A+"), new Validade(HOJE.plusDays(3)));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
