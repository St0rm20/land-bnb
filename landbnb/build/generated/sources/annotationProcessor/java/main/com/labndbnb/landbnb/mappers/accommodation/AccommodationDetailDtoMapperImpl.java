package com.labndbnb.landbnb.mappers.accommodation;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.util_dto.ImageDto;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;
import com.labndbnb.landbnb.mappers.util.ImageDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T17:55:05-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class AccommodationDetailDtoMapperImpl implements AccommodationDetailDtoMapper {

    @Autowired
    private UserDtoMapper userDtoMapper;
    @Autowired
    private ImageDtoMapper imageDtoMapper;

    @Override
    public AccommodationDetailDto toDto(Accommodation accommodation) {
        if ( accommodation == null ) {
            return null;
        }

        Integer id = null;
        String title = null;
        Integer maxCapacity = null;
        String mainImage = null;
        List<ImageDto> images = null;
        String description = null;
        String city = null;
        String address = null;
        Double latitude = null;
        Double longitude = null;
        List<String> services = null;
        UserDto host = null;

        id = longToInteger( accommodation.getId() );
        title = accommodation.getName();
        maxCapacity = accommodation.getCapacity();
        mainImage = accommodation.getPrincipalImageUrl();
        images = imageDtoMapper.mapStringListToImageDtoList( accommodation.getImages() );
        description = accommodation.getDescription();
        city = accommodation.getCity();
        address = accommodation.getAddress();
        if ( accommodation.getLatitude() != null ) {
            latitude = accommodation.getLatitude().doubleValue();
        }
        if ( accommodation.getLongitude() != null ) {
            longitude = accommodation.getLongitude().doubleValue();
        }
        List<String> list1 = accommodation.getServices();
        if ( list1 != null ) {
            services = new ArrayList<String>( list1 );
        }
        host = userDtoMapper.toDto( accommodation.getHost() );

        Double pricePerNight = accommodation.getPricePerNight() != null ? accommodation.getPricePerNight().doubleValue() : null;
        Double averageRating = accommodation.getAverageRating() != null ? accommodation.getAverageRating().doubleValue() : null;
        Integer totalBookings = accommodation.getBookings() != null ? accommodation.getBookings().size() : 0;

        AccommodationDetailDto accommodationDetailDto = new AccommodationDetailDto( id, title, description, city, address, latitude, longitude, pricePerNight, maxCapacity, services, host, averageRating, totalBookings, mainImage, images );

        return accommodationDetailDto;
    }

    @Override
    public Accommodation toEntity(AccommodationDetailDto dto) {
        if ( dto == null ) {
            return null;
        }

        Accommodation.AccommodationBuilder accommodation = Accommodation.builder();

        accommodation.name( dto.title() );
        accommodation.capacity( dto.maxCapacity() );
        accommodation.principalImageUrl( dto.mainImage() );
        accommodation.images( imageDtoMapper.mapImageDtoListToStringList( dto.images() ) );
        accommodation.description( dto.description() );
        accommodation.city( dto.city() );
        accommodation.address( dto.address() );
        if ( dto.latitude() != null ) {
            accommodation.latitude( BigDecimal.valueOf( dto.latitude() ) );
        }
        if ( dto.longitude() != null ) {
            accommodation.longitude( BigDecimal.valueOf( dto.longitude() ) );
        }
        List<String> list1 = dto.services();
        if ( list1 != null ) {
            accommodation.services( new ArrayList<String>( list1 ) );
        }
        accommodation.host( userDtoMapper.toEntity( dto.host() ) );

        accommodation.id( dto.id() != null ? dto.id().longValue() : null );
        accommodation.pricePerNight( dto.pricePerNight() != null ? dto.pricePerNight().intValue() : null );
        accommodation.averageRating( dto.averageRating() != null ? java.math.BigDecimal.valueOf(dto.averageRating()) : java.math.BigDecimal.ZERO );

        return accommodation.build();
    }
}
