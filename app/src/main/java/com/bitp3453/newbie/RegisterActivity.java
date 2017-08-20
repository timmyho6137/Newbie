package com.bitp3453.newbie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText email, password, name, matricNo;
    private NewbieDB newbieDB;
    private Session session;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        TextView backToLogin = (TextView) findViewById(R.id.txtVwLogin);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        email = (EditText) findViewById(R.id.regEmail);
        password = (EditText) findViewById(R.id.regPassword);
        name = (EditText) findViewById(R.id.regName);
        matricNo = (EditText) findViewById(R.id.regMatricNo);

        newbieDB = new NewbieDB(this);
        session = new Session(this);
        progressDialog = new ProgressDialog(this);

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String strEmail = email.getText().toString().trim();
                final String strPassword = password.getText().toString().trim();
                final String strName = name.getText().toString().trim();
                final String strMatricNo= matricNo.getText().toString().trim();

                if(TextUtils.isEmpty(strEmail)){
                    email.setError("Please enter this field");
                    email.requestFocus();
                }
                else if(!strEmail.contains("@")){
                    email.setError("Invalid email format");
                    email.requestFocus();
                }
                else if(strEmail.contains(" ")){
                    email.setError("Invalid email format");
                    email.requestFocus();
                }
                else if(TextUtils.isEmpty(strPassword)){
                    password.setError("Please enter this field");
                    password.requestFocus();
                }
                else if(strPassword.contains(" ")){
                    password.setError("Cannot contain whitespace");
                    password.requestFocus();
                }
                else if(TextUtils.isEmpty(strName)){
                    name.setError("Please enter this field");
                    name.requestFocus();
                }
                else if(TextUtils.isEmpty(strMatricNo)){
                    matricNo.setError("Please enter this field");
                    matricNo.requestFocus();
                }
                else if(strMatricNo.contains(" ")){
                    matricNo.setError("Cannot contain whitespace");
                    matricNo.requestFocus();
                }
                else {
                    progressDialog.setMessage("Registering as new user");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //TODO Register New User Here
                            registerNewUser(strName, strEmail, strPassword, strMatricNo);
                        }
                    }).start();
                }
            }
        });
    }

    private void registerNewUser(String strName, String strEmail, String strPassword, String strMatricNo) {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("register", "register")
                    .add("name", strName)
                    .add("email", strEmail)
                    .add("password", strPassword)
                    .add("matric", strMatricNo)
                    .build();
            Request request = new Request.Builder()
                    .url(Event.fcmUrl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String json = response.body().string();
                if(json.equals("success")){
                    Log.v("Registration", "Registering success");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Successfully Registered.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Failed to register.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
