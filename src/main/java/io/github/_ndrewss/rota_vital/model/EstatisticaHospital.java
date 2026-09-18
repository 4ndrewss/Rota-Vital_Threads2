package io.github._ndrewss.rota_vital.model;

public class EstatisticaHospital {

    private int totalRequisicoes;
    private Long somaTempoAtendimento;

    public EstatisticaHospital(){
        totalRequisicoes = 0;
        somaTempoAtendimento = 0L;
    }

    public void adicionar(long tempoAtendimento){
        totalRequisicoes = totalRequisicoes + 1;
        somaTempoAtendimento = somaTempoAtendimento + tempoAtendimento;
    }

    public Long getMedia(){
        return somaTempoAtendimento / totalRequisicoes;
    }

    public int getTotalRequisicoes(){
        return totalRequisicoes;
    }

    public Long getSomaTempoAtendimento(){
        return somaTempoAtendimento;
    }
    
}
