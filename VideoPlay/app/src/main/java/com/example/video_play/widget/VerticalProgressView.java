package com.example.video_play.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.video_play.R;
import com.example.video_play.util.CustomLog;
import com.example.video_play.util.DensityUtil;

/**
 * 垂直进度条
 * 没有文字显示，后面使用组合View或者直接TextView解决这个问题
 */
public class VerticalProgressView extends View {
    private static float DEF_PROGRESS_STROKE_VIEW_HEIGHT = 90f;//进度条整体高度 对应蓝色边框高度
    private static float DEF_PROGRESS_STROKE_VIEW_WEIGHT = 8f;//进度条整体宽度 对应蓝色边框宽度
    private static int DELAY_TIME = 1000;//手指松开后，多久后View消失
    private static int DEF_RADIUS_DIFFERENCE = 1;//大小圆半径之差 要小于或等于 垂直/水平间距最小值，不然大圆超过View范围 单位:dp
    private static int DEF_VERTICAL_GAP = 1;//垂直间隙 单位:dp
    private static int DEF_HORIZONTAL_GAP = 10;//水平间隙 单位:dp
    private static int DEF_PROGRESS_STOKE_WIGHT = 1;//蓝色边框宽度 单位:dp
    private Paint fillRectPaint,strokeRectPint;
    private Paint progressPaint;
    private Paint smallCirclePaint;
    private Paint bigCirclePaint;
    private float progressTop = 0f;//进度条顶部坐标
    private float startY = 0f, endY = 0f;
    private float circleX = 0f, circleY = 0f;
    private int defViewHeight, defViewWidth;
    private int viewHeight, viewWidth;
    private int progressRectHeight;
    private float strokeWidth = 0f;//蓝色边框宽度 单位:像素
    private float progressStrokeRectLeft, progressStrokeRectTop, progressStrokeRectRight, progressStrokeRectBottom;//进度条蓝色边框矩形绘制左上右下
    private float verticalGap,horizontalGap;
    private float rx, ry;//进度条矩形 圆角半径
    private float bigCircleRadius, smallCircleRadius;
    private float radiusDifference;//大小圆半径差
    private float progress = 0f;//进度条进度

    public interface OnListener{
        void getProgress(float progress);
        void onHideParent();

