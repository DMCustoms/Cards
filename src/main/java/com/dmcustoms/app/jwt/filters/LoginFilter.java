package com.dmcustoms.app.jwt.filters;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dmcustoms.app.data.dto.TokensDTO;
import com.dmcustoms.app.jwt.core.AccessTokenFactory;
import com.dmcustoms.app.jwt.core.RefreshTokenFactory;
import com.dmcustoms.app.jwt.core.Token;
import com.dmcustoms.app.jwt.serializers.AccessTokenSerializer;
import com.dmcustoms.app.jwt.serializers.RefreshTokenSerializer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Data
@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoginFilter extends OncePerRequestFilter {

	private final RequestMatcher requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST,
			"/api/auth/login");

	private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

	private final RefreshTokenFactory refreshTokenFactory = new RefreshTokenFactory();

	private final AccessTokenFactory accessTokenFactory = new AccessTokenFactory();

	private final RefreshTokenSerializer refreshTokenSerializer;

	private final AccessTokenSerializer accessTokenSerializer;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (requestMatcher.matches(request)) {
			if (this.securityContextRepository.containsContext(request)) {
				SecurityContext context = this.securityContextRepository.loadDeferredContext(request).get();
				if (context != null && !(context.getAuthentication() instanceof PreAuthenticatedAuthenticationToken)) {
					Token refreshToken = this.refreshTokenFactory.apply(context.getAuthentication());
					Token accessToken = this.accessTokenFactory.apply(refreshToken);
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					this.objectMapper.writeValue(response.getWriter(), new TokensDTO(
							this.accessTokenSerializer.apply(accessToken), accessToken.getExpiresAt().toString(),
							this.refreshTokenSerializer.apply(refreshToken), refreshToken.getExpiresAt().toString()));
					return;
				}
			}
			log.error("Caused issue in RequestTokenFilter.class");
			throw new AccessDeniedException("Caused issue in RequestTokenFilter.class");
		}
		filterChain.doFilter(request, response);
	}

}
