package com.hwn.sw_project.security;

import com.hwn.sw_project.entity.AppUser;
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
        AppUser u = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not found " + email));

        // ✅ AppUser.role -> ROLE_XXX 로 변환
        String roleName = "ROLE_" + u.getRole().name(); // ex) ROLE_USER, ROLE_ADMIN

        var authorities = List.of(new SimpleGrantedAuthority(roleName));

        return new User(
                u.getEmail(),
                u.getPassword(),
                true,  // enabled
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                authorities
        );
    }
}
