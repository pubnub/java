package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.endpoints.publishKT.TelemetryManagerExternal;
import com.pubnub.api.managers.TelemetryManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TelemetryManagerExternalImpl implements TelemetryManagerExternal {
    private TelemetryManager telemetryManager;

    public TelemetryManagerExternalImpl(TelemetryManager telemetryManager) {
        this.telemetryManager = telemetryManager;
    }

    @NotNull
    @Override
    public Map<String, String> operationsLatency(long currentDate) {
        return telemetryManager.operationsLatency();
    }

    @Override    //ToDo we should somehow replace PNOperationType that is in Kotlin with e.g. string
    public void storeLatency(long latency, @NotNull String pnOperationType, long currentDate) {
        telemetryManager.storeLatency(latency, pnOperationType);
    }
}
