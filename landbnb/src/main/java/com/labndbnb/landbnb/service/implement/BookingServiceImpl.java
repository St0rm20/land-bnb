package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.booking_dto.BookingDatesDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.Booking.BookingMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserService userService;
    private final BookingMapper bookingMapper;
    private final MailServiceImpl mailServiceImpl;


    private static BookingStatus parseStatus(String value) throws ExceptionAlert {
        try {
            return BookingStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ExceptionAlert("Invalid booking status: " + value);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Override
    public BookingDto createBooking(BookingRequest bookingRequest, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        // Bloqueo de fila: evita que dos reservas simultáneas pasen la validación de solape
        Accommodation accommodation = accommodationRepository.findByIdForUpdate(bookingRequest.accommodationId().longValue())
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

        if (!Boolean.TRUE.equals(accommodation.getActive())) {
            throw new ExceptionAlert("Accommodation is not available");
        }

        if (!bookingRequest.checkIn().isBefore(bookingRequest.checkOut())) {
            throw new ExceptionAlert("Check-in date must be before check-out date");
        }

        if (bookingRequest.checkIn().isBefore(LocalDate.now())) {
            throw new ExceptionAlert("Cannot book dates in the past");
        }

        if (user.getId().equals(accommodation.getHost().getId())) {
            throw new ExceptionAlert("Hosts cannot book their own accommodations");
        }

        if (bookingRequest.numberOfGuests() > accommodation.getCapacity()) {
            throw new ExceptionAlert("Number of guests exceeds the accommodation capacity (" + accommodation.getCapacity() + ")");
        }

        LocalDateTime checkInDateTime = bookingRequest.checkIn().atStartOfDay();
        LocalDateTime checkOutDateTime = bookingRequest.checkOut().atStartOfDay();

        if (!bookingRepository.findOverlappingBookings(accommodation.getId(), checkInDateTime, checkOutDateTime).isEmpty()) {
            // No se exponen datos de reservas de otros usuarios
            throw new ExceptionAlert("Accommodation is not available for the selected dates");
        }

        long nights = ChronoUnit.DAYS.between(bookingRequest.checkIn(), bookingRequest.checkOut());
        BigDecimal totalPrice = accommodation.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
                .guest(user)
                .accommodation(accommodation)
                .startDate(checkInDateTime)
                .endDate(checkOutDateTime)
                .totalPrice(totalPrice)
                .numberOfGuests(bookingRequest.numberOfGuests())
                .bookingCode(UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .bookingStatus(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public Page<BookingDto> getBookingsByUser(String estado, int page, int size, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        if(user==null){
            throw  new ExceptionAlert("User not found");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Booking> bookings;

        if (estado == null || estado.isBlank()) {
            bookings = bookingRepository.findByGuestId(user.getId(), pageable);
        }else{
            BookingStatus status = parseStatus(estado);
            bookings = bookingRepository.findByGuestIdAndBookingStatus(user.getId(), status, pageable);
        }
        return bookings.map(bookingMapper::toDto);
    }

    @Override
    public Page<BookingDto> getBookingsByHost(Integer accommodationId, String status, int page, int size, Long userId) throws ExceptionAlert {
        User host = userService.getActiveUser(userId);

        if (host.getRole() != UserRole.HOST) {
            throw new ExceptionAlert("User is not a host");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Booking> bookings;

        if (accommodationId != null) {
            if (status == null || status.isBlank()) {
                bookings = bookingRepository.findByAccommodationIdAndAccommodationHostId(
                        accommodationId.longValue(),
                        Long.valueOf(host.getId()),
                        pageable
                );
            } else {
                BookingStatus bookingStatus = parseStatus(status);
                bookings = bookingRepository.findByAccommodationIdAndAccommodationHostIdAndBookingStatus(accommodationId.longValue(), Long.valueOf(host.getId()), bookingStatus, pageable
                );
            }
        } else
        if (status == null || status.isBlank()){
            bookings = bookingRepository.findByAccommodationHostId(Long.valueOf(host.getId()), pageable);
        } else {
            BookingStatus bookingStatus = parseStatus(status);
            bookings = bookingRepository.findByAccommodationHostIdAndBookingStatus(Long.valueOf(host.getId()), bookingStatus, pageable);
        }
        return bookings.map(bookingMapper::toDto);
    }


    @Override
    public void cancelBooking(Long id, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ExceptionAlert("Booking not found"));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED){
            throw new ExceptionAlert("Booking has already been cancelled");
        }

        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new ExceptionAlert("Completed bookings cannot be cancelled");
        }

        if (!booking.getGuest().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not the owner of the booking");
        }

        if(LocalDateTime.now().isAfter(booking.getStartDate().minusHours(48))){
            throw new ExceptionAlert("Bookings can only be cancelled up to 48 hours before the start date");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        mailServiceImpl.sendSimpleEmail(
                booking.getGuest().getEmail(),
                "Booking Cancelled",
                "Your booking with code " + booking.getBookingCode() + " has been cancelled.\n" +
                        "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                        "Dates: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate()
        );

        mailServiceImpl.sendSimpleEmail(
                booking.getAccommodation().getHost().getEmail(),
                "Booking Cancelled",
                "The booking with code " + booking.getBookingCode() + " has been cancelled.\n" +
                        "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                        "Dates: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate() + "\n" +
                        "Guest: " + booking.getGuest().getName() + " " + booking.getGuest().getLastName()
        );
    }

    @Override
    public Booking getBookingById(Long id) throws ExceptionAlert {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ExceptionAlert("Booking not found"));
    }

    @Override
    public BookingDto getBookingById(Long id, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ExceptionAlert("Booking not found"));

        if (!booking.getGuest().getId().equals(user.getId()) &&
                !booking.getAccommodation().getHost().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not authorized to view this booking");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public InfoDto confirmBooking(Long id, Long userId) throws ExceptionAlert {
        User host = userService.getActiveUser(userId);

        if (host.getRole() != UserRole.HOST) {
            throw new ExceptionAlert("User is not a host");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ExceptionAlert("Booking not found"));

        // Solo el anfitrión del alojamiento puede confirmar la reserva
        if (!booking.getAccommodation().getHost().getId().equals(host.getId())) {
            throw new ExceptionAlert("User is not the host of the accommodation for this booking");
        }

        switch (booking.getBookingStatus()) {
            case CONFIRMED, COMPLETED -> {
                return new InfoDto("Already confirmed", "Booking has already been confirmed");
            }
            case CANCELLED -> throw new ExceptionAlert("Cancelled bookings cannot be confirmed");
            default -> { }
        }

        // Reserva ya validada contra solapes al crearla; se re-verifica contra otras CONFIRMADAS
        boolean conflict = bookingRepository.findOverlappingBookings(
                        booking.getAccommodation().getId(), booking.getStartDate(), booking.getEndDate())
                .stream()
                .anyMatch(b -> !b.getId().equals(booking.getId()) && b.getBookingStatus() == BookingStatus.CONFIRMED);
        if (conflict) {
            throw new ExceptionAlert("Accommodation is not available for the selected dates");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        mailServiceImpl.sendSimpleEmail(
                booking.getGuest().getEmail(),
                "Booking Confirmed",
                "Your booking with code " + booking.getBookingCode() + " has been confirmed.\n" +
                        "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                        "In the days: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate()
        );
        mailServiceImpl.sendSimpleEmail(
                booking.getAccommodation().getHost().getEmail(),
                "Booking Confirmed",
                "The booking with code " + booking.getBookingCode() + " has been confirmed.\n" +
                        "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                        "In the days: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate() + "\n" +
                        "Guest: " + booking.getGuest().getName() + " " + booking.getGuest().getLastName()
        );

        return new InfoDto("Booking confirmed", "The booking has been confirmed successfully");
    }

    @Override
    public void cancelBookingByHost(Long id, Long userId) throws ExceptionAlert {
        User host = userService.getActiveUser(userId);

        if (host.getRole() != UserRole.HOST) {
            throw new ExceptionAlert("User is not a host");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ExceptionAlert("Booking not found"));

        if (!booking.getAccommodation().getHost().getId().equals(host.getId())) {
            throw new ExceptionAlert("User is not the host of the accommodation for this booking");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ExceptionAlert("Booking has already been cancelled");
        }
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new ExceptionAlert("Completed bookings cannot be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

@Override
    public boolean accommodationHasFutureBookings(Long accommodationId) {
        LocalDateTime now = LocalDateTime.now();

        if(!accommodationRepository.existsById(accommodationId)){
            return false;
        }
        if(!bookingRepository.existsByAccommodation_Id(accommodationId)){
            return false;
        }

        // Solo bloquean el borrado las reservas activas (pendientes o confirmadas) que aún no terminaron
        return bookingRepository.existsByAccommodationIdAndEndDateAfterAndBookingStatusIn(
                accommodationId,
                now,
                java.util.Set.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );
    }

    @Override
    public List<BookingDatesDto> getFutureConfirmedBookingDates(Long accommodationId) {

        LocalDateTime currentDate = LocalDateTime.now();

        List<Booking> futureBookings = bookingRepository
                .findFutureConfirmedBookingsByAccommodation(accommodationId, currentDate);

        return futureBookings.stream()
                .map(booking -> new BookingDatesDto(
                        booking.getStartDate().toLocalDate(),
                        booking.getEndDate().toLocalDate()
                ))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void autoCompletePastBookings() {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> pastBookings = bookingRepository.findByEndDateBeforeAndBookingStatus(
                now, BookingStatus.CONFIRMED);

        if (pastBookings.isEmpty()) {
            log.info("No bookings to auto-complete");
            return;
        }

        int completedCount = 0;

        for (Booking booking : pastBookings) {
            try {
                booking.setBookingStatus(BookingStatus.COMPLETED);
                booking.setCompletedAt(now);
                bookingRepository.save(booking);

                // Opcional: Enviar emails de notificación
                sendAutoCompletionEmails(booking);

                completedCount++;
                log.info("Auto-completed booking: {}", booking.getBookingCode());

            } catch (Exception e) {
                log.error("Error auto-completing booking {}: {}",
                        booking.getBookingCode(), e.getMessage());
            }
        }

        log.info("Auto-completed {} bookings out of {} eligible", completedCount, pastBookings.size());
    }

    private void sendAutoCompletionEmails(Booking booking) {
        try {
            // Email al huésped
            mailServiceImpl.sendSimpleEmail(
                    booking.getGuest().getEmail(),
                    "Stay Completed - " + booking.getBookingCode(),
                    "Hello " + booking.getGuest().getName() + ",\n\n" +
                            "Your stay at \"" + booking.getAccommodation().getName() + "\" has been automatically completed.\n\n" +
                            "Stay details:\n" +
                            "• Check-in: " + booking.getStartDate().toLocalDate() + "\n" +
                            "• Check-out: " + booking.getEndDate().toLocalDate() + "\n" +
                            "• Total: $" + booking.getTotalPrice() + "\n" +
                            "• Booking code: " + booking.getBookingCode() + "\n\n" +
                            "We hope you had a wonderful experience! Please consider leaving a review to help other travelers.\n\n" +
                            "Best regards,\nThe Landbnb Team"
            );

            // Email al host
            mailServiceImpl.sendSimpleEmail(
                    booking.getAccommodation().getHost().getEmail(),
                    "Booking Completed - " + booking.getBookingCode(),
                    "Hello " + booking.getAccommodation().getHost().getName() + ",\n\n" +
                            "The booking for your accommodation \"" + booking.getAccommodation().getName() + "\" has been automatically marked as completed.\n\n" +
                            "Booking details:\n" +
                            "• Guest: " + booking.getGuest().getName() + " " + booking.getGuest().getLastName() + "\n" +
                            "• Dates: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate() + "\n" +
                            "• Total earnings: $" + booking.getTotalPrice() + "\n" +
                            "• Booking code: " + booking.getBookingCode() + "\n\n" +
                            "We encourage you to request a review from your guest to build your reputation.\n\n" +
                            "Best regards,\nThe Landbnb Team"
            );

        } catch (Exception e) {
            log.warn("Failed to send completion emails for booking {}: {}",
                    booking.getBookingCode(), e.getMessage());
        }
    }

}
