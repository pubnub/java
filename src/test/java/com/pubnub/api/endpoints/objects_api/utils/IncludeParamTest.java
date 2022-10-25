package com.pubnub.api.endpoints.objects_api.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class IncludeParamTest {
    private static final String MY_PARAM_VALUE = "ValueToBeAddedToIncludeParametersValuesList";
    private static final String EXISTING_INCLUDE_PARAMETER_VALUES_LIST = "custom,channel.custom";
    private static final String COMMA = ",";
    private IncludeParamValue objectUnderTest;


    @Test
    void when_includeStatusInParams_false_should_not_add_param_value_to_include_parameter() {
        objectUnderTest = new IncludeParamValue(MY_PARAM_VALUE, false);
        Map<String, String> baseParams = createBaseParams();
        Map<String, String> enrichedMap = objectUnderTest.enrichParameters(baseParams);

        assertFalse(enrichedMap.containsKey("include"));
    }

    @Test
    void when_includeStatusInParams_true_and_include_param_already_exist_should_add_param_value_to_existing_include_parameter_values_list() {
        objectUnderTest = new IncludeParamValue(MY_PARAM_VALUE, true);
        Map<String, String> baseParams = createBaseParams();
        baseParams.put("include", EXISTING_INCLUDE_PARAMETER_VALUES_LIST);

        Map<String, String> enrichedMap = objectUnderTest.enrichParameters(baseParams);

        String includeValues = enrichedMap.get("include");
        assertEquals(EXISTING_INCLUDE_PARAMETER_VALUES_LIST + COMMA + MY_PARAM_VALUE, includeValues);
    }

    @Test
    void when_includeStatusInParams_true_and_include_param_not_exist_should_add_include_to_params_map_with_paramName() {
        objectUnderTest = new IncludeParamValue(MY_PARAM_VALUE, true);
        Map<String, String> baseParams = createBaseParams();

        Map<String, String> enrichedMap = objectUnderTest.enrichParameters(baseParams);

        String includeValues = enrichedMap.get("include");
        assertEquals(MY_PARAM_VALUE, includeValues);
    }

    private Map<String, String> createBaseParams() {
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("pnsdk", "PubNub-Java-Unified/6.2.0");
        baseParams.put("uuid", "pn-e9070802-aaf9-4464-bdfb-5f9ece213c5b");
        baseParams.put("requestid", "e7a445d6-3c66-465d-8730-0da23bcb8fcf");
        return baseParams;
    }
}
