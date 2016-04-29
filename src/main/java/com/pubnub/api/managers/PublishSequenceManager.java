package com.pubnub.api.managers;

public class PublishSequenceManager {


    private int maxSequence;
    private int nextSequence;

    public PublishSequenceManager(final int providedMaxSequence) {
        this.maxSequence = providedMaxSequence;
    }

    public final synchronized int getNextSequence() {
        if (maxSequence == nextSequence) {
            nextSequence = 1;
        } else {
            nextSequence += 1;
        }

        return nextSequence;
    }

}
