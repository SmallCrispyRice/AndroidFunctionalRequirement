package com.example.video_play.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.video_play.R;

public class VerticalProgressLayout extends ConstraintLayout {
    public interface OnListener{
        void getProgress(float progress);
        default void onFingerUp(float progress){

        }
    }
    private OnListener onListener;
    private VerticalProgressLayout mViewGroup;
    private VerticalProgressView verticalProgressView;
    private TextView tvProgress;
    public VerticalProgressLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VerticalProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VerticalProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.vertical_progress_layout,this);
        verticalProgressView = view.findViewById(R.id.vp_volume);
        tvProgress = view.findViewById(R.id.tv_progress);
        mViewGroup = this;
        verticalProgressView.setOnListener(new VerticalProgressView.OnListener(){
            @Override
            public void getProgress(float progress) {
                if (onListener != null){
                    onListener.getProgress(progress);
                }
            }

            @Override
            public void onHideParent() {
                mViewGroup.setVisibility(View.GONE);
            }

            @Override
            public void onFingerUp(float progress) {
                if (onListener != null){
                    onListener.onFingerUp(progress);
                }
            }
        });
    }

    public VerticalProgressView getVerticalProgressView(){
        return verticalProgressView;
    }

    public TextView getTvProgress(){
        return tvProgress;
    }

    public void setProgress(float progress){
        verticalProgressView.setProgress(progress);
    }

    public void setOnListener(OnListener onListener){
        this.onListener = onListener;
    }
}
