package com.ucode.segmentedcontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GestureDetectorCompat;

public class SimpleSegmentedControl extends View implements GestureDetector.OnGestureListener {

    private static final String TAG = SimpleSegmentedControl.class.getName();

    private static final float DEFAULT_CORNER_RADIUS_DP = 6f;
    private static final float DEFAULT_BORDER_WIDTH = 1f;

    private int cornerRadius = 0;
    private int color = Color.BLACK;
    private int pressedColor = Color.GRAY;
    private int textColor;
    private int textSelectedColor;
    private int fontId;
    private boolean useDivider;

    private int borderWidth = 1;

    private int viewHeight;
    private int viewWidth;

    private String[] segmentTitles;
    private int segmentWidth;

    private RectF borderRect;
    private Paint borderPaint;
    private Paint selectedPaint;
    private Paint pressedPaint;
    private TextPaint titlePaint;

    private GestureDetectorCompat gestureDetector;

    private Path boundPath;
    private Path borderPath;
    private Rect pressedRect;
    private Rect selectedRect;

    private Rect textBound;

    private int pressedIndex=-1;
    private int selectedIndex = 0;

    private StaticLayout staticLayout;
    private Callback callback;
    private float textSize;

    public SimpleSegmentedControl(Context context) {
        super(context);
        init(null);
    }

