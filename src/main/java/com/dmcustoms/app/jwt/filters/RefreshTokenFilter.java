package com.dmcustoms.app.jwt.filters;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.jwt.core.AccessTokenFactory;
import com.dmcustoms.app.jwt.serializers.AccessTokenSerializer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefreshTokenFilter extends OncePerRequestFilter {

	private final RequestMatcher requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST,
			"/api/jwt/refresh");

	private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

	private final AccessTokenFactory accessTokenFactory = new AccessTokenFactory();

	private final AccessTokenSerializer accessTokenSerializer;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (requestMatcher.matches(request)) {
			SecurityContext context = this.securityContextRepository.loadDeferredContext(request).get();
			if (context != null && context.getAuthentication() instanceof PreAuthenticatedAuthenticationToken
					&& context.getAuthentication().getPrincipal() instanceof User user && context.getAuthentication()
							.getAuthorities().contains(new SimpleGrantedAuthority("JWT_REFRESH"))) {
			}
		}
	}

}
