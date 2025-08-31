package com.sist.baemin.direction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DirectionsService {

    @Value("${naver.directions.api.url}")
    private String apiUrl;

    @Value("${naver.directions.api.key.id}")
    private String apiKeyId;

    @Value("${naver.directions.api.key}")
    private String apiKey;

    public String getDuration(String start, String goal) {
        System.out.println("=== DirectionsService.getDuration() called ===");
        System.out.println("Start coords: " + start);
        System.out.println("Goal coords: " + goal);
        
        try {
            // Build the URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("start", start)
                    .queryParam("goal", goal)
                    .queryParam("option", "trafast"); // You can change this option as needed

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", apiKeyId);
            headers.set("X-NCP-APIGW-API-KEY", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the API call
            RestTemplate restTemplate = new RestTemplate();
            System.out.println("Making API call to: " + builder.toUriString());
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);

            System.out.println("API response status: " + response.getStatusCode());
            System.out.println("API response body: " + response.getBody());

            // Parse the response to get duration
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.has("route") && root.get("route").has("trafast")) {
                JsonNode trafast = root.get("route").get("trafast").get(0);
                if (trafast.has("summary")) {
                    JsonNode summary = trafast.get("summary");
                    if (summary.has("duration")) {
                        int durationInMillis = summary.get("duration").asInt();
                        String formattedDuration = formatDuration(durationInMillis);
                        System.out.println("Formatted duration: " + formattedDuration);
                        System.out.println("=== DirectionsService.getDuration() completed successfully ===");
                        return formattedDuration;
                    }
                }
            }
        } catch (Exception e) {
            // Log the error or handle it as needed
            System.out.println("Error in DirectionsService.getDuration(): " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== DirectionsService.getDuration() completed with default value ===");
        return "정보 없음";
    }

    private String formatDuration(int duration) {
        int totalSeconds = duration / 1000;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%d시간 %d분", hours, minutes);
        } else {
            return String.format("%d분", minutes);
        }
    }
}