package com.labndbnb.landbnb.unit.exceptions;

import com.labndbnb.landbnb.exceptions.EmailAlreadyInUse;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.exceptions.RestExceptionHandler;
import com.labndbnb.landbnb.exceptions.TokenIncorrect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestExceptionHandler")
class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    @DisplayName("ExceptionAlert -> 400 con su mensaje")
    void alert() {
        var res = handler.handleAlert(new ExceptionAlert("algo salió mal"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody().error()).isTrue();
        assertThat(res.getBody().content()).isEqualTo("algo salió mal");
    }

    @Test
    @DisplayName("AccessDeniedException -> 403 (antes caía en 500)")
    void accessDenied() {
        assertThat(handler.handleAccessDenied(new AccessDeniedException("x")).getStatusCode().value())
                .isEqualTo(403);
    }

    @Test
    @DisplayName("AuthenticationException -> 401")
    void authentication() {
        assertThat(handler.handleAuthentication(new BadCredentialsException("x")).getStatusCode().value())
                .isEqualTo(401);
    }

    @Test
    @DisplayName("EmailAlreadyInUse -> 409")
    void conflict() {
        assertThat(handler.handleEmailInUse(new EmailAlreadyInUse("dup")).getStatusCode().value())
                .isEqualTo(409);
    }

    @Test
    @DisplayName("TokenIncorrect -> 400")
    void tokenIncorrect() {
        assertThat(handler.handleToken(new TokenIncorrect()).getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("IllegalArgumentException -> 400 sin filtrar detalles internos")
    void illegalArgument() {
        var res = handler.handleBadInput(new IllegalArgumentException("detalle interno"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody().content()).isEqualTo("Solicitud inválida");
    }

    @Test
    @DisplayName("error inesperado -> 500 con mensaje genérico")
    void unexpected() {
        var res = handler.generalExceptionHandler(new RuntimeException("stacktrace secreto"));
        assertThat(res.getStatusCode().value()).isEqualTo(500);
        assertThat(res.getBody().content()).isEqualTo("Error interno del servidor");
    }
}
