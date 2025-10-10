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
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserService UserService;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto createBooking(BookingRequest bookingRequest, HttpServletRequest request) throws Exception {
        User user = UserService.getUserFromRequest(request);
        Accommodation accommodation = accommodationRepository.findById(bookingRequest.accommodationId().longValue())
                .orElseThrow(() -> new Exception("Accommodation not found"));

        if (bookingRequest.checkIn().isAfter(bookingRequest.checkOut()))
            throw new Exception("Check-in date must be before check-out date");

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

        Booking savedBooking = bookingRepository.save(booking);

        Booking reloadedBooking = bookingRepository.findByIdWithDetails(savedBooking.getId())
                .orElseThrow(() -> new Exception("Booking not found after save"));

        return bookingMapper.toDto(reloadedBooking);
    }

    @Override
    public Page<BookingDto> getBookingsByUser(String estado, int page, int size, HttpServletRequest request) throws Exception {
        User user = UserService.getUserFromRequest(request);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Booking> bookings;
        if (estado == null || estado.isBlank()) {
            bookings = bookingRepository.findByGuestId(Long.valueOf(user.getId()), pageable);
        }else{
            BookingStatus status = BookingStatus.valueOf(estado.toUpperCase());
            bookings = bookingRepository.findByGuestIdAndBookingStatus(Long.valueOf(user.getId()), status, pageable);
        }
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

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());


        bookingRepository.save(booking);
    }


}
