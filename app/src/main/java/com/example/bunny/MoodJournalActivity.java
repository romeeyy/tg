package com.example.bunny;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MoodJournalActivity extends AppCompatActivity {

    private EditText etFeeling;
    private Button btnSubmit, btnVoiceRecord;
    private ProgressBar progressBar;

    private LinearLayout entryContainer;

    private boolean isRecording = false;

    private OpenAIClient openAIClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_journal);

        openAIClient = new OpenAIClient();

        etFeeling = findViewById(R.id.etFeeling);
        btnSubmit = findViewById(R.id.btnSubmitEntry);
        btnVoiceRecord = findViewById(R.id.btnVoiceRecord);
        progressBar = findViewById(R.id.progressBar);

        entryContainer = findViewById(R.id.entryContainer);

        // 🎤 Voice Record Toggle
        btnVoiceRecord.setOnClickListener(v -> {
            isRecording = !isRecording;
            if (isRecording) {
                btnVoiceRecord.setText("🔴 Recording…");
                btnVoiceRecord.setBackgroundResource(R.drawable.voice_recording_bg);
            } else {
                btnVoiceRecord.setText("🎤 Voice Record");
                btnVoiceRecord.setBackgroundResource(R.drawable.voice_btn_bg);
            }
        });

        // ✨ Submit Entry
        btnSubmit.setOnClickListener(v -> {
            String text = etFeeling.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please write something 🌸", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSubmit.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            openAIClient.getTokkiResponse(text, new OpenAIClient.OpenAIListener() {
                @Override
                public void onSuccess(String replyText) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);

                        addJournalEntry(text, replyText);

                        etFeeling.setText("");
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MoodJournalActivity.this,
                                "Tokki error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }


    // 🌸 Adds NEW entry to your container
    private void addJournalEntry(String userText, String tokkiReply) {

        LayoutInflater inflater = LayoutInflater.from(this);

        // Inflate the entry layout you already use
        View entryView = inflater.inflate(R.layout.journal_entry_item, null);

        TextView tvUser = entryView.findViewById(R.id.tvUserText);
        TextView tvTokki = entryView.findViewById(R.id.tvTokkiText);
        TextView tvTimestamp = entryView.findViewById(R.id.tvTimestamp);
        TextView tvEmotion = entryView.findViewById(R.id.tvEmotionBadge);

        // Set user text
        tvUser.setText(userText);

        // Set Tokki response
        tvTokki.setText(tokkiReply);

        // Timestamp
        String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        tvTimestamp.setText(timestamp);

        // Detect emotion
        String emotion = EmotionTool.detectEmotion(userText);
        tvEmotion.setText(emotion);

        // Add the full entry to the top of the container
        entryContainer.addView(entryView, 0);
    }
}


