package es.uvigo.mei.jwt.seguridad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.uvigo.mei.jwt.seguridad.autenticacion.UserDetailsServiceImpl;
import es.uvigo.mei.jwt.seguridad.jwt.FiltroAutenticacionJWT;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		// Passwordencoder a usar
		return new BCryptPasswordEncoder();
	}

	@Bean
	public FiltroAutenticacionJWT filtroAutenticacionJWT() {
		// Filtro de autenticación de peticiones basado en JWT
		return new FiltroAutenticacionJWT();
	}

	@Bean
    public SecurityFilterChain filterChainConfig(HttpSecurity http) throws Exception {
		// Configuracion de autenticacion
		http.userDetailsService(userDetailsService);

		// Configuracion de autorizacion
		http.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // desactiva uso de cookies y sesiones (peticiones Stateless)
			.authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()     // acceso sin restricciones a endpoint de autenticacion
                                               .requestMatchers("/api/pruebas/**").permitAll()  // acceso sin restricciones a endpoint del API (se limitara con anotaciones)
                                               .anyRequest().authenticated()  // acceso autenticado al resto de URLs (en este caso no hay ninguna)
			);

		// Filtro JWT
		// Establece filtros por los que pasan las peticiones (y su orden)
		// - filtroAutenticacionJWT: comprueba que petición incluye un token JWT, lo valida 
		//  y extrae info. de autenticacion de usuarios de la BD
		// - UsernamePasswordAuthenticationFilter: filtro general que procesa info. de
		// autenticacion de usuarios
		http.addFilterBefore(filtroAutenticacionJWT(), UsernamePasswordAuthenticationFilter.class);
	
		return http.build();
	}
}
