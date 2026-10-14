package io.github._ndrewss.rota_vital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;

public interface ExecucaoHistoricoRepository extends JpaRepository<ExecucaoHistorico, Long> {
}