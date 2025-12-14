package com.dmcustoms.app.security;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.dmcustoms.app.data.repositories.DeactivatedTokenRepository;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.jwt.core.JwtAuthenticationConfigurer;
import com.dmcustoms.app.jwt.serializers.AccessTokenDeserializer;
import com.dmcustoms.app.jwt.serializers.AccessTokenSerializer;
import com.dmcustoms.app.jwt.serializers.RefreshTokenDeserializer;
import com.dmcustoms.app.jwt.serializers.RefreshTokenSerializer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;

@Configuration
@EnableWebSecurity
public class SecurityConfigurer {

	@Bean
	JwtAuthenticationConfigurer jwtAuthenticationConfigurer(@Value("${jwt.access-token-key}") String accessTokenKey,
			@Value("${jwt.refresh-token-key}") String refreshTokenKey,
			DeactivatedTokenRepository deactivatedTokenRepository, UserRepository userRepository)
			throws JOSEException, ParseException {
		return new JwtAuthenticationConfigurer(
				new RefreshTokenSerializer(new DirectEncrypter(OctetSequenceKey.parse(refreshTokenKey))),
				new AccessTokenSerializer(new MACSigner(OctetSequenceKey.parse(accessTokenKey))),
				new RefreshTokenDeserializer(new DirectDecrypter(OctetSequenceKey.parse(refreshTokenKey))),
				new AccessTokenDeserializer(new MACVerifier(OctetSequenceKey.parse(accessTokenKey))),
				deactivatedTokenRepository, userRepository);
	};

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
	@Order(1)
	SecurityFilterChain loginSecurityFilterChain(HttpSecurity http,
			JwtAuthenticationConfigurer jwtAuthenticationConfigurer) {
		return http.with(jwtAuthenticationConfigurer, Customizer.withDefaults())
				.sessionManagement(
						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.securityMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/**"))
				.authorizeHttpRequests(authorizeHttpRequests -> {
					authorizeHttpRequests.requestMatchers(HttpMethod.POST, "/api/auth/**").authenticated();
				}).httpBasic(Customizer.withDefaults()).build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			JwtAuthenticationConfigurer jwtAuthenticationConfigurer) {
		return http.with(jwtAuthenticationConfigurer, Customizer.withDefaults())
				.sessionManagement(
						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeHttpRequests -> {
					authorizeHttpRequests.requestMatchers("/api/user/**").hasRole("USER")
							.requestMatchers("/api/admin/**").hasRole("ADMIN")
							.requestMatchers(HttpMethod.GET, "/error").permitAll().anyRequest().denyAll();
				}).build();
	}

}
