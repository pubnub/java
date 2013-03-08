package com.pubnub.api;

import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Vector;

class SubscribeWorker extends AbstractSubscribeWorker {

    SubscribeWorker(Vector _requestQueue, int connectionTimeout,
            int requestTimeout, int maxRetries, int retryInterval, Hashtable headers) {
        super(_requestQueue, connectionTimeout, requestTimeout,
        		maxRetries, retryInterval, headers);
    }

    void process(HttpRequest hreq) {
        HttpResponse hresp = null;
        int currentRetryAttempt = (hreq.isDar())?1:maxRetries;
        log.verbose("disconnectAndResubscribe is " + hreq.isDar());
        while (currentRetryAttempt <= maxRetries) {
            try {
                log.debug(hreq.getUrl());
                hresp = httpclient.fetch(hreq.getUrl(), hreq.getHeaders());
                if (hresp != null
                        && httpclient.checkResponseSuccess(hresp
                                .getStatusCode())) {
                    currentRetryAttempt = 1;
                    break;
                }
            } catch (SocketTimeoutException e) {
                log.verbose("No Traffic , Read Timeout Exception in Fetch : " + e.toString());
                if (hreq.isDar()) {
                	hreq.getResponseHandler().handleBackFromDar(hreq);
                	return;
                }
                break;
                
            } catch (Exception e) {
                log.verbose("Retry Attempt : " + ((currentRetryAttempt == maxRetries)?"last":currentRetryAttempt)
                        + " Exception in Fetch : " + e.toString());
                currentRetryAttempt++;
            }

            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
            }
        }
        if (!_die) {
            if (hresp == null) {
                log.debug("Error in fetching url : " + hreq.getUrl());
                if (hreq.isDar()) {
                    log.verbose("Exhausted number of retries");
                    hreq.getResponseHandler().handleTimeout(hreq);
                } else {
                    hreq.getResponseHandler().handleError(hreq, "Request Timeout");
                }
                return;
            }
            log.debug(hresp.getResponse());
            hreq.getResponseHandler().handleResponse(hreq, hresp.getResponse());
        }

    }
}