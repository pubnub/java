package com.pubnub.api.endpoints.objects_api.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static com.pubnub.api.endpoints.objects_api.utils.Include.INCLUDE_PARAM_NAME;

@Getter
@Setter
@AllArgsConstructor
public class IncludeParamValue implements ParameterEnricher {

    private static final String COMMA = ",";
    private String valueName;
    private boolean includeValueNameInIncludeParamValueList;


    @Override
    public Map<String, String> enrichParameters(Map<String, String> baseParams) {
        if (includeValueNameInIncludeParamValueList) {
            final Map<String, String> enrichedMap = new HashMap<>(baseParams);
            String currentIncludeValue = enrichedMap.get(INCLUDE_PARAM_NAME);
            String newIncludeList = addParameterToCurrentList(currentIncludeValue, valueName);
            enrichedMap.put(INCLUDE_PARAM_NAME, newIncludeList);
            return enrichedMap;
        } else {
            return baseParams;
        }

    }

    private String addParameterToCurrentList(String currentIncludeValue, String toBeAdded) {
        if (currentIncludeValue == null || currentIncludeValue.isEmpty()) {
            return toBeAdded;
        } else {
            return currentIncludeValue + COMMA + toBeAdded;
        }
    }
}
