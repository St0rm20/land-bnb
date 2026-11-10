package com.labndbnb.landbnb.unit.security;

import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.security.JWTFilter;
import com.labndbnb.landbnb.security.JWTutils;
import com.labndbnb.landbnb.service.implement.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWTFilter")
class JWTFilterTest {

    @Mock private JWTutils jwtUtil;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private FilterChain chain;

    @InjectMocks private JWTFilter filter;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest request(String uri, String bearer) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRequestURI(uri);
        if (bearer != null) req.addHeader("Authorization", "Bearer " + bearer);
        return req;
    }

    @Test
    @DisplayName("endpoints públicos de auth no validan el token (aunque venga uno inválido)")
    void publicAuthPathsSkipJwtValidation() throws Exception {
        for (String uri : new String[]{"/api/auth/login", "/api/auth/register",
                "/api/auth/forgot-password", "/api/auth/reset-password"}) {
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(request(uri, "expired-or-garbage"), res, chain);
            assertThat(res.getStatus()).isEqualTo(200);
        }
        verify(chain, times(4)).doFilter(any(), any());
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    @DisplayName("otras rutas, incluso bajo /api/auth/, sí validan el token")
    void otherAuthPathsAreFiltered() throws Exception {
        when(jwtUtil.parseJwt("bad")).thenThrow(new MalformedJwtException("bad"));
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(request("/api/auth/otra-ruta", "bad"), res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("sin token deja pasar sin autenticar")
    void noTokenPassesThrough() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(request("/api/users/profile", null), res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("token válido autentica con el usuario activo y su rol")
    @SuppressWarnings("unchecked")
    void validTokenAuthenticates() throws Exception {
        User user = User.builder().id(1L).email("a@b.com").role(UserRole.HOST).build();
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseJwt("good")).thenReturn(jws);
        when(jws.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("a@b.com");
        when(userDetailsService.loadUserByUsername("a@b.com")).thenReturn(user);

        filter.doFilter(request("/api/users/profile", "good"), new MockHttpServletResponse(), chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isSameAs(user);
        assertThat(auth.getAuthorities()).extracting(a -> a.getAuthority()).containsExactly("ROLE_HOST");
        verify(chain).doFilter(any(), any());
    }

    @Test
    @DisplayName("token malformado/expirado responde 401 y corta la cadena")
    void invalidTokenReturns401() throws Exception {
        when(jwtUtil.parseJwt("bad")).thenThrow(new MalformedJwtException("bad"));
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(request("/api/users/profile", "bad"), res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("token válido de un usuario inactivo/borrado responde 401")
    @SuppressWarnings("unchecked")
    void inactiveUserReturns401() throws Exception {
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseJwt("good")).thenReturn(jws);
        when(jws.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("gone@b.com");
        when(userDetailsService.loadUserByUsername("gone@b.com"))
                .thenThrow(new UsernameNotFoundException("inactivo"));
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(request("/api/users/profile", "good"), res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, never()).doFilter(any(), any());
    }

    // ---- invalidación de tokens (cambio/reset de contraseña, borrado de cuenta) ----

    @SuppressWarnings("unchecked")
    private void stubToken(User user, java.util.Date issuedAt) {
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseJwt("tok")).thenReturn(jws);
        when(jws.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(user.getEmail());
        when(claims.getIssuedAt()).thenReturn(issuedAt);
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
    }

    @Test
    @DisplayName("un token emitido ANTES de cambiar la contraseña se rechaza con 401")
    void tokenIssuedBeforeInvalidationIsRejected() throws Exception {
        User user = User.builder().id(1L).email("a@b.com").role(UserRole.USER)
                .tokensValidAfter(java.time.LocalDateTime.now()).build();
        stubToken(user, java.util.Date.from(java.time.Instant.now().minusSeconds(3600)));
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(request("/api/users/profile", "tok"), res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("un token emitido DESPUÉS del cambio de contraseña sigue siendo válido")
    void tokenIssuedAfterInvalidationIsAccepted() throws Exception {
        User user = User.builder().id(1L).email("a@b.com").role(UserRole.USER)
                .tokensValidAfter(java.time.LocalDateTime.now().minusMinutes(5)).build();
        stubToken(user, java.util.Date.from(java.time.Instant.now()));

        filter.doFilter(request("/api/users/profile", "tok"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(chain).doFilter(any(), any());
    }

    @Test
    @DisplayName("sin invalidación previa (tokensValidAfter nulo) los tokens siguen funcionando")
    void noInvalidationMeansTokenAccepted() throws Exception {
        User user = User.builder().id(1L).email("a@b.com").role(UserRole.USER).build();
        stubToken(user, java.util.Date.from(java.time.Instant.now().minusSeconds(100)));

        filter.doFilter(request("/api/users/profile", "tok"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
