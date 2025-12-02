package com.dmcustoms.app.jwt.core;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.dmcustoms.app.data.repositories.DeactivatedTokenRepository;
import com.dmcustoms.app.data.repositories.UserRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TokenAuthenticationUserDetailsService
		implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	private final DeactivatedTokenRepository deactivatedTokenRepository;
	
	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken) throws UsernameNotFoundException {
		if (authenticationToken.getPrincipal() instanceof Token token) {
			return userRepository.findUserByEmail(token.getSubject()).get();
		}
		throw new UsernameNotFoundException("User not found");
	}

}
