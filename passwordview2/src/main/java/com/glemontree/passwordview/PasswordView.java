package com.glemontree.passwordview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.Timer;
import java.util.TimerTask;

public class PasswordView extends View {

    private int passwordLength;
    private long cursorFlashTime;
    private int passwordPadding;
    private int passwordSize = dp2px(50);
    private int borderColor;
    private int borderWidth;
    private boolean isCursorEnable;
    private int cursorColor;
    private int cursorWidth;
    private int cursorHeight;
    private boolean isCursorShowing;
    private boolean isInputComplete;
    private int cursorPosition;
    private int cipherTextSize;
    private boolean cipherEnable;
    private int mode;
    private static final String CIPHER_TEXT = "*";
    private Paint paint;
    private String[] password;
    private InputMethodManager inputManager;
    private TimerTask timerTask;
    private Timer timer;

    public PasswordView(Context context) {
        this(context, null);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PasswordView, defStyleAttr, 0);
        passwordLength = a.getInt(R.styleable.PasswordView_passwordLength, 6);
        passwordPadding = a.getDimensionPixelOffset(R.styleable.PasswordView_passwordPadding,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        borderColor = a.getColor(R.styleable.PasswordView_borderColor, Color.BLACK);
        borderWidth = a.getDimensionPixelOffset(R.styleable.PasswordView_bordorWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()));
        cursorFlashTime = a.getInt(R.styleable.PasswordView_cursorFlashTime, 500);
        isCursorEnable = a.getBoolean(R.styleable.PasswordView_isCursorEnable, true);
        cursorColor = a.getColor(R.styleable.PasswordView_cursorColor, Color.GRAY);
        mode = a.getInt(R.styleable.PasswordView_mode, 0);
        cipherEnable = a.getBoolean(R.styleable.PasswordView_cipherEnable, true);
        a.recycle();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int width = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                width = passwordSize * passwordLength + passwordPadding * (passwordLength - 1);
                break;
            case MeasureSpec.EXACTLY:
                width = MeasureSpec.getSize(widthMeasureSpec);
                passwordSize = (width - passwordPadding * (passwordLength - 1)) / passwordLength;
        }
        setMeasuredDimension(width, passwordSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cipherTextSize = passwordSize / 2;
        cursorWidth = dp2px(2);
        cursorHeight = passwordSize / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == 0) {
            drawUnderLine(canvas);
        } else {
            drawRect(canvas);
        }
        drawCursor(canvas);
        drawCipherText(canvas);
    }

    private void drawCipherText(Canvas canvas) {
        paint.setColor(Color.GRAY);
        paint.setTextSize(cipherTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);

        Rect rect = new Rect();
        canvas.getClipBounds(rect);
        paint.getTextBounds(CIPHER_TEXT, 0, CIPHER_TEXT.length(), rect);
        for (int i = 0; i < password.length; i++) {
            if (!TextUtils.isEmpty(password[i])) {
                if (cipherEnable) {
                    canvas.drawText(CIPHER_TEXT,
                            (getPaddingLeft() + passwordSize / 2) + (passwordSize + passwordPadding) * i,
                            getPaddingTop() + passwordSize / 2 + cursorHeight / 2, paint);
                } else {
                    canvas.drawText(password[i],
                            (getPaddingLeft() + passwordSize / 2) + (passwordSize + passwordPadding) * i,
                            getPaddingTop() + passwordSize / 2 + cursorHeight / 2, paint);
                }
            }
        }
    }

    private void drawCursor(Canvas canvas) {
        paint.setColor(cursorColor);
        paint.setStrokeWidth(cursorWidth);
        paint.setStyle(Paint.Style.FILL);
        if (!isCursorShowing && isCursorEnable && !isInputComplete && hasFocus()) {
            canvas.drawLine(getPaddingLeft() + passwordSize / 2 + (passwordSize + passwordPadding) * cursorPosition,
                    getPaddingTop() + (passwordSize - cursorHeight) / 2,
                    getPaddingLeft() + passwordSize / 2 + (passwordSize + passwordPadding) * cursorPosition,
                    getPaddingTop() + (passwordSize + cursorHeight) / 2,
                    paint);
        }
    }

    private void drawRect(Canvas canvas) {
        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < passwordLength; i++) {
            canvas.drawRect(getPaddingLeft() + (passwordSize + passwordPadding) * i, getPaddingTop(),
                    getPaddingLeft() + (passwordSize + passwordPadding) * i + passwordSize, getPaddingTop() + passwordSize, paint);
        }
    }

    private void drawUnderLine(Canvas canvas) {
        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < passwordLength; i++) {
            canvas.drawLine(getPaddingLeft() + (passwordSize + passwordPadding) * i, getPaddingTop() + passwordSize,
                    getPaddingLeft() + (passwordSize + passwordPadding) * i + passwordSize, getPaddingTop() + passwordSize, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            inputManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        timer.scheduleAtFixedRate(timerTask, 0, cursorFlashTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
    }

    private void init() {
        setFocusableInTouchMode(true);
        password = new String[passwordLength];
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        paint = new Paint();
        paint.setAntiAlias(true);

        MyKeyListener myKeyListener = new MyKeyListener();
        setOnKeyListener(myKeyListener);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                isCursorShowing = !isCursorShowing;
                postInvalidate();
            }
        };

        timer = new Timer();
    }

    private int dp2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5);
    }

    public void setPasswordSize(int passwordSize) {
        this.passwordSize = passwordSize;
        postInvalidate();
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
        postInvalidate();
    }

    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
        postInvalidate();
    }

    public void setCursorEnable(boolean cursorEnable) {
        isCursorEnable = cursorEnable;
        postInvalidate();
    }

    public void setCipherEnable(boolean cipherEnable) {
        this.cipherEnable = cipherEnable;
        postInvalidate();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        return super.onCreateInputConnection(outAttrs);
    }

    private PasswordListener passwordListener;

    public void setPasswordListener(PasswordListener passwordListener) {
        this.passwordListener = passwordListener;
    }

    public interface PasswordListener {
        void passwordChange(String changeText);
        void passwordComplete();
        void keyEnterPress(String password, boolean isComplete);
    }

    class MyKeyListener implements OnKeyListener {

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            int action = keyEvent.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (TextUtils.isEmpty(password[0])) {
                        return true;
                    }
                    String deleteText = delete();
                    if (passwordListener != null && !TextUtils.isEmpty(deleteText)) {
                        passwordListener.passwordChange(deleteText);
                    }
                    postInvalidate();
                    return true;
                }
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    if (isInputComplete) {
                        return true;
                    }
                    String addText = add((keyCode - 7 ) + "");
                    if (passwordListener != null && !TextUtils.isEmpty(addText)) {
                        passwordListener.passwordChange(addText);
                    }
                    postInvalidate();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (passwordListener != null) {
                        passwordListener.keyEnterPress(getPassword(), isInputComplete);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private String add(String s) {
        String addText = null;
        if (cursorPosition < passwordLength) {
            addText = s;
            password[cursorPosition] = s;
            cursorPosition++;
            if (cursorPosition == passwordLength) {
                isInputComplete = true;
                if (passwordListener != null) {
                    passwordListener.passwordComplete();
                }
            }
        }
        return addText;
    }

    private String delete() {
        String deleteText = null;
        if (cursorPosition > 0) {
            deleteText = password[cursorPosition - 1];
            password[cursorPosition - 1] = null;
            cursorPosition--;
        } else if (cursorPosition == 0) {
            deleteText = password[cursorPosition];
            password[cursorPosition] = null;
        }
        isInputComplete = false;
        return deleteText;
    }

    private String getPassword() {
        StringBuffer stringBuffer = new StringBuffer();
        for (String c : password) {
            if (TextUtils.isEmpty(c)) {
                continue;
            }
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }
}
