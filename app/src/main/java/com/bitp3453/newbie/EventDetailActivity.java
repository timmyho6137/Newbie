package com.bitp3453.newbie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventDetailActivity extends AppCompatActivity {

    TextView tvTitle, tvDesc, tvDate, tvStart, tvEnd, tvLocation, tvCategory, tvRemind, tvNumReports;
    String id, title, desc, date, start, end, location, category, eID, hostMatric, reports;
    Button btnJoinEvent;
    NewbieDB newbieDB;
    Spinner spinRemindHour;
    Session session;
    AlertDialog.Builder alertDialogBuider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDesc = (TextView) findViewById(R.id.tvDesc);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvStart = (TextView) findViewById(R.id.tvStart);
        tvEnd = (TextView) findViewById(R.id.tvEnd);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvRemind = (TextView) findViewById(R.id.tvRemind);
        tvRemind.setVisibility(View.GONE);
        tvNumReports = (TextView) findViewById(R.id.tvNumReports);
        spinRemindHour = (Spinner) findViewById(R.id.spinEventRemind);
        spinRemindHour.setVisibility(View.GONE);
        btnJoinEvent = (Button) findViewById(R.id.btnJoinEvent);
        newbieDB = new NewbieDB(getApplicationContext());
        session = new Session(this);
        alertDialogBuider = new AlertDialog.Builder(this);

        //Data sent from HomeActivity, therefore all data is given
        id = getIntent().getStringExtra("id");
        title = getIntent().getStringExtra("title");
        desc = getIntent().getStringExtra("desc");
        date = getIntent().getStringExtra("date");
        start = getIntent().getStringExtra("start");
        end = getIntent().getStringExtra("end");
        location = getIntent().getStringExtra("location");
        category = getIntent().getStringExtra("category");
        hostMatric = getIntent().getStringExtra("matric");
        reports = getIntent().getStringExtra("reports");

        tvTitle.setText(title);
        tvDesc.setText(desc);
        tvDate.setText(date);
        tvStart.setText(start);
        tvEnd.setText(end);
        tvLocation.setText(location);
        tvCategory.setText(category);
       
        int  numReport = 0;
        if(reports!=null){
            String seperated[] = reports.split(":");
            numReport = seperated.length;
        }
        tvNumReports.setText(String.valueOf(numReport-1));

        // Data sent from CalendarActivity, so only eventId available
        final String eventId = getIntent().getStringExtra("eventId");
        eID = eventId;
        if(!TextUtils.isEmpty(eventId)){
            getEventDetails(eventId);
            spinRemindHour.setVisibility(View.VISIBLE);
            tvRemind.setVisibility(View.VISIBLE);
            btnJoinEvent.setText("Update");
            btnJoinEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i =new Intent(EventDetailActivity.this, AddEventActivity.class);
                    i.putExtra("event_id", eventId);
                    i.putExtra("title",tvTitle.getText().toString().trim());
                    i.putExtra("desc",tvDesc.getText().toString().trim());
                    i.putExtra("date",tvDate.getText().toString().trim());
                    i.putExtra("start",tvStart.getText().toString().trim());
                    i.putExtra("end",tvEnd.getText().toString().trim());
                    i.putExtra("category",tvCategory.getText().toString().trim());
                    i.putExtra("location",tvLocation.getText().toString().trim());
                    i.putExtra("host",hostMatric);
                    startActivity(i);
                }
            });
            spinRemindHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    int alarmcode = Integer.parseInt("2"+eventId);
                    cancelAlarm(alarmcode);
                    setAlarm(alarmcode, time_filter(spinRemindHour.getSelectedItemPosition()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        else {
            btnJoinEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fnJoinEvent(title, desc, date, start, end, location, category, hostMatric, getApplicationContext());
                }
            });
        }
    }

    private void setAlarm(int alarmCode, int remindBefore) {
        if (remindBefore > 0) {
            String[] separated = tvStart.getText().toString().split(":");
            int hour = Integer.parseInt(separated[0].trim());
            int minute = Integer.parseInt(separated[1].trim());

            String[] separatedDate = tvDate.getText().toString().split("-");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(separatedDate[0]),  Integer.parseInt(separatedDate[1])-1,  Integer.parseInt(separatedDate[2]));
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MINUTE, -remindBefore);

            if(System.currentTimeMillis()<calendar.getTimeInMillis()){
                Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
                alertIntent.putExtra("event", tvTitle.getText().toString());
                alertIntent.putExtra("time", tvStart.getText().toString());
                alertIntent.putExtra("location", tvLocation.getText().toString());
                int remainder = remindBefore/60;
                String msg;
                if(remainder<1)
                    msg = "30 minutes";
                else if(remainder == 1)
                    msg = "1 hour";
                else
                    msg = remainder+" hours";
                alertIntent.putExtra("remaining", msg);

                //Use eventId as the unique alarm code
                alarmCode = Integer.parseInt("2"+String.valueOf(alarmCode));
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmCode, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(getApplicationContext(), "Reminds "+spinRemindHour.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Inappropriate time to remind", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Alarm cancelled", Toast.LENGTH_SHORT).show();

        String query = "UPDATE "+NewbieDB.eventTblName+" SET "+
                NewbieDB.colEventRemindHour+" = '"+spinRemindHour.getSelectedItemPosition()+"' WHERE "+
                NewbieDB.colEventId+" = "+eID;
        newbieDB.fnExecuteSql(query,getApplicationContext());

    }

    private void cancelAlarm(int alarmCode) {
        alarmCode = Integer.parseInt("2"+String.valueOf(alarmCode));
        Log.d("AlarmCode", "cancelAlarm: "+alarmCode);
        Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmCode, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private int time_filter(int selectedItemPosition) {
        switch (selectedItemPosition){
            case 0:
                return 0;
            case 1:
                return 30;
            case 2:
                return 60;
            case 3:
                return 120;
            case 4:
                return 720;
            case 5:
                return 1440;
        }
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.shareEvent :
                intentMessageTelegram(tvTitle.getText().toString()+
                        "\n\n"+tvDesc.getText().toString()+
                        "\n\nDate: "+tvDate.getText().toString()+
                        "\nStarting Time: "+tvStart.getText().toString()+
                        "\nEnd Time: "+tvEnd.getText().toString()+
                        "\nVenue: "+tvLocation.getText().toString()+
                        "\nCategory: "+tvCategory.getText().toString());
                break;
            case R.id.reportEvent:
                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
                builder.setTitle("Confirm report?");
                builder.setMessage("Are you sure want to report this event?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                OkHttpClient client = new OkHttpClient();
                                RequestBody body = new FormBody.Builder()
                                        .add("reporter", session.matricNo())
                                        .add("event_title", tvTitle.getText().toString())
                                        .add("event_host", hostMatric)
                                        .build();
                                Request request = new Request.Builder()
                                        .url("http://www.utemupass.16mb.com/WebService/script.php")
                                        .post(body)
                                        .build();
                                try {
                                    Response response = client.newCall(request).execute();
                                    final String json = response.body().string();
                                    Log.d("OkHttp", "onOptionsItemSelected: "+json);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            alertDialogBuider.setTitle("Reporting Event");
                                            alertDialogBuider.setMessage(json).setCancelable(false);
                                            AlertDialog alertDialog = alertDialogBuider.create();
                                            alertDialog.setCanceledOnTouchOutside(true);
                                            alertDialog.show();
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private  void intentMessageTelegram(String msg){
        final String appName = "org.telegram.messenger";
        final boolean isAppInstalled = isAppAvailable(this.getApplicationContext(), appName);
        if (isAppInstalled){
            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            myIntent.setPackage(appName);
            myIntent.putExtra(Intent.EXTRA_TEXT, msg);
            this.startActivity(Intent.createChooser(myIntent, "Share with"));
        }
        else{
            Toast.makeText(this, "Telegram not Installed", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isAppAvailable(Context context, String appName){
        PackageManager pm = context.getPackageManager();
        try{
            pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e){
            return false;
        }
    }

    private void getEventDetails(String eventId) {
        Cursor cursor = newbieDB.getReadableDatabase().rawQuery("SELECT * FROM "+NewbieDB.eventTblName+" WHERE "+NewbieDB.colEventId+" =?", new String[]{eventId});
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){
                tvTitle.setText(cursor.getString(cursor.getColumnIndex(NewbieDB.colEventTitle)));
                tvDesc.setText(cursor.getString(cursor.getColumnIndex(NewbieDB.colEventDesc)));
                tvDate.setText(cursor.getString(cursor.getColumnIndex(NewbieDB.colEventDate)));
                tvStart.setText(cursor.getString(cursor.getColumnIndex(NewbieDB.colEventStart)));
                tvEnd.setText(cursor.getString(cursor.getColumnIndex(NewbieDB.colEventEnd)));
                tvLocation.setText(cursor.getString(cursor.getColumnIndex(NewbieDB.colEventLocation)));
                tvCategory.setText(cursor.getString(cursor.getColumnIndex(NewbieDB.colEventCategory)));
                hostMatric = cursor.getString(cursor.getColumnIndex(NewbieDB.colEventHost));
                final int remind = cursor.getInt(cursor.getColumnIndex(NewbieDB.colEventRemindHour));
                spinRemindHour.post(new Runnable() {
                        @Override
                        public void run() {
                            spinRemindHour.setSelection(remind);
                        }
                    }
                );
            }
        }else {
            Toast.makeText(getApplicationContext(), "No result found", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    public void fnJoinEvent(String title, String desc, String date, String start, String end, String location, String category, String matric, Context context) {
        NewbieDB db = new NewbieDB(context);
        int eventId = db.fnTotalRow(NewbieDB.eventTblName)+1;
        String query = "INSERT INTO "+ NewbieDB.eventTblName +" ("+
                NewbieDB.colEventId + ", "+
                NewbieDB.colEventTitle + ", "+
                NewbieDB.colEventDesc + ", "+
                NewbieDB.colEventDate + ", "+
                NewbieDB.colEventStart + ", "+
                NewbieDB.colEventEnd + ", "+
                NewbieDB.colEventCategory + ", "+
                NewbieDB.colEventHost + ", "+
                NewbieDB.colEventRemindHour + ", "+
                NewbieDB.colEventLocation +") VALUES ("+
                eventId+", '"+
                title+"', '"+
                desc+"','"+
                date+"', '"+
                start+"', '"+
                end+"', '"+
                category+"', '"+
                matric+"', '"+
                "0', '"+
                location+"');";

        boolean success = db.fnExecuteSql(query, null);
        if (success){
            Toast.makeText(context, "Joined event and added to timetable successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            Toast.makeText(context, "Event create failed", Toast.LENGTH_SHORT).show();
            Log.d("Join", "fnJoinEvent: "+query);
        }
    }
}
