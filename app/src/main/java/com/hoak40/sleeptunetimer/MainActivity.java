package com.hoak40.sleeptunetimer;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(16, 21, 37);
    private static final int SURFACE = Color.rgb(27, 34, 56);
    private static final int SURFACE_ALT = Color.rgb(38, 46, 72);
    private static final int TEXT = Color.rgb(241, 243, 255);
    private static final int MUTED = Color.rgb(174, 185, 210);
    private static final int ACCENT = Color.rgb(170, 156, 255);
    private static final int GREEN = Color.rgb(116, 231, 207);
    private static final int GAP = 12;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView countdownText, stateText, helperText, permissionText;
    private Button startButton, cancelButton;
    private Switch muteSwitch;
    private EditText customInput;
    private Button[] presetButtons;
    private int selectedMinutes = 30;
    private final int[] presets = {15, 30, 45, 60, 90, 120};

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            refreshTimerUi();
            handler.postDelayed(this, 1_000L);
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        buildUi();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        ticker.run();
        refreshPermissions();
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable shape(int color, float radiusDp) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(color);
        result.setCornerRadius(dp(radiusDp));
        return result;
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String str, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(str);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private Button button(String value, int fill, int ink) {
        Button btn = new Button(this);
        btn.setAllCaps(false);
        btn.setText(value);
        btn.setTextColor(ink);
        btn.setTextSize(15);
        btn.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        btn.setBackground(shape(fill, 15));
        btn.setMinimumHeight(0);
        btn.setPadding(dp(10), 0, dp(10), 0);
        return btn;
    }

    private void add(LinearLayout parent, View view, int heightDp, int marginTopDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightDp < 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : dp(heightDp));
        params.topMargin = dp(marginTopDp);
        parent.addView(view, params);
    }

    private LinearLayout panel(LinearLayout parent, int topMarginDp) {
        LinearLayout inside = vertical();
        inside.setPadding(dp(20), dp(20), dp(20), dp(20));
        inside.setBackground(shape(SURFACE, 22));
        add(parent, inside, -1, topMarginDp);
        return inside;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }
        LinearLayout root = vertical();
        root.setPadding(dp(20), dp(25), dp(20), dp(30));
        scroll.addView(root);
        setContentView(scroll);

        TextView brand = text("☾  SleepTune Timer", 28, TEXT, true);
        add(root, brand, -1, 0);
        add(root, text("Nghe nhạc và ngủ ngon. Nhạc tự dừng đúng hẹn.", 14, MUTED, false), -1, 8);

        LinearLayout timerPanel = panel(root, 22);
        TextView timerLabel = text("THỜI GIAN CÒN LẠI", 12, GREEN, true);
        timerLabel.setLetterSpacing(0.13f);
        add(timerPanel, timerLabel, -1, 0);

        countdownText = text("30:00", 56, TEXT, true);
        countdownText.setGravity(Gravity.CENTER);
        countdownText.setFontFeatureSettings("tnum");
        add(timerPanel, countdownText, 78, 11);
        stateText = text("Chưa bật hẹn giờ", 16, MUTED, true);
        stateText.setGravity(Gravity.CENTER);
        add(timerPanel, stateText, -1, 3);
        helperText = text("Chọn số phút, sau đó bấm Bắt đầu", 13, MUTED, false);
        helperText.setGravity(Gravity.CENTER);
        add(timerPanel, helperText, -1, 8);

        TextView choose = text("Chọn thời gian", 19, TEXT, true);
        add(root, choose, -1, 25);

        LinearLayout presetArea = vertical();
        presetButtons = new Button[presets.length];
        for (int row = 0; row < 2; row++) {
            LinearLayout group = new LinearLayout(this);
            group.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams groupP = new LinearLayout.LayoutParams(-1, dp(56));
            groupP.topMargin = dp(row == 0 ? 13 : GAP);
            presetArea.addView(group, groupP);
            for (int col = 0; col < 3; col++) {
                final int index = row * 3 + col;
                int m = presets[index];
                Button b = button(m + " phút", SURFACE_ALT, TEXT);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -1, 1);
                if (col > 0) p.leftMargin = dp(10);
                group.addView(b, p);
                b.setOnClickListener(v -> {
                    selectedMinutes = presets[index];
                    customInput.setText("");
                    refreshPresetButtons();
                    refreshTimerUi();
                });
                presetButtons[index] = b;
            }
        }
        add(root, presetArea, -1, 0);
        refreshPresetButtons();

        LinearLayout custom = new LinearLayout(this);
        custom.setOrientation(LinearLayout.HORIZONTAL);
        customInput = new EditText(this);
        customInput.setSingleLine(true);
        customInput.setTextColor(TEXT);
        customInput.setHintTextColor(MUTED);
        customInput.setTextSize(16);
        customInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        customInput.setHint("Phút tùy chỉnh (1–720)");
        customInput.setPadding(dp(15), 0, dp(10), 0);
        customInput.setBackground(shape(SURFACE, 14));
        custom.addView(customInput, new LinearLayout.LayoutParams(0, dp(56), 1));
        Button apply = button("Chọn", SURFACE_ALT, GREEN);
        LinearLayout.LayoutParams applyP = new LinearLayout.LayoutParams(dp(84), dp(56));
        applyP.leftMargin = dp(10);
        custom.addView(apply, applyP);
        apply.setOnClickListener(v -> {
            try {
                int value = Integer.parseInt(customInput.getText().toString());
                if (value < 1 || value > 720) throw new NumberFormatException();
                selectedMinutes = value;
                refreshPresetButtons();
                refreshTimerUi();
                customInput.clearFocus();
                Toast.makeText(this, "Đã chọn " + value + " phút", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nhập số phút từ 1 đến 720", Toast.LENGTH_SHORT).show();
            }
        });
        add(root, custom, 56, 13);

        LinearLayout options = panel(root, 19);
        muteSwitch = new Switch(this);
        muteSwitch.setText("Đặt âm lượng media về 0 khi hết giờ");
        muteSwitch.setTextColor(TEXT);
        muteSwitch.setTextSize(14);
        muteSwitch.setChecked(TimerScheduler.shouldMute(this));
        add(options, muteSwitch, -1, 0);
        add(options, text("Tùy chọn này giúp im tiếng với app không nhận Pause. Bạn cần tự tăng âm lượng lại sau đó.",
                12, MUTED, false), -1, 9);

        startButton = button("▶  Bắt đầu hẹn giờ", ACCENT, BG);
        add(root, startButton, 59, 21);
        startButton.setOnClickListener(v -> {
            try {
                TimerScheduler.start(this, selectedMinutes, muteSwitch.isChecked());
                refreshTimerUi();
                Toast.makeText(this, "Đã bật hẹn giờ", Toast.LENGTH_SHORT).show();
                if (!MediaStopper.hasNotificationAccess(this)) {
                    Toast.makeText(this, "Nên bật quyền truy cập thông báo để dừng nhạc tốt hơn", Toast.LENGTH_LONG).show();
                }
            } catch (RuntimeException e) {
                Toast.makeText(this, "Không thể đặt hẹn giờ. Kiểm tra quyền hẹn giờ.", Toast.LENGTH_LONG).show();
            }
        });
        cancelButton = button("Hủy hẹn giờ", SURFACE_ALT, TEXT);
        add(root, cancelButton, 52, 10);
        cancelButton.setOnClickListener(v -> {
            TimerScheduler.cancel(this);
            refreshTimerUi();
        });

        LinearLayout permissions = panel(root, 24);
        add(permissions, text("Quyền ứng dụng", 18, TEXT, true), -1, 0);
        permissionText = text("", 13, MUTED, false);
        add(permissions, permissionText, -1, 12);
        Button listener = button("Cấp quyền điều khiển nhạc", SURFACE_ALT, TEXT);
        add(permissions, listener, 48, 14);
        listener.setOnClickListener(v -> safeOpenSettings(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Button exact = button("Bật hẹn giờ chính xác", SURFACE_ALT, TEXT);
            add(permissions, exact, 48, 10);
            exact.setOnClickListener(v -> {
                Intent settings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName()));
                safeOpenSettings(settings);
            });
        }
        if (Build.VERSION.SDK_INT >= 33) {
            Button notifyButton = button("Cho phép thông báo khi kết thúc", SURFACE_ALT, TEXT);
            add(permissions, notifyButton, 48, 10);
            notifyButton.setOnClickListener(v -> requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101));
        }
        add(root, text("Không cần Internet • Không tắt màn hình • Không đóng cưỡng bức ứng dụng", 12, MUTED, false), -1, 18);
    }

    private void safeOpenSettings(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
        }
    }

    private void refreshPresetButtons() {
        if (presetButtons == null) return;
        for (int i = 0; i < presets.length; i++) {
            boolean selected = selectedMinutes == presets[i];
            presetButtons[i].setBackground(shape(selected ? ACCENT : SURFACE_ALT, 14));
            presetButtons[i].setTextColor(selected ? BG : TEXT);
        }
    }

    private void refreshTimerUi() {
        if (countdownText == null) return;
        long end = TimerScheduler.getDeadline(this);
        long remaining = end > 0 ? Math.max(0L, end - System.currentTimeMillis()) : 0L;
        boolean active = end > 0 && remaining > 0;
        if (active) {
            long seconds = (remaining + 999L) / 1000L;
            countdownText.setText(String.format(Locale.US, "%02d:%02d:%02d",
                    seconds / 3600, (seconds / 60) % 60, seconds % 60));
            countdownText.setTextColor(GREEN);
            stateText.setText("ĐANG HẸN GIỜ");
            stateText.setTextColor(GREEN);
            helperText.setText("Tự dừng nhạc lúc " + DateFormat.getTimeInstance(DateFormat.SHORT)
                    .format(new Date(end)));
        } else {
            countdownText.setText(String.format(Locale.US, "%02d:%02d", selectedMinutes, 0));
            countdownText.setTextColor(TEXT);
            stateText.setText(end > 0 ? "Đang hoàn tất hẹn giờ..." : "Sẵn sàng hẹn giờ");
            stateText.setTextColor(MUTED);
            helperText.setText("Chọn số phút, sau đó bấm Bắt đầu");
        }
        cancelButton.setEnabled(end > 0);
        cancelButton.setAlpha(end > 0 ? 1f : 0.45f);
        startButton.setText(active ? "↻  Đặt lại hẹn giờ" : "▶  Bắt đầu hẹn giờ");
    }

    private void refreshPermissions() {
        if (permissionText == null) return;
        boolean listener = MediaStopper.hasNotificationAccess(this);
        boolean exact = TimerScheduler.canUseExactAlarms(this);
        permissionText.setText((listener ? "✓" : "○") + " Điều khiển nhạc: "
                + (listener ? "Đã cấp" : "Chưa cấp") + "\n"
                + (exact ? "✓" : "○") + " Hẹn giờ chính xác: "
                + (exact ? "Đã bật" : "Chưa bật, sẽ dùng hẹn giờ gần đúng"));
    }
}
