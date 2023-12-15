package com.pubnub.api.managers;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.ReconnectionCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class DelayedReconnectionManager {
    private static final int DELAY_SECONDS = 3;
    private static final int MAX_RANDOM = 3;
    private static final int MILLISECONDS = 1000;

    private final PNReconnectionPolicy pnReconnectionPolicy;
    private ReconnectionCallback callback;
    private PubNub pubnub;
    private Random random = new Random();

    /**
     * Timer for heartbeat operations.
     */
    private Timer timer;

    public DelayedReconnectionManager(PubNub pubnub) {
        this.pubnub = pubnub;
        this.pnReconnectionPolicy = pubnub.getConfiguration().getReconnectionPolicy();
    }

    public void scheduleDelayedReconnection() {
        stop();
        if (isReconnectionPolicyUndefined()) {
            return;
        }

        timer = new Timer("Delayed Reconnection Manager timer", true);
        int effectiveDelayInMilliSeconds = (int) ((DELAY_SECONDS + getRandomDelay()) * MILLISECONDS);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callTime();
            }
        }, effectiveDelayInMilliSeconds);
    }

    public void setReconnectionListener(ReconnectionCallback reconnectionCallback) {
        this.callback = reconnectionCallback;
    }

    void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private boolean isReconnectionPolicyUndefined() {
        if (pnReconnectionPolicy == null || pnReconnectionPolicy == PNReconnectionPolicy.NONE) {
            log.warn("reconnection policy is disabled, please handle reconnection manually.");
            return true;
        }
        return false;
    }

    private double getRandomDelay() {
        double randomDelay = MAX_RANDOM * random.nextDouble();
        randomDelay = roundTo3DecimalPlaces(randomDelay);
        return randomDelay;
    }

    private void callTime() {
        stop();
        callback.onReconnection();
    }

    private double roundTo3DecimalPlaces(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        return Double.parseDouble(decimalFormat.format(value));
    }
}
