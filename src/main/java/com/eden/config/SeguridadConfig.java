package com.eden.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.eden.seguridad.FiltroIntentosLogin;
import com.eden.seguridad.LimitadorIntentosLogin;

/**
 * Configuracion de Spring Security. CSRF queda activo por defecto
 * (Thymeleaf agrega el token automaticamente en cada th:action) y la
 * sesion se regenera al iniciar sesion (proteccion contra fijacion de sesion).
 */
@Configuration
public class SeguridadConfig {

    private static final String[] RUTAS_PUBLICAS = {
            "/login", "/registro", "/error", "/css/**", "/js/**", "/img/**",
            "/iconos/**", "/manifest.json", "/sw.js", "/offline.html",
            "/recuperar", "/recuperar/**"
    };

    /** 30 dias: en el telefono no hay que iniciar sesion cada vez que se abre la app. */
    private static final int DURACION_RECORDARME_SEGUNDOS = 30 * 24 * 60 * 60;

    private final String claveRecordarme;

    public SeguridadConfig(@Value("${app.clave-recordarme:}") String claveRecordarme) {
        if (claveRecordarme == null || claveRecordarme.isBlank()) {
            throw new IllegalStateException(
                    "Falta la propiedad app.clave-recordarme (variable APP_CLAVE_RECORDARME). "
                    + "Usa un texto largo y aleatorio, por ejemplo el resultado de: openssl rand -hex 32");
        }
        this.claveRecordarme = claveRecordarme;
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http, LimitadorIntentosLogin limitador) throws Exception {
        http
            // Frena la fuerza bruta antes de que la contrasena llegue a verificarse.
            .addFilterBefore(new FiltroIntentosLogin(limitador), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(autorizacion -> autorizacion
                    .requestMatchers(RUTAS_PUBLICAS).permitAll()
                    .anyRequest().authenticated())
            .formLogin(formulario -> formulario
                    .loginPage("/login")
                    .usernameParameter("nombreUsuario")
                    .passwordParameter("contrasena")
                    .defaultSuccessUrl("/inicio", true)
                    .failureUrl("/login?error")
                    .permitAll())
            .rememberMe(recordar -> recordar
                    .key(claveRecordarme)
                    .rememberMeParameter("recordarme")
                    .tokenValiditySeconds(DURACION_RECORDARME_SEGUNDOS))
            .logout(salida -> salida
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?salida")
                    .deleteCookies("JSESSIONID", "remember-me")
                    .permitAll())
            .headers(cabeceras -> cabeceras
                    .contentSecurityPolicy(csp -> csp.policyDirectives(
                            "default-src 'self'; "
                            + "style-src 'self' https://fonts.googleapis.com; "
                            + "font-src https://fonts.gstatic.com; "
                            + "img-src 'self' data:; "
                            + "script-src 'self'; "
                            + "frame-ancestors 'none'")));
        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder(12);
    }
}
