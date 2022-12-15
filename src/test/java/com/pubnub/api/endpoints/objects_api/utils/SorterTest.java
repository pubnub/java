package com.pubnub.api.endpoints.objects_api.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class SorterTest {
    @Test
    public void sorterProducesCorrectQueryParam() {
        //given
        final Sorter sorterUnderTest = new Sorter();

        final PNSortKey nameSortKey = PNSortKey.asc(PNSortKey.Key.NAME);
        final PNSortKey updatedSortKey = PNSortKey.desc(PNSortKey.Key.UPDATED);

        sorterUnderTest.addSortKeys(Arrays.asList(nameSortKey, updatedSortKey));

        //when
        final Map<String, String> enrichedParametersMap = sorterUnderTest.enrichParameters(Collections.emptyMap());

        //then
        final String parameterValue = enrichedParametersMap.get(Sorter.SORT_PARAM_NAME);

        final String expectedNameSortParamValue = nameSortKey.getKey().getFieldName() + ":"
                + nameSortKey.getDir().getDir();
        final String expectedUpdatedParamValue = updatedSortKey.getKey().getFieldName() + ":"
                + updatedSortKey.getDir().getDir();
        assertThat(parameterValue, allOf(containsString(expectedNameSortParamValue),
                containsString(expectedUpdatedParamValue)));
    }
}