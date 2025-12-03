package com.dmcustoms.app.jwt.core;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.dmcustoms.app.data.repositories.DeactivatedTokenRepository;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.jwt.filters.LogoutFilter;
import com.dmcustoms.app.jwt.filters.RefreshTokenFilter;
import com.dmcustoms.app.jwt.filters.RequestTokenFilter;
import com.dmcustoms.app.jwt.serializers.AccessTokenDeserializer;
import com.dmcustoms.app.jwt.serializers.AccessTokenSerializer;
import com.dmcustoms.app.jwt.serializers.RefreshTokenDeserializer;
import com.dmcustoms.app.jwt.serializers.RefreshTokenSerializer;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JwtAuthenticationConfigurer extends AbstractHttpConfigurer<JwtAuthenticationConfigurer, HttpSecurity> {

	private final RefreshTokenSerializer refreshTokenSerializer;

	private final AccessTokenSerializer accessTokenSerializer;

	private final RefreshTokenDeserializer refreshTokenDeserializer;

	private final AccessTokenDeserializer accessTokenDeserializer;

	private final DeactivatedTokenRepository deactivatedTokenRepository;

	private final UserRepository userRepository;

	@Override
	public void init(HttpSecurity builder) {
		@SuppressWarnings("unchecked")
		CsrfConfigurer<?> csrfConfigurer = builder.getConfigurer(CsrfConfigurer.class);
		if (csrfConfigurer != null)
			csrfConfigurer.ignoringRequestMatchers(
					PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/jwt/access"));
	}

	@Override
	public void configure(HttpSecurity builder) {
		RequestTokenFilter requestTokenFilter = new RequestTokenFilter(refreshTokenSerializer, accessTokenSerializer);
		AuthenticationFilter authenticationFilter = new AuthenticationFilter(
				(AuthenticationManager) builder.getSharedObject(AuthenticationManager.class),
				new JwtAuthenticationConverter(this.accessTokenDeserializer, this.refreshTokenDeserializer));
		authenticationFilter.setSuccessHandler((request, response, authentication) -> CsrfFilter.skipRequest(request));
		authenticationFilter.setFailureHandler(
				(request, response, exception) -> response.sendError(HttpServletResponse.SC_FORBIDDEN));
		PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider = new PreAuthenticatedAuthenticationProvider();
		preAuthenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(
				new TokenAuthenticationUserDetailsService(this.deactivatedTokenRepository, this.userRepository));
		RefreshTokenFilter refreshTokenFilter = new RefreshTokenFilter(this.accessTokenSerializer);
		LogoutFilter logoutFilter = new LogoutFilter(this.deactivatedTokenRepository);
		builder.addFilterBefore(requestTokenFilter, ExceptionTranslationFilter.class)
				.addFilterBefore(authenticationFilter, CsrfFilter.class)
				.addFilterBefore(refreshTokenFilter, ExceptionTranslationFilter.class)
				.addFilterBefore(logoutFilter, ExceptionTranslationFilter.class)
				.authenticationProvider(preAuthenticatedAuthenticationProvider);
	}

}
