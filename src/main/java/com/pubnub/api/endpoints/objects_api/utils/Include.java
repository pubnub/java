package com.pubnub.api.endpoints.objects_api.utils;

import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Include implements ParameterEnricher {
    public static final String INCLUDE_PARAM_NAME = "include";
    static final String INCLUDE_CUSTOM_PARAM_VALUE = "custom";
    static final String INCLUDE_CHANNEL_PARAM_VALUE = "channel";
    static final String INCLUDE_CHANNEL_CUSTOM_PARAM_VALUE = "channel.custom";
    static final String INCLUDE_UUID_PARAM_VALUE = "uuid";
    static final String INCLUDE_UUID_CUSTOM_PARAM_VALUE = "uuid.custom";

    public interface CustomIncludeAware<T extends RemoteAction<?>> {
        T includeCustom(boolean includeCustom);
    }

    public interface HavingCustomInclude<T extends RemoteAction<?>>
            extends CustomIncludeAware<T>, HavingCompositeParameterEnricher {
        @Override
        default T includeCustom(boolean includeCustom) {
            if (includeCustom) {
                getCompositeParameterEnricher().getInclude().addInclusionFlag(INCLUDE_CUSTOM_PARAM_VALUE);
            }
            return (T) this;
        }
    }

    public enum PNChannelDetailsLevel {
        CHANNEL(INCLUDE_CHANNEL_PARAM_VALUE),
        CHANNEL_WITH_CUSTOM(INCLUDE_CHANNEL_CUSTOM_PARAM_VALUE);

        private final String paramValue;

        PNChannelDetailsLevel(final String paramValue) {
            this.paramValue = paramValue;
        }
    }

    public interface ChannelIncludeAware<T extends Endpoint<?, ?>> {
        T includeChannel(PNChannelDetailsLevel channelDetailsLevel);
    }

    public interface HavingChannelInclude<T extends Endpoint<?, ?>>
            extends ChannelIncludeAware<T>, HavingCompositeParameterEnricher {

        @Override
        default T includeChannel(final PNChannelDetailsLevel channelDetailsLevel) {
            getCompositeParameterEnricher().getInclude().addInclusionFlag(channelDetailsLevel.paramValue);
            return (T) this;
        }
    }

    public enum PNUUIDDetailsLevel {
        UUID(INCLUDE_UUID_PARAM_VALUE),
        UUID_WITH_CUSTOM(INCLUDE_UUID_CUSTOM_PARAM_VALUE);

        private final String paramValue;

        PNUUIDDetailsLevel(final String paramValue) {
            this.paramValue = paramValue;
        }
    }

    public interface UUIDIncludeAware<T extends Endpoint<?, ?>> {
        T includeUUID(PNUUIDDetailsLevel uuidDetailsLevel);
    }

    public interface HavingUUIDInclude<T extends Endpoint<?, ?>>
            extends UUIDIncludeAware<T>, HavingCompositeParameterEnricher {

        @Override
        default T includeUUID(PNUUIDDetailsLevel uuidDetailsLevel) {
            getCompositeParameterEnricher().getInclude().addInclusionFlag(uuidDetailsLevel.paramValue);
            return (T) this;
        }
    }

    private final List<String> inclusionFlags = new ArrayList<>();

    public void addInclusionFlag(final String inclusionFlag) {
        inclusionFlags.add(inclusionFlag);
    }

    @Override
    public Map<String, String> enrichParameters(Map<String, String> baseParams) {
        final Map<String, String> enrichedMap = new HashMap<>(baseParams);
        if (!inclusionFlags.isEmpty()) {
            enrichedMap.put(INCLUDE_PARAM_NAME, join(inclusionFlags));
        }
        return enrichedMap;
    }

    private String join(Collection<String> values) {
        final StringBuilder builder = new StringBuilder();
        Iterator<String> flagsIterator = values.iterator();

        while (flagsIterator.hasNext()) {
            builder.append(flagsIterator.next());
            if (flagsIterator.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
}
