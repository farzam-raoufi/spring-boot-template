package com.github.farzam_raoufi.template.security;

import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("کاربر موجود: همان کاربر به‌عنوان UserDetails برگردانده می‌شود")
    void existingUser_isReturned() {
        User ali = User.builder().id(1L).username("ali").password("hashed").role(Role.USER).build();
        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(ali));

        UserDetails result = service.loadUserByUsername("ali");

        assertThat(result).isSameAs(ali);
    }

    @Test
    @DisplayName("کاربر ناموجود: UsernameNotFoundException با نام کاربری در پیام")
    void unknownUser_throwsUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}