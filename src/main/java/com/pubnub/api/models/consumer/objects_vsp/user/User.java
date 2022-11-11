package com.pubnub.api.models.consumer.objects_vsp.user;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.UserId;
import com.pubnub.api.models.consumer.objects_vsp.ObjectVsp;
import com.pubnub.api.models.consumer.objects_vsp.util.UserIdDeserializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.Map;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Data
public class User extends ObjectVsp {
    @EqualsAndHashCode.Include
    @JsonAdapter(UserIdDeserializer.class)
    @Setter
    private UserId id;
    private String name;
    private String email;
    private String externalId;
    private String profileUrl;
    private String status;
    private String type;

    @Override
    public User setCustom(Map<String, Object> custom) {
        super.setCustom(custom);
        return this;
    }
}
