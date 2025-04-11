package com.MaxHighReach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpireonAPIClient {

    private static final String SPIREON_TOKEN = "e2cfc07a-255a-4677-8f4f-07b8ae7149e8";
    private static final String BASE_URL = "https://api.nspirefleet.com/api"; // Change if your base is different


    public class FleetAPIClient {

        private static final String API_BASE = "https://api.nspirefleet.com"; // example domain
        private static final String API_KEY = Config.SPIREON_TOKEN; // or use OAuth token if needed
    
        public static String getAllAssets() throws IOException {
            URL url = new URL(API_BASE + "/assets");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
    
            int responseCode = conn.getResponseCode();
            System.out.println("GET /assets Response Code :: " + responseCode);
    
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                );
                String inputLine;
                StringBuilder response = new StringBuilder();
    
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
    
                return response.toString();
            } else {
                throw new IOException("GET /assets failed: " + responseCode);
            }
        }
    }

}
