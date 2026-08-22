package com.fillforme.backend.common.security;

import com.fillforme.backend.auth.entity.User;
import com.fillforme.backend.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user.getId(), user.getEmail(), user.getPassword(), user.getRole());
    }

    @Transactional
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseGet(() -> userRepository.findByEmail("guest@fillforme.com")
                        .orElseGet(() -> userRepository.save(User.builder()
                                .email("guest@fillforme.com")
                                .fullName("Guest User")
                                .password("$2a$10$UnusedPasswordHashForGuestUser")
                                .role("ROLE_USER")
                                .build())));

        return UserPrincipal.create(user.getId(), user.getEmail(), user.getPassword(), user.getRole());
    }
}
