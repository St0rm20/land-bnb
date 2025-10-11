package com.labndbnb.landbnb.unit;

import com.labndbnb.landbnb.controller.AccommodationController;
import com.labndbnb.landbnb.dto.accommodation_dto.*;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.AccommodationService;
import com.labndbnb.landbnb.service.definition.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para {@link AccommodationController}.
 *
 * <p>
 * Esta clase valida el comportamiento de los endpoints del controlador de alojamientos
 * mediante pruebas unitarias estructuradas con JUnit 5 y Mockito. Se utilizan mocks
 * para simular las dependencias externas y aislar el comportamiento del controlador.
 * </p>
 *
 * <p>
 * Las pruebas están organizadas mediante clases anidadas (@Nested) que agrupan los casos
 * por endpoint, siguiendo una estructura modular y legible. Cada prueba aplica el enfoque
 * <b>Given-When-Then</b>:
 * <ul>
 *   <li><b>Given:</b> Se preparan las condiciones o datos de entrada necesarios.</li>
 *   <li><b>When:</b> Se ejecuta la acción o método bajo prueba.</li>
 *   <li><b>Then:</b> Se verifican los resultados esperados y las interacciones con los mocks.</li>
 * </ul>
 * </p>
 *
 * <p>
 * El objetivo de esta suite es asegurar que el controlador responda correctamente en
 * distintos escenarios, incluyendo casos exitosos y excepcionales.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Accommodation Controller Unit Tests")
class AccommodationControllerTest {

    // ---------------------------------------------------------------------
    // Dependencias simuladas (mocks)
    // ---------------------------------------------------------------------

    @Mock
    private AccommodationService accommodationService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    // Controlador bajo prueba
    @InjectMocks
    private AccommodationController accommodationController;

    // ---------------------------------------------------------------------
    // Objetos de datos reutilizados en las pruebas
    // ---------------------------------------------------------------------

    private AccommodationDto accommodationDto;
    private AccommodationDetailDto accommodationDetailDto;
    private AccommodationMetrics accommodationMetrics;
    private SearchCriteria searchCriteria;
    private InfoDto infoDto;

    /**
     * Inicializa los datos de prueba antes de cada método de prueba.
     */
    @BeforeEach
    void setUp() {
        setupTestData();
    }

    /**
     * Configura los objetos de prueba que se reutilizan en los diferentes casos.
     */
    private void setupTestData() {
        accommodationDto = new AccommodationDto(
                1, "Cozy Apartment", "A comfortable apartment", "Medellín", "123 Main St",
                6.2442, -75.5812, 120000.0, 4,
                Arrays.asList("WiFi", "Kitchen"), "https://example.com/image.jpg"
        );

        accommodationDetailDto = new AccommodationDetailDto(
                1, "Cozy Apartment", "A comfortable apartment", "Medellín", "123 Main St",
                6.2442, -75.5812, 120000.0, 4,
                Arrays.asList("WiFi", "Kitchen"), null,
                4.5, 10, "https://example.com/main.jpg",
                Arrays.asList("https://example.com/img1.jpg", "https://example.com/img2.jpg")
        );

        accommodationMetrics = new AccommodationMetrics(
                1, "Cozy Apartment", 50, 40, 5, 5,
                5000000.0, 125000.0, 80.5, 120, 4.5, 35
        );

        searchCriteria = new SearchCriteria(
                "Medellín", LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                100000.0, 200000.0, Arrays.asList("WiFi", "Pool"),
                "price", "asc", true, true, false, true, true, true
        );

        infoDto = new InfoDto("Success", "Operation completed successfully");
    }

    // ---------------------------------------------------------------------
    // Sección de pruebas agrupadas por endpoint
    // ---------------------------------------------------------------------

    /**
     * Pruebas para el endpoint GET /api/accommodations
     * Encargado de obtener la lista de alojamientos disponibles de forma paginada.
     */
    @Nested
    @DisplayName("GET /api/accommodations - Obtener todos los alojamientos")
    class GetAccommodationsTests {

        @Test
        @DisplayName("Debería retornar una página con alojamientos cuando existen datos")
        void givenAccommodationsExist_whenGetAccommodations_thenReturnPageWithAccommodations() throws ExceptionAlert {
            // Given
            Page<AccommodationDto> accommodationPage = new PageImpl<>(List.of(accommodationDto));
            when(accommodationService.getAccommodations(0)).thenReturn(accommodationPage);

            // When
            ResponseEntity<?> response = accommodationController.getAccommodations(0);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(accommodationService, times(1)).getAccommodations(0)
            );
        }

