package com.eden.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades propias de la aplicacion (prefijo "app" en application.properties).
 * Se validan al arrancar: si falta alguna, la aplicacion no inicia.
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record PropiedadesApp(
        @NotBlank String nombre,
        @NotBlank String codigoInvitacion
) {
}
