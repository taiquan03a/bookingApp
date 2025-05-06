package com.ptit.booking.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeocodingApp {

    // Thay bằng API Key của bạn
    private static final String API_KEY = "1xR36Z0rKiXkXcewt25CPlN0d6Ojj4uH0zAQVKAL";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Nhập địa chỉ: ");
        String address = scanner.nextLine();
        scanner.close();

        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String apiUrl = "https://rsapi.goong.io/Geocode?address=" + encodedAddress + "&api_key=" + API_KEY;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON
            JSONObject json = new JSONObject(response.toString());
            JSONArray results = json.getJSONArray("results");

            if (results.length() > 0) {
                JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                System.out.println("Tọa độ:");
                System.out.println("Latitude: " + lat);
                System.out.println("Longitude: " + lng);
            } else {
                System.out.println("Không tìm thấy tọa độ cho địa chỉ này.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Đã xảy ra lỗi khi lấy tọa độ.");
        }
    }
}
