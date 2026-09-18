package io.github._ndrewss.rota_vital.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.model.EstatisticaHospital;
import io.github._ndrewss.rota_vital.model.Requisicao;

@Service
public class EstatisticaService {
    public Map<String, EstatisticaHospital> calcularSequencial(List<Requisicao> requisicoes){
        Map<String, EstatisticaHospital> mapaEstatisticas = new HashMap<>();

        for(int i=0 ; i < requisicoes.size() ; i++){
            Requisicao req = requisicoes.get(i);
            String hospitalId = req.getHospitalId();

            EstatisticaHospital estatistica = mapaEstatisticas.get(hospitalId);
            if (estatistica == null) {
                estatistica = new EstatisticaHospital();
                mapaEstatisticas.put(hospitalId, estatistica);
            }

            estatistica.adicionar(req.getTempoAtendimento());
        }

        return mapaEstatisticas;
    }
}