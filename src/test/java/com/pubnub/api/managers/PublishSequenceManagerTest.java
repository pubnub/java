package com.pubnub.api.managers;

import org.junit.Assert;
import org.junit.Test;

public class PublishSequenceManagerTest {

    @Test
    public void testSequenceManager() {
        PublishSequenceManager publishSequenceManager = new PublishSequenceManager(2);

        Assert.assertEquals(1, publishSequenceManager.getNextSequence());
        Assert.assertEquals(2, publishSequenceManager.getNextSequence());
        Assert.assertEquals(1, publishSequenceManager.getNextSequence());
        Assert.assertEquals(2, publishSequenceManager.getNextSequence());
    }
}

