package io.github._ndrewss.rota_vital.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;

/** Acesso persistente às execuções realizadas pela API. */
public interface ExecucaoHistoricoRepository extends JpaRepository<ExecucaoHistorico, Long> {
    /** Aplica paginação no SQL e retorna os IDs em ordem decrescente. */
    List<ExecucaoHistorico> findAllByOrderByIdDesc(Pageable pagina);
}
