package com.pubnub.api.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Converter;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

public class MapperManager {

    @Getter
    private final Gson objectMapper;
    @Getter
    private final Converter.Factory converterFactory;

    private final ObjectMapper jacksonObjectMapper = new ObjectMapper();

    public MapperManager() {
        TypeAdapter<Boolean> booleanAsIntAdapter = getBooleanTypeAdapter();

        this.objectMapper = new GsonBuilder()
                .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(JSONObject.class, new JSONObjectAdapter())
                .registerTypeAdapter(JSONArray.class, new JSONArrayAdapter())
                .disableHtmlEscaping()
                .create();
        this.converterFactory = GsonConverterFactory.create(this.getObjectMapper());
    }

    public boolean hasField(JsonElement element, String field) {
        return element.getAsJsonObject().has(field);
    }

    public JsonElement getField(JsonElement element, String field) {
        return element.getAsJsonObject().get(field);
    }

    public Iterator<JsonElement> getArrayIterator(JsonElement element) {
        return element.getAsJsonArray().iterator();
    }

    public Iterator<JsonElement> getArrayIterator(JsonElement element, String field) {
        return element.getAsJsonObject().get(field).getAsJsonArray().iterator();
    }

    public Iterator<Map.Entry<String, JsonElement>> getObjectIterator(JsonElement element) {
        return element.getAsJsonObject().entrySet().iterator();
    }

    public Iterator<Map.Entry<String, JsonElement>> getObjectIterator(JsonElement element, String field) {
        return element.getAsJsonObject().get(field).getAsJsonObject().entrySet().iterator();
    }

    public String elementToString(JsonElement element) {
        return element.getAsString();
    }

    public String elementToString(JsonElement element, String field) {
        return element.getAsJsonObject().get(field).getAsString();
    }

    public int elementToInt(JsonElement element, String field) {
        return element.getAsJsonObject().get(field).getAsInt();
    }

    public boolean isJsonObject(JsonElement element) {
        return element.isJsonObject();
    }

    public JsonObject getAsObject(JsonElement element) {
        return element.getAsJsonObject();
    }

    public boolean getAsBoolean(JsonElement element, String field) {
        return element.getAsJsonObject().get(field).getAsBoolean();
    }

    public void putOnObject(JsonObject element, String key, JsonElement value) {
        element.add(key, value);
    }

    public JsonElement getArrayElement(JsonElement element, int index) {
        return element.getAsJsonArray().get(index);
    }

    public Long elementToLong(JsonElement element) {
        return element.getAsLong();
    }

    public Long elementToLong(JsonElement element, String field) {
        return element.getAsJsonObject().get(field).getAsLong();
    }

    public JsonArray getAsArray(JsonElement element) {
        return element.getAsJsonArray();
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String input, Class<T> clazz) throws PubNubException {
        try {
            return this.objectMapper.fromJson(input, clazz);
        } catch (JsonParseException e) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR)
                    .errormsg(e.getMessage())
                    .cause(e)
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T convertValue(JsonElement input, Class clazz) {
        return (T) this.objectMapper.fromJson(input, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T convertValue(Object object, Class clazz) throws PubNubException {
        return (T) fromJson(toJson(object), clazz);
    }

    public JsonElement toJsonTree(Object object) {
        return objectMapper.toJsonTree(object);
    }

    public String toJson(Object input) throws PubNubException {
        try {
            return this.objectMapper.toJson(input);
        } catch (JsonParseException e) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_JSON_ERROR)
                    .errormsg(e.getMessage())
                    .cause(e)
                    .build();
        }
    }

    public String toJsonUsinJackson(Object input) throws PubNubException {
        try {
            return this.jacksonObjectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_JSON_ERROR)
                    .errormsg(e.getMessage())
                    .cause(e)
                    .build();
        }
    }

    @NotNull
    private TypeAdapter<Boolean> getBooleanTypeAdapter() {
        return new TypeAdapter<Boolean>() {
            @Override
            public void write(JsonWriter out, Boolean value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(value);
                }
            }

            @Override
            public Boolean read(JsonReader in) throws IOException {
                JsonToken peek = in.peek();
                switch (peek) {
                    case BOOLEAN:
                        return in.nextBoolean();
                    //case NULL:
                    //    in.nextNull();
                    //    return null;
                    case NUMBER:
                        return in.nextInt() != 0;
                    case STRING:
                        return Boolean.parseBoolean(in.nextString());
                    default:
                        throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
                }
            }
        };
    }

    private static class JSONObjectAdapter implements JsonSerializer<JSONObject>, JsonDeserializer<JSONObject> {

        @Override
        public JsonElement serialize(JSONObject src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return null;
            }
            JsonObject jsonObject = new JsonObject();
            Iterator<String> keys = src.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = src.opt(key);
                JsonElement jsonElement = context.serialize(value, value.getClass());
                jsonObject.add(key, jsonElement);
            }
            return jsonObject;
        }

        @Override
        public JSONObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json == null) {
                return null;
            }
            try {
                return new JSONObject(json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                throw new JsonParseException(e);
            }
        }
    }

    private static class JSONArrayAdapter implements JsonSerializer<JSONArray>, JsonDeserializer<JSONArray> {

        @Override
        public JsonElement serialize(JSONArray src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return null;
            }
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < src.length(); i++) {
                Object object = src.opt(i);
                JsonElement jsonElement = context.serialize(object, object.getClass());
                jsonArray.add(jsonElement);
            }
            return jsonArray;
        }

        @Override
        public JSONArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json == null) {
                return null;
            }
            try {
                return new JSONArray(json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                throw new JsonParseException(e);
            }
        }
    }
}
