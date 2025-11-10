package com.hwn.sw_project.security;

import com.hwn.sw_project.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AppUserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = userRepo.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Not found"+email));

        var authorities = List.of(new SimpleGrantedAuthority("Role_USER"));

        return new User(u.getEmail(),u.getPassword(),true,true,true,true,authorities);
    }
}
