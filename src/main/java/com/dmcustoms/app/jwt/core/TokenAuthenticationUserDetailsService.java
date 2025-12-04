package com.dmcustoms.app.jwt.core;

import java.time.Instant;
import java.util.Arrays;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.DeactivatedTokenRepository;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.data.types.Authorities;
import com.dmcustoms.app.data.types.JwtAuthorities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TokenAuthenticationUserDetailsService
		implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	private final DeactivatedTokenRepository deactivatedTokenRepository;

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
			throws UsernameNotFoundException {
		if (authenticationToken.getPrincipal() instanceof Token token
				&& !deactivatedTokenRepository.existsById(token.getId())
				&& token.getExpiresAt().isAfter(Instant.now())) {
			User user = userRepository.findUserByEmail(token.getSubject()).get();
			user.setToken(token);
			if (!token.getAuthorities().contains(JwtAuthorities.JWT_ACCESS)) user.setAuthorities(Arrays.asList(Authorities.JWT));
			return user;
		}
		throw new UsernameNotFoundException("User not found");
	}

}
