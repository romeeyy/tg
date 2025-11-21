package com.example.bunny;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIClient {

    // TODO: replace with your real API key
    private static final String API_KEY = "YOUR_OPENAI_API_KEY_HERE";

    // New Chat Completions endpoint (ChatGPT API)
    private static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;

    public interface OpenAIListener {
        void onSuccess(String replyText);
        void onError(String errorMessage);
    }

    public OpenAIClient() {
        client = new OkHttpClient.Builder().build();
    }

    public void getTokkiResponse(String userMessage, OpenAIListener listener) {
        // Build messages array: system + user
        JSONArray messages = new JSONArray();

        try {
            // System prompt – describes Tokki’s personality
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "You are Tokki, a gentle, encouraging AI bunny therapist for students. " +
                            "Your job is to respond with short, empathetic, supportive messages. " +
                            "Use emojis sometimes, speak very kindly, and give 1–2 simple suggestions. " +
                            "The user is journaling about their feelings.");
            messages.put(systemMsg);

            // User message
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);

            // Body
            JSONObject body = new JSONObject();
            // You can change model if needed (check your account / docs)
            body.put("model", "gpt-4o-mini");
            body.put("messages", messages);
            body.put("max_tokens", 200);

            RequestBody requestBody = RequestBody.create(body.toString(), JSON);

            Request request = new Request.Builder()
                    .url(CHAT_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError("Network error: " + e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        if (listener != null) {
                            listener.onError("API error: " + response.code());
                        }
                        return;
                    }
                    String responseBody = response.body().string();
                    Log.d("OpenAI", "response: " + responseBody);

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray choices = json.getJSONArray("choices");
                        if (choices.length() > 0) {
                            JSONObject firstChoice = choices.getJSONObject(0);
                            JSONObject messageObj = firstChoice.getJSONObject("message");
                            String content = messageObj.getString("content");
                            if (listener != null) {
                                listener.onSuccess(content.trim());
                            }
                        } else {
                            if (listener != null) {
                                listener.onError("No choices returned.");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onError("Parse error: " + e.getMessage());
                        }
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError("JSON error: " + e.getMessage());
            }
        }
    }
}

