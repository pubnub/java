package com.pubnub.api.endpoints.objects_api;

import com.pubnub.api.endpoints.objects_api.utils.Filter;
import com.pubnub.api.endpoints.objects_api.utils.Include;
import com.pubnub.api.endpoints.objects_api.utils.IncludeParamValue;
import com.pubnub.api.endpoints.objects_api.utils.Limiter;
import com.pubnub.api.endpoints.objects_api.utils.Pager;
import com.pubnub.api.endpoints.objects_api.utils.Sorter;
import com.pubnub.api.endpoints.objects_api.utils.TotalCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CompositeParameterEnricherTest {
    private CompositeParameterEnricher objectUnderTest;


    @Mock
    private Include include;
    @Mock
    private Sorter sorter;
    @Mock
    private Pager pager;
    @Mock
    private Filter filter;
    @Mock
    private TotalCounter totalCounter;
    @Mock
    private Limiter limiter;
    @Mock
    private IncludeParamValue includeStatus;
    @Mock
    private IncludeParamValue includeType;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void can_enrich_all_parameters() {
        objectUnderTest = new CompositeParameterEnricher(include, sorter, pager, filter, totalCounter, limiter, includeStatus, includeType);
        Map<String, String> baseParams = new HashMap<>();

        objectUnderTest.enrichParameters(baseParams);

        verify(include).enrichParameters(baseParams);
        verify(sorter).enrichParameters(baseParams);
        verify(pager).enrichParameters(baseParams);
        verify(filter).enrichParameters(baseParams);
        verify(totalCounter).enrichParameters(baseParams);
        verify(limiter).enrichParameters(baseParams);
        verify(includeStatus).enrichParameters(baseParams);
        verify(includeType).enrichParameters(baseParams);
    }

    @Test
    void can_enrich_specific_parameters() {
        objectUnderTest = new CompositeParameterEnricher(include, sorter, pager, filter, totalCounter, limiter, null, null);
        Map<String, String> baseParams = new HashMap<>();

        objectUnderTest.enrichParameters(baseParams);

        verify(include).enrichParameters(baseParams);
        verify(sorter).enrichParameters(baseParams);
        verify(pager).enrichParameters(baseParams);
        verify(filter).enrichParameters(baseParams);
        verify(totalCounter).enrichParameters(baseParams);
        verify(limiter).enrichParameters(baseParams);
        verify(includeStatus, times(0)).enrichParameters(baseParams);
        verify(includeType, times(0)).enrichParameters(baseParams);
    }
}
