/*
 * from:
 *
 * http://stackoverflow.com/questions/2537238/how-can-i-get-zoom-functionality-for-images
 *
 *
 */
package com.kinvey.sample.contentviewr.component;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


public class ZoomImageView extends ImageView {

    static final int MAX_SCALE_FACTOR = 2;

    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;





    public ZoomImageView(Context context) {
        super(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Getting initial Image matrix
        mViewMatrix = new Matrix(this.getImageMatrix());
        mMinScaleMatrix = new Matrix(mViewMatrix);
        float initialScale = getMatrixScale(mViewMatrix);


        if (initialScale < 1.0f) // Image is bigger than screen
            mMaxScale = MAX_SCALE_FACTOR;
        else
            mMaxScale = MAX_SCALE_FACTOR * initialScale;

        mMinScale = getMatrixScale(mMinScaleMatrix);
    }



    public boolean onTouchEvent(MotionEvent event){
        return onTouch(this, event);

    }



    public boolean onTouch(View v, MotionEvent event) {

        ImageView view = (ImageView) v;
        // We set scale only after onMeasure was called and automatically fit image to screen
        if(!mWasScaleTypeSet) {
            view.setScaleType(ImageView.ScaleType.MATRIX);
            mWasScaleTypeSet = true;
        }

        float scale;

        dumpEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // first finger down only
                mCurSavedMatrix.set(mViewMatrix);
                start.set(event.getX(), event.getY());
                mCurrentMode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted
            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                mCurrentMode = NONE;

                float resScale = getMatrixScale(mViewMatrix);

                if (resScale > mMaxScale) {
                    downscaleMatrix(resScale, mViewMatrix);
                } else if (resScale < mMinScale)
                    mViewMatrix = new Matrix(mMinScaleMatrix);
                else if ((resScale - mMinScale) < 0.1f) // Don't allow user to drag picture outside in case of FIT TO WINDOW zoom
                    mViewMatrix = new Matrix(mMinScaleMatrix);
                else
                    break;

                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
                mOldDist = spacing(event);
//                Helper.LOGD(TAG, "oldDist=" + mOldDist);
                if (mOldDist > 5f) {
                    mCurSavedMatrix.set(mViewMatrix);
                    midPoint(mCurMidPoint, event);
                    mCurrentMode = ZOOM;
                    //Helper.LOGD(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mCurrentMode == DRAG) {
                    mViewMatrix.set(mCurSavedMatrix);
                    mViewMatrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                } else if (mCurrentMode == ZOOM) {
                    // pinch zooming
                    float newDist = spacing(event);
                   // Helper.LOGD(TAG, "newDist=" + newDist);
                    if (newDist > 1.f) {
                        mViewMatrix.set(mCurSavedMatrix);
                        scale = newDist / mOldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        mViewMatrix.postScale(scale, scale, mCurMidPoint.x, mCurMidPoint.y);
                    }
                }
                break;
        }

        view.setImageMatrix(mViewMatrix); // display the transformation on screen

        return true; // indicate event was handled
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////// PRIVATE SECTION ///////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// These matrices will be used to scale points of the image
    private Matrix mViewMatrix = new Matrix();
    private Matrix mCurSavedMatrix = new Matrix();
    // These PointF objects are used to record the point(s) the user is touching
    private PointF start = new PointF();
    private PointF mCurMidPoint = new PointF();
    private float mOldDist = 1f;

    private Matrix mMinScaleMatrix;
    private float mMinScale;
    private float mMaxScale;
    float[] mTmpValues = new float[9];
    private boolean mWasScaleTypeSet;

    private int mCurrentMode;


    /**
     * Returns scale factor of the Matrix
     * @param matrix
     * @return
     */
    private float getMatrixScale(Matrix matrix) {
        matrix.getValues(mTmpValues);
        return mTmpValues[Matrix.MSCALE_X];
    }

    /**
     * Downscales matrix with the scale to maximum allowed scale factor, but the same translations
     * @param scale
     * @param dist
     */
    private void downscaleMatrix(float scale, Matrix dist) {
        float resScale = mMaxScale / scale;
        dist.postScale(resScale, resScale, mCurMidPoint.x, mCurMidPoint.y);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
