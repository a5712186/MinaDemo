package com.tjc.mina_demo;

import com.tjc.mina_demo.entity.JsonMessage;

import java.util.List;

public class ServiceMsgListener {

    interface clientListener{
         void connectException(Throwable cause);
         void connectClosed();
         void connectOpened();
         void connectError();
         void messageReceived(JsonMessage msg);
         void messageList(List<JsonMessage> msg);
         void sendMessageSuccess();
         void sendMessageFail();
         void jsonError(Throwable cause);
    }

    interface servicelListener{
        boolean isConnect();
        void restConnect();
        void sendMessage(Object msg);
        void closedMessage(String tag);
        void currentActivity(String activity);
    }

}
