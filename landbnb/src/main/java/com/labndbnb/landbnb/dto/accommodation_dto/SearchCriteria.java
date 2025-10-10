package com.labndbnb.landbnb.dto.accommodation_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record SearchCriteria(

        @Size(max = 100, message = "City name cannot exceed 100 characters")
        String city,

        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in date must be today or in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkIn,

        @NotNull(message = "Check-out date is required")
        @Future(message = "Check-out date must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkOut,

        @DecimalMin(value = "0.0", message = "Minimum price cannot be negative")
        Double minPrice,

        @DecimalMin(value = "0.0", message = "Maximum price cannot be negative")
        Double maxPrice,

        List<@NotBlank(message = "Service cannot be blank") String> services,

        @Pattern(regexp = "price|rating|name|createdDate",
                message = "Sort by must be one of: price, rating, name, createdDate")
        String sortBy,

        @Pattern(regexp = "asc|desc", message = "Sort direction must be 'asc' or 'desc'")
        String sortDirection,

        Boolean hasWifi,
        Boolean hasPool,
        Boolean allowsPets,
        Boolean hasAirConditioning,
        Boolean hasKitchen,
        Boolean hasParking

) {}