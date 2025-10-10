package com.labndbnb.landbnb.mappers.accommodation;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.model.Accommodation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-10T11:18:37-0500",
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
        if ( dto.id() != null ) {
            accommodation.id( dto.id().longValue() );
        }
        accommodation.description( dto.description() );
        accommodation.city( dto.city() );
        accommodation.address( dto.address() );
        if ( dto.latitude() != null ) {
            accommodation.latitude( BigDecimal.valueOf( dto.latitude() ) );
        }
        if ( dto.longitude() != null ) {
            accommodation.longitude( BigDecimal.valueOf( dto.longitude() ) );
        }
        List<String> list = dto.services();
        if ( list != null ) {
            accommodation.services( new ArrayList<String>( list ) );
        }

        accommodation.pricePerNight( dto.pricePerNight() != null ? dto.pricePerNight() : null );

        return accommodation.build();
    }

    @Override
    public AccommodationDto toDto(Accommodation accommodation) {
        if ( accommodation == null ) {
            return null;
        }

        String title = null;
        Integer maxCapacity = null;
        Integer id = null;
        String description = null;
        String city = null;
        String address = null;
        Double latitude = null;
        Double longitude = null;
        List<String> services = null;

        title = accommodation.getName();
        maxCapacity = accommodation.getCapacity();
        if ( accommodation.getId() != null ) {
            id = accommodation.getId().intValue();
        }
        description = accommodation.getDescription();
        city = accommodation.getCity();
        address = accommodation.getAddress();
        if ( accommodation.getLatitude() != null ) {
            latitude = accommodation.getLatitude().doubleValue();
        }
        if ( accommodation.getLongitude() != null ) {
            longitude = accommodation.getLongitude().doubleValue();
        }
        List<String> list = accommodation.getServices();
        if ( list != null ) {
            services = new ArrayList<String>( list );
        }

        Double pricePerNight = accommodation.getPricePerNight() != null ? accommodation.getPricePerNight().doubleValue() : null;
        String url = null;

        AccommodationDto accommodationDto = new AccommodationDto( id, title, description, city, address, latitude, longitude, pricePerNight, maxCapacity, services, url );

        return accommodationDto;
    }
}
