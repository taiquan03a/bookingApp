package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.dto.hotel.HomeDto;
import com.ptit.booking.dto.hotel.HotelRequest;
import com.ptit.booking.dto.hotelDetail.*;
import com.ptit.booking.dto.hotelDetail.Review;
import com.ptit.booking.dto.policy.PolicyRoom;
import com.ptit.booking.enums.EnumServiceType;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.mapping.RoomResponseMapper;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.HotelRepository;
import com.ptit.booking.repository.PromotionRepository;
import com.ptit.booking.repository.RoomRepository;
import com.ptit.booking.repository.ServiceRepository;
import com.ptit.booking.service.HotelService;
import com.ptit.booking.specification.HotelSpecification;
import com.ptit.booking.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.ptit.booking.constants.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final PromotionRepository promotionRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private String apiKey = "fsq3VsauLSw5an6aSmbScZFmlw1nco2b+9ozuSaKfVJzdK4=";

    private final RestTemplate restTemplate = new RestTemplate();

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

    @Override
    public ResponseEntity<?> hotelDetail(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        long daysBetween = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if(daysBetween < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(403)
                    .message("CHECKIN_AFTER_CHECKOUT")
                    .description(CHECKIN_AFTER_CHECKOUT)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Review review = Review.builder()
                .description(hotel.getDescription())
                .amenities(hotel.getAmenities())
                .rating(hotel.getRating())
                .phoneNumber("123456789")
                .location(hotel.getLocation().getName())
                .sumReview(hotel.getFeedbackSum())
                .build();
        NearBy nearBy = NearBy.builder()
                .descriptionLocation(hotel.getLocation().getDescription())
                .ratingLocation(String.valueOf(hotel.getLocation().getRating()))
                .activityList(searchPlaces(convertVietnamese(hotel.getLocation().getName())))
                .build();
        HotelDetail hotelDetail = HotelDetail.builder()
                .images(hotel.getImages().stream().map(Image::getUrl).toList())
                .nearBy(nearBy)
                .priceMin(daysBetween * hotel.getRooms()
                        .stream()
                        .map(Room::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO)
                        .floatValue())
                .review(review)
                .build();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.HOTEL_DETAIL + " " + hotelId)
                .data(hotelDetail)
                .build());
    }

    @Override
    @Transactional
    public ResponseEntity<?> selectRooms(SelectRoomRequest selectRoomRequest) {
        Hotel hotel = hotelRepository.findById(selectRoomRequest.getHotelId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Specification<Room> specRoomAvi = RoomSpecification.availableRooms(selectRoomRequest,hotel);
        List<RoomResponse> re = roomRepository.findAll(specRoomAvi)
                .stream()
                .map(
                        room -> mapRoomResponse(
                                room,
                                (int) ChronoUnit.DAYS.between(
                                        selectRoomRequest.getCheckInDate(),
                                        selectRoomRequest.getCheckOutDate()
                                ),
                                roomRepository.countAvailableRoom(
                                        room,selectRoomRequest.getCheckInDate(),
                                        selectRoomRequest.getCheckOutDate()
                                )
                        )
                )
                .toList();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.LIST_ROOM_AVAILABLE + selectRoomRequest.getHotelId())
                .data(re)
                .build());
    }
    private RoomResponse mapRoomResponse(Room room, int selectDay,int availableRoom) {
        Set<com.ptit.booking.model.Service> serviceSet = serviceRepository.findAllByRoom(room);
        Set<PolicyRoom> policyRoomList = new LinkedHashSet<>();
        for(HotelPolicy hotelPolicy:room.getHotel().getHotelPolicies()){
            PolicyRoom policyRoom = PolicyRoom.builder()
                    .policyId(hotelPolicy.getPolicy().getId())
                    .policyDescription(hotelPolicy.getDescription())
                    .policyName(hotelPolicy.getPolicy().getName())
                    .build();
            policyRoomList.add(policyRoom);
        }
        return RoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .area(room.getArea())
                .bed(room.getBed())
                .serviceList(serviceSet)
                .selectDay(selectDay)
                .price(selectDay * room.getPrice().floatValue())
                .roomQuantity(availableRoom)
                .policyRoomList(policyRoomList)
                .build();
    }
    public List<Activity> searchPlaces(String location) {
        // 1. Get coordinates from OpenStreetMap
        String geocodeUrl = "https://nominatim.openstreetmap.org/search";
        UriComponentsBuilder geoBuilder = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
                .queryParam("q", location)
                .queryParam("format", "json");

        ResponseEntity<List> geoResponse = restTemplate.exchange(
                geoBuilder.toUriString(), HttpMethod.GET, null, List.class);

        if (geoResponse.getBody() == null || geoResponse.getBody().isEmpty()) {
            throw new RuntimeException("Location not found");
        }

        Map<String, Object> geoData = (Map<String, Object>) geoResponse.getBody().get(0);
        String ll = geoData.get("lat") + "," + geoData.get("lon");

        // 2. Setup Foursquare API request
        String searchUrl = "https://api.foursquare.com/v3/places/search";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(searchUrl)
                .queryParam("ll", ll)
                .queryParam("radius", "5000")
                .queryParam("limit", "5")
                .queryParam("categories", "16000,16038,16034");

        // 3. Get places from Foursquare
        ResponseEntity<Map> response = restTemplate.exchange(
                uriBuilder.toUriString(), HttpMethod.GET, entity, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("No places found");
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
        List<Activity> activities = new ArrayList<>();

        // 4. Process each place
        for (Map<String, Object> place : results) {
            Activity activity = new Activity();
            String fsqId = place.get("fsq_id").toString();

            // Basic info
            activity.setName((String) place.getOrDefault("name", "No name"));
            activity.setDescription((String) place.getOrDefault("description", "No description"));
            activity.setDistance(place.containsKey("distance")
                    ? place.get("distance") + " m"
                    : "Unknown");

            // 5. Get rating
            float rating = getPlaceRating(fsqId, entity);
            activity.setRating(rating);

            activities.add(activity);
        }

        return activities;
    }
    private float getPlaceRating(String fsqId, HttpEntity<String> entity) {
        String detailsUrl = "https://api.foursquare.com/v3/places/" + fsqId;
        ResponseEntity<Map> detailsResponse = restTemplate.exchange(
                detailsUrl, HttpMethod.GET, entity, Map.class);

        if (detailsResponse.getBody() != null && detailsResponse.getBody().containsKey("rating")) {
            Object ratingObj = detailsResponse.getBody().get("rating");
            return ratingObj instanceof Number
                    ? ((Number) ratingObj).floatValue()
                    : Float.parseFloat(ratingObj.toString());
        }
        // Generate random rating between 4.0 and 5.0 if not available
        return (float) (Math.round((4.0 + Math.random() * (5.0 - 4.0)) * 10.0) / 10.0);
    }
    public static String convertVietnamese(String input) {
        // Bảng ánh xạ các ký tự có dấu sang không dấu
        String vietnamese = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
        String nonVietnamese = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";

        // Chuyển chuỗi về chữ thường
        String result = input.toLowerCase();

        // Thay thế từng ký tự có dấu
        for (int i = 0; i < vietnamese.length(); i++) {
            result = result.replace(vietnamese.charAt(i), nonVietnamese.charAt(i));
        }

        // Tách chuỗi thành mảng các từ
        String[] words = result.split("\\s+");
        StringBuilder finalResult = new StringBuilder();

        // Nối các từ bằng dấu +
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                // Viết hoa chữ cái đầu mỗi từ
                String capitalized = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
                finalResult.append(capitalized);
                // Thêm dấu + nếu không phải từ cuối cùng
                if (i < words.length - 1) {
                    finalResult.append("+");
                }
            }
        }

        return finalResult.toString();
    }

}
