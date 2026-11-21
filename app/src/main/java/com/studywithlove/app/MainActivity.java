package com.studywithlove.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends Activity {
    private final int BG = 0xFFFFFBFC, PINK = 0xFFD96C91, PALE = 0xFFFFF0F4, DARK = 0xFF553B46, MUTED = 0xFF927D86;
    private LinearLayout root;
    private SharedPreferences prefs;
    private String selectedTopic = null, selectedChapter = null;
    private static final String PREFS = "study_progress";
    private static final String[] DAY_NAMES = {"Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private final List<String> reminderTimes = new ArrayList<>();
    private final List<CheckBox> dayChecks = new ArrayList<>();
    private final Map<String, List<String>> chapters = new LinkedHashMap<>();
    private final String[] messages = {"Ты большая умничка! ❤️", "Я верю в тебя, у тебя всё получится! 🌸", "Не сдавайся: маленькие шаги приводят к большим результатам! 💗", "Ты справишься! И не забывай улыбаться ❤️"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFFFFF3F6);
        getWindow().setNavigationBarColor(0xFFFFF3F6);
        if (Build.VERSION.SDK_INT >= 23) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
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

    private static String todayKey() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("Asia/Baku"));
        return f.format(new Date());
    }

    private static int reminderRequestCode(int day, int hour, int minute) { return 5000 + day * 10000 + hour * 100 + minute; }

    public static void scheduleReminders(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        // Cancel alarms from the previous saved schedule.
        Set<String> oldKeys = p.getStringSet("scheduled_alarm_keys", new TreeSet<String>());
        for (String key : new TreeSet<>(oldKeys)) {
            try {
                String[] bits = key.split("_");
                int day = Integer.parseInt(bits[0]);
                String[] hm = bits[1].split(":");
                int hour = Integer.parseInt(hm[0]), minute = Integer.parseInt(hm[1]);
                int id = reminderRequestCode(day, hour, minute);
                Intent old = new Intent(context, ReminderReceiver.class).setAction("study_reminder_" + key);
                PendingIntent oldPi = PendingIntent.getBroadcast(context, id, old, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.cancel(oldPi); oldPi.cancel();
            } catch (Exception ignored) { }
        }
        // Cancel the four fixed reminders from version 1, if this is an upgrade.
        for (int i = 0; i < 4; i++) {
            Intent old = new Intent(context, ReminderReceiver.class).setAction(null);
            PendingIntent pi = PendingIntent.getBroadcast(context, 300 + i, old, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi); pi.cancel();
        }
        Set<String> days = p.getStringSet("reminder_days", null);
        if (days == null) { days = new TreeSet<>(); for (int d = 1; d <= 7; d++) days.add(String.valueOf(d)); }
        Set<String> times = p.getStringSet("reminder_times", null);
        if (times == null) times = new TreeSet<>(java.util.Arrays.asList("10:00", "15:00", "19:00", "21:00"));
        Set<String> newKeys = new TreeSet<>();
        TimeZone tz = TimeZone.getTimeZone("Asia/Baku");
        for (String dayValue : days) for (String timeValue : times) {
            try {
                int day = Integer.parseInt(dayValue);
                String[] parts = timeValue.split(":");
                int hour = Integer.parseInt(parts[0]), minute = Integer.parseInt(parts[1]);
                String key = day + "_" + String.format(Locale.US, "%02d:%02d", hour, minute);
                Calendar next = Calendar.getInstance(tz);
                next.set(Calendar.DAY_OF_WEEK, day);
                next.set(Calendar.HOUR_OF_DAY, hour); next.set(Calendar.MINUTE, minute);
                next.set(Calendar.SECOND, 0); next.set(Calendar.MILLISECOND, 0);
                if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 7);
                int id = reminderRequestCode(day, hour, minute);
                Intent intent = new Intent(context, ReminderReceiver.class).setAction("study_reminder_" + key)
                        .putExtra("message", "🌸 Пора немного позаниматься! Ты сможешь, Назка!").putExtra("id", id);
                PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7L, pi);
                newKeys.add(key);
            } catch (Exception ignored) { }
        }
        p.edit().putStringSet("scheduled_alarm_keys", newKeys).apply();
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
        int streak = currentStreak();
        int record = prefs.getInt("longest_streak", 0);
        LinearLayout progress = card();
        addText(progress, "🌸  ТВОЙ ПРОГРЕСС", 12, PINK, true);
        addText(progress, "🔥  " + streak + " " + dayWord(streak) + " подряд!", 25, DARK, true);
        addText(progress, "Рекорд: " + record + " " + dayWord(record) + "  ·  Завершено: " + done + " " + word(done), 13, MUTED, false);
        addText(progress, "Занимайся каждый день и собирай свою серию!", 13, MUTED, false);
        root.addView(progress, lp(-1, -2, 0, 0, 0, 18));
        action("📐  Математика", "Выбрать раздел и тему", this::showChapters);
        action("📚  Повторить темы", "Вернуться к списку уроков", this::showChapters);
        action("✅  Я позанималась", "Отметить выбранную тему", this::markDone);
        action("💌  Послание", "Немного поддержки на сегодня", () -> { String m = messages[new Random().nextInt(messages.length)]; base("Послание для тебя 💌", "Маленькое напоминание:"); addText(root, m, 22, PINK, true); backHome(); });
        action("🔔  Настроить напоминания", "Выбрать дни недели и удобное время", this::showReminderSettings);
        LinearLayout note = card(); addText(note, "⏰  НАПОМИНАНИЯ", 13, PINK, true); addText(note, reminderSummary(), 14, DARK, false); root.addView(note, lp(-1, -2, 0, 8, 0, 0));
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
        SharedPreferences.Editor edit = prefs.edit();
        if (selectedTopic != null && !prefs.getBoolean("done_" + selectedTopic, false)) {
            edit.putBoolean("done_" + selectedTopic, true).putInt("done_count", prefs.getInt("done_count", 0) + 1);
        }
        String today = todayKey();
        String last = prefs.getString("last_study_date", "");
        if (!today.equals(last)) {
            int streak = prefs.getInt("current_streak", 0);
            if (isYesterday(last)) streak++; else streak = 1;
            edit.putInt("current_streak", streak)
                    .putInt("longest_streak", Math.max(streak, prefs.getInt("longest_streak", 0)))
                    .putString("last_study_date", today);
        }
        edit.apply();
        Toast.makeText(this, "Умничка! ❤️ День засчитан в твою серию!", Toast.LENGTH_LONG).show();
        showHome();
    }

    private boolean isYesterday(String date) {
        if (date == null || date.isEmpty()) return false;
        try {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("Asia/Baku"));
            java.util.Date d = f.parse(date);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Baku"));
            c.add(Calendar.DAY_OF_YEAR, -1);
            return d != null && f.format(c.getTime()).equals(f.format(d));
        } catch (Exception e) { return false; }
    }

    private int currentStreak() {
        String last = prefs.getString("last_study_date", "");
        if (todayKey().equals(last) || isYesterday(last)) return prefs.getInt("current_streak", 0);
        return 0;
    }

    private String dayWord(int n) {
        int x = Math.abs(n) % 10, y = Math.abs(n) % 100;
        if (x == 1 && y != 11) return "день";
        if (x >= 2 && x <= 4 && (y < 12 || y > 14)) return "дня";
        return "дней";
    }

    private Set<String> selectedDays() {
        Set<String> result = new TreeSet<>();
        for (int d = 1; d <= 7; d++) if (prefs.getBoolean("day_" + d, true)) result.add(String.valueOf(d));
        return result;
    }

    private List<String> getSavedTimes() {
        Set<String> saved = prefs.getStringSet("reminder_times", null);
        Set<String> values = saved == null ? new TreeSet<>(java.util.Arrays.asList("10:00", "15:00", "19:00", "21:00")) : new TreeSet<>(saved);
        return new ArrayList<>(values);
    }

    private String reminderSummary() {
        Set<String> days = selectedDays();
        List<String> times = getSavedTimes();
        if (times.isEmpty() || days.isEmpty()) return "Напоминания выключены. Открой настройки, чтобы включить.";
        List<String> labels = new ArrayList<>();
        for (String day : days) { int index = Integer.parseInt(day) - 1; labels.add(new String[]{"Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб"}[index]); }
        return android.text.TextUtils.join(", ", labels) + "  ·  " + android.text.TextUtils.join(", ", times) + " (Баку)";
    }

    private void showReminderSettings() {
        base("Напоминания 🔔", "Выбери дни и время. Часовой пояс — Баку.");
        addText(root, "ДНИ НЕДЕЛИ", 12, PINK, true);
        dayChecks.clear();
        Set<String> savedDays = selectedDays();
        for (int d = 1; d <= 7; d++) {
            CheckBox cb = new CheckBox(this); cb.setText(DAY_NAMES[d - 1]); cb.setTextSize(15); cb.setTextColor(DARK);
            cb.setButtonTintList(android.content.res.ColorStateList.valueOf(PINK)); cb.setChecked(savedDays.contains(String.valueOf(d)));
            dayChecks.add(cb); root.addView(cb, lp(-1, 42, 0, 0, 0, 0));
        }
        addText(root, "ВРЕМЯ НАПОМИНАНИЙ", 12, PINK, true);
        reminderTimes.clear(); reminderTimes.addAll(getSavedTimes());
        LinearLayout timeList = new LinearLayout(this); timeList.setOrientation(LinearLayout.VERTICAL); root.addView(timeList, lp(-1, -2, 0, 4, 0, 8));
        final Runnable[] renderTimes = new Runnable[1];
        renderTimes[0] = () -> {
            timeList.removeAllViews();
            if (reminderTimes.isEmpty()) addText(timeList, "Время не выбрано — напоминания выключены", 14, MUTED, false);
            for (String time : new ArrayList<>(reminderTimes)) {
                LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
                TextView label = text("⏰  " + time, 17, DARK, true); row.addView(label, new LinearLayout.LayoutParams(0, dp(46), 1));
                Button remove = button("Убрать", false); remove.setOnClickListener(v -> { reminderTimes.remove(time); renderTimes[0].run(); });
                row.addView(remove, lp(100, 42, 6, 0, 0, 0)); timeList.addView(row, lp(-1, 46, 0, 2, 0, 2));
            }
        };
        renderTimes[0].run();
        Button addTime = button("＋  Добавить время", false);
        addTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Baku"));
            new TimePickerDialog(this, (TimePicker picker, int hour, int minute) -> {
                String value = String.format(Locale.US, "%02d:%02d", hour, minute);
                if (!reminderTimes.contains(value)) { reminderTimes.add(value); java.util.Collections.sort(reminderTimes); renderTimes[0].run(); }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });
        root.addView(addTime, lp(-1, 48, 0, 4, 0, 14));
        Button save = button("💗  Сохранить расписание", true);
        save.setOnClickListener(v -> {
            Set<String> days = new TreeSet<>();
            for (int i = 0; i < dayChecks.size(); i++) if (dayChecks.get(i).isChecked()) days.add(String.valueOf(i + 1));
            Set<String> times = new TreeSet<>(reminderTimes);
            SharedPreferences.Editor e = prefs.edit().putStringSet("reminder_days", days).putStringSet("reminder_times", times);
            for (int d = 1; d <= 7; d++) e.putBoolean("day_" + d, days.contains(String.valueOf(d)));
            e.apply(); scheduleReminders(this);
            Toast.makeText(this, days.isEmpty() || times.isEmpty() ? "Напоминания выключены" : "Расписание сохранено! ❤️", Toast.LENGTH_LONG).show();
            showHome();
        });
        root.addView(save, lp(-1, 54, 0, 4, 0, 8));
        Button back = button("← Назад", false); back.setOnClickListener(v -> showHome()); root.addView(back, lp(-1, 48, 0, 0, 0, 0));
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
