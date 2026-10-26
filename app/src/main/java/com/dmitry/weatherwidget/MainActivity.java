package com.dmitry.weatherwidget;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));
        root.setPadding(dp(20), dp(24), dp(20), dp(24));

        TextView title = new TextView(this);
        title.setText("🌤️ Прогноз на 7 дней");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listContainer);

        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrollLp.topMargin = dp(16);
        root.addView(scrollView, scrollLp);

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            String dayName = i == 0 ? "Сегодня" : day.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru"));

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            card.setGravity(Gravity.CENTER_VERTICAL);

            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(dp(16));
            bg.setColor(Color.parseColor("#20FFFFFF"));
            card.setBackground(bg);

            TextView tvDay = new TextView(this);
            tvDay.setText(dayName);
            tvDay.setTextColor(Color.WHITE);
            tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            card.addView(tvDay);

            TextView tvRange = new TextView(this);
            tvRange.setText("+" + (20 + i) + "° / +" + (14 + i) + "°");
            tvRange.setTextColor(Color.parseColor("#B0FFFFFF"));
            tvRange.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            card.addView(tvRange);

            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.bottomMargin = dp(8);
            listContainer.addView(card, cardLp);
        }

        setContentView(root);
    }

    private int dp(int val) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }
}
