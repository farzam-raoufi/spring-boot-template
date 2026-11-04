package com.github.farzam_raoufi.template.security.jwt;

import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User ali;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        ali = User.builder().id(1L).username("ali").role(Role.USER).enabled(true).build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void withBearerToken(String token) {
        request.addHeader("Authorization", "Bearer " + token);
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // ---------------------------------------------------------------
    // درخواست‌هایی که اصلاً توکن Bearer ندارند
    // ---------------------------------------------------------------

    @Test
    @DisplayName("بدون هدر Authorization: درخواست ادامه پیدا می‌کند و به JWT و دیتابیس دست نمی‌زند")
    void noAuthorizationHeader_continuesUnauthenticated() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    @DisplayName("هدر غیر Bearer (مثلاً Basic): نادیده گرفته می‌شود")
    void nonBearerHeader_isIgnored() throws Exception {
        request.addHeader("Authorization", "Basic YWxpOnBhc3M=");

        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    // ---------------------------------------------------------------
    // توکن Bearer معتبر
    // ---------------------------------------------------------------

    @Test
    @DisplayName("توکن معتبر و کاربر فعال: کاربر با نقش‌هایش authenticate می‌شود")
    void validToken_setsAuthentication() throws Exception {
        withBearerToken("good-token");
        when(jwtService.extractUserId("good-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(ali));
        when(jwtService.isTokenValid("good-token", ali)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        Authentication auth = currentAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getPrincipal()).isSameAs(ali);
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("از قبل authenticate شده: احراز هویت موجود عوض نمی‌شود و دیتابیس صدا زده نمی‌شود")
    void alreadyAuthenticated_isNotOverridden() throws Exception {
        Authentication existing = UsernamePasswordAuthenticationToken.authenticated("someone", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);
        withBearerToken("good-token");

        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isSameAs(existing);
        verifyNoInteractions(userRepository);
        verify(filterChain).doFilter(request, response);
    }

    // ---------------------------------------------------------------
    // توکن Bearer که نباید authenticate شود (ولی درخواست هم قطع نمی‌شود)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("توکن خراب یا منقضی (JwtException): بدون احراز هویت ادامه می‌دهد و خطا بیرون نمی‌دهد")
    void invalidToken_continuesUnauthenticated() throws Exception {
        withBearerToken("bad-token");
        when(jwtService.extractUserId("bad-token")).thenThrow(new MalformedJwtException("bad"));

        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("هر خطای JWT دیگر هم بی‌صدا نادیده گرفته می‌شود")
    void anyJwtException_continuesUnauthenticated() throws Exception {
        withBearerToken("weird-token");
        when(jwtService.extractUserId("weird-token")).thenThrow(new JwtException("something"));

        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("توکن معتبر ولی کاربر دیگر وجود ندارد (مثلاً حذف شده): authenticate نمی‌شود")
    void userNotFound_continuesUnauthenticated() throws Exception {
        withBearerToken("good-token");
        when(jwtService.extractUserId("good-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isNull();
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("کاربر غیرفعال (disabled): با وجود توکن معتبر authenticate نمی‌شود")
    void disabledUser_continuesUnauthenticated() throws Exception {
        ali.setEnabled(false);
        withBearerToken("good-token");
        when(jwtService.extractUserId("good-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(ali));

        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("JwtService توکن را برای این کاربر معتبر نمی‌داند: authenticate نمی‌شود")
    void tokenNotValidForUser_continuesUnauthenticated() throws Exception {
        withBearerToken("good-token");
        when(jwtService.extractUserId("good-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(ali));
        when(jwtService.isTokenValid("good-token", ali)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(currentAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}