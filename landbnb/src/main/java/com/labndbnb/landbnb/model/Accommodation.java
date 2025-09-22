package com.labndbnb.landbnb.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accommodations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Lob
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "address", nullable = false, length = 300)
    private String address;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;


    @Column(name = "price_per_night", nullable = false)
    private Integer pricePerNight;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @ElementCollection
    @CollectionTable(name = "accommodation_services",
            joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "service", length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private List<String> services = new ArrayList<>();

    @Column(name = "principal_image_url", length = 500)
    private String principalImageUrl;

    @ElementCollection
    @CollectionTable(name = "accommodation_images",
            joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "image_url", length = 500)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private List<String> images = new ArrayList<>();

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User host;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Booking> bookings = new ArrayList<>();

@OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Review> reviews = new ArrayList<>();
}
