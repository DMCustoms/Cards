package com.dmcustoms.app.jwt.filters;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Date;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dmcustoms.app.data.entities.DeactivatedToken;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.DeactivatedTokenRepository;
import com.dmcustoms.app.data.types.JwtAuthorities;

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
public class LogoutFilter extends OncePerRequestFilter {

	private final RequestMatcher requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST,
			"/api/auth/logout");

	private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

	private final DeactivatedTokenRepository deactivatedTokenRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (requestMatcher.matches(request)) {
			if (this.securityContextRepository.containsContext(request)) {
				SecurityContext context = this.securityContextRepository.loadDeferredContext(request).get();
				if (context != null && context.getAuthentication() instanceof PreAuthenticatedAuthenticationToken
						&& context.getAuthentication().getPrincipal() instanceof User user
						&& user.getToken().getAuthorities().contains(JwtAuthorities.JWT_LOGOUT)) {
					this.deactivatedTokenRepository.save(
							new DeactivatedToken(user.getToken().getId(), Date.from(user.getToken().getExpiresAt())));
					this.objectMapper.writeValue(response.getWriter(), "Logout success!");
					return;
				}
			}
			log.error("Caused issue in LogoutFilter.class");
			throw new AccessDeniedException("Caused issue in LogoutFilter.class");
		}
		filterChain.doFilter(request, response);

	}

}
