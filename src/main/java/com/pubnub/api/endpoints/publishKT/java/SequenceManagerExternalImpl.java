package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.endpoints.publishKT.SequenceManagerExternal;
import com.pubnub.api.managers.PublishSequenceManager;

public class SequenceManagerExternalImpl implements SequenceManagerExternal {
    private PublishSequenceManager publishSequenceManager;

    public SequenceManagerExternalImpl(PublishSequenceManager publishSequenceManager) {
        this.publishSequenceManager = publishSequenceManager;
    }

    public int nextSequence() {
        return publishSequenceManager.getNextSequence();
    }
}
