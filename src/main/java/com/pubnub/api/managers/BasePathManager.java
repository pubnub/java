package com.pubnub.api.managers;

import com.pubnub.api.PNConfiguration;

/**
 * A stateful manager to support base path construction, proxying and cache busting.
 */
public class BasePathManager {

    /**
     * PubNub configuration storage.
     */
    private PNConfiguration config;
    /**
     * for cache busting, the current subdomain number used.
     */
    private int currentSubdomain;

    /**
     * if using cache busting, this is the max number of subdomains that are supported.
     */
    private static final int MAX_SUBDOMAIN = 20;
    /**
     * default subdomain used if cache busting is disabled.
     */
    private static final String DEFAULT_SUBDOMAIN = "ps";
    /**
     * default base path if a custom one is not provided.
     */
    private static final String DEFAULT_BASE_PATH = "pndsn.com";

    /**
     * Initialize the path management.
     *
     * @param initialConfig configuration object
     */
    public BasePathManager(PNConfiguration initialConfig) {
        this.config = initialConfig;
        currentSubdomain = 1;
    }


    /**
     * Prepares a next usable base url.
     *
     * @return usable base url.
     */
    @SuppressWarnings("deprecation")
    public String getBasePath() {
        StringBuilder constructedUrl = new StringBuilder("http");

        if (config.isSecure()) {
            constructedUrl.append("s");
        }

        constructedUrl.append("://");

        if (config.getOrigin() != null) {
            constructedUrl.append(config.getOrigin());
        } else if (config.isCacheBusting()) {
            constructedUrl.append("ps").append(currentSubdomain).append(".").append(DEFAULT_BASE_PATH);

            if (currentSubdomain == MAX_SUBDOMAIN) {
                currentSubdomain = 1;
            } else {
                currentSubdomain += 1;
            }

        } else {
            constructedUrl.append(DEFAULT_SUBDOMAIN).append(".").append(DEFAULT_BASE_PATH);
        }

        return constructedUrl.toString();
    }

}
