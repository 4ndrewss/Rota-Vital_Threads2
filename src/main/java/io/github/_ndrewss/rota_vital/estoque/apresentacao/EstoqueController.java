package io.github._ndrewss.rota_vital.estoque.apresentacao;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.ConsultarBolsas;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.RegistrarBolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.ReservarBolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Reserva;

/**
 * Endpoints do Estoque. Sem regra de negócio: converte JSON em chamadas aos casos de uso
 * e a Bolsa em {@link BolsaResponse}. Os erros viram HTTP no ApiExceptionHandler.
 */
@RestController
@RequestMapping("/estoque/bolsas")
public class EstoqueController {

    /** Corpo do cadastro: {"tipoSanguineo": "O-", "validade": "2026-12-10"}. */
    public record CadastrarBolsaRequest(String tipoSanguineo, LocalDate validade) {
    }

    /** Corpo da reserva: {"requisicaoId": "req-1"}. */
    public record ReservarBolsaRequest(String requisicaoId) {
    }

    /** Representação JSON de uma bolsa; requisicaoId é nulo enquanto não houver reserva. */
    public record BolsaResponse(UUID id, String tipoSanguineo, LocalDate validade, boolean reservada,
            String requisicaoId) {

        static BolsaResponse de(Bolsa bolsa) {
            return new BolsaResponse(bolsa.getId(), bolsa.getTipo().getValor(), bolsa.getValidade().getData(),
                    bolsa.estaReservada(), bolsa.getReserva().map(Reserva::getRequisicaoId).orElse(null));
        }
    }

    private final RegistrarBolsa registrarBolsa;
    private final ConsultarBolsas consultarBolsas;
    private final ReservarBolsa reservarBolsa;

    public EstoqueController(RegistrarBolsa registrarBolsa, ConsultarBolsas consultarBolsas,
            ReservarBolsa reservarBolsa) {
        this.registrarBolsa = registrarBolsa;
        this.consultarBolsas = consultarBolsas;
        this.reservarBolsa = reservarBolsa;
    }

    @PostMapping
    public ResponseEntity<BolsaResponse> cadastrar(@RequestBody CadastrarBolsaRequest request) {
        Bolsa bolsa = registrarBolsa.executar(request.tipoSanguineo(), request.validade());
        return ResponseEntity.created(URI.create("/estoque/bolsas/" + bolsa.getId())).body(BolsaResponse.de(bolsa));
    }

    @GetMapping
    public List<BolsaResponse> listar() {
        return consultarBolsas.listar().stream().map(BolsaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public BolsaResponse consultar(@PathVariable String id) {
        return BolsaResponse.de(consultarBolsas.buscar(paraUuid(id)));
    }

    @PostMapping("/{id}/reservas")
    public BolsaResponse reservar(@PathVariable String id, @RequestBody ReservarBolsaRequest request) {
        return BolsaResponse.de(reservarBolsa.executar(paraUuid(id), request.requisicaoId()));
    }

    /** O id chega como texto para que um valor malformado vire 400 no formato {"erro": "..."}. */
    private static UUID paraUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("id de bolsa inválido: " + id);
        }
    }
}
