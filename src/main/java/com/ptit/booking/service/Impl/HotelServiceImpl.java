package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.dto.hotel.HomeDto;
import com.ptit.booking.dto.hotel.HotelRequest;
import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Image;
import com.ptit.booking.model.Promotion;
import com.ptit.booking.model.Room;
import com.ptit.booking.repository.HotelRepository;
import com.ptit.booking.repository.PromotionRepository;
import com.ptit.booking.repository.RoomRepository;
import com.ptit.booking.service.HotelService;
import com.ptit.booking.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final PromotionRepository promotionRepository;
    private final RoomRepository roomRepository;

    @Override
    public ResponseEntity<?> home() {
        List<Promotion> promotions = promotionRepository.findAll();
        List<HomeDto> homeDtoList = new ArrayList<>();
        for (Promotion promotion : promotions) {
            Set<Hotel> hotels = promotion.getHotels();
            List<HotelRequest> hotelRequestList = new ArrayList<>();
            for (Hotel hotel : hotels) {
                HotelRequest hotelRequest = HotelRequest.builder()
                        .hotelId(hotel.getId())
                        .hotelLocation(hotel.getLocation().getName())
                        .hotelName(hotel.getName())
                        .hotelRating(hotel.getRating())
                        .promotionName(promotion.getName())
                        .sumReview(hotel.getFeedbackSum())
                        .price(roomRepository.getRoomPriceMin(hotel))
                        .imageUrl(hotel.getImages().stream().findFirst().get().getUrl())
                        .build();
                hotelRequestList.add(hotelRequest);
            }
            HomeDto homeDto = HomeDto.builder()
                    .hotelRequestList(hotelRequestList)
                    .historySearchList(new ArrayList<>())
                    .build();
            homeDtoList.add(homeDto);
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.HOME_PAGE)
                .data(homeDtoList)
                .build());
    }

    @Override
    public ResponseEntity<?> search(String sortBy, String sort, int page, FilterRequest filterRequest) {
        Pageable pageable = PageRequest.of(page, 5,
                sort.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<Hotel> hotels = hotelRepository.findAllWithDetails(HotelSpecification.filterHotels(filterRequest), pageable);

        Page<HotelRequest> hotelRequestPage = hotels.map(hotel -> new HotelRequest(
                hotel.getId(),
                hotel.getName(),
                hotel.getLocation() != null ? hotel.getLocation().getName() : null,
                hotel.getRating(),
                hotel.getImages() != null ? hotel.getImages()
                        .stream()
                        .findFirst()
                        .map(Image::getUrl)
                        .orElse(null) : null,
                hotel.getFeedbackSum(),
                hotel.getPromotions() != null ? hotel.getPromotions()
                        .stream()
                        .findFirst()
                        .map(Promotion::getName)
                        .orElse(null) : null,
                hotel.getRooms() != null ? hotel.getRooms()
                        .stream()
                        .map(Room::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO)
                        .floatValue() : 0.0f
        ));
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.FILTER_PAGE)
                .data(hotelRequestPage)
                .build());
    }
}
