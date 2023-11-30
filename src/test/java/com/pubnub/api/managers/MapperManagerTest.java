package com.pubnub.api.managers;

import com.pubnub.api.PubNubException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MapperManagerTest {

    @Test
    void toJson_anonymousList() throws PubNubException {
        MapperManager mapperManager = new MapperManager();

        String expected = "[1,2,3]";
        List<Integer> anonList = new ArrayList<Integer>(){{
            add(1);
            add(2);
            add(3);
        }};
        List<Integer> regularList = new ArrayList<Integer>();
        regularList.add(1);
        regularList.add(2);
        regularList.add(3);


        String json1 = mapperManager.toJson(anonList);
        String json2 = mapperManager.toJson(regularList);


        assertEquals(expected, json1);
        assertEquals(expected, json2);
    }

    @Test
    void toJson_anonymousMap() throws PubNubException {
        MapperManager mapperManager = new MapperManager();
        String expected = "{\"city\":\"Toronto\"}";

        HashMap<String, String> anonMap = new HashMap<String, String>() {{
            put("city", "Toronto");
        }};

        HashMap<String, String> regularMap = new HashMap<String, String>();
        regularMap.put("city", "Toronto");


        String json1 = mapperManager.toJson(anonMap);
        String json2 = mapperManager.toJson(regularMap);
        assertEquals(expected, json1);
        assertEquals(expected, json2);
    }

    @Test
    void toJson_anonymousSet() throws PubNubException {
        MapperManager mapperManager = new MapperManager();

        String expected = "[1,2,3]";
        Set<Integer> anonSet = new HashSet<Integer>(){{
            add(1);
            add(2);
            add(3);
        }};
        Set<Integer> regularSet = new HashSet<Integer>();
        regularSet.add(1);
        regularSet.add(2);
        regularSet.add(3);


        String json1 = mapperManager.toJson(anonSet);
        String json2 = mapperManager.toJson(regularSet);


        assertEquals(expected, json1);
        assertEquals(expected, json2);
    }

}