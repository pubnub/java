package com.pubnub.contract

import com.google.gson.Gson
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata
import com.pubnub.apikt.models.consumer.objects.member.PNMember
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembership
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import java.io.File

val gson = Gson()

fun readDataFile(personaName: String) = File(
    "${ContractTestConfig.dataFilesLocation}/${personaName.lowercase()}.json"
).readText(Charsets.UTF_8)

inline fun <reified T> readResourceFromDataFile(dataFileName: String): T = gson.fromJson(readDataFile(dataFileName), T::class.java)

fun loadPersonaUUIDMetadata(personaName: String): PNUUIDMetadata = readResourceFromDataFile(personaName)
fun loadChannelMetadata(channelName: String): PNChannelMetadata = readResourceFromDataFile(channelName)
fun loadChannelMembership(membershipName: String): PNChannelMembership = readResourceFromDataFile(membershipName)
fun loadMember(memberName: String): PNMember = readResourceFromDataFile(memberName)
