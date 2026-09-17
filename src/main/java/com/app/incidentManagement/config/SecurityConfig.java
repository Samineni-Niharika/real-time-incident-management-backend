package com.app.incidentManagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.app.incidentManagement.security.JwtAuthenticationFilter;
import com.app.incidentManagement.security.RestAccessDeniedHandler;
import com.app.incidentManagement.security.RestAuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	 private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private  RestAuthenticationEntryPoint authenticationEntryPoint;
	@Autowired
	private  RestAccessDeniedHandler accessDeniedHandler;
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	

	    @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

	        http
	            .csrf(csrf -> csrf.disable())
	            .cors(cors -> {})
	            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
	            .authorizeHttpRequests(auth -> auth
	                .requestMatchers("/api/auth/register").permitAll()
	                .requestMatchers("/api/auth/login").permitAll()
	                .requestMatchers("/ws/**").permitAll()
	                .requestMatchers("/api/users/reset-password").permitAll()
	                .requestMatchers("/api/users/forgot-password").permitAll()
	                .anyRequest().authenticated()
	            )
	            .exceptionHandling(exception -> exception
	                    .authenticationEntryPoint(authenticationEntryPoint)
	                    .accessDeniedHandler(accessDeniedHandler)
	                )
	            .addFilterBefore(
	                    jwtAuthenticationFilter,
	                    UsernamePasswordAuthenticationFilter.class
	                );

	        return http.build();
	    }
	

}
