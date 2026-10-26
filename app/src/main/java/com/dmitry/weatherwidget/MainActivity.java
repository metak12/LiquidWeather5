package com.dmitry.weatherwidget;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(0, 0);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        FrameLayout fullScreenRoot = new FrameLayout(this);
        fullScreenRoot.setBackgroundColor(Color.TRANSPARENT);
        fullScreenRoot.setOnClickListener(v -> finish());

        LinearLayout rootCard = new LinearLayout(this);
        rootCard.setOrientation(LinearLayout.VERTICAL);
        
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setCornerRadius(dp(28));
        cardBg.setColor(Color.parseColor("#CC1E293B"));
        cardBg.setStroke(dp(1), Color.parseColor("#55FFFFFF"));
        rootCard.setBackground(cardBg);
        
        rootCard.setClickable(true);
        int pad = dp(20);
        rootCard.setPadding(pad, pad, pad, pad);

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                (int) (dm.widthPixels * 0.92f), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.gravity = Gravity.CENTER;
        fullScreenRoot.addView(rootCard, cardLp);

        TextView title = new TextView(this);
        title.setText("🌤️ Прогноз на 7 дней");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        rootCard.addView(title);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);
        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listContainer);

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            String dayName = i == 0 ? "Сегодня" : day.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru"));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(14), dp(12), dp(14), dp(12));
            row.setGravity(Gravity.CENTER_VERTICAL);

            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(dp(16));
            rowBg.setColor(Color.parseColor("#18FFFFFF"));
            row.setBackground(rowBg);

            TextView tvDay = new TextView(this);
            tvDay.setText(dayName);
            tvDay.setTextColor(Color.WHITE);
            tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tvDay);

            TextView tvRange = new TextView(this);
            tvRange.setText("+" + (22 - i % 3) + "° / +" + (15 - i % 2) + "°");
            tvRange.setTextColor(Color.parseColor("#CCFFFFFF"));
            tvRange.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvRange.setTypeface(null, Typeface.BOLD);
            row.addView(tvRange);

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.topMargin = dp(6);
            listContainer.addView(row, rowLp);
        }

        int maxHeight = (int) (dm.heightPixels * 0.5f);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, maxHeight);
        scrollLp.topMargin = dp(14);
        rootCard.addView(scrollView, scrollLp);

        setContentView(fullScreenRoot);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.dimAmount = 0.45f;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(wlp);
        }
    }

    private int dp(int val) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }
}
