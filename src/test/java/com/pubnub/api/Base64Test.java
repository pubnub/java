package com.pubnub.api;

import com.pubnub.api.utils.Base64;
import org.junit.Assert;
import org.junit.Test;

public class Base64Test {


    @Test
    public void testBase64Encode(){
       Assert.assertEquals("YWJj", Base64.encodeToString("abc".getBytes(), 0).trim());
    }

}
