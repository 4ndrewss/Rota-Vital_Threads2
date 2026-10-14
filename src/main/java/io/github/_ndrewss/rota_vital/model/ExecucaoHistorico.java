package io.github._ndrewss.rota_vital.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // vira uma tabela no banco
public class ExecucaoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // o banco gera o id
    private Long id;

    private String tipo;          // "SEQUENCIAL" ou "PARALELO"
    private int tamanho;          // quantidade de requisições
    private int threads;          // 1 para sequencial
    private long tempoMs;
    private LocalDateTime criadoEm;

    // JPA exige construtor vazio
    public ExecucaoHistorico() {
    }

    public ExecucaoHistorico(String tipo, int tamanho, int threads, long tempoMs) {
        this.tipo = tipo;
        this.tamanho = tamanho;
        this.threads = threads;
        this.tempoMs = tempoMs;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public int getTamanho() { return tamanho; }
    public int getThreads() { return threads; }
    public long getTempoMs() { return tempoMs; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}