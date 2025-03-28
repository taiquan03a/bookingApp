//package com.ptit.booking.service.Impl;
//
//import com.ptit.booking.service.FoursquareService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class FoursquareServiceImpl implements FoursquareService {
//
//    @Value("${foursquare.api.key}")
//    private String apiKey;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    @Override
//    public ResponseEntity<?> searchPlaces(String location) {
//        // 1️⃣ Gọi API để lấy tọa độ của địa điểm
//        String geocodeUrl = "https://nominatim.openstreetmap.org/search";
//        UriComponentsBuilder geoBuilder = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
//                .queryParam("q", location)
//                .queryParam("format", "json");
//
//        ResponseEntity<List> geoResponse = restTemplate.exchange(
//                geoBuilder.toUriString(), HttpMethod.GET, null, List.class);
//
//        if (geoResponse.getBody() == null || geoResponse.getBody().isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa điểm");
//        }
//
//        Map<String, Object> geoData = (Map<String, Object>) geoResponse.getBody().get(0);
//        String latitude = geoData.get("lat").toString();
//        String longitude = geoData.get("lon").toString();
//        String ll = latitude + "," + longitude;
//
//        // 2️⃣ Gọi API Foursquare để lấy danh sách địa điểm
//        String searchUrl = "https://api.foursquare.com/v3/places/search";
//        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(searchUrl)
//                .queryParam("ll", ll)
//                .queryParam("radius", "5000")
//                .queryParam("limit", "5");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", "application/json");
//        headers.set("Authorization", apiKey);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<Map> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, Map.class);
//
//        if (response.getBody() == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa điểm");
//        }
//
//        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
//
//        // 3️⃣ Lấy ảnh cho từng địa điểm
//        for (Map<String, Object> place : results) {
//            String fsqId = place.get("fsq_id").toString();
//            String photoUrl = "https://api.foursquare.com/v3/places/" + fsqId + "/photos";
//            ResponseEntity<List> photoResponse = restTemplate.exchange(photoUrl, HttpMethod.GET, entity, List.class);
//
//            if (photoResponse.getBody() != null && !photoResponse.getBody().isEmpty()) {
//                Map<String, Object> photo = (Map<String, Object>) photoResponse.getBody().get(0);
//                String imageUrl = photo.get("prefix").toString() + "original" + photo.get("suffix").toString();
//                place.put("image_url", imageUrl);
//            } else {
//                place.put("image_url", "No image available");
//            }
//        }
//
//        return ResponseEntity.ok(results);
//    }
//}
//
