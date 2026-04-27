package com.comercial.crm.auth;

import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new UsernameNotFoundException("Usuario inactivo");
    }

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPasswordHash(),
        user.getRoles().stream()
            .map(r -> new SimpleGrantedAuthority(r.getCode()))
            .collect(Collectors.toSet()));
  }
}