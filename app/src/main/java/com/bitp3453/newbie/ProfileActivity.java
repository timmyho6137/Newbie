package com.bitp3453.newbie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    EditText etName, etMatric, etEmail, etNewPassword, etOldPassword;
    Session session;
    Button btnUpdate;
    CheckBox cbSports, cbTechnology, cbSelfDevelop, cbIndoor, cbOutdoor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        etName = (EditText) findViewById(R.id.etName);
        etMatric = (EditText) findViewById(R.id.etMatric);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etNewPassword = (EditText) findViewById(R.id.etNewPassword);
        etOldPassword = (EditText) findViewById(R.id.etOldPassword);
        session = new Session(this);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        cbSports = (CheckBox) findViewById(R.id.cbSports) ;
        cbTechnology = (CheckBox) findViewById(R.id.cbTechnology) ;
        cbSelfDevelop = (CheckBox) findViewById(R.id.cbSelfDevelop) ;
        cbIndoor = (CheckBox) findViewById(R.id.cbIndoor) ;
        cbOutdoor = (CheckBox) findViewById(R.id.cbOutdoor) ;

        etName.setText(session.name());
        etEmail.setText(session.email());
        etMatric.setText(session.matricNo());

        populateCheckbox();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    private void populateCheckbox() {
        if(!TextUtils.isEmpty(session.categories())){
            String [] seperated = session.categories().split(":");
            for (int i = 0; i<seperated.length;i++){
                if(seperated[i].equals(cbSports.getText().toString())){
                    cbSports.setChecked(true);
                }
                else if(seperated[i].equals(cbTechnology.getText().toString())){
                    cbTechnology.setChecked(true);
                }
                else if(seperated[i].equals(cbSelfDevelop.getText().toString())){
                    cbSelfDevelop.setChecked(true);
                }
                else if(seperated[i].equals(cbIndoor.getText().toString())){
                    cbIndoor.setChecked(true);
                }
                else if(seperated[i].equals(cbOutdoor.getText().toString())){
                    cbOutdoor.setChecked(true);
                }
            }
        }
    }

    private void updateProfile() {
        final StringBuilder categories = new StringBuilder();
        if(cbSports.isChecked()){
            categories.append(cbSports.getText().toString());
        }
        if(cbTechnology.isChecked()){
            categories.append(":").append(cbTechnology.getText().toString());
        }
        if(cbSelfDevelop.isChecked()){
            categories.append(":").append(cbSelfDevelop.getText().toString());
        }
        if(cbIndoor.isChecked()){
            categories.append(":").append(cbIndoor.getText().toString());
        }
        if(cbOutdoor.isChecked()){
            categories.append(":").append(cbOutdoor.getText().toString());
        }
        final String strName = etName.getText().toString().trim();
        final String strEmail = etEmail.getText().toString().trim();
        final String strNewPassword = etNewPassword.getText().toString().trim();
        String strOldPassword = etOldPassword.getText().toString().trim();
        final String strMatricNo = etMatric.getText().toString().trim();

        if(TextUtils.isEmpty(strName)){
            etName.setError("Please enter this field");
        }
        else if(TextUtils.isEmpty(strMatricNo)){
            etName.setError("Please enter this field");
        }
        else if(TextUtils.isEmpty(strEmail)){
            etEmail.setError("Please enter this field");
        }
        else if(TextUtils.isEmpty(strNewPassword)){
            etNewPassword.setError("Please enter this field");
        }
        else if(TextUtils.isEmpty(strOldPassword)){
            etOldPassword.setError("Please enter this field");
        }
        else try {
                if(!strOldPassword.equals(decrypt(session.password()))){
                    Toast.makeText(getApplicationContext(), "Incorrect old password", Toast.LENGTH_SHORT).show();
                }
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final OkHttpClient client = new OkHttpClient();
                            RequestBody body = new FormBody.Builder()
                                    .add("update", "true")
                                    .add("name", strName)
                                    .add("email", strEmail)
                                    .add("old_email", session.email())
                                    .add("password", strNewPassword)
                                    .add("matric", strMatricNo)
                                    .add("category", categories.toString())
                                    .build();
                            final Request request = new Request.Builder()
                                    .url(Event.fcmUrl)
                                    .post(body)
                                    .build();
                            try {
                                final Response response = client.newCall(request).execute();
                                final String strResponse = response.body().string();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(response.isSuccessful()){
                                            if(strResponse.equals("success")){
                                                session.setUserName(strName);
                                                session.setEmail(strEmail);
                                                session.setPassword(strNewPassword);
                                                session.setMatricNo(strMatricNo);
                                                session.setCategories(categories.toString());
                                                Toast.makeText(getApplicationContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else {
                                                Toast.makeText(getApplicationContext(), strResponse, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), strResponse, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private String decrypt(String password) throws Exception{
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte [] decodedValue = Base64.decode(password, Base64.DEFAULT);
        byte [] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
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
