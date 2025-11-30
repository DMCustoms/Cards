package com.dmcustoms.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.dmcustoms.app.data.repositories.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfigurer {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(UserRepository userRepository) {
		return email -> {
			return userRepository.findUserByEmail(email).orElseThrow(
					() -> new UsernameNotFoundException("User with Email address " + email + " not found"));
		};
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http.httpBasic(Customizer.withDefaults()).authorizeHttpRequests(authorizeHttpRequests -> {
			authorizeHttpRequests.requestMatchers(HttpMethod.GET, "/api/user/**").hasRole("USER")
					.requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
					.requestMatchers(HttpMethod.GET, "/error").permitAll().anyRequest().denyAll();
		}).build();
	}

}
