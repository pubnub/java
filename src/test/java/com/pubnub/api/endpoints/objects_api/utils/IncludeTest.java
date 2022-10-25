package com.pubnub.api.endpoints.objects_api.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncludeTest {
    private Include objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = new Include();
    }

    @Test
    void when_inclusionFlags_are_not_empty_then_should_enrich_base_param_map() {
        objectUnderTest.addInclusionFlag("custom");
        Map<String, String> baseParams = createBaseParams();

        Map<String, String> enrichedParameterMap = objectUnderTest.enrichParameters(baseParams);

        assertTrue(enrichedParameterMap.containsKey("include"));
    }

    @Test
    void when_inclusionFlags_are_empty_then_should_not_enrich_base_param_map() {
        Map<String, String> baseParams = createBaseParams();

        Map<String, String> enrichedParameterMap = objectUnderTest.enrichParameters(baseParams);

        assertFalse(enrichedParameterMap.containsKey("include"));
    }

    private Map<String, String> createBaseParams() {
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("pnsdk", "PubNub-Java-Unified/6.2.0");
        baseParams.put("uuid", "pn-e9070802-aaf9-4464-bdfb-5f9ece213c5b");
        baseParams.put("requestid", "e7a445d6-3c66-465d-8730-0da23bcb8fcf");
        return baseParams;
    }
}