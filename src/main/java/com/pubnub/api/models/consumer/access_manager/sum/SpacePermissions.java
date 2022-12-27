package com.pubnub.api.models.consumer.access_manager.sum;

import com.pubnub.api.SpaceId;
import com.pubnub.api.models.consumer.access_manager.v3.PNResource;

public class SpacePermissions extends PNResource<SpacePermissions> {

    private SpacePermissions() {
    }

    public static SpacePermissions id(SpaceId spaceId) {
        SpacePermissions spacePermissions = new SpacePermissions();
        spacePermissions.resourceName = spaceId.getValue();
        return spacePermissions;
    }

    public static SpacePermissions pattern(String spaceIdPattern) {
        SpacePermissions spacePermissions = new SpacePermissions();
        spacePermissions.resourcePattern = spaceIdPattern;
        return spacePermissions;
    }

    @Override
    public SpacePermissions read() {
        return super.read();
    }

    @Override
    public SpacePermissions delete() {
        return super.delete();
    }

    @Override
    public SpacePermissions write() {
        return super.write();
    }

    @Override
    public SpacePermissions get() {
        return super.get();
    }

    @Override
    public SpacePermissions manage() {
        return super.manage();
    }

    @Override
    public SpacePermissions update() {
        return super.update();
    }

    @Override
    public SpacePermissions join() {
        return super.join();
    }

}
