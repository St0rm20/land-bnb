package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.review.ReviewMapper;
import com.labndbnb.landbnb.mappers.review.ReviewRequestMapper;
import com.labndbnb.landbnb.model.*;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.repository.ReviewAnswerRepository;
import com.labndbnb.landbnb.repository.ReviewRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.CommentService;
import com.labndbnb.landbnb.service.definition.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor

@Transactional
public class CommentServiceImpl implements CommentService {


    private final BookingService bookingService;
    private final UserService userService;
    private final ReviewRequestMapper reviewRequestMapper;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;
    private final AccommodationRepository accommodationRepository;
    private final BookingRepository bookingRepository;

    private final int SIZE = 10;


    @Override
    @Transactional
    public InfoDto createComment(ReviewRequest reviewRequest, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        Booking booking = bookingService.getBookingById(Long.valueOf(reviewRequest.bookingId()));

        if (!booking.getGuest().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not the owner of the booking");
        }

        BookingStatus status = booking.getBookingStatus();
        if (!status.equals(BookingStatus.COMPLETED) && !status.equals(BookingStatus.CONFIRMED)) {
            throw new ExceptionAlert("Booking is not completed or confirmed");
        }
        if (isBookingReviewed(booking.getId(), user)) {
            throw new ExceptionAlert("Booking already reviewed");
        }

        Review review = reviewRequestMapper.toEntity(reviewRequest);
        review.setBooking(booking);
        review.setUser(user);
        review.setAccommodation(booking.getAccommodation());
        reviewRepository.save(review);

        refreshRating(booking.getAccommodation());
        return new InfoDto("Review created", "Review created");
    }

    /** Recalcula promedio y cantidad desde las reseñas reales: no acumula error de redondeo. */
    private void refreshRating(Accommodation accommodation) {
        Double avg = reviewRepository.averageRatingByAccommodationId(accommodation.getId());
        accommodation.setAverageRating(avg == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        accommodation.setNumberOfReviews((int) reviewRepository.countByAccommodationId(accommodation.getId()));
        accommodationRepository.save(accommodation);
    }

    private boolean isBookingReviewed(Long id, User user) {
        return reviewRepository.existsByUser_IdAndBooking_Id(user.getId(), id);
    }

    @Override
    public Page<CommentDTO> getCommentsByAccommodation(Integer alojamientoId, int page) {
        Pageable pageable = PageRequest.of(page, SIZE);
        Page<Review> reviews =  reviewRepository.getByAccommodationId(Long.valueOf(alojamientoId), pageable);
        return reviews.map(reviewMapper::toDto);
    }


    @Override
    public CommentDTO replyToComment(CommentAnswerDto answerDto, Long userId) throws ExceptionAlert {
        Optional<Review> review = reviewRepository.findById(answerDto.commentId());
        if (review.isEmpty()) {
            throw new ExceptionAlert("Review not found");
        }
        User user = userService.getActiveUser(userId);
        if (!review.get().getAccommodation().getHost().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not the host of the booking");
        }
        if (review.get().getReviewAnswer() != null) {
            throw new ExceptionAlert("This review has already been answered");
        }

        ReviewAnswer answer = new ReviewAnswer();
        answer.setAnswer(answerDto.message());
        answer.setReview(review.get());
        answer.setCreatedAt(LocalDate.now());

        reviewAnswerRepository.save(answer);
        review.get().setReviewAnswer(answer);
        reviewRepository.save(review.get());
        return reviewMapper.toDto(review.get());
    }




    @Override
    public CommentDTO getCommentByBookingId(Long bookingId, Long userId) throws ExceptionAlert {
        Review review = reviewRepository.getByBookingId(bookingId);
        if(review==null){
            throw new ExceptionAlert("Review not found");
        }
        // Solo el huésped que la escribió o el anfitrión del alojamiento pueden consultarla por reserva
        User user = userService.getActiveUser(userId);
        boolean isAuthor = review.getUser().getId().equals(user.getId());
        boolean isHost = review.getAccommodation().getHost().getId().equals(user.getId());
        if (!isAuthor && !isHost) {
            throw new ExceptionAlert("User is not authorized to view this review");
        }
        return reviewMapper.toDto(review);
    }

    @Override
    public InfoDto deleteComment(Long id, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        Review review = reviewRepository.findById(id).orElse(null);

        if (review == null) {
            return new InfoDto("Review not found", "Review not found");
        }

        if (user == null || review.getUser() == null || !review.getUser().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not the owner of the review");
        }

        Accommodation accommodation = review.getAccommodation();
        reviewRepository.delete(review);
        if (accommodation != null) {
            reviewRepository.flush();
            refreshRating(accommodation);
        }
        return new InfoDto("Review deleted", "Review deleted");
    }


    @Override
    public InfoDto deleteReplyComment(Long id, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        ReviewAnswer reviewAnswer = reviewAnswerRepository.findById(id).orElse(null);

        if (reviewAnswer == null) {
            return new InfoDto("Review answer not found", "Review answer not found");
        }

        if (!reviewAnswer.getReview().getAccommodation().getHost().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not the host of the accommodation");
        }

        Review review = reviewAnswer.getReview();
        review.setReviewAnswer(null);
        reviewRepository.save(review);

        reviewAnswerRepository.delete(reviewAnswer);

        return new InfoDto("Review answer deleted", "Review answer deleted");
    }

    @Override
    public boolean canUserCommentAccommodation(Long accommodationId, Long userId) throws ExceptionAlert {

        User user = userService.getActiveUser(userId);
        if (user == null) {
            return false;
        }

        // Estados válidos
        Set<BookingStatus> VALID_BOOKING_STATUSES = Set.of(
                BookingStatus.COMPLETED,
                BookingStatus.CONFIRMED
        );

        // Obtener todas las reservas del usuario para ese alojamiento
        List<Booking> bookings = bookingRepository
                .findByGuest_IdAndAccommodation_IdAndBookingStatusIn(
                        user.getId(),
                        accommodationId,
                        VALID_BOOKING_STATUSES
                );

        if (bookings.isEmpty()) {
            return false; // No tiene reservas válidas
        }

        // Validar que no haya comentado la reserva correspondiente
        for (Booking booking : bookings) {
            if (!isBookingReviewed(booking.getId(), user)) {
                return true; // Hay al menos una reserva válida sin review
            }
        }

        return false; // Todas sus reservas ya tienen review
    }


    @Override
    public boolean canUserReplyComment(Long commentId, Long userId) throws ExceptionAlert {

        // Buscar el comentario por ID (NO el alojamiento)
        Review comment = reviewRepository.findById(commentId)
                .orElse(null);

        if (comment == null) {
            return false;
        }

        // Obtener el alojamiento desde el comentario
        Accommodation accommodation = comment.getAccommodation();
        if (accommodation == null) {
            return false;
        }

        // Usuario autenticado
        User user = userService.getActiveUser(userId);
        if (user == null) {
            return false;
        }

        // Solo el host del alojamiento, y solo si la reseña no tiene respuesta aún
        return comment.getReviewAnswer() == null
                && accommodation.getHost().getId().equals(user.getId());
    }



}
