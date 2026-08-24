package com.amitbharat.songsplayer.service;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SleepTimerManager {

    public interface OnSleepTimerListener {
        void onTimerFinish();
    }

    private static SleepTimerManager instance;
    private CountDownTimer countDownTimer;
    private final MutableLiveData<Long> remainingMillis = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>(false);
    private OnSleepTimerListener listener;

    private SleepTimerManager() {}

    public static synchronized SleepTimerManager getInstance() {
        if (instance == null) {
            instance = new SleepTimerManager();
        }
        return instance;
    }

    public void setListener(OnSleepTimerListener listener) {
        this.listener = listener;
    }

    public LiveData<Long> getRemainingMillis() {
        return remainingMillis;
    }

    public LiveData<Boolean> getIsTimerRunning() {
        return isTimerRunning;
    }

    /**
     * Starts a sleep timer with specified duration in minutes
     */
    public synchronized void startTimer(int minutes) {
        cancelTimer();

        long durationMillis = (long) minutes * 60 * 1000;
        isTimerRunning.postValue(true);
        remainingMillis.postValue(durationMillis);

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis.postValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                remainingMillis.postValue(0L);
                isTimerRunning.postValue(false);
                if (listener != null) {
                    listener.onTimerFinish();
                }
            }
        }.start();
    }

    public synchronized void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        remainingMillis.postValue(0L);
        isTimerRunning.postValue(false);
    }
}
