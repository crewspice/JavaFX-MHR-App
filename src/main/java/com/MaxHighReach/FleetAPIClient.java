package com.MaxHighReach;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;

public class FleetAPIClient {

    private static final String API_BASE = "https://services.spireon.com/v0/rest";
    private static final String API_KEY = Config.SPIREON_TOKEN;

    public static String getAllAssets() throws IOException {
        System.out.println("Get truck assets called");

        URL url = new URL(API_BASE + "/assets");
        HttpURLConnection conn = createConnection(url);

        int responseCode = conn.getResponseCode();
        System.out.println("GET /assets Response Code :: " + responseCode);

        return readResponse(conn, responseCode);
    }

    public static List<AssetContent> getAssetsNearLocation(double lat, double lng, double radiusMiles) throws IOException {
        System.out.printf("Searching assets near (%.5f, %.5f) within %.2f miles%n", lat, lng, radiusMiles);

        String query = String.format("%s/assetsLocationSearch?lat=%.5f&lng=%.5f&radius=%.2f&distanceUnits=MILES",
                API_BASE, lat, lng, radiusMiles);

        URL url = new URL(query);
        HttpURLConnection conn = createConnection(url);

        int responseCode = conn.getResponseCode();
        String json = readResponse(conn, responseCode);

        ObjectMapper mapper = new ObjectMapper();
        AssetLocationResponse response = mapper.readValue(json, AssetLocationResponse.class);
        return response.content;
    }

    private static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String userpass = Config.SPIREON_USR + ":" + Config.SPIREON_PSWD;
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());

        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestProperty("X-Nspire-AppToken", API_KEY);
        conn.setRequestProperty("Accept", "application/json");

        return conn;
    }

    private static String readResponse(HttpURLConnection conn, int responseCode) throws IOException {
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            throw new IOException("API call failed with response code: " + responseCode);
        }
    }

    public static double[] getTruckCoordsByName(String truckName) throws IOException {
        List<AssetContent> assets = getAssetsNearLocation(39.43, -104.93, 100);
    
        for (AssetContent asset : assets) {
            if (asset.assetRef.name != null && asset.assetRef.name.equalsIgnoreCase(truckName)) {
                double foundLat = asset.assetRef.lastLocation.lat;
                double foundLng = asset.assetRef.lastLocation.lng;
                return new double[]{foundLat, foundLng};
            }
        }
    
        throw new IOException("Truck with name '" + truckName + "' not found within radius.");
    }
    

    // JSON classes
    public static class AssetLocationResponse {
        public int total;
        public int count;
        public List<AssetContent> content;
    }

    public static class AssetContent {
        public AssetRef assetRef;
        public double distance;
    }

    public static class AssetRef {
        public String id;
        public String name;
        public String vin;
        public String href;
        public Location lastLocation;
    }

    public static class Location {
        public double lat;
        public double lng;
    }
}
