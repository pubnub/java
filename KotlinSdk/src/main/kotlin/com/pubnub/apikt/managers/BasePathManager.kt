package com.pubnub.apikt.managers

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PNConfiguration.Companion.isValid

internal class BasePathManager(private val config: PNConfiguration) {

    /**
     * for cache busting, the current subdomain number used.
     */
    private var currentSubdomain = 1

    /**
     * if using cache busting, this is the max number of subdomains that are supported.
     */
    private val MAX_SUBDOMAIN = 20

    /**
     * default subdomain used if cache busting is disabled.
     */

    private val DEFAULT_SUBDOMAIN = "ps"

    /**
     * default base path if a custom one is not provided.
     */

    private val DEFAULT_BASE_PATH = "pndsn.com"

    fun basePath(): String {
        val basePathBuilder = StringBuilder("http")
            .append(if (config.secure) "s" else "")
            .append("://")

        when {
            config.origin.isValid() -> {
                basePathBuilder.append(config.origin)
            }
            config.cacheBusting -> {
                basePathBuilder
                    .append("ps")
                    .append(currentSubdomain)
                    .append(".")
                    .append(DEFAULT_BASE_PATH)

                incrementSubdomain()
            }
            else -> {
                basePathBuilder
                    .append(DEFAULT_SUBDOMAIN)
                    .append(".")
                    .append(DEFAULT_BASE_PATH)
            }
        }

        return basePathBuilder.toString()
    }

    private fun incrementSubdomain() {
        if (currentSubdomain == MAX_SUBDOMAIN) {
            currentSubdomain = 1
        } else {
            currentSubdomain++
        }
    }
}
