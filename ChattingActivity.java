package com.example.messaging.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChattingActivity extends AppCompatActivity implements MessageAdapter.DataUpdateAfterMessageDelete{

    private final String TAG="demochat";
    OkHttpClient client = new OkHttpClient();
    ArrayList<MessagesListResponse.Message> messagesList;
    MessageAdapter messageAdapter;

    ThreadResponse.MessageThread messageThread;
    TextView threadNameTV;
    EditText newMessageET;
    ImageButton homeButton,sendButton;
    ListView messagesLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.messaging.chattingapp.R.layout.activity_chat);

        threadNameTV=findViewById(com.example.messaging.chattingapp.R.id.threadNameTV);
        newMessageET=findViewById(com.example.messaging.chattingapp.R.id.newMessageET);
        homeButton=findViewById(com.example.messaging.chattingapp.R.id.homeButton);
        sendButton=findViewById(com.example.messaging.chattingapp.R.id.sendButton);
        messagesLV=findViewById(com.example.messaging.chattingapp.R.id.messagesLV);

        threadNameTV.setTextColor(Color.parseColor("#000000"));

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("messageThreadDetails")) {
                messageThread = (ThreadResponse.MessageThread) getIntent().getSerializableExtra("messageThreadDetails");
                threadNameTV.setText(messageThread.title);
                getMessages(getToken(),messageThread.id);
            }
        }else{
            Toast.makeText(this, "No data received", Toast.LENGTH_SHORT).show();
        }

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getToken()!=null) {
                    Intent intent = new Intent(ChattingActivity.this, ThreadActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ChattingActivity.this, "You need to login", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChattingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=newMessageET.getText().toString();
                if(message.isEmpty()){
                    Toast.makeText(ChattingActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                }else{
                    addMessage(getToken(),message,messageThread.id);
                }
            }
        });
    }

    public String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    public void getMessages(String token, final String thread_id){
        Request request = new Request.Builder()
                .header("Authorization", "BEARER " + token)
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/messages/"+thread_id).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "getMessagesOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.d(TAG, "getMessagesOnResponse: "+str);
                Gson gson=new Gson();
                final MessagesListResponse messagesListResponse = gson.fromJson(str, MessagesListResponse.class);
                messagesList=messagesListResponse.messages;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(messagesList, new Comparator<MessagesListResponse.Message>() {
                            @Override
                            public int compare(MessagesListResponse.Message o1, MessagesListResponse.Message o2) {
                                return o1.id.compareTo(o2.id);
                            }
                        });
                        messageAdapter= new MessageAdapter(ChattingActivity.this, com.example.messaging.chattingapp.R.layout.msgs_listview, messagesList,ChattingActivity.this);
                        messagesLV.setAdapter(messageAdapter);
                    }
                });
            }
        });
    }

    public void addMessage(String token, String message, final String thread_id){

        RequestBody formBody = new FormBody.Builder()
                .add("message", message)
                .add("thread_id", thread_id)
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "BEARER " + token)
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/message/add")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "addMessageOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.d(TAG, "addMessageOnResponse: "+str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChattingActivity.this, "message added", Toast.LENGTH_SHORT).show();
                        newMessageET.setText("");
                        getMessages(getToken(),thread_id);
                    }
                });
            }
        });
    }

    @Override
    public void deleteMessage(String token,String message_id){
        Request request = new Request.Builder()
                .header("Authorization", "BEARER " + token)
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/message/delete/"+message_id)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "deleteMessageOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.d(TAG, "deleteMessageOnResponse: "+str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChattingActivity.this, "message deleted", Toast.LENGTH_SHORT).show();
                        getMessages(getToken(),messageThread.id);
                    }
                });
            }
        });

    }
}
