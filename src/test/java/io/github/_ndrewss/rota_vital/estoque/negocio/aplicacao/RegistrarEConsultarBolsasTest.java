package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaNaoEncontrada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.TipoSanguineo;

/** Cartão #12: casos de uso RegistrarBolsa e ConsultarBolsas com repositório em memória. */
class RegistrarEConsultarBolsasTest {

    private static final LocalDate VALIDADE = LocalDate.of(2026, 12, 20);
    private final BolsaRepositoryEmMemoria repository = new BolsaRepositoryEmMemoria();
    private final RegistrarBolsa registrar = new RegistrarBolsa(repository);
    private final ConsultarBolsas consultar = new ConsultarBolsas(repository);

    @Test
    void registraUmaBolsaSemReserva() {
        Bolsa bolsa = registrar.executar("A-", VALIDADE);

        assertEquals(new TipoSanguineo("A-"), bolsa.getTipo());
        assertEquals(VALIDADE, bolsa.getValidade().getData());
        assertFalse(bolsa.estaReservada());
        assertEquals(bolsa, consultar.buscar(bolsa.getId()));
    }

    @Test
    void registrarComDadosInvalidosNaoGrava() {
        assertThrows(IllegalArgumentException.class, () -> registrar.executar("X+", VALIDADE));
        assertThrows(IllegalArgumentException.class, () -> registrar.executar("A+", null));

        assertTrue(consultar.listar().isEmpty());
    }

    @Test
    void listaAsBolsasRegistradas() {
        Bolsa a = registrar.executar("A+", VALIDADE);
        Bolsa b = registrar.executar("B+", VALIDADE.minusDays(1));

        assertEquals(List.of(b, a), consultar.listar());
    }

    @Test
    void buscarInexistenteLancaBolsaNaoEncontrada() {
        assertThrows(BolsaNaoEncontrada.class, () -> consultar.buscar(UUID.randomUUID()));
    }
}
