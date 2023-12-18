package com.pubnub.api.managers;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.ReconnectionCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.PNTimeResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


@Slf4j
public class ReconnectionManager {

    private static final int BASE_LINEAR_INTERVAL_IN_MILLISECONDS = 3000;
    private static final int MIN_EXPONENTIAL_BACKOFF = 2;
    private static final int MAX_EXPONENTIAL_BACKOFF = 32;

    private static final int BOUND = 1000;
    private static final int MILLISECONDS = BOUND;
    private static final int MAXIMUM_RECONNECTION_RETRIES_DEFAULT = 10;

    private ReconnectionCallback callback;
    private PubNub pubnub;

    private int exponentialMultiplier = 1;
    private int failedCalls = 0;

    private PNReconnectionPolicy pnReconnectionPolicy;
    private int maxConnectionRetries;
    private final Random random = new Random();

    /**
     * Timer for heartbeat operations.
     */
    private Timer timer;

    public ReconnectionManager(PubNub pubnub) {
        this.pubnub = pubnub;
        this.pnReconnectionPolicy = pubnub.getConfiguration().getReconnectionPolicy();
        this.maxConnectionRetries = pubnub.getConfiguration().getMaximumReconnectionRetries();
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

        if (maxConnectionIsNotSetToInfinite() && failedCalls >= maxConnectionRetries) {
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

    private boolean maxConnectionIsNotSetToInfinite() {
        return maxConnectionRetries != -1;
    }

    int getNextIntervalInMilliSeconds() {
        int timerInterval = 0;
        failedCalls++;

        if (pnReconnectionPolicy == PNReconnectionPolicy.EXPONENTIAL) {
            exponentialMultiplier++;
            timerInterval = (int) (Math.pow(2, exponentialMultiplier) - 1);
            if (timerInterval > MAX_EXPONENTIAL_BACKOFF) {
                timerInterval = MIN_EXPONENTIAL_BACKOFF;
                exponentialMultiplier = 1;
                log.debug("timerInterval > MAXEXPONENTIALBACKOFF at: " + Calendar.getInstance().getTime());
            } else if (timerInterval < 1) {
                timerInterval = MIN_EXPONENTIAL_BACKOFF;
            }
            timerInterval = (int) ((timerInterval * MILLISECONDS) + getRandomDelayInMilliSeconds());
            log.debug("timerInterval = " + timerInterval + "ms at: " + Calendar.getInstance().getTime());
        }

        if (pnReconnectionPolicy == PNReconnectionPolicy.LINEAR) {
            timerInterval = (int) (BASE_LINEAR_INTERVAL_IN_MILLISECONDS + getRandomDelayInMilliSeconds());
        }
        return timerInterval;
    }

    private double getRandomDelayInMilliSeconds() {
        return random.nextInt(BOUND);
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
