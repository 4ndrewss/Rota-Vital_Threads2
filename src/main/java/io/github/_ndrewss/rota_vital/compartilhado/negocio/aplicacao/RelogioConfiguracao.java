package io.github._ndrewss.rota_vital.compartilhado.negocio.aplicacao;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relógio único da aplicação; os casos de uso recebem o Clock para não depender de "agora" fixo no código. */
@Configuration
public class RelogioConfiguracao {

    @Bean
    public Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
