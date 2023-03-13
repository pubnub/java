package com.pubnub.apikt.legacy.endpoints.access

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.findAll
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.SignatureUtils.decomposeAndVerifySignature
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.models.consumer.access_manager.PNAccessManagerKeyData
import org.awaitility.Awaitility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GrantEndpointTest : BaseTest() {

    override fun onBefore() {
        super.onBefore()
        pubnub.configuration.secretKey = "secretKey"
        pubnub.configuration.includeInstanceIdentifier = true
        pubnub.configuration.includeRequestIdentifier = true
    }

    @Test
    fun noGroupsOneChannelOneKeyTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "user",
                                "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                                "ttl": 1,
                                "channel": "ch1",
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1")
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(1, result.channels["ch1"]!!.size) // todo replace with error
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsOneChannelTwoKeyTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "user",
                                "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                                "ttl": 1,
                                "channel": "ch1",
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1", "key2"),
            channels = listOf("ch1")
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(2, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key2"]!!.javaClass
        )
        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsTwoChannelOneKeyTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "user",
                                "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                                "ttl": 1,
                                "channels": {
                                  "ch1": {
                                    "auths": {
                                      "key1": {
                                        "r": 0,
                                        "w": 0,
                                        "m": 0
                                      }
                                    }
                                  },
                                  "ch2": {
                                    "auths": {
                                      "key1": {
                                        "r": 0,
                                        "w": 0,
                                        "m": 0
                                      }
                                    }
                                  }
                                }
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1", "ch2")
        ).sync()!!

        assertEquals(2, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(1, result.channels["ch2"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch2"]!!["key1"]!!.javaClass
        )
        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)
        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsTwoChannelTwoKeyTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "user",
                                "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                                "ttl": 1,
                                "channels": {
                                  "ch1": {
                                    "auths": {
                                      "key1": {
                                        "r": 0,
                                        "w": 0,
                                        "m": 0
                                      },
                                      "key2": {
                                        "r": 0,
                                        "w": 0,
                                        "m": 0
                                      }
                                    }
                                  },
                                  "ch2": {
                                    "auths": {
                                      "key1": {
                                        "r": 0,
                                        "w": 0,
                                        "m": 0
                                      },
                                      "key2": {
                                        "r": 0,
                                        "w": 0,
                                        "m": 0
                                      }
                                    }
                                  }
                                }
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = (listOf("key1", "key2")),
            channels = (listOf("ch1", "ch2"))
        ).sync()!!

        assertEquals(2, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(2, result.channels["ch1"]!!.size)
        assertEquals(2, result.channels["ch2"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch2"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key2"]!!.javaClass
        )
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch2"]!!["key2"]!!.javaClass)
        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun oneGroupNoChannelOneKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "channel-group+auth",
                                "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                                "ttl": 1,
                                "channel-groups": "cg1",
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channelGroups = listOf("cg1")
        ).sync()!!

        assertEquals(0, result.channels.size)
        assertEquals(1, result.channelGroups.size)
        assertEquals(1, result.channelGroups["cg1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun oneGroupNoChannelTwoKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                         "message": "Success",
                         "payload": {
                          "level": "channel-group+auth",
                          "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                          "ttl": 1,
                          "channel-groups": "cg1",
                          "auths": {
                           "key1": {
                            "r": 0,
                            "w": 0,
                            "m": 0
                           },
                           "key2": {
                            "r": 0,
                            "w": 0,
                            "m": 0
                           }
                          }
                         },
                         "service": "Access Manager",
                         "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1", "key2"),
            channelGroups = listOf("cg1")
        ).sync()!!

        assertEquals(0, result.channels.size)
        assertEquals(1, result.channelGroups.size)
        assertEquals(2, result.channelGroups["cg1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key2"]!!.javaClass
        )

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun oneGroupOneChannelOneKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "message": "Success",
                             "payload": {
                              "level": "channel-group+auth",
                              "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                              "ttl": 1,
                              "channel": "ch1",
                              "auths": {
                               "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                               }
                              },
                              "channel-groups": "cg1"
                             },
                             "service": "Access Manager",
                             "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1"),
            channelGroups = listOf("cg1")
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(1, result.channelGroups.size)
        assertEquals(1, result.channelGroups["cg1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )
        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun oneGroupOneChannelTwoKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "channel-group+auth",
                                "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                                "ttl": 1,
                                "channel": "ch1",
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                },
                                "channel-groups": "cg1"
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1", "key2"),
            channels = listOf("ch1"),
            channelGroups = listOf("cg1")
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(1, result.channelGroups.size)
        assertEquals(2, result.channelGroups["cg1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key2"]!!.javaClass
        )
        assertEquals(2, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key2"]!!.javaClass)
        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun oneGroupTwoChannelOneKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "channel-group+auth",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channels": {
                              "ch1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "ch2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            },
                            "channel-groups": "cg1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1", "ch2"),
            channelGroups = listOf("cg1")
        ).sync()!!

        assertEquals(2, result.channels.size)
        assertEquals(1, result.channelGroups.size)
        assertEquals(1, result.channelGroups["cg1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch2"]!!["key1"]!!.javaClass
        )
        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun oneGroupTwoChannelTwoKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "channel-group+auth",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channels": {
                              "ch1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "ch2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            },
                            "channel-groups": "cg1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              },
                              "key2": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1", "key2"),
            channels = listOf("ch1", "ch2"),
            channelGroups = listOf("cg1")
        ).sync()!!

        assertEquals(2, result.channels.size)
        assertEquals(1, result.channelGroups.size)
        assertEquals(2, result.channelGroups["cg1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key2"]!!.javaClass
        )
        assertEquals(2, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key2"]!!.javaClass)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch2"]!!["key1"]!!.javaClass)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch2"]!!["key2"]!!.javaClass)

        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun twoGroupNoChannelOneKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "channel-group+auth",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel-groups": {
                              "cg1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "cg2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channelGroups = listOf("cg1", "cg2")
        ).sync()!!

        assertEquals(0, result.channels.size)
        assertEquals(2, result.channelGroups.size)
        assertEquals(1, result.channelGroups["cg1"]!!.size)
        assertEquals(1, result.channelGroups["cg2"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg2"]!!["key1"]!!.javaClass
        )
        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun twoGroupNoChannelTwoKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                         {
                           "message": "Success",
                           "payload": {
                             "level": "channel-group+auth",
                             "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                             "ttl": 1,
                             "channel-groups": {
                               "cg1": {
                                 "auths": {
                                   "key1": {
                                     "r": 0,
                                     "w": 0,
                                     "m": 0
                                   },
                                   "key2": {
                                     "r": 0,
                                     "w": 0,
                                     "m": 0
                                   }
                                 }
                               },
                               "cg2": {
                                 "auths": {
                                   "key1": {
                                     "r": 0,
                                     "w": 0,
                                     "m": 0
                                   },
                                   "key2": {
                                     "r": 0,
                                     "w": 0,
                                     "m": 0
                                   }
                                 }
                               }
                             }
                           },
                           "service": "Access Manager",
                           "status": 200
                         }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1", "key2"),
            channelGroups = listOf("cg1", "cg2")
        ).sync()!!

        assertEquals(0, result.channels.size)
        assertEquals(2, result.channelGroups.size)
        assertEquals(2, result.channelGroups["cg1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key2"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg2"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg2"]!!["key2"]!!.javaClass
        )

        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun twoGroupOneChannelOneKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "channel-group+auth",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            },
                            "channel-groups": {
                              "cg1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "cg2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1"),
            channelGroups = listOf("cg1", "cg2")
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(2, result.channelGroups.size)
        assertEquals(1, result.channelGroups["cg1"]!!.size)
        assertEquals(1, result.channelGroups["cg2"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg2"]!!["key1"]!!.javaClass
        )
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key1"]!!.javaClass)

        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun twoGroupOneChannelTwoKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "channel-group+auth",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              },
                              "key2": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            },
                            "channel-groups": {
                              "cg1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "cg2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1", "key2"),
            channels = listOf("ch1"),
            channelGroups = listOf("cg1", "cg2")
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(2, result.channelGroups.size)
        assertEquals(2, result.channelGroups["cg1"]!!.size)
        assertEquals(2, result.channelGroups["cg2"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key2"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg2"]!!["key1"]!!.javaClass
        )
        assertEquals(PNAccessManagerKeyData::class.java, result.channelGroups["cg2"]!!["key2"]!!.javaClass)
        assertEquals(2, result.channels["ch1"]!!.size)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key1"]!!.javaClass)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key2"]!!.javaClass)

        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun twoGroupTwoChannelOneKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "channel-group+auth",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channels": {
                              "ch1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "ch2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            },
                            "channel-groups": {
                              "cg1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "cg2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1", "ch2"),
            channelGroups = listOf("cg1", "cg2")
        ).sync()!!

        assertEquals(2, result.channels.size)
        assertEquals(2, result.channelGroups.size)
        assertEquals(1, result.channelGroups["cg1"]!!.size)
        assertEquals(1, result.channelGroups["cg2"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg2"]!!["key1"]!!.javaClass
        )
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(1, result.channels["ch2"]!!.size)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key1"]!!.javaClass)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch2"]!!["key1"]!!.javaClass)

        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun twoGroupTwoChannelTwoKey() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "channel-group+auth",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channels": {
                              "ch1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "ch2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            },
                            "channel-groups": {
                              "cg1": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "cg2": {
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  },
                                  "key2": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1", "key2"),
            channels = listOf("ch1", "ch2"),
            channelGroups = listOf("cg1", "cg2")
        ).sync()!!

        assertEquals(2, result.channels.size)
        assertEquals(2, result.channelGroups.size)
        assertEquals(2, result.channelGroups["cg1"]!!.size)
        assertEquals(2, result.channelGroups["cg2"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key1"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg1"]!!["key2"]!!.javaClass
        )
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channelGroups["cg2"]!!["key1"]!!.javaClass
        )
        assertEquals(PNAccessManagerKeyData::class.java, result.channelGroups["cg2"]!!["key2"]!!.javaClass)
        assertEquals(2, result.channels["ch1"]!!.size)
        assertEquals(2, result.channels["ch2"]!!.size)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key1"]!!.javaClass)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch1"]!!["key2"]!!.javaClass)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch2"]!!["key1"]!!.javaClass)
        assertEquals(PNAccessManagerKeyData::class.java, result.channels["ch2"]!!["key2"]!!.javaClass)

        val requests = findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsOneChannelOneKeyTTLTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .withQueryParam("ttl", matching("1334"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "user",
                                "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                                "ttl": 1,
                                "channel": "ch1",
                                "auths": {
                                  "key1": {
                                    "r": 0,
                                    "w": 0,
                                    "m": 0
                                  }
                                }
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1"),
            ttl = 1334
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsOneChannelOneReadKeyTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("1"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "user",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1"),
            read = true
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsOneChannelOneWriteKeyTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("1"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "user",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1"),
            write = true
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsOneChannelOneDeleteKeyTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .withQueryParam("d", matching("1"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "user",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1"),
            delete = true
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun noGroupsOneChannelOneKeyManageTest() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("1"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "user",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1"),
            manage = true
        ).sync()!!

        assertEquals(1, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals(1, result.channels["ch1"]!!.size)
        assertEquals(
            PNAccessManagerKeyData::class.java,
            result.channels["ch1"]!!["key1"]!!.javaClass
        )

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "user",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )
        pubnub.configuration.authKey = "myKey"

        pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1")
        ).sync()!!

        val requests =
            findAll(getRequestedFor(urlMatching("/v2/auth/grant/sub-key/mySubscribeKey.*")))
        assertEquals(1, requests.size)

        decomposeAndVerifySignature(pubnub.configuration, requests[0])
    }

    @Test
    fun testOperationTypeSuccessAsync() {
        val atomic = AtomicBoolean(false)
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "user",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.grant(
            authKeys = listOf("key1"),
            channels = listOf("ch1")
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNAccessManagerGrant, status.operation)
            atomic.set(true)
        }

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(atomic)
    }

    @Test
    fun testBlankSecretKey() {
        pubnub.configuration.secretKey = " "
        try {
            pubnub.grant(
                authKeys = listOf("key1"),
                channels = listOf("ch1")
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.SECRET_KEY_MISSING, e)
        }
    }

    @Test
    fun testEmptySecretKey() {
        pubnub.configuration.secretKey = ""
        try {
            pubnub.grant(
                authKeys = listOf("key1"),
                channels = listOf("ch1")
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.SECRET_KEY_MISSING, e)
        }
    }

    @Test
    fun testBlankSubscribeKey() {
        pubnub.configuration.subscribeKey = " "
        try {
            pubnub.grant(
                authKeys = listOf("key1"),
                channels = listOf("ch1")
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testEmptySubscribeKey() {
        pubnub.configuration.subscribeKey = ""
        try {
            pubnub.grant(
                authKeys = listOf("key1"),
                channels = listOf("ch1")
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testMissingChannelsAndChannelGroup() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withStatus(200).withBody(
                        """
                            {
                              "message": "Success",
                              "payload": {
                                "level": "subkey",
                                "subscribe_key": "mySubscribeKey",
                                "ttl": 1440,
                                "r": 0,
                                "w": 1,
                                "m": 0,
                                "d": 0
                              },
                              "service": "Access Manager",
                              "status": 200
                            }
                        """.trimIndent()
                    )
                )
        )

        try {
            val grantResult = pubnub.grant().sync()!!
            assertEquals("subkey", grantResult.level)
            assertEquals(1440, grantResult.ttl)
            assertEquals(0, grantResult.channels.size)
            assertEquals(0, grantResult.channelGroups.size)
        } catch (e: PubNubException) {
            failTest()
        }
    }

    @Test
    fun testNullPayload() {
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """{"message":"Success","service":"Access Manager","status":200}"""
                    )
                )
        )

        try {
            pubnub.grant(
                authKeys = listOf("key1"),
                channels = listOf("ch1")
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testNullAuthKeyAsync() {
        val atomic = AtomicBoolean(false)
        stubFor(
            get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "message": "Success",
                          "payload": {
                            "level": "user",
                            "subscribe_key": "sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f",
                            "ttl": 1,
                            "channel": "ch1",
                            "auths": {
                              "key1": {
                                "r": 0,
                                "w": 0,
                                "m": 0
                              }
                            }
                          },
                          "service": "Access Manager",
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.grant(
            channels = listOf("ch1")
        ).async { _, status ->
            if (status.operation == PNOperationType.PNAccessManagerGrant && !status.error) {
                atomic.set(true)
            }
        }

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(atomic)
    }
}