    public SimpleSegmentedControl(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SimpleSegmentedControl(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public SimpleSegmentedControl(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SimpleSegmentedControl);

            cornerRadius = array.getDimensionPixelSize(R.styleable.SimpleSegmentedControl_segmentCornerRadius, dp2px(DEFAULT_CORNER_RADIUS_DP));
            color = array.getColor(R.styleable.SimpleSegmentedControl_segmentSelectedColor, Color.BLACK);
            pressedColor = array.getColor(R.styleable.SimpleSegmentedControl_segmentPressedColor, Color.GRAY);
            textColor = array.getColor(R.styleable.SimpleSegmentedControl_segmentColorTextNormal, Color.GRAY);
            textSelectedColor = array.getColor(R.styleable.SimpleSegmentedControl_segmentColorTextSelected, Color.WHITE);
            borderWidth = array.getDimensionPixelSize(R.styleable.SimpleSegmentedControl_segmentBorderWidth, dp2px(DEFAULT_BORDER_WIDTH));
            fontId = array.getResourceId(R.styleable.SimpleSegmentedControl_segmentTitleFont, 0);
            useDivider = array.getBoolean(R.styleable.SimpleSegmentedControl_segmentUseDivier, true);
            textSize = array.getDimensionPixelSize(R.styleable.SimpleSegmentedControl_segmentTitleTextSize, 16);

            array.recycle();
        }

        borderPaint = new Paint();
        borderPaint.setColor(color);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        selectedPaint = new Paint();
        selectedPaint.setColor(color);
        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setAntiAlias(true);

        pressedPaint = new Paint();
        pressedPaint.setStyle(Paint.Style.FILL);
        pressedPaint.setColor(Color.GRAY);

        titlePaint = new TextPaint();
        titlePaint.setAntiAlias(true);
        titlePaint.setTextSize(textSize);
        if (fontId>0) {
            titlePaint.setTypeface(ResourcesCompat.getFont(getContext(), fontId));
        }

        gestureDetector = new GestureDetectorCompat(getContext(), this);

        pressedRect = new Rect();
        selectedRect = new Rect();

        textBound = new Rect();


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0) {
            viewHeight = h;
            viewWidth = w;
            initDimensions();
        }
    }

    private void initDimensions() {
        if (segmentTitles != null && segmentTitles.length > 0) {
            invalidateDrawing();
        }
    }

    private void invalidateDrawing() {
        segmentWidth = viewWidth / segmentTitles.length;
        boundPath = new Path();
        borderPath = new Path();
        borderRect = new RectF();
        borderRect.set(0, 0, viewWidth, viewHeight);
        borderRect.inset(borderWidth, borderWidth);

        boundPath.addRoundRect(borderRect, cornerRadius, cornerRadius, Path.Direction.CW);
        borderPath.addRoundRect(borderRect, cornerRadius, cornerRadius, Path.Direction.CW);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.clipPath(boundPath);
        drawPressedRectangle(canvas);
        canvas.drawPath(borderPath, borderPaint);
        drawSelectedRectangle(canvas);
        drawTitles(canvas);
        if (useDivider) {
            drawDividers(canvas);
        }
    }

    private void drawPressedRectangle(Canvas canvas) {
        if (pressedIndex > -1) {
            pressedPaint.setColor(pressedColor);
            pressedRect.set(pressedIndex * segmentWidth, 0, (pressedIndex + 1) * segmentWidth, viewHeight);
            canvas.drawRect(pressedRect, pressedPaint);
        }
    }

    private void drawSelectedRectangle(Canvas canvas) {
        if (selectedIndex > -1) {
            selectedPaint.setColor(color);
            selectedRect.set(selectedIndex * segmentWidth, 0, (selectedIndex + 1) * segmentWidth, viewHeight);
            canvas.drawRect(selectedRect, selectedPaint);
        }
    }

    private void drawTitles(Canvas canvas) {
        for (int i = 0; i < segmentTitles.length; i++) {
            String text = segmentTitles[i];
//            staticLayout = StaticLayout.Builder.obtain(text, 0, text.length(), titlePaint, segmentWidth)
//                    .setIncludePad(false)
//                    .setEllipsize(TextUtils.TruncateAt.END)
//                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
//                    .setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE).build();
//            staticLayout.draw(canvas);
            titlePaint.setColor(i == selectedIndex ? textSelectedColor : textColor);
            titlePaint.getTextBounds(text, 0, text.length(), textBound);
            float x =(segmentWidth * (i + 0.5f)) - textBound.width() / 2f;
            float y = (canvas.getHeight() / 2f) - ((titlePaint.descent() + titlePaint.ascent()) / 2f) ;
            canvas.drawText(text, x, y, titlePaint);

        }
    }

    private void drawDividers(Canvas canvas) {
        if (segmentTitles.length == 1) return;
        for (int i = 1; i < segmentTitles.length; i++) {
            canvas.drawLine(segmentWidth*i, borderRect.top, segmentWidth*i, borderRect.bottom, borderPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        int pressingIndex = findTouchIndex(motionEvent);
        if (pressingIndex != selectedIndex) {
            pressedIndex = pressingIndex;
            invalidateDrawing();
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        validateSelectedIndex(motionEvent);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        validateSelectedIndex(motionEvent);
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    private int findTouchIndex(MotionEvent event) {
        for (int i = 0; i < segmentTitles.length; i++) {
            if (event.getX() > i * segmentWidth
                    && event.getX() < (i + 1) * segmentWidth
                    && event.getY() > 0
                    && event.getY() < viewHeight) {
                return i;
            }
        }
        return -1;
    }

    private void validateSelectedIndex(MotionEvent event) {
        int upIndex = findTouchIndex(event);
        if (upIndex == pressedIndex) {
            selectedIndex = pressedIndex;
            if (callback != null) {
                callback.onSegmentSelected(selectedIndex);
            }
        }
        pressedIndex = -1;
        invalidateDrawing();
    }

    public interface Callback {
        void onSegmentSelected(int index);
    }

    private int dp2px(float dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp);
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getContext().getResources().getDisplayMetrics());
    }


    public void setSegmentTitles(String[] segmentTitles) {
        this.segmentTitles = segmentTitles;
        invalidateDrawing();
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setPressedColor(int pressedColor) {
        this.pressedColor = pressedColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextSelectedColor(int textSelectedColor) {
        this.textSelectedColor = textSelectedColor;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setSegmentWidth(int segmentWidth) {
        this.segmentWidth = segmentWidth;
    }

    public void setSelected(int selected) {
        this.selectedIndex = selected;
        invalidate();
    }

    public int getSelected() {
        return selectedIndex;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
