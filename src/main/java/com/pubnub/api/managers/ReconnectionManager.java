package com.pubnub.api.managers;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.ReconnectionCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.PNTimeResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


@Slf4j
public class ReconnectionManager {

    private static final int LINEAR_INTERVAL = 3;
    private static final int MAX_RANDOM = 3;
    private static final int MIN_EXPONENTIAL_BACKOFF = 2;
    private static final int MAX_EXPONENTIAL_BACKOFF = 32;

    private static final int MILLISECONDS = 1000;
    public static final int MAXIMUM_RECONNECTION_RETRIES_DEFAULT = 10;

    private ReconnectionCallback callback;
    private PubNub pubnub;

    private int exponentialMultiplier = 1;
    private int failedCalls = 0;

    private PNReconnectionPolicy pnReconnectionPolicy;
    private int maxConnectionRetries;
    private Random random = new Random();

    /**
     * Timer for heartbeat operations.
     */
    private Timer timer;

    public ReconnectionManager(PubNub pubnub) {
        this.pubnub = pubnub;
        this.pnReconnectionPolicy = pubnub.getConfiguration().getReconnectionPolicy();
        this.maxConnectionRetries = getMaximumReconnectionRetries();
    }

    private int getMaximumReconnectionRetries() {
        int maximumReconnectionRetries = pubnub.getConfiguration().getMaximumReconnectionRetries();
        if (maximumReconnectionRetries < 0 || maximumReconnectionRetries > MAXIMUM_RECONNECTION_RETRIES_DEFAULT) {
            maximumReconnectionRetries = MAXIMUM_RECONNECTION_RETRIES_DEFAULT;
        }
        log.debug("maximumReconnectionRetries is: " + maximumReconnectionRetries);
        return maximumReconnectionRetries;
    }

    public void setReconnectionListener(ReconnectionCallback reconnectionCallback) {
        this.callback = reconnectionCallback;
    }

    public void startPolling() {
        if (isReconnectionPolicyUndefined()) {
            return;
        }

        exponentialMultiplier = 1;
        failedCalls = 0;

        registerRetryTimer();
    }

    private void registerRetryTimer() {
        // make sure only one timer is running at a time.
        stopHeartbeatTimer();

        if (isReconnectionPolicyUndefined()) {
            return;
        }

        if (failedCalls >= maxConnectionRetries) {
            callback.onMaxReconnectionExhaustion();
            return;
        }

        timer = new Timer("Reconnection Manager timer", true);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callTime();
            }
        }, getNextIntervalInMilliSeconds());
    }

    int getNextIntervalInMilliSeconds() {
        int timerInterval = LINEAR_INTERVAL;
        failedCalls++;

        if (pnReconnectionPolicy == PNReconnectionPolicy.EXPONENTIAL) {
            exponentialMultiplier++;
            timerInterval = (int) (Math.pow(2, exponentialMultiplier) - 1);
            if (timerInterval > MAX_EXPONENTIAL_BACKOFF) {
                timerInterval = MIN_EXPONENTIAL_BACKOFF;
                exponentialMultiplier = 1;
                log.debug("timerInterval > MAXEXPONENTIALBACKOFF at: " + Calendar.getInstance().getTime().toString());
            } else if (timerInterval < 1) {
                timerInterval = MIN_EXPONENTIAL_BACKOFF;
            }
            timerInterval = (int) ((timerInterval + getRandomDelay()) * MILLISECONDS);
            log.debug("timerInterval = " + timerInterval + "ms at: " + Calendar.getInstance().getTime().toString());
        }

        if (pnReconnectionPolicy == PNReconnectionPolicy.LINEAR) {
            timerInterval = (int) ((LINEAR_INTERVAL + getRandomDelay()) * MILLISECONDS);
        }
        return timerInterval;
    }

    private double getRandomDelay() {
        double randomDelay = MAX_RANDOM * random.nextDouble();
        return roundTo3DecimalPlaces(randomDelay);
    }

    private double roundTo3DecimalPlaces(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        return Double.parseDouble(decimalFormat.format(value));
    }

    private void stopHeartbeatTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void callTime() {
        pubnub.time().async(new PNCallback<PNTimeResult>() {
            @Override
            public void onResponse(PNTimeResult result, @NotNull PNStatus status) {
                if (!status.isError()) {
                    stopHeartbeatTimer();
                    callback.onReconnection();
                } else {
                    log.debug("callTime() at: " + Calendar.getInstance().getTime().toString());
                    registerRetryTimer();
                }
            }
        });
    }

    private boolean isReconnectionPolicyUndefined() {
        if (pnReconnectionPolicy == null || pnReconnectionPolicy == PNReconnectionPolicy.NONE) {
            log.warn("reconnection policy is disabled, please handle reconnection manually.");
            return true;
        }
        return false;
    }
}
