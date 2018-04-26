package com.example.messaging.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class Signup extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();
    private final String TAG = "demoSignUp";

    EditText fNameET, lNameET, suEmailET, chPwdET, rptPwdET;
    Button cancelButton, signUpButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.messaging.chattingapp.R.layout.activity_signup);
        setTitle("Sign Up");

        fNameET = findViewById(com.example.messaging.chattingapp.R.id.fNameET);
        lNameET = findViewById(com.example.messaging.chattingapp.R.id.lNameET);
        suEmailET = findViewById(com.example.messaging.chattingapp.R.id.suEmailET);
        chPwdET = findViewById(com.example.messaging.chattingapp.R.id.chPwdET);
        rptPwdET = findViewById(com.example.messaging.chattingapp.R.id.rptPwdET);

        cancelButton = findViewById(com.example.messaging.chattingapp.R.id.cancelButton);
        signUpButton2 = findViewById(com.example.messaging.chattingapp.R.id.signUpButton2);

        Log.d(TAG, "SignUpActivityOnCreate: "+getToken());

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Signup.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signUpButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName = fNameET.getText().toString();
                String lName = lNameET.getText().toString();
                String email = suEmailET.getText().toString();
                String cPassword = chPwdET.getText().toString();
                String rPassword = rptPwdET.getText().toString();
                if (cPassword.equals(rPassword)) {
                    String password = cPassword;
                    performSignUp(fName, lName, email, password);
                } else {
                    Toast.makeText(Signup.this, "Chosen Password does not match Repeated Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void performSignUp(String fName, String lName, String email, String password) {

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("fname", fName)
                .add("lname", lName)
                .build();

        Request request = new Request.Builder()
                .url("http://ec2-54-91-96-147.compute-1.amazonaws.com/api/signup")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "performSignUpOnFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.d(TAG, "performSignUpOnResponse: " + str);
                Gson gson = new Gson();
                final SignUpResponse signUpResponse = gson.fromJson(str, SignUpResponse.class);
                Log.d(TAG, "performSignUpOnResponse: " + signUpResponse.token);
                saveToken(signUpResponse.token,signUpResponse.user_fname,signUpResponse.user_lname,signUpResponse.user_id);

                if (signUpResponse.token != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: "+getToken());
                            Toast.makeText(Signup.this, "User "+signUpResponse.user_fname+" has been created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Signup.this, ThreadActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Signup.this, ""+signUpResponse.message, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    public void saveToken(String token,String user_fname,String user_lname,String user_id) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("user_fname",user_fname);
        editor.putString("user_lname",user_lname);
        editor.putString("user_id",user_id);
        editor.apply();
    }

    public String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }
}
