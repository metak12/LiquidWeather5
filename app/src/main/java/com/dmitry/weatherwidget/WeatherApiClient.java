package com.dmitry.weatherwidget;

import android.content.Context;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApiClient {

    private static final String PREFS_NAME = "WeatherCache";
    private static final String KEY_JSON = "cached_weather_json";
    private static final String KEY_CITY = "cached_city_name";

    public static class WeatherData {
        public double temp;
        public int weatherCode;
        public String conditionText;
        public String cityName;

        public WeatherData(double temp, int weatherCode, String cityName) {
            this.temp = temp;
            this.weatherCode = weatherCode;
            this.conditionText = decodeWeatherCode(weatherCode);
            this.cityName = cityName != null ? cityName : "Моя погода";
        }
    }

    public static WeatherData fetchWeatherSync(Context context) {
        double lat = 55.7522;
        double lon = 37.6156;
        String cityName = "Москва";

        try {
            URL ipUrl = new URL("http://ip-api.com/json/?lang=ru");
            HttpURLConnection ipConn = (HttpURLConnection) ipUrl.openConnection();
            ipConn.setConnectTimeout(3000);
            ipConn.setReadTimeout(3000);
            if (ipConn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(ipConn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject ipObj = new JSONObject(sb.toString());
                if (ipObj.optString("status").equals("success")) {
                    lat = ipObj.getDouble("lat");
                    lon = ipObj.getDouble("lon");
                    cityName = ipObj.optString("city", "Моя погода");
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().putString(KEY_CITY, cityName).apply();
                }
            }
        } catch (Exception ignored) {
            cityName = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_CITY, "Москва");
        }

        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current=temperature_2m,weather_code&timezone=auto";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                String jsonStr = sb.toString();
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(KEY_JSON, jsonStr).apply();

                return parseJson(jsonStr, cityName);
            }
        } catch (Exception e) {
            String cached = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_JSON, null);
            if (cached != null) {
                return parseJson(cached, cityName);
            }
        }
        return null;
    }

    private static WeatherData parseJson(String jsonStr, String cityName) {
        try {
            JSONObject root = new JSONObject(jsonStr);
            JSONObject current = root.getJSONObject("current");
            double temp = current.getDouble("temperature_2m");
            int code = current.getInt("weather_code");
            return new WeatherData(temp, code, cityName);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decodeWeatherCode(int code) {
        switch (code) {
            case 0: return "Ясно ☀️";
            case 1:
            case 2: return "Переменная облачность ⛅";
            case 3: return "Пасмурно ☁️";
            case 51:
            case 53:
            case 55: return "Морось 🌧️";
            case 61:
            case 63: return "Дождь 🌧️";
            case 71:
            case 73:
            case 75: return "Снег ❄️";
            case 95: return "Гроза ⚡";
            default: return "Облачно 🌥️";
        }
    }
}
