package com.pubnub.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pubnub.api.Crypto;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class History extends Endpoint<JsonNode, PNHistoryResult> {

    @Setter private String channel;
    @Setter private Long start;
    @Setter private Long end;
    @Setter private Boolean reverse;
    @Setter private Integer count;
    @Setter private Boolean includeTimetoken;

    public History(PubNub pubnub) {
        super(pubnub);
    }

    private interface HistoryService {
        @GET("v2/history/sub-key/{subKey}/channel/{channel}")
        Call<JsonNode> fetchHistory(@Path("subKey") String subKey,
                                    @Path("channel") String channel,
                                    @QueryMap Map<String, String> options);
    }

    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<JsonNode> doWork(Map<String, String> params) {

        HistoryService service = this.createRetrofit().create(HistoryService.class);

        if (reverse != null) {
            params.put("reverse", String.valueOf(reverse));
        }

        if (includeTimetoken != null) {
            params.put("include_token", String.valueOf(includeTimetoken));
        }

        if (count != null && count > 0 && count <= 10) {
            params.put("count", String.valueOf(count));
        } else {
            params.put("count", "100");
        }

        if (start != null) {
            params.put("start", Long.toString(start).toLowerCase());
        }
        if (end != null) {
            params.put("end", Long.toString(end).toLowerCase());
        }

        return service.fetchHistory(pubnub.getConfiguration().getSubscribeKey(), channel, params);
    }

    @Override
    protected PNHistoryResult createResponse(Response<JsonNode> input) throws PubNubException {
        PNHistoryResult.PNHistoryResultBuilder historyData = PNHistoryResult.builder();
        List<PNHistoryItemResult> messages = new ArrayList<>();

        if (input.body() != null) {
            historyData.startTimeToken(input.body().get(1).asLong());
            historyData.endTimeToken(input.body().get(2).asLong());

            ArrayNode historyItems = (ArrayNode) input.body().get(0);

            for (final JsonNode historyEntry : historyItems) {
                PNHistoryItemResult.PNHistoryItemResultBuilder historyItem = PNHistoryItemResult.builder();
                Object message;

                if (includeTimetoken != null && includeTimetoken) {
                    historyItem.timetoken(historyEntry.get("timetoken").asLong());
                    message = processMessage(historyEntry.get("message"));
                } else {
                    message = processMessage(historyEntry);
                }

                historyItem.entry(message);
                messages.add(historyItem.build());
            }

            historyData.messages(messages);
        }

        return historyData.build();
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNHistoryOperation;
    }

    private Object processMessage(JsonNode message) throws PubNubException {
        if (this.pubnub.getConfiguration().getCipherKey() == null) {
            return message;
        }

        Crypto crypto = new Crypto(pubnub.getConfiguration().getCipherKey());
        String outputText = crypto.decrypt(message.asText());

        ObjectMapper mapper = new ObjectMapper();
        Object outputObject;
        try {
            outputObject = mapper.readValue(outputText, JsonNode.class);
        } catch (IOException e) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).errormsg(e.getMessage()).build();
        }

        return outputObject;
    }

}