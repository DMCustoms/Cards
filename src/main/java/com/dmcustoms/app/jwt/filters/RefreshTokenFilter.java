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

import com.dmcustoms.app.data.dto.AccessTokenDTO;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.types.JwtAuthorities;
import com.dmcustoms.app.jwt.core.AccessTokenFactory;
import com.dmcustoms.app.jwt.core.Token;
import com.dmcustoms.app.jwt.serializers.AccessTokenSerializer;

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
public class RefreshTokenFilter extends OncePerRequestFilter {

	private final RequestMatcher requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST,
			"/api/auth/refresh");

	private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

	private final AccessTokenFactory accessTokenFactory = new AccessTokenFactory();

	private final AccessTokenSerializer accessTokenSerializer;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (requestMatcher.matches(request)) {
			if (this.securityContextRepository.containsContext(request)) {
				SecurityContext context = this.securityContextRepository.loadDeferredContext(request).get();
				if (context != null && context.getAuthentication() instanceof PreAuthenticatedAuthenticationToken
						&& context.getAuthentication().getPrincipal() instanceof User user
						&& user.getToken().getAuthorities().contains(JwtAuthorities.JWT_REFRESH)) {
					Token accessToken = this.accessTokenFactory.apply(user.getToken());
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					this.objectMapper.writeValue(response.getWriter(),
							new AccessTokenDTO(this.accessTokenSerializer.apply(accessToken),
									accessToken.getExpiresAt().toString()));
					return;
				}
			}
			log.error("Caused issue in RefreshTokenFilter.class");
			throw new AccessDeniedException("Caused issue in RefreshTokenFilter.class");
		}
		filterChain.doFilter(request, response);
	}

}
