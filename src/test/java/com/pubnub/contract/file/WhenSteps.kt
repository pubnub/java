package com.pubnub.contract.file

import com.pubnub.api.PubNubException
import com.pubnub.api.SpaceId
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    val world: World
) {
    @When("I send a file with {string} space id and {string} type")
    fun I_send_a_file_with_space_id_and_test_type(spaceIdValue: String, typeValue: String) {
        val fileContent = "file content"
        val byteInputStream = fileContent.byteInputStream()
        val spaceId = SpaceId(spaceIdValue)
        val type = typeValue

        try {
            val pnFileUploadResult = world.pubnub.sendFile()
                .channel("myChannel")
                .fileName("fileName.jpg")
                .inputStream(byteInputStream)
                .type(type)
                .spaceId(spaceId)
                .sync()

            world.responseStatus = pnFileUploadResult.status
        } catch (ex: PubNubException) {
            world.pnException = ex
            world.responseStatus = ex.statusCode
        }
    }
}