package com.android.inputmethod.keyboard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class KeyDrawable extends Drawable {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public KeyDrawable(int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        int radius = 10; // note this is actual pixels
        canvas.drawRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), radius, radius,  paint);
    }

    @Override
    public int getOpacity() {
        return 1;
    }

    @Override
    public void setAlpha(int arg0)
    {
    }

    @Override
    public void setColorFilter(ColorFilter arg0)
    {
    }
}
