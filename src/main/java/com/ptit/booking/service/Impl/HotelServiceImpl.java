package com.ptit.booking.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.feedback.Comment;
import com.ptit.booking.dto.hotel.*;
import com.ptit.booking.dto.hotelDetail.*;
import com.ptit.booking.dto.hotelDetail.ReviewDto;
import com.ptit.booking.enums.EnumBookingStatus;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.*;
import com.ptit.booking.service.CloudinaryService;
import com.ptit.booking.service.HotelService;
import com.ptit.booking.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final LocationRepository locationRepository;
    private static final int MAX_HISTORY = 10;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final CloudinaryService cloudinaryService;
    private final ImageRepository imageRepository;
    private final OtaRepository otaRepository;
    private String apiKey = "fsq3VsauLSw5an6aSmbScZFmlw1nco2b+9ozuSaKfVJzdK4=";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ResponseEntity<?> home(Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        List<SearchHistory> historySearchList = new ArrayList<>();
        if(user != null){
            historySearchList = getSearchHistory(user.getId());
        }
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
                        .imageUrl(hotel.getImages()
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_HOTEL_NOT_FOUND))
                                .getUrl())
                        .build();
                hotelRequestList.add(hotelRequest);
            }
            HomeDto homeDto = HomeDto.builder()
                    .hotelRequestList(hotelRequestList)
                    .historySearchList(historySearchList)
                    .build();
            homeDtoList.add(homeDto);
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.HOME_PAGE)
                .data(homeDtoList)
                .build());
    }
    private List<SearchHistory> getSearchHistory(Long userId) {
        try {
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            String key = "searchHistory:" + userId;
            List<String> searchList = listOps.range(key, 0, -1);

            return searchList.stream()
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, SearchHistory.class);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public ResponseEntity<?> search(String sortBy, String sort, int page, FilterRequest filterRequest, Principal principal) throws JsonProcessingException {

        if (filterRequest.getCheckin().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.CHECKIN_NOW);
        }
        if (!filterRequest.getCheckout().isAfter(filterRequest.getCheckin())) {
            throw new AppException(ErrorCode.CHECKOUT_AFTER_CHECKIN);
        }
        if (ChronoUnit.DAYS.between(filterRequest.getCheckin(), filterRequest.getCheckout()) > 30) {
            throw new AppException(ErrorCode.DURATION_NOT_30);
        }
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        Pageable pageable = PageRequest.of(page, 5);
        if(user != null){
            SearchHistory search = SearchHistory.builder()
                    .locationId(filterRequest.getLocationId())
                    .location(locationRepository
                            .findById(filterRequest.getLocationId())
                            .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND))
                            .getName())
                    .checkIn(filterRequest.getCheckin())
                    .checkOut(filterRequest.getCheckout())
                    .adults(filterRequest.getAdults())
                    .children(filterRequest.getChildren())
                    .rooms(filterRequest.getRoomNumber())
                    .build();

            try {
                ListOperations<String, String> listOps = redisTemplate.opsForList();
                String key = "searchHistory:" + user.getId();
                String searchJson = objectMapper.writeValueAsString(search);

                // Lấy toàn bộ lịch sử tìm kiếm hiện có
                List<String> searchList = listOps.range(key, 0, -1);

                // Xóa lịch sử cũ của cùng một địa điểm (nếu có)
                if (searchList != null) {
                    for (String json : searchList) {
                        try {
                            SearchHistory oldSearch = objectMapper.readValue(json, SearchHistory.class);
                            if (oldSearch.getLocation().equals(search.getLocation())) {
                                listOps.remove(key, 1, json); // Xóa mục cũ
                                break;
                            }
                        } catch (JsonProcessingException e) {
                            e.printStackTrace(); // Có thể ghi log lỗi JSON nếu cần
                        }
                    }
                }

                // Lưu lịch sử mới vào đầu danh sách
                listOps.leftPush(key, searchJson);
                System.out.println("Pushed to Redis key " + key + ": " + searchJson);

                // Giữ tối đa MAX_HISTORY mục
                listOps.trim(key, 0, MAX_HISTORY - 1);

            } catch (Exception redisException) {
                // Ghi log lỗi Redis nhưng không throw để tránh ảnh hưởng luồng chính
                System.err.println("Không thể ghi lịch sử tìm kiếm vào Redis: " + redisException.getMessage());
                // Nếu bạn dùng logging framework:
                // logger.warn("Redis error while saving search history", redisException);
            }
        }
        Page<Hotel> hotels = hotelRepository.findAll(HotelSpecification.filterHotels(filterRequest,sortBy,sort), pageable);
        Page<HotelRequest> hotelRequestPage = hotels.map(hotel -> {
            String imageUrl = hotel.getImages() != null
                    ? hotel.getImages().stream().findFirst().map(Image::getUrl).orElse(null)
                    : null;

            Promotion promotion = hotel.getPromotions() != null
                    ? hotel.getPromotions().stream()
                    .filter(Promotion::getStatus)
                    .filter(promotionMap -> LocalDateTime.now().isAfter(promotionMap.getStartDate()))
                    .filter(promotionMap -> LocalDateTime.now().isBefore(promotionMap.getEndDate()))
                    .findFirst().orElse(null)
                    : null;

            float price = hotel.getRooms() != null
                    ? hotel.getRooms().stream().map(Room::getPrice).min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO).floatValue()
                    : 0.0f;

            String promotionValueStr = promotion != null ? String.valueOf(promotion.getDiscountValue()) : null;
            String promotionName = promotion != null ? promotion.getName() : null;

            float promotionPrice = price;

            if (promotion != null) {
                if ("PERCENTAGE".equalsIgnoreCase(promotion.getDiscountType())) {
                    try {
                        float percent = Float.parseFloat(promotion.getDiscountValue().substring(0,promotion.getDiscountValue().length() - 1));
                        promotionPrice = price - price * (percent / 100);
                    } catch (NumberFormatException e) {
                        // bỏ qua nếu format sai
                    }
                } else if ("FIXED".equalsIgnoreCase(promotion.getDiscountType())) {
                    try {
                        float fixed = Float.parseFloat(promotion.getDiscountValue().toString());
                        promotionPrice = price - fixed;
                    } catch (NumberFormatException e) {
                        // bỏ qua nếu format sai
                    }
                }
            }
            List<OtaPriceMin> otaPriceMinList = otaRepository.findOtaByHotelId(hotel.getId()).stream()
                    .map(ota -> {
                        return OtaPriceMin.builder()
                                .otaName(ota.getName())
                                .minPrice(otaRepository.findOtaHotel(ota.getId(),hotel.getId()).getPriceMin().toString())
                                .build();
                    })
                    .toList();

            return new HotelRequest(
                    hotel.getId(),
                    hotel.getName(),
                    hotel.getAddress(),
                    hotel.getLat(),
                    hotel.getLng(),
                    hotel.getRating(),
                    imageUrl,
                    hotel.getFeedbackSum(),
                    promotionValueStr,
                    String.format("%.0f", promotionPrice), // nếu bạn muốn format thành chuỗi không có số thập phân
                    promotionName,
                    otaPriceMinList,
                    price
            );
        });

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
        List<Review> reviewList = reviewRepository.findByHotel(hotel);
        List<Comment> commentList = new ArrayList<>();
        int fiveStar = 0;
        int fourStar = 0;
        int threeStar = 0;
        int twoStar = 0;
        int oneStar = 0;
        int ratingHotel = 0;
        int ratingRoom = 0;
        int ratingLocation = 0;
        int ratingService = 0;
        for(Review review : reviewList) {
            Comment comment = Comment.builder()
                    .reviewId(review.getId())
                    .username(review.getUser().getUsername())
                    .urlAvatar(review.getUser().getAvatar())
                    .comment(review.getComment())
                    .rating(review.getRatingHotel())
                    .build();
            commentList.add(comment);

            ratingHotel += review.getRatingHotel();
            ratingRoom += review.getRatingRoom();
            ratingLocation += review.getRatingLocation();
            ratingService += review.getRatingService();

            if(review.getRatingHotel() == 5) fiveStar ++;
            if(review.getRatingRoom() == 5) fiveStar ++;
            if(review.getRatingLocation() == 5) fiveStar ++;
            if(review.getRatingService() == 5) fiveStar ++;

            if(review.getRatingHotel() == 4) fourStar ++;
            if(review.getRatingRoom() == 4) fourStar ++;
            if(review.getRatingLocation() == 4) fourStar ++;
            if(review.getRatingService() == 4) fourStar ++;

            if(review.getRatingHotel() == 3) threeStar ++;
            if(review.getRatingRoom() == 3) threeStar ++;
            if(review.getRatingLocation() == 3) threeStar ++;
            if(review.getRatingService() == 3) threeStar ++;

            if(review.getRatingHotel() == 2) twoStar ++;
            if(review.getRatingRoom() == 2) twoStar ++;
            if(review.getRatingLocation() == 2) twoStar ++;
            if(review.getRatingService() == 2) twoStar ++;

            if(review.getRatingHotel() == 1) oneStar ++;
            if(review.getRatingRoom() == 1) oneStar ++;
            if(review.getRatingLocation() == 1) oneStar ++;
            if(review.getRatingService() == 1) oneStar ++;

        }
        Feedback feedback = Feedback.builder()
                .comments(commentList)
                .fiveStar((float) (Math.round(((float) fiveStar * 100 / (4 * reviewList.size())) * 100.0) / 100.0))
                .fourStar((float) (Math.round(((float) fourStar * 100 / (4 * reviewList.size())) * 100.0) / 100.0))
                .threeStar((float) (Math.round(((float) threeStar * 100 / (4 * reviewList.size())) * 100.0) / 100.0))
                .twoStar((float) (Math.round(((float)twoStar * 100 / (4 * reviewList.size())) * 100.0) / 100.0))
                .oneStar((float) (Math.round(((float) oneStar * 100 / (4 * reviewList.size())) * 100.0) / 100.0))
                .ratingHotel((float) (Math.round(((float) ratingHotel/reviewList.size()) * 10.0) / 10.0))
                .ratingRoom((float) (Math.round(((float) ratingRoom/reviewList.size()) * 10.0) / 10.0))
                .ratingLocation((float) (Math.round(((float) ratingLocation/reviewList.size()) * 10.0) / 10.0))
                .ratingService((float) (Math.round(((float) ratingService/reviewList.size()) * 10.0) / 10.0))
                .build();

