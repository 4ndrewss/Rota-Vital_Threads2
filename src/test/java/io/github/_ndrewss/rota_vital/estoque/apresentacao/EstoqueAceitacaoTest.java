package io.github._ndrewss.rota_vital.estoque.apresentacao;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

/**
 * Cartão #12: teste de ponta a ponta do Estoque, no estilo Dado / Quando / Então.
 * A chamada passa por controller, casos de uso, BolsaRepositoryJpa e H2 de verdade.
 */
@SpringBootTest
class EstoqueAceitacaoTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private JdbcTemplate jdbc;

    private MockMvc mockMvc;

    @BeforeEach
    void prepararBancoVazio() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        jdbc.update("delete from bolsa");
    }

    private String cadastrar(String tipo, LocalDate validade) throws Exception {
        String json = mockMvc.perform(post("/estoque/bolsas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSanguineo\":\"" + tipo + "\",\"validade\":\"" + validade + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    private static String corpoReserva(String requisicaoId) {
        return "{\"requisicaoId\":\"" + requisicaoId + "\"}";
    }

    @Test
    @DisplayName("Dado o estoque vazio, quando cadastro e reservo uma bolsa válida, então ela fica gravada como reservada e não aceita outra reserva")
    void fluxoCompletoDeCadastroEReserva() throws Exception {
        String id = cadastrar("O-", LocalDate.now().plusDays(10));

        mockMvc.perform(get("/estoque/bolsas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", id)
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservada").value(true));

        mockMvc.perform(get("/estoque/bolsas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservada").value(true))
                .andExpect(jsonPath("$.requisicaoId").value("req-1"));

        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", id)
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-2")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Dada uma bolsa vencida no banco, quando tento reservá-la, então recebo 422 e ela continua livre")
    void bolsaVencidaNaoEReservada() throws Exception {
        String id = cadastrar("A+", LocalDate.now().minusDays(1));

        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", id)
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-1")))
                .andExpect(status().is(422));

        mockMvc.perform(get("/estoque/bolsas/{id}", id))
                .andExpect(jsonPath("$.reservada").value(false));
    }

    @Test
    @DisplayName("Dado um id que não existe, quando consulto ou reservo, então recebo 404")
    void idInexistenteDevolve404() throws Exception {
        String id = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/estoque/bolsas/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(post("/estoque/bolsas/{id}/reservas", id)
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva("req-1")))
                .andExpect(status().isNotFound());
    }
}
