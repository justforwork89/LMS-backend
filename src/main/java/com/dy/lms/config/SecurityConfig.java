package com.dy.lms.config;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.common.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		 ObjectMapper om = new ObjectMapper();
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(a -> a
		                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
		                .requestMatchers("/api/auth/login", "/api/health").permitAll()
		                .requestMatchers("/api/users/**").hasRole("ADMIN")
		                .requestMatchers(HttpMethod.POST, "/api/courses").hasAnyRole("INSTRUCTOR", "ADMIN")
		                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasAnyRole("INSTRUCTOR", "ADMIN")
		                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasAnyRole("INSTRUCTOR", "ADMIN")
		                .requestMatchers(HttpMethod.GET, "/api/codes").hasRole("ADMIN")
		                .requestMatchers(HttpMethod.POST, "/api/codes").hasRole("ADMIN")
		                .requestMatchers(HttpMethod.PUT, "/api/codes/**").hasRole("ADMIN")
		                .requestMatchers(HttpMethod.DELETE, "/api/codes/**").hasRole("ADMIN")
		                .requestMatchers(HttpMethod.POST, "/api/submissions").hasRole("STUDENT")
		                .anyRequest().authenticated())
		            .exceptionHandling(e -> e
		                .authenticationEntryPoint((req, res, ex) -> {
		                    res.setStatus(401);
		                    res.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
		                    res.getWriter().write(om.writeValueAsString(ApiResponse.fail("로그인이 필요합니다.")));
		                })
		                .accessDeniedHandler((req, res, ex) -> {
		                    res.setStatus(403);
		                    res.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
		                    res.getWriter().write(om.writeValueAsString(ApiResponse.fail("접근 권한이 없습니다.")));
		                }))
		            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}