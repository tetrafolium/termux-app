package com.termux.view;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/** A combination of {@link GestureDetector} and {@link ScaleGestureDetector}. */
final class GestureAndScaleRecognizer {

    public interface Listener {
        boolean onSingleTapUp(MotionEvent e);

        boolean onDoubleTap(MotionEvent e);

        boolean onScroll(MotionEvent e2, float dx, float dy);

        boolean onFling(MotionEvent e, float velocityX, float velocityY);

        boolean onScale(float focusX, float focusY, float scale);

        boolean onDown(float x, float y);

        boolean onUp(MotionEvent e);

        void onLongPress(MotionEvent e);
    }

    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    final Listener mListener;
    boolean isAfterLongPress;

    public GestureAndScaleRecognizer(final Context context, final Listener listener) {
        mListener = listener;

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float dx, final float dy) {
                return mListener.onScroll(e2, dx, dy);
            }

            @Override
            public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
                return mListener.onFling(e2, velocityX, velocityY);
            }

            @Override
            public boolean onDown(final MotionEvent e) {
                return mListener.onDown(e.getX(), e.getY());
            }

            @Override
            public void onLongPress(final MotionEvent e) {
                mListener.onLongPress(e);
                isAfterLongPress = true;
            }
        }, null, true /* ignoreMultitouch */);

        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(final MotionEvent e) {
                return mListener.onSingleTapUp(e);
            }

            @Override
            public boolean onDoubleTap(final MotionEvent e) {
                return mListener.onDoubleTap(e);
            }

            @Override
            public boolean onDoubleTapEvent(final MotionEvent e) {
                return true;
            }
        });

        mScaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(final ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public boolean onScale(final ScaleGestureDetector detector) {
                return mListener.onScale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
            }
        });
        mScaleDetector.setQuickScaleEnabled(false);
    }

    public void onTouchEvent(final MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isAfterLongPress = false;
                break;
            case MotionEvent.ACTION_UP:
                if (!isAfterLongPress) {
                    // This behaviour is desired when in e.g. vim with mouse events, where we do not
                    // want to move the cursor when lifting finger after a long press.
                    mListener.onUp(event);
                }
                break;
        }
    }

    public boolean isInProgress() {
        return mScaleDetector.isInProgress();
    }

}
