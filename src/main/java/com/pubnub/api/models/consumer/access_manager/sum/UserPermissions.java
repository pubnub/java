package com.pubnub.api.models.consumer.access_manager.sum;

import com.pubnub.api.UserId;
import com.pubnub.api.models.consumer.access_manager.v3.PNResource;

public class UserPermissions extends PNResource<UserPermissions> {

    private UserPermissions() {
    }

    public static UserPermissions id(UserId userId) {
        UserPermissions userPermissions = new UserPermissions();
        userPermissions.resourceName = userId.getValue();
        return userPermissions;
    }

    public static UserPermissions pattern(String userIdPattern) {
        UserPermissions userPermissions = new UserPermissions();
        userPermissions.resourcePattern = userIdPattern;
        return userPermissions;
    }


    @Override
    public UserPermissions get() {
        return super.get();
    }

    @Override
    public UserPermissions update() {
        return super.update();
    }

    @Override
    public UserPermissions delete() {
        return super.delete();
    }

}
