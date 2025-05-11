package com.example.appGrabacion.utils;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * VideoView que siempre mide match_parent en ancho y alto,
 * de modo que el vídeo se recorta (center crop) en lugar de
 * dejar bandas negras o deformarse.
 */
public class FullScreenVideoView extends VideoView {

    public FullScreenVideoView(Context context) {
        super(context);
    }

    public FullScreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullScreenVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Obliga a que VideoView mida exactamente el tamaño del padre
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}