package com.pubnub.examples.blackberry;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.MainScreen;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

public class Subscribe extends PubnubCommand {

    public Subscribe(Pubnub pubnub) {
        super(pubnub, "Subscribe");
    }

    protected void initScreen() {
        final BasicEditField txtChannel = new BasicEditField("Channel : ", "", 256, BasicEditField.FILTER_DEFAULT);
        screen = new MainScreen();
        screen.add(txtChannel);

        ButtonField btn = new ButtonField();
        btn.setLabel("Subscribe");
        screen.add(btn);

        btn.setChangeListener(new FieldChangeListener() {

            public void fieldChanged(Field field, int context) {
                Hashtable args = new Hashtable();

                try {
                    _pubnub.subscribe(txtChannel.toString(), new Callback() {
                        public void connectCallback(String channel) {
                            notifyUser("CONNECT on channel:" + channel);
                        }

                        public void disconnectCallback(String channel) {
                            notifyUser("DISCONNECT on channel:" + channel);
                        }

                        public void reconnectCallback(String channel) {
                            notifyUser("RECONNECT on channel:" + channel);
                        }

                        public void successCallback(String channel, Object message) {
                            notifyUser("Channel " + channel + " : " + message.toString());
                        }
                        public void errorCallback(String channel, PubnubError error) {
                            notifyUser("Channel " + channel + " : " + error.toString());
                        }
                    });
                    close();

                } catch (Exception e) {

                }
            }});
    }

}
