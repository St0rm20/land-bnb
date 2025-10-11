package com.labndbnb.landbnb.service.implement;


import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
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


@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserService UserService;
    private final BookingMapper bookingMapper;
    static final Logger logger = Logger.getLogger(BookingServiceImpl.class.getName());

    @Override
    public BookingDto createBooking(BookingRequest bookingRequest, HttpServletRequest request) throws Exception {
        User user = UserService.getUserFromRequest(request);
        logger.info("The user id is: " + user.getId());
        Accommodation accommodation = accommodationRepository.findById(bookingRequest.accommodationId().longValue())
                .orElseThrow(() -> new Exception("Accommodation not found"));

        if (bookingRequest.checkIn().isAfter(bookingRequest.checkOut())) {
            throw new Exception("Check-in date must be before check-out date");
        }

        if (bookingRequest.checkIn().isBefore(LocalDate.now())){
            throw new Exception("Cannot book dates in the past");
        }

        LocalDateTime checkInDateTime = bookingRequest.checkIn().atStartOfDay();
        LocalDateTime checkOutDateTime = bookingRequest.checkOut().atStartOfDay();

        boolean hasOverlap= bookingRepository.existsOverlappingBooking(accommodation.getId(), checkInDateTime, checkOutDateTime);

        if (hasOverlap) {
            throw  new Exception("Accommodation is not available for the selected dates");
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
            throw  new Exception (message.toString());

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
    public Page<BookingDto> getBookingsByUser(String estado, int page, int size, HttpServletRequest request) throws Exception {
        User user = UserService.getUserFromRequest(request);
        if(user==null){
            throw  new Exception("User not found");
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
    public Page<BookingDto> getBookingsByHost(Integer accommodationId, String status, int page, int size, HttpServletRequest request) throws Exception {
        User host = UserService.getUserFromRequest(request);

        if (host.getRole() != UserRole.HOST) {
            throw new Exception("User is not a host");
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
    public void cancelBooking(Long id, HttpServletRequest request) throws Exception {
        User user = UserService.getUserFromRequest(request);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("Booking not found"));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED){
            throw new Exception("Booking has already been cancelled");
        }

        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new Exception("Completed bookings cannot be cancelled");
        }

        if (!booking.getGuest().getId().equals(user.getId())) {
            throw new Exception("User is not the owner of the booking");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());


        bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long id) throws Exception {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("Booking not found"));
    }

    @Override
    public void completeBooking(Long id, HttpServletRequest request) {
        try {
            User user = UserService.getUserFromRequest(request);

            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new Exception("Booking not found"));

            if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
                throw new Exception("Booking has already been completed");
            }

            if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                throw new Exception("Cancelled bookings cannot be completed");
            }

            if (!booking.getGuest().getId().equals(user.getId())) {
                throw new Exception("User is not the owner of the booking");
            }
            if (LocalDateTime.now().isBefore(booking.getEndDate())) {
                throw new Exception("Booking cannot be completed before the end date");
            }

            booking.setBookingStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        } catch (Exception e) {
            logger.severe("Error completing booking: " + e.getMessage());
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


}
