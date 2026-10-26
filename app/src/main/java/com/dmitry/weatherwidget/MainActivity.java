package com.dmitry.weatherwidget;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private LinearLayout listContainer;
    private TextView subHeaderDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(0, 0);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        FrameLayout fullScreenRoot = new FrameLayout(this);
        fullScreenRoot.setBackgroundColor(Color.TRANSPARENT);
        fullScreenRoot.setOnClickListener(v -> finishWithAnimation());

        LinearLayout rootCard = new LinearLayout(this);
        rootCard.setOrientation(LinearLayout.VERTICAL);
        rootCard.setBackgroundResource(R.drawable.popup_bg);
        rootCard.setClickable(true);
        int padH = dp(18);
        int padV = dp(16);
        rootCard.setPadding(padH, padV, padH, padV);

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                (int) (dm.widthPixels * 0.92f), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.gravity = Gravity.CENTER;
        fullScreenRoot.addView(rootCard, cardLp);

        // Header
        FrameLayout headerRow = new FrameLayout(this);

        TextView iconBtn = new TextView(this);
        iconBtn.setText("🌤️");
        iconBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        iconBtn.setGravity(Gravity.CENTER);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setCornerRadius(dp(18));
        iconBg.setColor(Color.parseColor("#25FFFFFF"));
        iconBg.setStroke(dp(1), Color.parseColor("#50FFFFFF"));
        iconBtn.setBackground(iconBg);
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(dp(36), dp(36));
        iconLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        headerRow.addView(iconBtn, iconLp);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setGravity(Gravity.CENTER);

        TextView header = new TextView(this);
        header.setText("Погода");
        header.setTextColor(Color.WHITE);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        header.setTypeface(null, Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        titleBox.addView(header);

        subHeaderDate = new TextView(this);
        subHeaderDate.setTextColor(Color.parseColor("#B0FFFFFF"));
        subHeaderDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        subHeaderDate.setGravity(Gravity.CENTER);
        titleBox.addView(subHeaderDate);

        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.gravity = Gravity.CENTER;
        headerRow.addView(titleBox, titleLp);

        TextView refreshBtn = new TextView(this);
        refreshBtn.setText("🔄");
        refreshBtn.setTextColor(Color.WHITE);
        refreshBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        refreshBtn.setGravity(Gravity.CENTER);
        GradientDrawable refBg = new GradientDrawable();
        refBg.setCornerRadius(dp(18));
        refBg.setColor(Color.parseColor("#25FFFFFF"));
        refBg.setStroke(dp(1), Color.parseColor("#50FFFFFF"));
        refreshBtn.setBackground(refBg);
        refreshBtn.setOnClickListener(v -> loadWeatherData());

        FrameLayout.LayoutParams refLp = new FrameLayout.LayoutParams(dp(36), dp(36));
        refLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        headerRow.addView(refreshBtn, refLp);

        rootCard.addView(headerRow);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);
        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listContainer);

        int maxListHeight = (int) (dm.heightPixels * 0.48f);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, maxListHeight);
        scrollLp.topMargin = dp(12);
        rootCard.addView(scrollView, scrollLp);

        TextView hintText = new TextView(this);
        hintText.setText("Реальный прогноз • Open-Meteo API");
        hintText.setTextColor(Color.parseColor("#80FFFFFF"));
        hintText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        hintText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hintLp.topMargin = dp(8);
        rootCard.addView(hintText, hintLp);

        setContentView(fullScreenRoot);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.dimAmount = 0.40f;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(wlp);
        }

        loadWeatherData();
    }

    private void loadWeatherData() {
        subHeaderDate.setText("Загрузка...");
        listContainer.removeAllViews();

        Executors.newSingleThreadExecutor().execute(() -> {
            WeatherApiClient.WeatherData data = WeatherApiClient.fetchWeatherSync(this);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (data != null) {
                    subHeaderDate.setText(data.cityName + " • " + String.format("%.0f°C", data.temp));
                    renderForecast(data);
                } else {
                    subHeaderDate.setText("Ошибка загрузки");
                }
            });
        });
    }

    private void renderForecast(WeatherApiClient.WeatherData data) {
        listContainer.removeAllViews();
        for (int i = 0; i < data.dailyForecasts.size(); i++) {
            WeatherApiClient.DayForecast day = data.dailyForecasts.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int pH = dp(14);
            int pV = dp(10);
            row.setPadding(pH, pV, pH, pV);

            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(dp(16));
            rowBg.setColor(Color.parseColor(i == 0 ? "#38FFFFFF" : "#18FFFFFF"));
            row.setBackground(rowBg);

            TextView numBadge = new TextView(this);
            numBadge.setText(getWeatherEmoji(day.weatherCode));
            numBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            numBadge.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(dp(28), dp(28));
            badgeLp.rightMargin = dp(10);
            row.addView(numBadge, badgeLp);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

            TextView title = new TextView(this);
            title.setText(day.dayOfWeek);
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            title.setTypeface(null, i == 0 ? Typeface.BOLD : Typeface.NORMAL);
            textLayout.addView(title);

            TextView cond = new TextView(this);
            cond.setText(day.conditionText);
            cond.setTextColor(Color.parseColor("#B0FFFFFF"));
            cond.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            textLayout.addView(cond);

            row.addView(textLayout, textLp);

            TextView temp = new TextView(this);
            temp.setText(String.format("+%.0f° / +%.0f°", day.maxTemp, day.minTemp));
            temp.setTextColor(Color.WHITE);
            temp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            temp.setTypeface(null, Typeface.BOLD);
            row.addView(temp);

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(6);
            listContainer.addView(row, rowLp);
        }
    }

    private String getWeatherEmoji(int code) {
        switch (code) {
            case 0: return "☀️";
            case 1:
            case 2: return "⛅";
            case 3: return "☁️";
            case 51:
            case 53:
            case 55:
            case 61:
            case 63: return "🌧️";
            case 71:
            case 73:
            case 75: return "❄️";
            case 95: return "⚡";
            default: return "🌥️";
        }
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(0, 0);
    }

    private int dp(int val) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }
}
