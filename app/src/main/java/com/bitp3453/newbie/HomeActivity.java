package com.bitp3453.newbie;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    private Session session;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    private ArrayList<String> allId, allTitle, allDesc, allDate, allStart, allEnd, allLocation, allCategory, allMatric, allReports;
    String [] ids, titles, details, dates, starts, ends, locations, categories, matrices, reports;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        session = new Session(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading events...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-message"));
        boolean isPenalty = getIntent().getBooleanExtra("penalty", false);
        if(isPenalty){
            session.setPenalty(true);
        }
        reload();

        FloatingActionButton calendarFab = (FloatingActionButton) findViewById(R.id.calendarFab);
        calendarFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
            }
        });

        FloatingActionButton eventFab = (FloatingActionButton) findViewById(R.id.eventFab);
        eventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddEventActivity.class));
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("test");
        final String token = FirebaseInstanceId.getInstance().getToken();

        //Update user database with phone token
        if(!session.tokenStored() && !TextUtils.isEmpty(token)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendRegistrationToServer(token);
                }
            }).start();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_logout){
            logout();
        }
        else if (item.getItemId()==R.id.action_profile){
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        }
        else if(item.getItemId()==R.id.action_refresh){
            progressDialog.setMessage("Refreshing events...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            reload();
        }
        return super.onOptionsItemSelected(item);
    }
    private void reload(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(fetchAllEvents()){
                    ids = allId.toArray(new String[0]);
                    titles = allTitle.toArray(new String[0]);
                    details = allDesc.toArray(new String[0]);
                    dates = allDate.toArray(new String[0]);
                    starts = allStart.toArray(new String[0]);
                    ends = allEnd.toArray(new String[0]);
                    locations = allLocation.toArray(new String[0]);
                    categories= allCategory.toArray(new String[0]);
                    matrices= allMatric.toArray(new String[0]);
                    reports = allReports.toArray(new String[0]);
                }
                else {
                    titles = new String[]{"Empty"};
                    details = new String []{"No event available yet"};
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

                        layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(layoutManager);

                        adapter = new RecyclerAdapter(ids, titles, details, dates, starts, ends, locations, categories, matrices, getApplicationContext());
                        recyclerView.setAdapter(adapter);
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    private void logout(){
        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
        alert.setTitle("Confirm Delete");
        alert.setMessage("Are you sure want to log out?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Delete session variable here and sqlite instance data
                session.setLoggedIn(false);
                session.setUserName(null);
                session.setEmail(null);
                session.setPassword(null);
                session.setMatricNo(null);
                session.setCategories(null);
                session.setTokenStored(false);
//                new NewbieDB(getApplicationContext()).fnExecuteSql("DELETE FROM "+NewbieDB.userTblName, getApplicationContext());
                Toast.makeText(getApplicationContext(), "Logged out.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    private void sendRegistrationToServer(String token) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .add("email", session.email())
                .build();
        Request request = new Request.Builder()
                .url(Event.fcmUrl)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            if(json.equals("success")){
                session.setTokenStored(true);
                Log.v("MyStoredToken", "Storing success");
            }
            else {
                Log.v("MyStoredToken", "Storing Failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean  fetchAllEvents(){
        boolean success = false;
        allId= new ArrayList<String>();
        allTitle= new ArrayList<String>();
        allDesc= new ArrayList<String>();
        allDate= new ArrayList<String>();
        allStart= new ArrayList<String>();
        allEnd= new ArrayList<String>();
        allLocation= new ArrayList<String>();
        allCategory= new ArrayList<String>();
        allMatric= new ArrayList<String>();
        allReports = new ArrayList<String>();
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("event", "true")
                .add("date", sdf.format(Calendar.getInstance().getTime()))
                .build();
        Request request = new Request.Builder()
                .url(Event.fcmUrl)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            JSONArray jsonArray = new JSONArray(json);
            Log.d("Data", "run: "+json);
            if(jsonArray.length()>0){
                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    allId.add(jsonObj.getString("event_id"));
                    allTitle.add(jsonObj.getString("event_title"));
                    allDesc.add(jsonObj.getString("event_desc"));
                    allDate.add(jsonObj.getString("event_date"));
                    allStart.add(jsonObj.getString("event_start"));
                    allEnd.add(jsonObj.getString("event_end"));
                    allLocation.add(jsonObj.getString("event_location"));
                    allCategory.add(jsonObj.getString("event_category"));
                    allMatric.add(jsonObj.getString("user_matric"));
                    allReports.add(jsonObj.getString("report_persons"));
                }
                success =  true;
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "No upcoming events available at the moment.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "There's a problem communicating with the server. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        }
        return success;
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            final int position = intent.getIntExtra("position",0);
            if (intent.getBooleanExtra("join", false)) {
                //Execute join query
                AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                alert.setTitle("Confirm Join");
                alert.setMessage("Join "+titles[position]+" ?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new EventDetailActivity().fnJoinEvent(titles[position],details[position],dates[position], starts[position], ends[position], locations[position], categories[position], matrices[position], getApplicationContext());
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            }
            else {
                Intent i = new Intent(HomeActivity.this, EventDetailActivity.class);
                i.putExtra("id", ids[position]);
                i.putExtra("title", titles[position]);
                i.putExtra("desc", details[position]);
                i.putExtra("date", dates[position]);
                i.putExtra("start", starts[position]);
                i.putExtra("end", ends[position]);
                i.putExtra("location", locations[position]);
                i.putExtra("category", categories[position]);
                i.putExtra("matric", matrices[position]);
                i.putExtra("reports", reports[position]);
                startActivity(i);
            }
        }
    };
}
