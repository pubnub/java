package com.pubnub.api;

class SubscribeManager extends AbstractSubscribeManager {

    public SubscribeManager(String name, int connectionTimeout,
            int requestTimeout) {
        super(name, connectionTimeout, requestTimeout);
    }

    public void clearRequestQueue() {
        _waiting.clear();
    }
}