//        String geocodeUrl = "https://nominatim.openstreetmap.org/search";
//        UriComponentsBuilder geoBuilder = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
//                .queryParam("q", convertVietnamese(hotel.getLocation().getName()))
//                .queryParam("format", "json");
//
//        ResponseEntity<List> geoResponse = restTemplate.exchange(
//                geoBuilder.toUriString(), HttpMethod.GET, null, List.class);
//
//        if (geoResponse.getBody() == null || geoResponse.getBody().isEmpty()) {
//            throw new RuntimeException("Location not found");
//        }
//
//        Map<String, Object> geoData = (Map<String, Object>) geoResponse.getBody().get(0);
//        String ll = geoData.get("lat") + "," + geoData.get("lon");
        ReviewDto review = ReviewDto.builder()
                .lat(hotel.getLat())
                .lng(hotel.getLng())
                .description(hotel.getDescription())
                .amenities(hotel.getAmenities())
                .rating(hotel.getRating())
                .phoneNumber("123456789")
                .location(hotel.getAddress())
                .sumReview(hotel.getFeedbackSum())
                .feedback(feedback)
                .build();
        NearBy nearBy = NearBy.builder()
                .descriptionLocation(hotel.getLocation().getDescription())
                .ratingLocation(String.valueOf(hotel.getLocation().getRating()))
                .activityList(searchPlaces(convertVietnamese(hotel.getLocation().getName())))
                .build();

        Promotion promotion = hotel.getPromotions() != null
                ? hotel.getPromotions().stream()
                .filter(Promotion::getStatus)
                .filter(promotionMap -> LocalDateTime.now().isAfter(promotionMap.getStartDate()))
                .filter(promotionMap -> LocalDateTime.now().isBefore(promotionMap.getEndDate()))
                .findFirst().orElse(null)
                : null;

        float price = hotel.getRooms() != null
                ? hotel.getRooms().stream().map(Room::getPrice).min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO).floatValue()
                : 0.0f;

        float promotionPrice = price;

        if (promotion != null) {
            if ("PERCENTAGE".equalsIgnoreCase(promotion.getDiscountType())) {
                try {
                    float percent = Float.parseFloat(promotion.getDiscountValue().substring(0,promotion.getDiscountValue().length() - 1));
                    promotionPrice = price - price * (percent / 100);
                } catch (NumberFormatException e) {
                    // bỏ qua nếu format sai
                }
            } else if ("FIXED".equalsIgnoreCase(promotion.getDiscountType())) {
                try {
                    float fixed = Float.parseFloat(promotion.getDiscountValue().toString());
                    promotionPrice = price - fixed;
                } catch (NumberFormatException e) {
                    // bỏ qua nếu format sai
                }
            }
        }
        HotelDetail hotelDetail = HotelDetail.builder()
                .images(hotel.getImages().stream().map(Image::getUrl).toList())
                .nearBy(nearBy)
                .priceNoPromotion(daysBetween * hotel.getRooms()
                        .stream()
                        .map(Room::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO)
                        .floatValue())
                .priceMin(promotionPrice)
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
    public ResponseEntity<?> review(Principal principal, UserReviewRequest request) throws IOException {
        //Kiem tra dang nhap
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        //Kiem tra co dat phong hay khong
        if(!booking.getHotel().getId().equals(hotel.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(2020)
                    .message(ErrorMessage.BOOKING_IS_NOT_HOTEL)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        //Kiem tra da checkout chua
        if(!booking.getStatus().equals(EnumBookingStatus.CHECKOUT.name())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(ErrorMessage.BOOKING_NOT_CHECKOUT)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        // Kiểm tra nếu gửi ảnh thì không vượt quá 5
        if (request.getImage() != null && request.getImage().size() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(405)
                    .message(MAXIMUM_5_IMAGE)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }

        Review review = Review.builder()
                .user(user)
                .hotel(hotel)
                .ratingHotel(request.getHotelPoint())
                .ratingRoom(request.getRoomPoint())
                .ratingService(request.getServicePoint())
                .ratingLocation(request.getLocationPoint())
                .comment(request.getComment())
                .build();
        reviewRepository.save(review);
        //List<String> urlImageList = new ArrayList<>();
        // Nếu có ảnh thì upload
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            List<String> urlImageList = cloudinaryService.uploadImages(request.getImage(), "review");
            List<Image> images = urlImageList.stream().map(url -> Image.builder()
                    .url(url)
                    .type("REVIEW")
                    .reviewId(review.getId())
                    .build()).toList();
            imageRepository.saveAll(images);
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.SEND_REVIEW_SUCCESSFULLY)
                .data(reviewMap(review))
                .build());
    }

    @Override
    public ResponseEntity<?> reviewDetail(Long reviewId) {
        ReviewHotelResponse response = reviewMap(reviewRepository.findById(reviewId)
                .orElseThrow(()-> new AppException(ErrorCode.REVIEW_NOT_FOUND)));
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.REVIEW_DETAIL_SUCCESSFULLY)
                .data(response)
                .build());
    }

    private ReviewHotelResponse reviewMap(Review review){
        List<String> urlImages = reviewRepository.findImageReview(review.getId());
        return ReviewHotelResponse.builder()
                .reviewId(review.getId())
                .hotelPoint(review.getRatingHotel())
                .servicePoint(review.getRatingService())
                .roomPoint(review.getRatingRoom())
                .locationPoint(review.getRatingLocation())
                .comment(review.getComment())
                .image(urlImages)
                .build();
    }


    private List<Activity> searchPlaces(String location) {
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

            activity.setName((String) place.getOrDefault("name", "No name"));
            activity.setDescription((String) place.getOrDefault("description", "Không có mô tả nào."));
            activity.setDistance(place.containsKey("distance")
                    ? place.get("distance") + " m"
                    : "Unknown");

            //Get coordinates
            Map<String, Object> geocodes = (Map<String, Object>) place.get("geocodes");
            if (geocodes != null && geocodes.containsKey("main")) {
                Map<String, Object> mainCoords = (Map<String, Object>) geocodes.get("main");
                activity.setLatitude((String) mainCoords.get("latitude").toString());
                activity.setLongitude((String) mainCoords.get("longitude").toString());
            }

            //Get photo
            String photoUrl = getFirstPhotoUrl(fsqId, entity);
            activity.setPhotoUrl(photoUrl);

            //Get rating
            float rating = getPlaceRating(fsqId, entity);
            activity.setRating(rating);

            activities.add(activity);
        }

        return activities;
    }
    private String getFirstPhotoUrl(String fsqId, HttpEntity<String> entity) {
        String url = "https://api.foursquare.com/v3/places/" + fsqId + "/photos";
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        if (response.getBody() != null && !response.getBody().isEmpty()) {
            Map<String, Object> photo = (Map<String, Object>) response.getBody().get(0);
            String prefix = photo.get("prefix").toString();
            String suffix = photo.get("suffix").toString();
            return prefix + "original" + suffix;
        }

        return null; // hoặc URL ảnh mặc định
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
