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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserService UserService;
    private final BookingMapper bookingMapper;
    static final Logger logger = Logger.getLogger(BookingServiceImpl.class.getName());
    private final MailServiceImpl mailServiceImpl;

    @Override
    public BookingDto createBooking(BookingRequest bookingRequest, HttpServletRequest request) throws ExceptionAlert {
        User user = UserService.getUserFromRequest(request);
        logger.info("The user id is: " + user.getId());
        Accommodation accommodation = accommodationRepository.findById(bookingRequest.accommodationId().longValue())
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

        if (bookingRequest.checkIn().isAfter(bookingRequest.checkOut())) {
            throw new ExceptionAlert("Check-in date must be before check-out date");
        }

        if (bookingRequest.checkIn().isBefore(LocalDate.now())){
            throw new ExceptionAlert("Cannot book dates in the past");
        }

        LocalDateTime checkInDateTime = bookingRequest.checkIn().atStartOfDay();
        LocalDateTime checkOutDateTime = bookingRequest.checkOut().atStartOfDay();

        boolean hasOverlap= bookingRepository.existsOverlappingBooking(accommodation.getId(), checkInDateTime, checkOutDateTime);

        if (hasOverlap) {
            throw  new ExceptionAlert("Accommodation is not available for the selected dates");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(accommodation.getId(), checkInDateTime, checkOutDateTime);
        if (!overlappingBookings.isEmpty()) {
            StringBuilder message = new StringBuilder("Accommodation is not avaliable for the selected dates. Overlapping bookings:\n");
            for (Booking b : overlappingBookings) {
                message.append("\n- From ")
                        .append(b.getStartDate().toLocalDate())
                        .append(" to ")
                        .append(b.getEndDate().toLocalDate())
                        .append(" (Status: ")
                        .append(b.getBookingStatus())
                        .append(")");
            }
            throw  new ExceptionAlert (message.toString());

        }


        Double totalPrice = accommodation.getPricePerNight() * (bookingRequest.checkOut().toEpochDay() - bookingRequest.checkIn().toEpochDay());

        Booking booking = Booking.builder()
                .guest(user)
                .accommodation(accommodation)
                .startDate(bookingRequest.checkIn().atStartOfDay())
                .endDate(bookingRequest.checkOut().atStartOfDay())
                .totalPrice(totalPrice)
                .numberOfGuests(bookingRequest.numberOfGuests())
                .bookingCode(UUID.randomUUID().toString().substring(0,12).toUpperCase())
                .createdAt(LocalDateTime.now())
                .bookingStatus(BookingStatus.PENDING)
                .build();

        logger.info("User id: " + booking.getGuest().getId()+ "user: " + user.getId());
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public Page<BookingDto> getBookingsByUser(String estado, int page, int size, HttpServletRequest request) throws ExceptionAlert {
        User user = UserService.getUserFromRequest(request);
        if(user==null){
            throw  new ExceptionAlert("User not found");
        }
        logger.info("The user id is: " + user.getId());
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Booking> bookings;
        logger.info("se pageo");
        if (estado == null || estado.isBlank()) {
            logger.info("estado is blank");
            bookings = bookingRepository.findByGuestId(Long.valueOf(user.getId()), pageable);
        }else{
            logger.info("estado is: " + estado);
            BookingStatus status = BookingStatus.valueOf(estado.toUpperCase());
            logger.info("status is 2 : " + status);
            bookings = bookingRepository.findByGuestIdAndBookingStatus(Long.valueOf(user.getId()), status, pageable);
        }
        logger.info("bookings size: " + bookings.getSize());
        return bookings.map(bookingMapper::toDto);
    }

    @Override
    public Page<BookingDto> getBookingsByHost(Integer accommodationId, String status, int page, int size, HttpServletRequest request) throws ExceptionAlert {
        User host = UserService.getUserFromRequest(request);

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
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                bookings = bookingRepository.findByAccommodationIdAndAccommodationHostIdAndBookingStatus(accommodationId.longValue(), Long.valueOf(host.getId()), bookingStatus, pageable
                );
            }
        } else
        if (status == null || status.isBlank()){
            bookings = bookingRepository.findByAccommodationHostId(Long.valueOf(host.getId()), pageable);
        } else {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            bookings = bookingRepository.findByAccommodationHostIdAndBookingStatus(Long.valueOf(host.getId()), bookingStatus, pageable);
        }
        return bookings.map(bookingMapper::toDto);
    }


    @Override
    public void cancelBooking(Long id, HttpServletRequest request) throws ExceptionAlert {
        User user = UserService.getUserFromRequest(request);

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

        // CORREGIDO: Orden correcto de parámetros
        mailServiceImpl.sendSimpleEmail(
                booking.getGuest().getEmail(),  // to (PRIMERO)
                "Booking Cancelled",            // subject (SEGUNDO)
                "Your booking with code " + booking.getBookingCode() + " has been cancelled.\n" +
                        "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                        "Dates: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate()  // text (TERCERO)
        );

        mailServiceImpl.sendSimpleEmail(
                booking.getAccommodation().getHost().getEmail(),  // to (PRIMERO)
                "Booking Cancelled",                              // subject (SEGUNDO)
                "The booking with code " + booking.getBookingCode() + " has been cancelled.\n" +
                        "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                        "Dates: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate() + "\n" +
                        "Guest: " + booking.getGuest().getName() + " " + booking.getGuest().getLastName()  // text (TERCERO)
        );

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long id) throws ExceptionAlert {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ExceptionAlert("Booking not found"));
    }

    @Override
    public InfoDto completeBooking(Long id, HttpServletRequest request) {
        try {
            User user = UserService.getUserFromRequest(request);

            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new ExceptionAlert("Booking not found"));

            if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
                return new InfoDto("Already completed", "Booking has already been completed");
            }

            if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                return new InfoDto("Cannot complete", "Cancelled bookings cannot be completed");
            }

            if (!booking.getGuest().getId().equals(user.getId())) {
                return new InfoDto("Unauthorized", "User is not the owner of the booking");
            }


            mailServiceImpl.sendSimpleEmail(
                    booking.getGuest().getEmail(),
                    "Booking Completed",
                    "Your booking with code " + booking.getBookingCode() + " has been marked as completed.\n" +
                            "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                            "In the days: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate() + "\n" +
                            "We hope you had a great stay!"
            );

            mailServiceImpl.sendSimpleEmail(booking.getAccommodation().getHost().getEmail(),
                    "Booking Completed",
                    "The booking with code " + booking.getBookingCode() + " has been marked as completed.\n" +
                            "The accommodation is: " + booking.getAccommodation().getName() + "\n" +
                            "In the days: " + booking.getStartDate().toLocalDate() + " to " + booking.getEndDate().toLocalDate() + "\n" +
                            "Guest: " + booking.getGuest().getName() + " " + booking.getGuest().getLastName()
            );



            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            return new InfoDto("Booking completed", "The booking has been marked as completed successfully");

        } catch (ExceptionAlert e) {
            logger.severe("Error completing booking: " + e.getMessage());
            return new InfoDto("Error", e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error completing booking: " + e.getMessage());
            return new InfoDto("Error", "An unexpected error occurred");
        }
    }

    @Override
    public void cancelBookingByHost(Long id, HttpServletRequest request) {
        try {
            User host = UserService.getUserFromRequest(request);

            if (host.getRole() != UserRole.HOST) {
                throw new Exception("User is not a host");
            }

            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new Exception("Booking not found"));

            if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                throw new Exception("Booking has already been cancelled");
            }

            if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
                throw new Exception("Completed bookings cannot be cancelled");
            }

            if (!booking.getAccommodation().getHost().getId().equals(host.getId())) {
                throw new Exception("User is not the host of the accommodation for this booking");
            }


            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());
            bookingRepository.save(booking);
        } catch (Exception e) {
            logger.severe("Error cancelling booking by host: " + e.getMessage());
        }
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

        return bookingRepository.existsByAccommodationIdAndEndDateAfterAndBookingStatusNot(
                accommodationId,
                now,
                BookingStatus.CONFIRMED
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

}
