package com.example.messaging.chattingapp;

import java.util.ArrayList;

public class MessagesListResponse {

    ArrayList<Message> messages=new ArrayList<>();
    String status;

    public class Message{

        String user_fname,user_lname,user_id,id,message,created_at;

        @Override
        public String toString() {
            return "Message{" +
                    "user_fname='" + user_fname + '\'' +
                    ", user_lname='" + user_lname + '\'' +
                    ", user_id='" + user_id + '\'' +
                    ", id='" + id + '\'' +
                    ", message='" + message + '\'' +
                    ", created_at='" + created_at + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MessagesListResponse{" +
                "messagesList=" + messages +
                ", status='" + status + '\'' +
                '}';
    }
}
