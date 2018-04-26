package com.example.messaging.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ThreadActivity extends AppCompatActivity implements ThreadAdapter.DataUpdateAfterDelete {

    OkHttpClient client = new OkHttpClient();

    ArrayList<ThreadResponse.MessageThread> threadsList;
    ThreadAdapter threadAdapter;

    TextView username, threadslist;
    EditText addthread;
    ImageButton logOutButton;
    ImageButton addThreadButton;
    ListView threadlistItems;
    private final String TAG="demothreads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.messaging.chattingapp.R.layout.activity_thread);
        setTitle("Message Threads");

        username =findViewById(com.example.messaging.chattingapp.R.id.userNameTV);
        threadslist =findViewById(com.example.messaging.chattingapp.R.id.threadListTV);
        addthread =findViewById(com.example.messaging.chattingapp.R.id.newThreadET);
        logOutButton=findViewById(com.example.messaging.chattingapp.R.id.logOutButton);
        addThreadButton=findViewById(com.example.messaging.chattingapp.R.id.addThreadButton);
        threadlistItems =findViewById(com.example.messaging.chattingapp.R.id.threadItemsLV);

        username.setTextColor(Color.parseColor("#000000"));
        threadslist.setTextColor(Color.parseColor("#000000"));



//        Log.d(TAG, "ThreadsActivityOnCreate: "+getToken());
        getThreadList(getToken());

        username.setText(getUserName());

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.clear().apply();
                Intent intent = new Intent(ThreadActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title= addthread.getText().toString();
                if(!title.isEmpty()) {
                    addNewThread(getToken(), title);
                }else{
                    Toast.makeText(ThreadActivity.this, "Enter thread title", Toast.LENGTH_SHORT).show();
                }
            }
        });

        threadlistItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ThreadActivity.this, "list view item "+position+" clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ThreadActivity.this, ChattingActivity.class);
                intent.putExtra("messageThreadDetails",threadsList.get(position));
                startActivity(intent);
                finish();
            }
        });
    }

    public void getThreadList(String token) {
        Request request = new Request.Builder()
                .header("Authorization", "BEARER " + token)
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/thread").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "getThreadListOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.d(TAG, "getThreadListOnResponse: " + str);

                Gson gson=new Gson();
                final ThreadResponse threadResponse = gson.fromJson(str, ThreadResponse.class);
                threadsList= threadResponse.threads;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        threadAdapter = new ThreadAdapter(ThreadActivity.this, com.example.messaging.chattingapp.R.layout.thread_listview, threadsList,ThreadActivity.this);
                        threadlistItems.setAdapter(threadAdapter);
                    }
                });
            }
        });
    }
    public String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    public String getUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        String fName= sharedPreferences.getString("user_fname", "");
        String lName= sharedPreferences.getString("user_lname", "");
        return fName+" "+lName;
    }

    public void addNewThread(String token,String title){

        RequestBody formBody = new FormBody.Builder()
                .add("title", title)
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "BEARER " + token)
                .post(formBody)
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/thread/add").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "addNewThreadOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.d(TAG, "addNewThreadOnResponse: " + str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadActivity.this, "New thread created", Toast.LENGTH_SHORT).show();
                        addthread.setText("");
                        //threadAdapter.notifyDataSetChanged();
                        getThreadList(getToken());
                    }
                });
            }
        });

    }

    @Override
    public void deleteThread(String token,String thread_id){
        Request request = new Request.Builder()
                .header("Authorization", "BEARER " + token)
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/thread/delete/"+thread_id).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "deleteThreadOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.d(TAG, "deleteThreadOnResponse: "+str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadActivity.this, "thread deleted", Toast.LENGTH_SHORT).show();
                        getThreadList(getToken());
                    }
                });
            }
        });
    }
}
