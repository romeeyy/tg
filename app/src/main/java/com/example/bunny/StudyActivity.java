package com.example.bunny;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;    // <-- REQUIRED IMPORT
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudyActivity extends AppCompatActivity {

    // TIMER SYSTEM
    private TextView tvTimerMain, tvTimerTotal, tvTimerMode, tvTimerHint;
    private Button btnStart, btnPause, btnComplete, btnReset;
    private Button btnPreset25, btnPreset50, btnPreset5, btnPreset10;
    private PomodoroRingView pomodoroRing;

    private CountDownTimer timer;
    private boolean isRunning = false;
    private int selectedDuration = 25;
    private int remainingMillis = 25 * 60 * 1000;

    // TASK SYSTEM
    private EditText etTaskTitle, etTaskSubject;
    private Button btnAddTask;
    private LinearLayout containerActive, containerCompleted;

    // STATS
    private TextView tvStatTotalTime, tvStatSessions, tvStatTasks, tvStatStreak;
    private int totalMinutes = 145;
    private int sessions = 8;
    private int tasksDone = 12;
    private int streak = 5;

    // PREFS
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "mindtokki_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initTimerViews();
        initTaskViews();
        initStatViews();

        // Load saved state
        loadStatsFromPrefs();
        loadTimerFromPrefs();
        loadTasksFromPrefs();

        updateStats();
        updateTimerText();

        setupPresetButtons();
        setupControlButtons();
    }

    // ------------------ TIMER UI INIT --------------------
    private void initTimerViews() {
        tvTimerMain = findViewById(R.id.tvTimerMain);
        tvTimerTotal = findViewById(R.id.tvTimerTotal);
        tvTimerMode = findViewById(R.id.tvTimerMode);
        tvTimerHint = findViewById(R.id.tvTimerHint);

        btnStart = findViewById(R.id.btnStartFocus);
        btnPause = findViewById(R.id.btnPause);
        btnComplete = findViewById(R.id.btnComplete);
        btnReset = findViewById(R.id.btnReset);

        btnPreset25 = findViewById(R.id.btnPreset25);
        btnPreset50 = findViewById(R.id.btnPreset50);
        btnPreset5 = findViewById(R.id.btnPreset5);
        btnPreset10 = findViewById(R.id.btnPreset10);

        pomodoroRing = findViewById(R.id.pomodoroRing);
    }

    // ------------------ TASK UI INIT --------------------
    private void initTaskViews() {
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskSubject = findViewById(R.id.etTaskSubject);
        btnAddTask = findViewById(R.id.btnAddTask);

        containerActive = findViewById(R.id.containerActiveTasks);
        containerCompleted = findViewById(R.id.containerCompletedTasks);

        btnAddTask.setOnClickListener(v -> addTask());
    }

    // ------------------ STATS INIT --------------------
    private void initStatViews() {
        tvStatTotalTime = findViewById(R.id.tvStatTotalTime);
        tvStatSessions = findViewById(R.id.tvStatSessions);
        tvStatTasks = findViewById(R.id.tvStatTasks);
        tvStatStreak = findViewById(R.id.tvStatStreak);
    }

    // ------------------ PREF LOAD/SAVE ------------------

    private void loadStatsFromPrefs() {
        totalMinutes = prefs.getInt("study_total_minutes", 145);
        sessions = prefs.getInt("study_sessions", 8);
        tasksDone = prefs.getInt("study_tasks_done", 12);
        streak = prefs.getInt("study_streak", 5);
    }

    private void saveStatsToPrefs() {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("study_total_minutes", totalMinutes);
        e.putInt("study_sessions", sessions);
        e.putInt("study_tasks_done", tasksDone);
        e.putInt("study_streak", streak);
        e.apply();
    }

    private void loadTimerFromPrefs() {
        selectedDuration = prefs.getInt("study_selected_duration", 25);
        remainingMillis = prefs.getInt("study_remaining_millis", selectedDuration * 60 * 1000);

        if (selectedDuration > 10) {
            tvTimerMode.setText("⏱️ Focus Session");
            tvTimerHint.setText("🍅 Focus deeply on your task, you got this!");
        } else {
            tvTimerMode.setText("⏱️ Break Time");
            tvTimerHint.setText("☕ Relax and recharge your energy!");
        }
    }

    private void saveTimerToPrefs() {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("study_selected_duration", selectedDuration);
        e.putInt("study_remaining_millis", remainingMillis);
        e.apply();
    }

    // ------------------ TASK LOAD/SAVE ------------------

    private void loadTasksFromPrefs() {
        String json = prefs.getString("study_tasks_json", null);
        if (json == null) return;

        try {
            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String title = obj.getString("title");
                String subject = obj.getString("subject");
                boolean completed = obj.getBoolean("completed");

                addTaskView(title, subject, completed, true);

                if (completed) tasksDone++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveTasksToPrefs() {
        JSONArray arr = new JSONArray();

        // active tasks
        for (int i = 0; i < containerActive.getChildCount(); i++) {
            View child = containerActive.getChildAt(i);

            JSONObject obj = new JSONObject();
            try {
                obj.put("title", ((TextView) child.findViewById(R.id.tvTaskTitle)).getText().toString());
                obj.put("subject", ((TextView) child.findViewById(R.id.tvTaskSubject)).getText().toString());
                obj.put("completed", false);
                arr.put(obj);
            } catch (Exception ignored) {}
        }

        // completed tasks
        for (int i = 0; i < containerCompleted.getChildCount(); i++) {
            View child = containerCompleted.getChildAt(i);

            JSONObject obj = new JSONObject();
            try {
                obj.put("title", ((TextView) child.findViewById(R.id.tvTaskTitle)).getText().toString());
                obj.put("subject", ((TextView) child.findViewById(R.id.tvTaskSubject)).getText().toString());
                obj.put("completed", true);
                arr.put(obj);
            } catch (Exception ignored) {}
        }

        prefs.edit().putString("study_tasks_json", arr.toString()).apply();
    }

    // ------------------ STATS DISPLAY ------------------

    private void updateStats() {
        tvStatTotalTime.setText(String.valueOf(totalMinutes));
        tvStatSessions.setText(String.valueOf(sessions));
        tvStatTasks.setText(String.valueOf(tasksDone));
        tvStatStreak.setText(streak + " 🔥");
    }

    // ------------------- TIMER LOGIC -------------------

    private void setupPresetButtons() {
        btnPreset25.setOnClickListener(v -> setDuration(25));
        btnPreset50.setOnClickListener(v -> setDuration(50));
        btnPreset5.setOnClickListener(v -> setDuration(5));
        btnPreset10.setOnClickListener(v -> setDuration(10));
    }

    private void setDuration(int minutes) {
        selectedDuration = minutes;
        remainingMillis = minutes * 60 * 1000;

        if (minutes > 10) {
            tvTimerMode.setText("⏱️ Focus Session");
            tvTimerHint.setText("🍅 Focus deeply on your task, you got this!");
        } else {
            tvTimerMode.setText("⏱️ Break Time");
            tvTimerHint.setText("☕ Relax and recharge!");
        }

        stopTimer();
        updateTimerText();
        saveTimerToPrefs();
    }

    private void setupControlButtons() {
        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnComplete.setOnClickListener(v -> completeSession());
        btnReset.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        if (isRunning) return;

        isRunning = true;

        timer = new CountDownTimer(remainingMillis, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = (int) millisUntilFinished;
                updateTimerText();
                saveTimerToPrefs();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                remainingMillis = 0;
                updateTimerText();
                completeSession();
            }

        }.start();
    }

    private void pauseTimer() {
        stopTimer();
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
    }

    private void stopTimer() {
        isRunning = false;
        if (timer != null) timer.cancel();
    }

    private void resetTimer() {
        stopTimer();
        remainingMillis = selectedDuration * 60 * 1000;
        updateTimerText();
        saveTimerToPrefs();
    }

    private void updateTimerText() {
        int sec = remainingMillis / 1000;
        int min = sec / 60;
        int s = sec % 60;

        tvTimerMain.setText(String.format("%02d:%02d", min, s));
        tvTimerTotal.setText(selectedDuration + ":00 total");

        if (pomodoroRing != null && selectedDuration > 0) {
            float total = selectedDuration * 60f * 1000f;
            float done = total - remainingMillis;
            float progress = done / total;

            pomodoroRing.setProgress(progress, selectedDuration > 10);
        }
    }

    private void completeSession() {
        stopTimer();

        boolean isFocus = selectedDuration > 10;

        if (isFocus) {
            totalMinutes += selectedDuration;
            sessions++;

            int earned = Math.max(10, selectedDuration * 2);

            Toast.makeText(this,
                    "Session Complete! +" + earned + " Calm Points ✨",
                    Toast.LENGTH_LONG).show();

            showBreakDialog();

        } else {
            Toast.makeText(this, "Break Complete! 💆‍♀️", Toast.LENGTH_SHORT).show();
        }

        updateStats();
        saveStatsToPrefs();

        remainingMillis = selectedDuration * 60 * 1000;
        updateTimerText();
        saveTimerToPrefs();
    }

    private void showBreakDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Time for a Break! ☕")
                .setMessage("You've been studying for a while.\nTake a 5-10 minute break 🌸")
                .setPositiveButton("Start Break", (d, w) -> setDuration(5))
                .setNegativeButton("Continue Studying", (d, w) -> d.dismiss())
                .show();
    }

    // ------------------- TASK SYSTEM -------------------

    private void addTask() {
        String title = etTaskTitle.getText().toString().trim();
        String subject = etTaskSubject.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter a task!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (subject.isEmpty()) subject = "General";

        addTaskView(title, subject, false, false);

        etTaskTitle.setText("");
        etTaskSubject.setText("");

        saveTasksToPrefs();
    }

    private void addTaskView(String title, String subject, boolean completed, boolean fromLoad) {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_task,
                        completed ? containerCompleted : containerActive,
                        false);

        TextView tvTitle = view.findViewById(R.id.tvTaskTitle);
        TextView tvSubject = view.findViewById(R.id.tvTaskSubject);
        ImageView btnCheck = view.findViewById(R.id.btnCheck);
        ImageView btnDelete = view.findViewById(R.id.btnDelete);

        tvTitle.setText(title);
        tvSubject.setText(subject);

        if (completed) {
            btnCheck.setImageResource(R.drawable.ic_checkbox_checked);
            btnCheck.setEnabled(false);
            containerCompleted.addView(view);
        } else {
            btnCheck.setImageResource(R.drawable.ic_checkbox_unchecked);
            containerActive.addView(view);
        }

        btnCheck.setOnClickListener(v -> completeTask(view));

        btnDelete.setOnClickListener(v -> {
            containerActive.removeView(view);
            containerCompleted.removeView(view);
            saveTasksToPrefs();
        });

        if (!fromLoad && completed) tasksDone++;

        updateStats();
        saveTasksToPrefs();
    }

    private void completeTask(View view) {

        ImageView btnCheck = view.findViewById(R.id.btnCheck);
        btnCheck.setImageResource(R.drawable.ic_checkbox_checked);
        btnCheck.setEnabled(false);

        containerActive.removeView(view);
        containerCompleted.addView(view);

        tasksDone++;
        saveStatsToPrefs();
        updateStats();

        saveTasksToPrefs();

        Toast.makeText(this, "+5 Calm Points 🌸", Toast.LENGTH_SHORT).show();
    }

    // --------------------------------------------------

    @Override
    protected void onPause() {
        super.onPause();
        saveTimerToPrefs();
        saveTasksToPrefs();
        saveStatsToPrefs();
    }
}



