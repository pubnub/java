package com.pubnub.api.endpoints.files;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.models.consumer.files.PNFileUrlResult;
import okhttp3.HttpUrl;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class GetFileUrlTest {

    private final String channel = "channel";
    private final String fileName = "fileName";
    private final String fileId = "fileId";
    private final Set<String> defaultQueryParams = new HashSet<>(Arrays.asList("pnsdk", "requestid", "uuid"));

    @Test
    public void noAdditionalQueryParamsWhenNotSecretNorAuth() throws PubNubException {
        //given
        PubNub pubnub = new PubNub(config());

        //when
        PNFileUrlResult result = pubnub.getFileUrl()
                .channel(channel)
                .fileName(fileName)
                .fileId(fileId)
                .sync();

        //then
        Collection<String> queryParamNames = queryParameterNames(result.getUrl());
        queryParamNames.removeAll(defaultQueryParams);
        assertEquals(Collections.emptySet(), queryParamNames);
    }

    @Test
    public void signatureAndTimestampQueryParamsAreSetWhenSecret() throws PubNubException {
        //given
        PubNub pubnub = new PubNub(withSecret(config()));

        //when
        PNFileUrlResult result = pubnub.getFileUrl()
                .channel(channel)
                .fileName(fileName)
                .fileId(fileId)
                .sync();

        //then
        Collection<String> queryParamNames = queryParameterNames(result.getUrl());
        queryParamNames.removeAll(defaultQueryParams);
        assertThat(queryParamNames, Matchers.containsInAnyOrder("signature", "timestamp"));
    }

    @Test
    public void authQueryParamIsSetWhenAuth() throws PubNubException {
        //given
        PubNub pubnub = new PubNub(withAuth(config()));

        //when
        PNFileUrlResult result = pubnub.getFileUrl()
                .channel(channel)
                .fileName(fileName)
                .fileId(fileId)
                .sync();

        //then
        Collection<String> queryParamNames = queryParameterNames(result.getUrl());
        queryParamNames.removeAll(defaultQueryParams);
        assertThat(queryParamNames, Matchers.containsInAnyOrder("auth"));
    }

    @Test
    public void signatureAndTimestampAndAuthQueryParamsAreSetWhenSecretAndAuth() throws PubNubException {
        //given
        PubNub pubnub = new PubNub(withSecret(withAuth(config())));

        //when
        PNFileUrlResult result = pubnub.getFileUrl()
                .channel(channel)
                .fileName(fileName)
                .fileId(fileId)
                .sync();

        //then
        System.out.println(result.getUrl());
        Collection<String> queryParamNames = queryParameterNames(result.getUrl());
        queryParamNames.removeAll(defaultQueryParams);
        assertThat(queryParamNames, Matchers.containsInAnyOrder("auth", "signature", "timestamp"));
    }

    private PNConfiguration config() throws PubNubException {
        PNConfiguration config = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
        config.setPublishKey("pk");
        config.setSubscribeKey("sk");
        return config;
    }

    private PNConfiguration withSecret(PNConfiguration config) {
        config.setSecretKey("secK");
        return config;
    }

    private PNConfiguration withAuth(PNConfiguration config) {
        config.setAuthKey("ak");
        return config;
    }

    private Collection<String> queryParameterNames(String url) {
        return new HashSet<>(HttpUrl.get(url).queryParameterNames());
    }
}
