package com.labndbnb.landbnb.mappers.accommodation;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.model.Accommodation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-14T18:45:00-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class AccommodationDtoMapperImpl implements AccommodationDtoMapper {

    @Override
    public Accommodation toEntity(AccommodationDto dto) {
        if ( dto == null ) {
            return null;
        }

        Accommodation.AccommodationBuilder accommodation = Accommodation.builder();

        accommodation.name( dto.title() );
        accommodation.capacity( dto.maxCapacity() );
        accommodation.principalImageUrl( dto.mainImage() );
        if ( dto.id() != null ) {
            accommodation.id( dto.id().longValue() );
        }
        accommodation.description( dto.description() );
        accommodation.city( dto.city() );
        accommodation.address( dto.address() );
        accommodation.pricePerNight( dto.pricePerNight() );
        List<String> list = dto.services();
        if ( list != null ) {
            accommodation.services( new ArrayList<String>( list ) );
        }

        accommodation.latitude( dto.latitude() != null ? new java.math.BigDecimal(dto.latitude().toString()) : null );
        accommodation.longitude( dto.longitude() != null ? new java.math.BigDecimal(dto.longitude().toString()) : null );

        return accommodation.build();
    }

    @Override
    public AccommodationDto toDto(Accommodation accommodation) {
        if ( accommodation == null ) {
            return null;
        }

        String title = null;
        Integer maxCapacity = null;
        String mainImage = null;
        Integer id = null;
        String description = null;
        String city = null;
        String address = null;
        Double pricePerNight = null;
        List<String> services = null;

        title = accommodation.getName();
        maxCapacity = accommodation.getCapacity();
        mainImage = accommodation.getPrincipalImageUrl();
        if ( accommodation.getId() != null ) {
            id = accommodation.getId().intValue();
        }
        description = accommodation.getDescription();
        city = accommodation.getCity();
        address = accommodation.getAddress();
        pricePerNight = accommodation.getPricePerNight();
        List<String> list = accommodation.getServices();
        if ( list != null ) {
            services = new ArrayList<String>( list );
        }

        Double latitude = accommodation.getLatitude() != null ? accommodation.getLatitude().doubleValue() : null;
        Double longitude = accommodation.getLongitude() != null ? accommodation.getLongitude().doubleValue() : null;

        AccommodationDto accommodationDto = new AccommodationDto( id, title, description, city, address, latitude, longitude, pricePerNight, maxCapacity, services, mainImage );

        return accommodationDto;
    }
}
