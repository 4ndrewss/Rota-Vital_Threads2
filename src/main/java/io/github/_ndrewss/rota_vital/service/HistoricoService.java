package io.github._ndrewss.rota_vital.service;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;
import io.github._ndrewss.rota_vital.repository.ExecucaoHistoricoRepository;

/**
 * Guarda no banco cada execução feita pela API, para o tempo medido não se perder.
 */
@Service
public class HistoricoService {

    private static final String TIPO_SEQUENCIAL = "SEQUENCIAL";
    private static final String TIPO_PARALELO = "PARALELO";

    private final ExecucaoHistoricoRepository repository;

    public HistoricoService(ExecucaoHistoricoRepository repository) {
        this.repository = repository;
    }

    /** A versão sequencial roda numa única thread, por isso grava threads = 1. */
    public void registrarSequencial(int tamanho, long tempoMs) {
        repository.save(new ExecucaoHistorico(TIPO_SEQUENCIAL, tamanho, 1, tempoMs));
    }

    public void registrarParalelo(int tamanho, int threads, long tempoMs) {
        repository.save(new ExecucaoHistorico(TIPO_PARALELO, tamanho, threads, tempoMs));
    }
}
