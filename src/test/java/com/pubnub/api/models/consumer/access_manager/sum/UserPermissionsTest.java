package com.pubnub.api.models.consumer.access_manager.sum;

import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPermissionsTest {

    @Test
    void can_create_userPermissions_by_userId() throws PubNubException {
        String userId01Value = "userId01";
        UserPermissions userPermissionsById = UserPermissions.id(new UserId(userId01Value)).get().update();

        assertEquals(userId01Value, userPermissionsById.getId());
        assertTrue(userPermissionsById.isGet());
        assertTrue(userPermissionsById.isUpdate());
        assertFalse(userPermissionsById.isDelete());
        assertFalse(userPermissionsById.isWrite());
        assertFalse(userPermissionsById.isCreate());
        assertFalse(userPermissionsById.isManage());
        assertFalse(userPermissionsById.isRead());
        assertFalse(userPermissionsById.isJoin());
    }

    @Test
    void can_create_userPermissions_by_user_pattern() {
        String userIdPattern = "userId.*";
        UserPermissions userPermissionsByPattern = UserPermissions.pattern(userIdPattern).get().update().delete();

        assertEquals(userIdPattern, userPermissionsByPattern.getId());
        assertTrue(userPermissionsByPattern.isGet());
        assertTrue(userPermissionsByPattern.isDelete());
        assertTrue(userPermissionsByPattern.isUpdate());
        assertFalse(userPermissionsByPattern.isWrite());
        assertFalse(userPermissionsByPattern.isCreate());
        assertFalse(userPermissionsByPattern.isManage());
        assertFalse(userPermissionsByPattern.isRead());
        assertFalse(userPermissionsByPattern.isJoin());
    }
}