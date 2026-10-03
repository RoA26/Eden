package com.eden.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj de la aplicacion en hora de Colombia. Los servicios piden "hoy"
 * a este bean en lugar de a LocalDate.now(), lo que tambien permite
 * fijar la fecha en las pruebas.
 */
@Configuration
public class RelojConfig {

    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    @Bean
    public Clock reloj() {
        return Clock.system(ZONA_COLOMBIA);
    }
}
