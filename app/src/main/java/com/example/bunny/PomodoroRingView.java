package com.example.bunny;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class PomodoroRingView extends View {

    private Paint bgPaint;
    private Paint progressPaint;
    private RectF circleBounds = new RectF();
    private float progress = 0f; // 0..1
    private boolean isFocus = true;

    public PomodoroRingView(Context context) {
        super(context);
        init();
    }

    public PomodoroRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PomodoroRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(16f);
        bgPaint.setColor(0x33FFFFFF); // subtle background ring

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(16f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.purple_main));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = 16f;
        circleBounds.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // background circle
        canvas.drawArc(circleBounds, 0, 360, false, bgPaint);

        // color based on mode
        int color = ContextCompat.getColor(
                getContext(),
                isFocus ? R.color.purple_main : R.color.peach_soft
        );
        progressPaint.setColor(color);

        float sweep = 360f * progress;
        // start at top (-90 degrees)
        canvas.drawArc(circleBounds, -90, sweep, false, progressPaint);
    }

    public void setProgress(float progress, boolean isFocusMode) {
        if (progress < 0f) progress = 0f;
        if (progress > 1f) progress = 1f;
        this.progress = progress;
        this.isFocus = isFocusMode;
        invalidate();
    }
}
