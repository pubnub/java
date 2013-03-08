package com.pubnub.api;

class NonSubscribeManager extends AbstractNonSubscribeManager {

    public NonSubscribeManager(String name, int connectionTimeout,
            int requestTimeout) {
        super(name, connectionTimeout, requestTimeout);
    }

    public void clearRequestQueue() {
        _waiting.clear();
    }
}
