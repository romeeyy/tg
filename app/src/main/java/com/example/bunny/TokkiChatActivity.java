package com.example.bunny;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TokkiChatActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "mindtokki_prefs";
    private static final String KEY_CALM_POINTS = "calm_points";
    private static final String KEY_CHAT_HISTORY = "chat_history";

    private EditText etMessage;
    private Button btnSend;
    private ScrollView scrollChat;
    private LinearLayout messagesContainer;
    private View typingLayout;
    private TextView tvTyping;

    private OpenAIClient openAIClient;

    // Calm points
    private SharedPreferences prefs;
    private int calmPoints = 0;

    // Typing animation
    private Handler typingHandler = new Handler();
    private Runnable typingRunnable;
    private int typingStep = 0;

    // Prevent saving while we're restoring history
    private boolean isLoadingHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tokki_chat);

        openAIClient = new OpenAIClient();

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        calmPoints = prefs.getInt(KEY_CALM_POINTS, 0);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        scrollChat = findViewById(R.id.scrollChat);
        messagesContainer = findViewById(R.id.messagesContainer);
        typingLayout = findViewById(R.id.layoutTyping);
        tvTyping = findViewById(R.id.tvTyping);

        // disable send if empty
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1f : 0.5f);
            }

            @Override public void afterTextChanged(Editable s) { }
        });

        // Enter = send, Shift+Enter = new line (like web version)
        etMessage.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER &&
                    !event.isShiftPressed()) {

                sendMessage();
                return true;
            }
            return false;
        });

        btnSend.setOnClickListener(v -> sendMessage());

        // Load previous chat if exists
        loadChatHistory();

        // If first time / no history, show greeting
        if (messagesContainer.getChildCount() == 0) {
            addTokkiMessage("Hi! I'm Tokki, your friendly bunny companion 🐰💕\n" +
                    "I'm here to listen, support, and help you feel your best. How are you doing today?");
        }
    }

    private void sendMessage() {
        final String userText = etMessage.getText().toString().trim();
        if (userText.isEmpty()) return;

        // show user bubble
        addUserMessage(userText);
        etMessage.setText("");

        // disable send while Tokki replies
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        // show typing indicator + start animation
        typingLayout.setVisibility(View.VISIBLE);
        startTypingAnimation();

        // call OpenAI
        openAIClient.getTokkiResponse(userText, new OpenAIClient.OpenAIListener() {
            @Override
            public void onSuccess(final String replyText) {
                runOnUiThread(() -> {
                    // hide typing indicator + stop animation
                    typingLayout.setVisibility(View.GONE);
                    stopTypingAnimation();

                    addTokkiMessage(replyText);

                    btnSend.setEnabled(true);
                    btnSend.setAlpha(1f);

                    // +1 Calm Point per Tokki reply
                    addCalmPoints(1);
                    // Optional toast:
                    // Toast.makeText(TokkiChatActivity.this,
                    //        "+1 Calm Point! Total: " + calmPoints,
                    //        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(final String errorMessage) {
                runOnUiThread(() -> {
                    typingLayout.setVisibility(View.GONE);
                    stopTypingAnimation();

                    btnSend.setEnabled(true);
                    btnSend.setAlpha(1f);
                    Toast.makeText(TokkiChatActivity.this,
                            "Tokki had a small error: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ==== BUBBLES ====

    private void addUserMessage(String text) {
        addMessageBubble(text, true, System.currentTimeMillis(), true);
    }

    private void addTokkiMessage(String text) {
        addMessageBubble(text, false, System.currentTimeMillis(), true);
    }

    // Used for both new messages + history restore
    private void addMessageBubble(String text, boolean isUser, long timeMillis, boolean saveToHistory) {
        LayoutInflater inflater = LayoutInflater.from(this);
        int layoutId = isUser ? R.layout.item_chat_user : R.layout.item_chat_tokki;

        View bubble = inflater.inflate(layoutId, messagesContainer, false);

        TextView tvSender = bubble.findViewById(R.id.tvSender);
        TextView tvMessageText = bubble.findViewById(R.id.tvMessageText);
        TextView tvMessageTime = bubble.findViewById(R.id.tvMessageTime);

        if (!isUser) {
            if (tvSender != null) {
                tvSender.setText("Tokki");
                tvSender.setVisibility(View.VISIBLE);
            }
        } else {
            if (tvSender != null) {
                tvSender.setVisibility(View.GONE);
            }
        }

        tvMessageText.setText(text);
        tvMessageTime.setText(formatTime(timeMillis));

        messagesContainer.addView(bubble);
        scrollToBottom();

        if (saveToHistory && !isLoadingHistory) {
            saveMessageToHistory(text, isUser, timeMillis);
        }
    }

    private void scrollToBottom() {
        scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
    }

    private String formatTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    // ==== CALM POINTS ====

    private void addCalmPoints(int amount) {
        calmPoints += amount;
        prefs.edit().putInt(KEY_CALM_POINTS, calmPoints).apply();
    }

    // ==== TYPING ANIMATION ====

    private void startTypingAnimation() {
        if (tvTyping == null) return;

        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        typingRunnable = new Runnable() {
            @Override
            public void run() {
                typingStep = (typingStep + 1) % 3;

                String dots;
                switch (typingStep) {
                    case 0:
                        dots = "●";
                        break;
                    case 1:
                        dots = "● ●";
                        break;
                    default:
                        dots = "● ● ●";
                        break;
                }

                tvTyping.setText(dots + "  Tokki is typing...");
                typingHandler.postDelayed(this, 400); // every 0.4 sec
            }
        };

        typingHandler.post(typingRunnable);
    }

    private void stopTypingAnimation() {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
            typingRunnable = null;
        }
        if (tvTyping != null) {
            tvTyping.setText("● ● ●  Tokki is typing...");
        }
    }

    // ==== CHAT HISTORY (LOCAL “DATABASE”) ====

    private void saveMessageToHistory(String text, boolean isUser, long timeMillis) {
        try {
            String existing = prefs.getString(KEY_CHAT_HISTORY, "[]");
            JSONArray array = new JSONArray(existing);

            JSONObject obj = new JSONObject();
            obj.put("text", text);
            obj.put("isUser", isUser);
            obj.put("time", timeMillis);

            array.put(obj);

            prefs.edit().putString(KEY_CHAT_HISTORY, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadChatHistory() {
        String json = prefs.getString(KEY_CHAT_HISTORY, null);
        if (json == null) return;

        try {
            isLoadingHistory = true;

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String text = obj.getString("text");
                boolean isUser = obj.getBoolean("isUser");
                long time = obj.getLong("time");

                addMessageBubble(text, isUser, time, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            isLoadingHistory = false;
        }
    }
}


