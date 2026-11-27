package io.github._ndrewss.rota_vital.estoque.dados;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Reserva;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.TipoSanguineo;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Validade;

/**
 * Cartão #10: teste de integração do BolsaRepositoryJpa no H2 (em memória), sem dublês.
 * Garante o caminho de ida e volta Bolsa -> BolsaJpa -> banco -> BolsaJpa -> Bolsa.
 */
@SpringBootTest
class BolsaRepositoryJpaIntegracaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 27);

    @Autowired
    private BolsaRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.update("delete from bolsa");
    }

    @Test
    void salvaEBuscaPorId() {
        Bolsa bolsa = Bolsa.nova(new TipoSanguineo("AB-"), new Validade(HOJE.plusDays(5)));

        repository.salvar(bolsa);
        Bolsa lida = repository.buscarPorId(bolsa.getId()).orElseThrow();

        assertEquals(bolsa.getId(), lida.getId());
        assertEquals(new TipoSanguineo("AB-"), lida.getTipo());
        assertEquals(new Validade(HOJE.plusDays(5)), lida.getValidade());
        assertFalse(lida.estaReservada());
    }

    @Test
    void buscarIdInexistenteDevolveVazio() {
        assertEquals(Optional.empty(), repository.buscarPorId(UUID.randomUUID()));
    }

    @Test
    void listaDaValidadeMaisProximaParaAMaisDistante() {
        Bolsa depois = repository.salvar(Bolsa.nova(new TipoSanguineo("A+"), new Validade(HOJE.plusDays(9))));
        Bolsa antes = repository.salvar(Bolsa.nova(new TipoSanguineo("O-"), new Validade(HOJE.plusDays(1))));

        List<Bolsa> lista = repository.listar();

        assertEquals(List.of(antes.getId(), depois.getId()), lista.stream().map(Bolsa::getId).toList());
    }

    @Test
    void listaVaziaQuandoNaoHaBolsas() {
        assertTrue(repository.listar().isEmpty());
    }

    @Test
    void salvaUmaBolsaReservadaEPreservaAReserva() {
        Bolsa bolsa = repository.salvar(Bolsa.nova(new TipoSanguineo("B+"), new Validade(HOJE.plusDays(3))));

        bolsa.reservar("req-42", HOJE);
        repository.salvar(bolsa);
        Bolsa lida = repository.buscarPorId(bolsa.getId()).orElseThrow();

        assertTrue(lida.estaReservada());
        assertEquals(Optional.of(new Reserva("req-42")), lida.getReserva());
        assertEquals(1, repository.listar().size());
    }
}