        default void onFingerUp(float progress) {

        }
    }
    private OnListener onListener;
    public void setOnListener(OnListener onListener){
        this.onListener = onListener;
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (onListener!=null){
                onListener.onHideParent();
            }
        }
    };


    public VerticalProgressView(Context context) {
        super(context);
        init(context);
    }

    public VerticalProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VerticalProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        fillRectPaint = new Paint();
        fillRectPaint.setColor(Color.WHITE);
        fillRectPaint.setStyle(Paint.Style.FILL);
        fillRectPaint.setAntiAlias(true);

        strokeWidth = (float) DensityUtil.dp2px(context,DEF_PROGRESS_STOKE_WIGHT);
        strokeRectPint = new Paint();
        strokeRectPint.setColor(context.getResources().getColor(R.color.text_blue));
        strokeRectPint.setStyle(Paint.Style.STROKE);
        strokeRectPint.setStrokeWidth(strokeWidth);
        strokeRectPint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(context.getResources().getColor(R.color.text_blue));
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setAntiAlias(true);

        smallCirclePaint = new Paint();
        smallCirclePaint.setColor(context.getResources().getColor(R.color.circle_small));
        smallCirclePaint.setStyle(Paint.Style.FILL);
        smallCirclePaint.setAntiAlias(true);

        bigCirclePaint = new Paint();
        bigCirclePaint.setColor(Color.WHITE);
        bigCirclePaint.setStyle(Paint.Style.FILL);
        bigCirclePaint.setAntiAlias(true);

        defViewWidth = DensityUtil.dp2px(context, DEF_PROGRESS_STROKE_VIEW_WEIGHT + DEF_HORIZONTAL_GAP * 2);
        defViewHeight = DensityUtil.dp2px(context, DEF_PROGRESS_STROKE_VIEW_HEIGHT + DEF_VERTICAL_GAP * 2);


        verticalGap = DensityUtil.dp2px(context, DEF_VERTICAL_GAP);
        horizontalGap = DensityUtil.dp2px(context, DEF_HORIZONTAL_GAP);
        radiusDifference = DensityUtil.dp2px(context, DEF_RADIUS_DIFFERENCE);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            //该代码用于，VerticalProgressView在从首页进入视频界面时候，会触发onVisibilityChanged方法，且为View.VISIBLE
            //若只有postDelayed，在刚进入视频界面就快速点击按钮让垂直进度条显示，由于已经触发postDelayed，会导致进度条很快就消失
            //因此在点击按钮让VerticalProgressView显示的时候，需要先removeCallbacks掉，在重新定时执行
            CustomLog.INSTANCE.d("VISIBLE");
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable,DELAY_TIME);
        }else if (visibility == View.GONE || visibility == View.INVISIBLE){
            handler.removeCallbacks(runnable);
            CustomLog.INSTANCE.d("GONE OR INVISIBLE");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defViewWidth, defViewHeight);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defViewWidth, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, defViewHeight);
        } else {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        progressStrokeRectLeft = horizontalGap;
        progressStrokeRectRight = viewWidth - horizontalGap;
        rx = (progressStrokeRectRight - progressStrokeRectLeft) / 2;
        ry = rx;
        smallCircleRadius = (progressStrokeRectRight - progressStrokeRectLeft) / 2;
        bigCircleRadius = smallCircleRadius + radiusDifference;

        progressStrokeRectTop = verticalGap + bigCircleRadius;
        progressStrokeRectBottom = viewHeight - verticalGap - bigCircleRadius;

        progressRectHeight = (int) (progressStrokeRectBottom - progressStrokeRectTop);

        progressTop = progressStrokeRectBottom - progressRectHeight * progress;

        circleX = (progressStrokeRectLeft + progressStrokeRectRight) / 2;
        circleY = progressTop;
        CustomLog.INSTANCE.d("onSizeChanged","progress:"+progress);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(progressStrokeRectLeft, progressStrokeRectTop, progressStrokeRectRight, progressStrokeRectBottom, rx, ry, fillRectPaint);
//        canvas.drawRoundRect(progressStrokeRectLeft, progressStrokeRectTop, progressStrokeRectRight, progressStrokeRectBottom, rx, ry, strokeRectPint);
        canvas.drawRoundRect(progressStrokeRectLeft + strokeWidth/2, progressStrokeRectTop + strokeWidth/2, progressStrokeRectRight - strokeWidth/2, progressStrokeRectBottom - strokeWidth/2, rx, ry, strokeRectPint);
        canvas.drawRoundRect(progressStrokeRectLeft, progressTop, progressStrokeRectRight, progressStrokeRectBottom, rx, ry, progressPaint);
        canvas.drawCircle(circleX, circleY, bigCircleRadius, bigCirclePaint);
        canvas.drawCircle(circleX, circleY, smallCircleRadius, smallCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                handler.removeCallbacks(runnable);
                break;
            case MotionEvent.ACTION_MOVE:
                endY = event.getY();
                float changeY = endY - startY;
                progressTop += changeY;
                if (progressTop < progressStrokeRectTop) {
                    progressTop = progressStrokeRectTop;
                } else if (progressTop > progressStrokeRectBottom) {
                    progressTop = progressStrokeRectBottom;
                }
                circleY = progressTop;
                startY = endY;
                progress = (progressStrokeRectBottom - progressTop)/(progressStrokeRectBottom - progressStrokeRectTop);
                if (onListener!=null){//注意onListener要赋值，不然报空错误
                    onListener.getProgress(progress);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (onListener!=null){//注意onListener要赋值，不然报空错误
                    onListener.onFingerUp(progress);
                }
                handler.postDelayed(runnable,DELAY_TIME);
                break;
        }
        postInvalidate();
        return true;
    }

    /** 设置进度条进度 */
    public void setProgress(float progress){
        this.progress = progress;
        progressTop = progressStrokeRectBottom - progressRectHeight * progress;
        if (progressTop < progressStrokeRectTop) {
            progressTop = progressStrokeRectTop;
        } else if (progressTop > progressStrokeRectBottom) {
            progressTop = progressStrokeRectBottom;
        }
        circleY = progressTop;
        postInvalidate();
    }
}