        @Test
        @DisplayName("Debería retornar una página vacía cuando no hay alojamientos disponibles")
        void givenNoAccommodationsExist_whenGetAccommodations_thenReturnEmptyPage() throws ExceptionAlert {
            // Given
            Page<AccommodationDto> emptyPage = new PageImpl<>(Collections.emptyList());
            when(accommodationService.getAccommodations(0)).thenReturn(emptyPage);

            // When
            ResponseEntity<?> response = accommodationController.getAccommodations(0);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(accommodationService, times(1)).getAccommodations(0);
        }
    }

    /**
     * Pruebas para el endpoint GET /api/accommodations/{id}
     * Obtiene un alojamiento específico según su identificador.
     */
    @Nested
    @DisplayName("GET /api/accommodations/{id} - Obtener alojamiento por ID")
    class GetAccommodationByIdTests {

        @Test
        @DisplayName("Debería retornar el alojamiento cuando el ID existe")
        void givenValidAccommodationId_whenGetAccommodationById_thenReturnAccommodation() throws ExceptionAlert {
            // Given
            when(accommodationService.getAccommodation(1L)).thenReturn(accommodationDetailDto);

            // When
            ResponseEntity<?> response = accommodationController.getAccommodationById(1L);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(accommodationDetailDto, response.getBody()),
                    () -> verify(accommodationService, times(1)).getAccommodation(1L)
            );
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el ID no existe")
        void givenInvalidAccommodationId_whenGetAccommodationById_thenPropagateException() throws ExceptionAlert {
            // Given
            when(accommodationService.getAccommodation(999L))
                    .thenThrow(new ExceptionAlert("Accommodation not found"));

            // When & Then
            assertThrows(ExceptionAlert.class, () ->
                    accommodationController.getAccommodationById(999L)
            );
        }
    }

    /**
     * Pruebas para el endpoint POST /api/accommodations
     * Permite crear un nuevo alojamiento.
     */
    @Nested
    @DisplayName("POST /api/accommodations - Crear alojamiento")
    class CreateAccommodationTests {

        @Test
        @DisplayName("Debería crear un alojamiento exitosamente")
        void givenValidAccommodationData_whenCreateAccommodation_thenReturnSuccess() throws ExceptionAlert {
            // Given
            when(accommodationService.createAccommodation(any(), any())).thenReturn(infoDto);

            // When
            ResponseEntity<?> response = accommodationController.createAccommodation(accommodationDetailDto, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(accommodationService, times(1)).createAccommodation(any(), any())
            );
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando ocurre un error en la creación")
        void givenInvalidData_whenCreateAccommodation_thenPropagateException() throws ExceptionAlert {
            // Given
            when(accommodationService.createAccommodation(any(), any()))
                    .thenThrow(new ExceptionAlert("Creation failed"));

            // When & Then
            assertThrows(ExceptionAlert.class, () ->
                    accommodationController.createAccommodation(accommodationDetailDto, request)
            );
        }
    }

    /**
     * Pruebas para el endpoint PUT /api/accommodations/{id}
     * Permite actualizar un alojamiento existente.
     */
    @Nested
    @DisplayName("PUT /api/accommodations/{id} - Actualizar alojamiento")
    class UpdateAccommodationTests {

        @Test
        @DisplayName("Debería actualizar un alojamiento correctamente")
        void givenValidData_whenUpdateAccommodation_thenReturnUpdatedAccommodation() throws ExceptionAlert {
            // Given
            when(accommodationService.updateAccommodation(any(), anyLong(), any()))
                    .thenReturn(accommodationDetailDto);

            // When
            ResponseEntity<?> response = accommodationController.updateAccommodation(1L, accommodationDetailDto, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(accommodationDetailDto, response.getBody())
            );
        }

        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando la actualización falla")
        void givenInvalidData_whenUpdateAccommodation_thenReturnBadRequest() throws ExceptionAlert {
            // Given
            when(accommodationService.updateAccommodation(any(), anyLong(), any()))
                    .thenThrow(new ExceptionAlert("Update failed"));

            // When
            ResponseEntity<?> response = accommodationController.updateAccommodation(1L, accommodationDetailDto, request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    /**
     * Pruebas para el endpoint DELETE /api/accommodations/{id}
     * Elimina un alojamiento existente según su ID.
     */
    @Nested
    @DisplayName("DELETE /api/accommodations/{id} - Eliminar alojamiento")
    class DeleteAccommodationTests {

        @Test
        @DisplayName("Debería eliminar un alojamiento correctamente")
        void givenValidId_whenDeleteAccommodation_thenReturnSuccess() throws ExceptionAlert {
            // Given
            doNothing().when(accommodationService).deleteAccommodation(anyLong(), any());

            // When
            ResponseEntity<?> response = accommodationController.deleteAccommodation(1L, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando ocurre un error en la eliminación")
        void givenInvalidId_whenDeleteAccommodation_thenPropagateException() throws ExceptionAlert {
            // Given
            doThrow(new ExceptionAlert("Delete failed"))
                    .when(accommodationService).deleteAccommodation(anyLong(), any());

            // When & Then
            assertThrows(ExceptionAlert.class, () ->
                    accommodationController.deleteAccommodation(1L, request)
            );
        }
    }

    /**
     * Pruebas para el endpoint POST /api/accommodations/search
     * Permite realizar búsquedas avanzadas de alojamientos según criterios.
     */
    @Nested
    @DisplayName("POST /api/accommodations/search - Buscar alojamientos")
    class SearchAccommodationsTests {

        @Test
        @DisplayName("Debería retornar resultados cuando existen coincidencias")
        void givenValidSearchCriteria_whenSearchAccommodations_thenReturnResults() throws ExceptionAlert {
            // Given
            Page<AccommodationDetailDto> page = new PageImpl<>(List.of(accommodationDetailDto));
            when(accommodationService.searchAccommodations(any(), anyInt())).thenReturn(page);

            // When
            ResponseEntity<?> response = accommodationController.searchAccommodations(searchCriteria, 0);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería retornar página vacía cuando no existen resultados")
        void givenNoResults_whenSearchAccommodations_thenReturnEmptyPage() throws ExceptionAlert {
            // Given
            Page<AccommodationDetailDto> emptyPage = new PageImpl<>(Collections.emptyList());
            when(accommodationService.searchAccommodations(any(), anyInt())).thenReturn(emptyPage);

            // When
            ResponseEntity<?> response = accommodationController.searchAccommodations(searchCriteria, 0);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    /**
     * Pruebas para el endpoint GET /api/accommodations/host/my-accommodations
     * Recupera los alojamientos publicados por el host autenticado.
     */
    @Nested
    @DisplayName("GET /api/accommodations/host/my-accommodations - Obtener mis alojamientos")
    class GetMyAccommodationsTests {

        @Test
        @DisplayName("Debería retornar los alojamientos del host autenticado")
        void givenAuthenticatedHost_whenGetMyAccommodations_thenReturnHostAccommodations() throws ExceptionAlert {
            // Given
            Page<AccommodationDetailDto> page = new PageImpl<>(List.of(accommodationDetailDto));
            when(accommodationService.getMyAccommodations(anyInt(), any())).thenReturn(page);

            // When
            ResponseEntity<?> response = accommodationController.getMyAccommodations(0, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    /**
     * Pruebas para el endpoint GET /api/accommodations/{id}/metrics
     * Obtiene métricas de rendimiento asociadas a un alojamiento.
     */
    @Nested
    @DisplayName("GET /api/accommodations/{id}/metrics - Obtener métricas del alojamiento")
    class GetAccommodationMetricsTests {

        @Test
        @DisplayName("Debería retornar las métricas correctamente")
        void givenValidParameters_whenGetAccommodationMetrics_thenReturnMetrics() throws ExceptionAlert {
            // Given
            when(accommodationService.getAccommodationMetrics(anyInt(), any(), any(), any()))
                    .thenReturn(accommodationMetrics);

            // When
            ResponseEntity<?> response = accommodationController.getAccommodationMetrics(
                    1, LocalDate.now().minusDays(30), LocalDate.now(), request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(accommodationMetrics, response.getBody())
            );
        }
    }

    /**
     * Pruebas relacionadas con las funcionalidades de gestión de favoritos.
     */
    @Nested
    @DisplayName("Gestión de favoritos")
    class FavoritesTests {

        @Test
        @DisplayName("Debería agregar un alojamiento a favoritos exitosamente")
        void givenValidAccommodationId_whenAddFavorite_thenReturnSuccess() throws ExceptionAlert {
            // Given
            when(accommodationService.addFavorite(anyLong(), any())).thenReturn(infoDto);

            // When
            ResponseEntity<?> response = accommodationController.addFavorite(1L, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería eliminar un alojamiento de favoritos correctamente")
        void givenValidAccommodationId_whenRemoveFavorite_thenReturnSuccess() throws ExceptionAlert {
            // Given
            when(accommodationService.removeFavorite(anyLong(), any())).thenReturn(infoDto);

            // When
            ResponseEntity<?> response = accommodationController.removeFavorite(1L, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería verificar correctamente si un alojamiento es favorito")
        void givenAccommodationId_whenIsFavorite_thenReturnBoolean() throws ExceptionAlert {
            // Given
            when(accommodationService.isFavorite(anyLong(), any())).thenReturn(true);

            // When
            ResponseEntity<?> response = accommodationController.isFavorite(1L, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(true, response.getBody());
        }

        @Test
        @DisplayName("Debería retornar la lista de alojamientos favoritos del usuario autenticado")
        void givenAuthenticatedUser_whenGetFavoriteAccommodations_thenReturnFavorites() throws ExceptionAlert {
            // Given
            Page<AccommodationDto> favoritesPage = new PageImpl<>(List.of(accommodationDto));
            when(accommodationService.getFavoriteAccommodations(anyInt(), any())).thenReturn(favoritesPage);

            // When
            ResponseEntity<?> response = accommodationController.getFavoriteAccommodations(0, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}
