package com.bitp3453.newbie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button btnLogIn, btnRegister;
    private ProgressDialog progressDialog;
    private NewbieDB newbieDB;
    private Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.etEmail);
        password = (EditText) findViewById(R.id.etOldPassword);
        btnLogIn = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        newbieDB = new NewbieDB(getApplicationContext());
        session = new Session(this);
        progressDialog = new ProgressDialog(this);

        if(session.loggedIn()){
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String strEmail = email.getText().toString().trim();
                final String strPassword = password.getText().toString().trim();
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
                else if (TextUtils.isEmpty(strPassword)){
                    password.setError("Please enter this field");
                    password.requestFocus();
                }
                else if(strPassword.contains(" ")){
                    password.setError("Cannot contain whitespace");
                    password.requestFocus();
                }
                else {
//                    loginFromSqlite(strEmail, strPassword);
                    progressDialog.setMessage("Logging in...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //TODO Login with MySQL Database
                            loginFromMySql(strEmail, strPassword);
                        }
                    }).start();
                }
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginFromSqlite(String strEmail, String strPassword) {
        String query = "SELECT * FROM "+NewbieDB.userTblName+" WHERE "+NewbieDB.colUserEmail+" = ? AND "+NewbieDB.colUserPassword+" =?";
        Cursor c = newbieDB.getReadableDatabase().rawQuery(query, new String[]{strEmail, strPassword});
        if(c.getCount()>0){
            while (c.moveToNext()){
                String name = c.getString(c.getColumnIndex(NewbieDB.colUserName));
                String matricNo = c.getString(c.getColumnIndex(NewbieDB.colUserMatricNo));
                session.setLoggedIn(true);
                session.setUserName(name);
                session.setMatricNo(matricNo);
                Toast.makeText(getApplicationContext(), session.name()+" "+session.matricNo(), Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
        }
        //progressDialog.dismiss();
        c.close();
    }

    private void loginFromMySql(String strEmail, String strPassword){
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("email", strEmail)
                .add("password", strPassword)
                .add("login", "true")
                .build();
        Request request = new Request.Builder()
                .url(Event.fcmUrl)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            Log.d("Login", "loginFromMySql: "+json);
            JSONArray  jsonArr = new JSONArray(json);
            if(jsonArr.length()>0){
                JSONObject jsonObj = jsonArr.getJSONObject(0);
                session.setLoggedIn(true);
                session.setEmail(strEmail);
                session.setPassword(strPassword);
                Log.d("Password", "loginFromMySql: "+strPassword);
                Log.d("Password", "loginFromMySql: "+encrypt(session.password()));
                session.setUserName(jsonObj.getString("user_name"));
                session.setMatricNo(jsonObj.getString("user_matric"));
                session.setCategories(jsonObj.getString("user_category"));
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        email.requestFocus();
                    }
                });
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Connection Timeout", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encrypt(String password) throws Exception{
        SecretKeySpec key = generateKey("NpEaWsBsIwEord");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(password.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;
    }

    private SecretKeySpec generateKey(String password) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte [] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }
}
