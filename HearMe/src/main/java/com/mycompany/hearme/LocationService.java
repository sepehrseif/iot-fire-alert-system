package com.mycompany.hearme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import org.json.JSONObject;

public class LocationService {
    private double lat = 0.0;
    private double lon = 0.0;

    public String getLocationMessage() {
        try {
            String apiUrl = "http://ip-api.com/json/";
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();

            // For Java 20+ Version Please use THIS
            // HttpURLConnection con = (HttpURLConnection)
            // URI.create(apiUrl).toURL().openConnection();

            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                con.disconnect();

                // Parse JSON response
                JSONObject json = new JSONObject(response.toString());
                lat = json.optDouble("lat");
                lon = json.optDouble("lon");

                // Build and return location details in a readable format
                return String.format(
                        Locale.US,
                        "City: %s<br>Region: %s<br>Country: %s<br>Latitude: %.4f<br>Longitude: %.4f",
                        json.optString("city"),
                        json.optString("regionName"),
                        json.optString("country"),
                        lat,
                        lon);
            } else {
                return "Failed to get location";
            }
        } catch (Exception e) {
            return "Error getting location: " + e.getMessage();
        }
    }

    public String getGoogleMapsUrl() {
        return String.format(Locale.US,"https://www.google.com/maps?q=%.4f,%.4f", lat, lon);
    }
}
