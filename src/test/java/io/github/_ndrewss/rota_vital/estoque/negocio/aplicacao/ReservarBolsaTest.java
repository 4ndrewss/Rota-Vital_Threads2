package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaJaReservada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaNaoEncontrada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaVencida;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.TipoSanguineo;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Validade;

/** Cartão #11: caso de uso ReservarBolsa com repositório em memória e relógio fixo. */
class ReservarBolsaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 27);
    private final Clock relogio = Clock.fixed(HOJE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC"));
    private final BolsaRepositoryEmMemoria repository = new BolsaRepositoryEmMemoria();
    private final ReservarBolsa reservarBolsa = new ReservarBolsa(repository, relogio);

    private Bolsa gravada(LocalDate validade) {
        Bolsa bolsa = repository.salvar(Bolsa.nova(new TipoSanguineo("O+"), new Validade(validade)));
        repository.salvamentos = 0;
        return bolsa;
    }

    @Test
    void reservaESalvaABolsa() {
        Bolsa bolsa = gravada(HOJE.plusDays(2));

        Bolsa reservada = reservarBolsa.executar(bolsa.getId(), "req-1");

        assertTrue(reservada.estaReservada());
        assertEquals("req-1", repository.buscarPorId(bolsa.getId()).orElseThrow()
                .getReserva().orElseThrow().getRequisicaoId());
        assertEquals(1, repository.salvamentos);
    }

    /** A data de "hoje" vem do Clock: no dia do vencimento ainda reserva. */
    @Test
    void usaADataDoRelogio() {
        Bolsa bolsa = gravada(HOJE);

        assertTrue(reservarBolsa.executar(bolsa.getId(), "req-1").estaReservada());
    }

    @Test
    void bolsaInexistenteLancaBolsaNaoEncontrada() {
        UUID id = UUID.randomUUID();

        BolsaNaoEncontrada e = assertThrows(BolsaNaoEncontrada.class, () -> reservarBolsa.executar(id, "req-1"));

        assertEquals(id, e.getBolsaId());
        assertEquals(0, repository.salvamentos);
    }

    @Test
    void bolsaVencidaNaoESalva() {
        Bolsa bolsa = gravada(HOJE.minusDays(1));

        assertThrows(BolsaVencida.class, () -> reservarBolsa.executar(bolsa.getId(), "req-1"));

        assertFalse(repository.buscarPorId(bolsa.getId()).orElseThrow().estaReservada());
        assertEquals(0, repository.salvamentos);
    }

    @Test
    void bolsaJaReservadaNaoTrocaAReserva() {
        Bolsa bolsa = gravada(HOJE.plusDays(2));
        reservarBolsa.executar(bolsa.getId(), "req-1");

        assertThrows(BolsaJaReservada.class, () -> reservarBolsa.executar(bolsa.getId(), "req-2"));

        assertEquals("req-1", repository.buscarPorId(bolsa.getId()).orElseThrow()
                .getReserva().orElseThrow().getRequisicaoId());
        assertEquals(1, repository.salvamentos);
    }
}
