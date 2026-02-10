package com.mycompany.hearme;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class EmailSender implements WebSocketMessageHandler {
    private static final Logger logger = Logger.getLogger(EmailSender.class.getName());
    private static final String API_KEY = System.getenv("SENDINBLUE_API_KEY");
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    private final LocationService locationService;

    public EmailSender() {
        this.locationService = new LocationService();
    }

    @Override
    public void handleMessage(String message) {
        // Only proceed if the message matches the help request command (case-insensitive)
        if ("Send-Help-Request".equalsIgnoreCase(message.trim())) {
            logger.info("🛠️ Sending Help Request...");

            try {
                String locationMessage = locationService.getLocationMessage();
                String mapUrl = locationService.getGoogleMapsUrl();
                String jsonPayload = buildEmailJson(locationMessage, mapUrl);

                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("api-key", API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                }

                // Get response code to check if email was accepted by the API
                int responseCode = conn.getResponseCode();
                logger.info("Response Code: " + responseCode);

                // HTTP 201 indicates the email was successfully accepted by the server
                if (responseCode == 201) {
                    logger.info("✅ Email sent successfully.");
                } else {
                    logger.warning("❌ Failed to send email.");
                    try (InputStream errorStream = conn.getErrorStream()) {
                        if (errorStream != null) {
                            String errorResponse = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                            logger.warning("Error response: " + errorResponse);
                        }
                    }
                }

                conn.disconnect();
            } catch (Exception e) {
                logger.severe("Exception while sending email: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

private String buildEmailJson(String locationMessage, String mapUrl) {
    return """
        {
            "sender": {
                "name": "Abulfazl",
                "email": "abolfazlm7831@gmail.com"
            },
            "to": [
                {
                    "email": "abulfazl1363@gmail.com",
                    "name": "Department"
                }
            ],
            "subject": "Urgent Help Request",
            "htmlContent": "<html><body><h1>🚨 Help Request</h1><p>This message was sent by someone requesting urgent assistance.</p><p><strong>Location:</strong></p><p>%s</p><p><a href='%s' target='_blank'>📍 View Location on Google Maps</a></p><hr><p>Please take appropriate action as soon as possible.</p></body></html>"
        }
        """.formatted(locationMessage, mapUrl);
    }
}    



