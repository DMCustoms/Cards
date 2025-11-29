package com.dmcustoms.app.data.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dmcustoms.app.data.types.Authorities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Entity(name = "users")
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class User implements UserDetails {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_surname", nullable = false, length = 32)
	@NonNull
	private String surname;
	
	@Column(name = "user_name", nullable = false, length = 32)
	@NonNull
	private String name;
	
	@Column(name = "user_lastname", nullable = false, length = 32)
	@NonNull
	private String lastname;
	
	@Column(name = "user_email", nullable = false, length = 64)
	@NonNull
	private String email;
	
	@Column(name = "user_password", nullable = false, length = 80)
	@NonNull
	private String password;
	
	@Column(name = "acc_non_expired", nullable = false)
	@NonNull
	private Boolean isAccountNonExpired;
	
	@Column(name = "acc_non_locked", nullable = false)
	@NonNull
	private Boolean isAccountNonLocked;
	
	@Column(name = "creds_non_expired", nullable = false)
	@NonNull
	private Boolean isCredentialsNonExpired;
	
	@Column(name = "acc_enabled", nullable = false)
	@NonNull
	private Boolean isEnabled;
	
	@Column(name = "user_authorities", nullable = false)
	@NonNull
	private List<Authorities> authorities;
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_owner")
	private List<Card> cards = new ArrayList<Card>();
	
	public void addCard(Card card) {
		this.cards.add(card);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.isAccountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.isAccountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.isCredentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
}
