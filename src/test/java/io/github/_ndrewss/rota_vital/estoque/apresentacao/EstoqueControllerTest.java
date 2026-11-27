package io.github._ndrewss.rota_vital.estoque.apresentacao;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.github._ndrewss.rota_vital.compartilhado.apresentacao.ApiExceptionHandler;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.BolsaRepositoryEmMemoria;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.ConsultarBolsas;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.RegistrarBolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.ReservarBolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.TipoSanguineo;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Validade;

/**
 * Cartão #12: o EstoqueController via MockMvc sem subir o Spring, com os casos de uso reais
 * sobre o repositório em memória e o ApiExceptionHandler. Confere o HTTP: status, JSON e erros.
 */
class EstoqueControllerTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 27);
    private final Clock relogio = Clock.fixed(HOJE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC"));
    private final BolsaRepositoryEmMemoria repository = new BolsaRepositoryEmMemoria();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new EstoqueController(new RegistrarBolsa(repository),
                    new ConsultarBolsas(repository), new ReservarBolsa(repository, relogio)))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    private Bolsa gravada(String tipo, LocalDate validade) {
        return repository.salvar(Bolsa.nova(new TipoSanguineo(tipo), new Validade(validade)));
    }

    private static String corpoReserva(String requisicaoId) {
        return "{\"requisicaoId\":\"" + requisicaoId + "\"}";
    }

    @Test
    void cadastrarDevolve201ComABolsaEOLocation() throws Exception {
        mockMvc.perform(post("/estoque/bolsas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSanguineo\":\"O-\",\"validade\":\"2026-12-10\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.tipoSanguineo").value("O-"))
                .andExpect(jsonPath("$.validade").value("2026-12-10"))
                .andExpect(jsonPath("$.reservada").value(false));
    }

    @Test
    void cadastrarComTipoInvalidoDevolve400() throws Exception {
        mockMvc.perform(post("/estoque/bolsas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSanguineo\":\"Z+\",\"validade\":\"2026-12-10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void cadastrarSemValidadeDevolve400() throws Exception {
        mockMvc.perform(post("/estoque/bolsas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSanguineo\":\"A+\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("validade é obrigatória"));
    }

    @Test
    void cadastrarComDataMalformadaDevolve400() throws Exception {
        mockMvc.perform(post("/estoque/bolsas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSanguineo\":\"A+\",\"validade\":\"10/12/2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void listarDevolveAsBolsas() throws Exception {
        gravada("A+", HOJE.plusDays(1));
        gravada("B-", HOJE.plusDays(2));

        mockMvc.perform(get("/estoque/bolsas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipoSanguineo").value("A+"));
    }

    @Test
    void consultarDevolveABolsa() throws Exception {
        Bolsa bolsa = gravada("AB+", HOJE.plusDays(1));

        mockMvc.perform(get("/estoque/bolsas/{id}", bolsa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bolsa.getId().toString()))
                .andExpect(jsonPath("$.tipoSanguineo").value("AB+"));
    }

    @Test
    void consultarInexistenteDevolve404() throws Exception {
        mockMvc.perform(get("/estoque/bolsas/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void consultarComIdMalformadoDevolve400() throws Exception {
        mockMvc.perform(get("/estoque/bolsas/nao-e-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void reservarDevolve200ComABolsaReservada() throws Exception {
        Bolsa bolsa = gravada("O+", HOJE.plusDays(1));

        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", bolsa.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservada").value(true))
                .andExpect(jsonPath("$.requisicaoId").value("req-1"));
    }

    @Test
    void reservarDeNovoDevolve409() throws Exception {
        Bolsa bolsa = gravada("O+", HOJE.plusDays(1));
        bolsa.reservar("req-1", HOJE);
        repository.salvar(bolsa);

        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", bolsa.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-2")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void reservarVencidaDevolve422() throws Exception {
        Bolsa bolsa = gravada("O+", HOJE.minusDays(1));

        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", bolsa.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-1")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void reservarInexistenteDevolve404() throws Exception {
        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void reservarSemRequisicaoIdDevolve400() throws Exception {
        Bolsa bolsa = gravada("O+", HOJE.plusDays(1));

        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", bolsa.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
