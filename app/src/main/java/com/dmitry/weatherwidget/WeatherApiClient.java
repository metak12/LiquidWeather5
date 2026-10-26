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

    public static class WeatherData {
        public double temp;
        public int weatherCode;
        public String conditionText;

        public WeatherData(double temp, int weatherCode) {
            this.temp = temp;
            this.weatherCode = weatherCode;
            this.conditionText = decodeWeatherCode(weatherCode);
        }
    }

    public static WeatherData fetchWeatherSync(Context context) {
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=55.7522&longitude=37.6156&current=temperature_2m,weather_code&timezone=auto";
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
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String jsonStr = sb.toString();
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(KEY_JSON, jsonStr).apply();

                return parseJson(jsonStr);
            }
        } catch (Exception e) {
            String cached = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_JSON, null);
            if (cached != null) {
                return parseJson(cached);
            }
        }
        return null;
    }

    private static WeatherData parseJson(String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);
            JSONObject current = root.getJSONObject("current");
            double temp = current.getDouble("temperature_2m");
            int code = current.getInt("weather_code");
            return new WeatherData(temp, code);
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
