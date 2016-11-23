package com.kyvlabs.brrr2.utils;

import android.os.Handler;

import com.dd.processbutton.ProcessButton;

import java.util.Random;

public class ProgressGenerator {

    public interface OnCompleteListener {

        public void onComplete();
    }

    private OnCompleteListener mListener;
    private int mProgress;
    private boolean isStop = false;

    public ProgressGenerator(OnCompleteListener listener) {
        mListener = listener;
    }

    public void start(final ProcessButton button) {
        isStop = false;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgress += 10;
                button.setProgress(mProgress);
                if (mProgress < 100 && !isStop) {
                    handler.postDelayed(this, generateDelay());
                } else {
                    mListener.onComplete();
                    button.setProgress(0);
                }
            }
        }, generateDelay());
    }
    public  void stop(final ProcessButton button){
        isStop = true;
        button.setProgress(0);
    }

    private Random random = new Random();

    private int generateDelay() {
        return random.nextInt(1000);
    }
}
