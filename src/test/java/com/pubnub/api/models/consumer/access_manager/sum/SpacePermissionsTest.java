package com.pubnub.api.models.consumer.access_manager.sum;

import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpacePermissionsTest {

    @Test
    void can_set_spacePermissions_by_spaceId() throws PubNubException {
        String space01Value = "space01";
        SpacePermissions spacePermissionsById = SpacePermissions.id(new SpaceId(space01Value)).read().delete().write().get().manage().update().join();

        assertEquals(space01Value, spacePermissionsById.getId());
        assertTrue(spacePermissionsById.isRead());
        assertTrue(spacePermissionsById.isDelete());
        assertTrue(spacePermissionsById.isWrite());
        assertTrue(spacePermissionsById.isGet());
        assertTrue(spacePermissionsById.isManage());
        assertTrue(spacePermissionsById.isUpdate());
        assertTrue(spacePermissionsById.isJoin());
    }

    @Test
    void can_set_spacePermissions_by_space_pattern() {

        String spaceIdPattern = "space.*";
        SpacePermissions spacePermissionsById = SpacePermissions.pattern(spaceIdPattern).read();

        assertEquals(spaceIdPattern, spacePermissionsById.getId());
        assertTrue(spacePermissionsById.isRead());
        assertFalse(spacePermissionsById.isDelete());
        assertFalse(spacePermissionsById.isWrite());
        assertFalse(spacePermissionsById.isGet());
        assertFalse(spacePermissionsById.isManage());
        assertFalse(spacePermissionsById.isUpdate());
        assertFalse(spacePermissionsById.isJoin());
    }
}