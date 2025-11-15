package com.labndbnb.landbnb.mappers.accommodation;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.user_dto.UserInfoDto;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-14T19:02:57-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class AccommodationDetailDtoMapperImpl implements AccommodationDetailDtoMapper {

    @Override
    public AccommodationDetailDto toDto(Accommodation accommodation) {
        if ( accommodation == null ) {
            return null;
        }

        Integer id = null;
        String title = null;
        Double latitude = null;
        Double longitude = null;
        Double averageRating = null;
        Integer maxCapacity = null;
        String mainImage = null;
        Integer totalBookings = null;
        UserInfoDto host = null;
        String description = null;
        String city = null;
        String address = null;
        Double pricePerNight = null;
        List<String> services = null;
        List<String> images = null;

        id = longToInteger( accommodation.getId() );
        title = accommodation.getName();
        latitude = bigDecimalToDouble( accommodation.getLatitude() );
        longitude = bigDecimalToDouble( accommodation.getLongitude() );
        averageRating = bigDecimalToDouble( accommodation.getAverageRating() );
        maxCapacity = accommodation.getCapacity();
        mainImage = accommodation.getPrincipalImageUrl();
        totalBookings = countBookings( accommodation.getBookings() );
        host = userToUserInfoDto( accommodation.getHost() );
        description = accommodation.getDescription();
        city = accommodation.getCity();
        address = accommodation.getAddress();
        pricePerNight = accommodation.getPricePerNight();
        List<String> list = accommodation.getServices();
        if ( list != null ) {
            services = new ArrayList<String>( list );
        }
        List<String> list1 = accommodation.getImages();
        if ( list1 != null ) {
            images = new ArrayList<String>( list1 );
        }

        AccommodationDetailDto accommodationDetailDto = new AccommodationDetailDto( id, title, description, city, address, latitude, longitude, pricePerNight, maxCapacity, services, host, averageRating, totalBookings, mainImage, images );

        return accommodationDetailDto;
    }

    @Override
    public Accommodation toEntity(AccommodationDetailDto dto) {
        if ( dto == null ) {
            return null;
        }

        Accommodation.AccommodationBuilder accommodation = Accommodation.builder();

        accommodation.id( integerToLong( dto.id() ) );
        accommodation.name( dto.title() );
        accommodation.latitude( doubleToBigDecimal( dto.latitude() ) );
        accommodation.longitude( doubleToBigDecimal( dto.longitude() ) );
        accommodation.averageRating( doubleToBigDecimal( dto.averageRating() ) );
        accommodation.capacity( dto.maxCapacity() );
        accommodation.principalImageUrl( dto.mainImage() );
        accommodation.description( dto.description() );
        accommodation.city( dto.city() );
        accommodation.address( dto.address() );
        accommodation.pricePerNight( dto.pricePerNight() );
        List<String> list = dto.services();
        if ( list != null ) {
            accommodation.services( new ArrayList<String>( list ) );
        }
        List<String> list1 = dto.images();
        if ( list1 != null ) {
            accommodation.images( new ArrayList<String>( list1 ) );
        }
        accommodation.host( userInfoDtoToUser( dto.host() ) );

        return accommodation.build();
    }

    @Override
    public void updateEntityFromDto(AccommodationDetailDto dto, Accommodation entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.title() != null ) {
            entity.setName( dto.title() );
        }
        if ( dto.maxCapacity() != null ) {
            entity.setCapacity( dto.maxCapacity() );
        }
        if ( dto.mainImage() != null ) {
            entity.setPrincipalImageUrl( dto.mainImage() );
        }
        if ( dto.description() != null ) {
            entity.setDescription( dto.description() );
        }
        if ( dto.city() != null ) {
            entity.setCity( dto.city() );
        }
        if ( dto.address() != null ) {
            entity.setAddress( dto.address() );
        }
        if ( dto.latitude() != null ) {
            entity.setLatitude( BigDecimal.valueOf( dto.latitude() ) );
        }
        if ( dto.longitude() != null ) {
            entity.setLongitude( BigDecimal.valueOf( dto.longitude() ) );
        }
        if ( dto.averageRating() != null ) {
            entity.setAverageRating( BigDecimal.valueOf( dto.averageRating() ) );
        }
        if ( dto.pricePerNight() != null ) {
            entity.setPricePerNight( dto.pricePerNight() );
        }
        if ( entity.getServices() != null ) {
            List<String> list = dto.services();
            if ( list != null ) {
                entity.getServices().clear();
                entity.getServices().addAll( list );
            }
        }
        else {
            List<String> list = dto.services();
            if ( list != null ) {
                entity.setServices( new ArrayList<String>( list ) );
            }
        }
        if ( entity.getImages() != null ) {
            List<String> list1 = dto.images();
            if ( list1 != null ) {
                entity.getImages().clear();
                entity.getImages().addAll( list1 );
            }
        }
        else {
            List<String> list1 = dto.images();
            if ( list1 != null ) {
                entity.setImages( new ArrayList<String>( list1 ) );
            }
        }
    }

    protected UserInfoDto userToUserInfoDto(User user) {
        if ( user == null ) {
            return null;
        }

        String name = null;
        String lastName = null;

        name = user.getName();
        lastName = user.getLastName();

        String photoProfile = null;

        UserInfoDto userInfoDto = new UserInfoDto( name, lastName, photoProfile );

        return userInfoDto;
    }

    protected User userInfoDtoToUser(UserInfoDto userInfoDto) {
        if ( userInfoDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.name( userInfoDto.name() );
        user.lastName( userInfoDto.lastName() );

        return user.build();
    }
}
