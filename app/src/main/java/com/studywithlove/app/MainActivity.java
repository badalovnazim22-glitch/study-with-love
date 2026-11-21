package com.studywithlove.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends Activity {
    private final int BG = 0xFFFFFBFC, PINK = 0xFFD96C91, PALE = 0xFFFFF0F4, DARK = 0xFF553B46, MUTED = 0xFF927D86;
    private LinearLayout root;
    private SharedPreferences prefs;
    private String selectedTopic = null, selectedChapter = null;
    private final Map<String, List<String>> chapters = new LinkedHashMap<>();
    private final String[] messages = {"Ты большая умничка! ❤️", "Я верю в тебя, у тебя всё получится! 🌸", "Не сдавайся: маленькие шаги приводят к большим результатам! 💗", "Ты справишься! И не забывай улыбаться ❤️"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFFFFF3F6);
        getWindow().setNavigationBarColor(0xFFFFF3F6);
        if (Build.VERSION.SDK_INT >= 23) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        prefs = getSharedPreferences("study_progress", MODE_PRIVATE);
        loadChapters();
        scheduleReminders(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 20);
        showHome();
    }

    private void loadChapters() {
        add("1️⃣ Натуральные числа и действия с ними", "1.1. Натуральные числа", "1.2. Сравнение и упорядочивание", "1.3. Округление натуральных чисел", "1.4. Сложение и вычитание натуральных чисел", "1.5. Квадрат и куб натурального числа", "1.6. Умножение и деление натуральных чисел", "1.7. Числовые выражения", "1.8. Делители и кратные числа");
        add("2️⃣ Обыкновенные дроби", "2.1. Правильные и неправильные дроби", "2.2. Сравнение и упорядочивание", "2.3. Сложение и вычитание дробей с разными знаменателями", "2.4. Сложение смешанных чисел", "2.5. Вычитание смешанных чисел", "2.6. Умножение обыкновенных дробей", "2.7. Умножение смешанных чисел", "2.8. Деление обыкновенных дробей", "2.9. Деление смешанных чисел", "2.10. Нахождение части числа и числа по его части");
        add("3️⃣ Десятичные дроби", "3.1. Десятичные дроби", "3.2. Сравнение и упорядочивание", "3.3. Округление десятичных дробей", "3.4. Перевод обыкновенной дроби в десятичную и наоборот", "3.5. Сложение и вычитание десятичных дробей", "3.6. Умножение и деление десятичных дробей на степени числа 10", "3.7. Умножение десятичной дроби на натуральное число", "3.8. Умножение десятичных дробей", "3.9. Деление десятичной дроби на натуральное число", "3.10. Деление числа на десятичную дробь", "3.11. Действия над обыкновенными и десятичными дробями");
        add("4️⃣ Процент", "4.1. Процент, обыкновенная дробь, десятичная дробь", "4.2. Процент от числа", "4.3. Нахождение числа по проценту", "4.4. Увеличение и уменьшение величины на определённый процент");
    }
    private void add(String name, String... topics) { List<String> list = new ArrayList<>(); for (String t : topics) list.add(t); chapters.put(name, list); }

    public static void scheduleReminders(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        int[] hours = {10, 15, 19, 21};
        String[] texts = {"🌸 Доброе утро! Пора заниматься математикой!", "📚 Время повторить учебные темы. У тебя всё получится!", "✅ Не забудь проверить задания и закрепить материал!", "💌 Ты сегодня большая умничка! Не забудь отдохнуть ❤️"};
        for (int i = 0; i < hours.length; i++) {
            Intent intent = new Intent(context, ReminderReceiver.class).putExtra("message", texts[i]).putExtra("id", i + 1);
            PendingIntent pi = PendingIntent.getBroadcast(context, 300 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Baku"));
            c.set(Calendar.HOUR_OF_DAY, hours[i]); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() <= System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR, 1);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }
    }

    private void base(String title, String subtitle) {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22), dp(20), dp(22), dp(28)); root.setBackgroundColor(BG);
        scroll.addView(root); setContentView(scroll);
        TextView brand = text("STUDY WITH LOVE  🌷", 12, PINK, true); brand.setLetterSpacing(0.10f); root.addView(brand, lp(-1, -2, 0, 0, 0, 8));
        TextView h = text(title, 27, DARK, true); root.addView(h, lp(-1, -2, 0, 0, 0, 5));
        TextView s = text(subtitle, 14, MUTED, false); root.addView(s, lp(-1, -2, 0, 0, 0, 18));
    }
    private void showHome() {
        base("Привет, Назка! ❤️", "Твой маленький помощник в учёбе. Каждый шаг — уже успех.");
        int done = prefs.getInt("done_count", 0);
        LinearLayout progress = card();
        addText(progress, "🌸  ТВОЙ ПРОГРЕСС", 12, PINK, true);
        addText(progress, "Завершено: " + done + " " + word(done), 21, DARK, true);
        addText(progress, "Продолжай в своём темпе — у тебя всё получится!", 13, MUTED, false);
        root.addView(progress, lp(-1, -2, 0, 0, 0, 18));
        action("📐  Математика", "Выбрать раздел и тему", this::showChapters);
        action("📚  Повторить темы", "Вернуться к списку уроков", this::showChapters);
        action("✅  Я позанималась", "Отметить выбранную тему", this::markDone);
        action("💌  Послание", "Немного поддержки на сегодня", () -> { String m = messages[new Random().nextInt(messages.length)]; base("Послание для тебя 💌", "Маленькое напоминание:"); addText(root, m, 22, PINK, true); backHome(); });
        LinearLayout note = card(); addText(note, "⏰  НАПОМИНАНИЯ", 13, PINK, true); addText(note, "Каждый день по времени Баку: 10:00 · 15:00 · 19:00 · 21:00", 14, DARK, false); root.addView(note, lp(-1, -2, 0, 18, 0, 0));
    }
    private String word(int n) { int x=n%10, y=n%100; if(x==1 && y!=11)return "урок"; if(x>=2&&x<=4&&(y<12||y>14))return "урока"; return "уроков"; }
    private void showChapters() {
        base("Выбери раздел 📐", "Все темы из твоего учебного плана");
        for (String chapter : chapters.keySet()) action(chapter, chapters.get(chapter).size() + " учебных тем", () -> { selectedChapter = chapter; showTopics(chapter); });
        backHome();
    }
    private void showTopics(String chapter) {
        base(chapter, "Выбери тему, которую хочешь изучить:");
        for (String topic : chapters.get(chapter)) {
            boolean done = prefs.getBoolean("done_" + topic, false);
            action((done ? "✓  " : "♡  ") + topic, done ? "Уже выполнено — можно повторить" : "Нажми, чтобы открыть урок", () -> { selectedTopic = topic; showTopic(topic); });
        }
        Button back = button("← К разделам", false); back.setOnClickListener(v -> showChapters()); root.addView(back, lp(-1, 52, 0, 12, 0, 0));
    }
    private void showTopic(String topic) {
        base("Твоя тема 📖", "Сегодня занимаемся с любовью к знаниям");
        LinearLayout box = card(); addText(box, topic, 20, DARK, true); addText(box, "Открой соответствующий урок в учебнике, изучи материал и реши задания. Когда закончишь, возвращайся сюда и отметь занятие.", 15, MUTED, false); root.addView(box, lp(-1, -2, 0, 0, 0, 16));
        Button done = button("✅  Я позанималась", true); done.setOnClickListener(v -> markDone()); root.addView(done, lp(-1, 54, 0, 0, 0, 10));
        Button back = button("← К темам", false); back.setOnClickListener(v -> showTopics(selectedChapter)); root.addView(back, lp(-1, 50, 0, 0, 0, 0));
    }
    private void markDone() {
        if (selectedTopic != null && !prefs.getBoolean("done_" + selectedTopic, false)) {
            prefs.edit().putBoolean("done_" + selectedTopic, true).putInt("done_count", prefs.getInt("done_count", 0) + 1).apply();
            Toast.makeText(this, "Умничка! Тема отмечена ❤️", Toast.LENGTH_LONG).show();
        } else Toast.makeText(this, "Умничка! Продолжай заниматься ❤️", Toast.LENGTH_LONG).show();
        showHome();
    }
    private void backHome() { Button b = button("← Главное меню", false); b.setOnClickListener(v -> showHome()); root.addView(b, lp(-1, 50, 0, 18, 0, 0)); }
    private void action(String title, String sub, OnAction run) {
        LinearLayout c = card(); c.setOrientation(LinearLayout.VERTICAL); addText(c, title, 17, DARK, true); addText(c, sub, 13, MUTED, false); c.setOnClickListener(v -> run.go()); root.addView(c, lp(-1, -2, 0, 0, 0, 10));
    }
    private LinearLayout card() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16), dp(15), dp(16), dp(15)); l.setBackground(shape(0xFFFFFFFF, 20, 0xFFFFE2EA)); return l; }
    private void addText(LinearLayout parent, String value, int size, int color, boolean bold) { TextView t = text(value, size, color, bold); parent.addView(t, lp(-1, -2, 0, 0, 0, 5)); }
    private TextView text(String value, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); t.setGravity(Gravity.CENTER_VERTICAL); return t; }
    private Button button(String label, boolean primary) { Button b = new Button(this); b.setText(label); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(primary ? 0xFFFFFFFF : PINK); b.setBackground(shape(primary ? PINK : PALE, 18, primary ? PINK : 0xFFFFDCE6)); return b; }
    private interface OnAction { void go(); }
    private GradientDrawable shape(int color, int radius, int stroke) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); d.setStroke(dp(1), stroke); return d; }
    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h)); p.setMargins(dp(l), dp(t), dp(r), dp(b)); return p; }
    private int dp(int x) { return (int)(x * getResources().getDisplayMetrics().density + 0.5f); }
}
