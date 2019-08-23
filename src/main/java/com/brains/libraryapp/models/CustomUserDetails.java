package com.brains.libraryapp.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {
	

	private static final long serialVersionUID = 1L;
	private User user;
	
	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		for(Role r: user.getRoles()) {
			auths.add(new SimpleGrantedAuthority(r.getRoleName().toUpperCase()));			
		}	
		return auths;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return user.isNonLocked();
	}

	@Override
	public boolean isAccountNonLocked() {
		return user.isNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return user.isNonLocked();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

}
