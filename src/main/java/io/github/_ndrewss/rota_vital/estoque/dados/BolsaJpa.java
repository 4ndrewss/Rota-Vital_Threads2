package io.github._ndrewss.rota_vital.estoque.dados;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA da tabela "bolsa". Só existe na camada de dados; o domínio usa {@code Bolsa}.
 * A tabela é criada pelo Hibernate (ddl-auto), como o restante do projeto.
 */
@Entity
@Table(name = "bolsa")
public class BolsaJpa {

    /** Gerado pelo domínio (UUID), não pelo banco. */
    @Id
    private UUID id;

    @Column(name = "tipo_sanguineo", nullable = false, length = 3)
    private String tipoSanguineo;

    @Column(nullable = false)
    private LocalDate validade;

    /** Nulo enquanto a bolsa não estiver reservada. */
    @Column(name = "requisicao_id")
    private String requisicaoId;

    /** JPA exige construtor vazio. */
    protected BolsaJpa() {
    }

    BolsaJpa(UUID id, String tipoSanguineo, LocalDate validade, String requisicaoId) {
        this.id = id;
        this.tipoSanguineo = tipoSanguineo;
        this.validade = validade;
        this.requisicaoId = requisicaoId;
    }

    UUID getId() { return id; }
    String getTipoSanguineo() { return tipoSanguineo; }
    LocalDate getValidade() { return validade; }
    String getRequisicaoId() { return requisicaoId; }
}
