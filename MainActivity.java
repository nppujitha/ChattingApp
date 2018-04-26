package com.example.messaging.chattingapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();


    EditText email;
    EditText password;
    Button loginButton;
    Button signUpButton;
    private final String TAG = "demologin";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.messaging.chattingapp.R.layout.activity_login);
        setTitle("Chat Room");

        email = findViewById(com.example.messaging.chattingapp.R.id.emailET);
        password = findViewById(com.example.messaging.chattingapp.R.id.pwdET);
        loginButton = findViewById(com.example.messaging.chattingapp.R.id.loginButton);
        signUpButton = findViewById(com.example.messaging.chattingapp.R.id.signUpButton);


//        Log.d(TAG, "LoginActivityOnCreate: " + getToken());

        if (!getToken().isEmpty()) {
            Intent intent = new Intent(MainActivity.this, ThreadActivity.class);
            startActivity(intent);
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = MainActivity.this.email.getText().toString();
                String password = MainActivity.this.password.getText().toString();
                if(email.isEmpty()){
                    MainActivity.this.email.setError("Email field empty");
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    MainActivity.this.email.setError("Invalid Email pattern");
                }else if(password.isEmpty()){
                    MainActivity.this.password.setError("Password field empty");
                }else {
                    performLogin(email, password);
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Signup.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void performLogin(String email, String password) {

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/login")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "performLoginOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "performLoginOnResponse: " + String.valueOf(Thread.currentThread().getId()));
                String str = response.body().string();
                Log.d(TAG, "performLoginOnResponse: " + str);
                Gson gson = new Gson();
                TokenLoginResponse tokenLoginResponse = gson.fromJson(str, TokenLoginResponse.class);
                Log.d(TAG, "performLoginOnResponse: " + tokenLoginResponse.token);
                saveToken(tokenLoginResponse.token, tokenLoginResponse.user_fname, tokenLoginResponse.user_lname, tokenLoginResponse.user_id);
                if (tokenLoginResponse.token != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + getToken());
                            Intent intent = new Intent(MainActivity.this, ThreadActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, " Enter Valid Email/Password yout login is not successful.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }
    public String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    public void saveToken(String token, String user_fname, String user_lname, String user_id) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("user_fname", user_fname);
        editor.putString("user_lname", user_lname);
        editor.putString("user_id", user_id);
        editor.apply();
    }


}
