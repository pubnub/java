package com.pubnub.contract.file

import com.pubnub.api.MessageType
import com.pubnub.api.PubNubException
import com.pubnub.api.SpaceId
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    val world: World
) {
    @When("I send a file with {string} space id and {string} message type")
    fun I_send_a_file_with_space_id_and_test_message_type(spaceIdValue: String, messageTypeValue: String) {
        val fileContent = "file content"
        val byteInputStream = fileContent.byteInputStream()
        val spaceId = SpaceId(spaceIdValue)
        val messageType = MessageType(messageTypeValue)

        //Mock server doesn't simulate retry yet.
        world.pubnub.configuration.fileMessagePublishRetryLimit = 1

        try {
            val pnFileUploadResult = world.pubnub.sendFile()
                .channel("myChannel")
                .fileName("fileName.jpg")
                .inputStream(byteInputStream)
                .messageType(messageType)
                .spaceId(spaceId)
                .sync()

            world.responseStatus = pnFileUploadResult.status
        } catch (ex: PubNubException) {
            world.pnException = ex
            world.responseStatus = ex.statusCode
        }
    }
}