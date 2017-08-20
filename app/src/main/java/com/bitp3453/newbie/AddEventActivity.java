package com.bitp3453.newbie;

import android.app.DatePickerDialog;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddEventActivity extends AppCompatActivity {
    private EditText eventTitle, eventDescription, eventDate, eventStart, eventEnd, eventLocation;
    private Spinner eventCategory;
    private NewbieDB newbieDB;
//    private ProgressDialog progressDialog;
    private Session session;
    Calendar calendar;
    final int currHour=0, currMin=0;
    Button btnCreate;
    String id;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        eventTitle = (EditText) findViewById(R.id.eventTitle);
        eventDescription = (EditText) findViewById(R.id.eventDescription);
        eventDate = (EditText) findViewById(R.id.eventDate);
        eventStart = (EditText) findViewById(R.id.eventStart);
        eventEnd = (EditText) findViewById(R.id.eventEnd);
        eventLocation = (EditText) findViewById(R.id.eventLocation);
        eventCategory = (Spinner) findViewById(R.id.eventCategory);
        newbieDB = new NewbieDB(getApplicationContext());
//        progressDialog = new ProgressDialog(getApplicationContext());
        session = new Session(this);
        calendar = Calendar.getInstance();
        btnCreate = (Button) findViewById(R.id.btnCreate);
        progressDialog = new ProgressDialog(this);

        // Call from EventDetailActivity for update event
        if(getIntent().getStringExtra("event_id")!=null){
            id = getIntent().getStringExtra("event_id");
            String title = getIntent().getStringExtra("title");
            String details = getIntent().getStringExtra("desc");
            String date = getIntent().getStringExtra("date");
            String start = getIntent().getStringExtra("start");
            String end = getIntent().getStringExtra("end");
            String category = getIntent().getStringExtra("category");
            String location = getIntent().getStringExtra("location");
            final String matric = getIntent().getStringExtra("host");

            setTitle("Edit Event Details");

            eventTitle.setText(title);
            eventDescription.setText(details);
            eventDate.setText(date);
            eventStart.setText(start);
            eventEnd.setText(end);
            eventLocation.setText(location);
            if(category.equals("Personal")){
                eventCategory.post(new Runnable() {
                    @Override
                    public void run() {
                        eventCategory.setSelection(0);
                    }
                });
            }
            else if(category.equals("Sports")){
                eventCategory.post(new Runnable() {
                    @Override
                    public void run() {
                        eventCategory.setSelection(1);
                    }
                });
            }
            else if(category.equals("Technology")){
                eventCategory.post(new Runnable() {
                    @Override
                    public void run() {
                        eventCategory.setSelection(2);
                    }
                });
            }
            else if(category.equals("Self Development")){
                eventCategory.post(new Runnable() {
                    @Override
                    public void run() {
                        eventCategory.setSelection(3);
                    }
                });
            }
            else if(category.equals("Indoor")){
                eventCategory.post(new Runnable() {
                    @Override
                    public void run() {
                        eventCategory.setSelection(4);
                    }
                });
            }
            else if(category.equals("Outdoor")){
                eventCategory.post(new Runnable() {
                    @Override
                    public void run() {
                        eventCategory.setSelection(5);
                    }
                });
            }
            btnCreate.setText("Update Event Details");
            btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO
                    if(matric.equals(session.matricNo())){
                        progressDialog.setMessage("Updating event...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        validateInput("update");
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Cannot edit because you are not event host.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            btnCreate.setText("Create now");
            btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    validateInput("create");
                }
            });
        }
        //Show DatePickerDialog when click on date textview
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        //Reusable TimePickerListener
        View.OnClickListener showTimePicker = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String strHour ="", strMin = "";
                        if(selectedHour < 10){
                            strHour = "0"+String.valueOf(selectedHour);
                        }
                        else {
                            strHour=String.valueOf(selectedHour);
                        }
                        if (selectedMinute<10){
                            strMin = "0"+String.valueOf(selectedMinute);
                        }
                        else {
                            strMin = String.valueOf(selectedMinute);
                        }
                        ((EditText) view).setText(strHour +":"+strMin);
                    }
                }, currHour, currMin, false); //Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        };
        eventStart.setOnClickListener(showTimePicker);
        eventEnd.setOnClickListener(showTimePicker);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_delete){
            if(id!=null) {
                String query = "DELETE FROM " + NewbieDB.eventTblName + " WHERE " + NewbieDB.colEventId + " = " + id;
                boolean success = newbieDB.fnExecuteSql(query, getApplicationContext());
                if (success) {
                    Toast.makeText(getApplicationContext(), "Deleted Successfully from your timetable", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddEventActivity.this, CalendarActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    eventDescription.setText(query);
                    Toast.makeText(getApplicationContext(), "Delete Failed", Toast.LENGTH_SHORT).show();
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Delete option unavailable", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        eventDate.setText(sdf.format(calendar.getTime()));
    }

    private void validateInput(final String mode){
        final String title = eventTitle.getText().toString().trim();
        final String desc = eventDescription.getText().toString().trim();
        final String date = eventDate.getText().toString().trim();
        final String start = eventStart.getText().toString().trim();
        final String end = eventEnd.getText().toString().trim();
        final String location = eventLocation.getText().toString().trim();
        final String category = eventCategory.getSelectedItem().toString();

        if(TextUtils.isEmpty(title)){
            eventTitle.setError("Please enter this field.");
            eventTitle.requestFocus();
        }
        else if(TextUtils.isEmpty(desc)){
            eventDescription.setError("Please enter this field");
            eventDescription.requestFocus();
        }
        else if(TextUtils.isEmpty(date)){
            eventDate.setError("Please enter this field");
            eventDate.requestFocus();
        }
        else if(TextUtils.isEmpty(start)){
            eventStart.setError("Please enter this field");
            eventStart.requestFocus();
        }
        else if(TextUtils.isEmpty(end)){
            eventEnd.setError("Please enter this field");
            eventEnd.requestFocus();
        }
        else if(TextUtils.isEmpty(location)){
            eventLocation.setError("Please enter this field");
            eventLocation.requestFocus();
        }
        else if(category.equals("Personal")){
            fnCreateEvent(title, desc, date, start, end, location, category, mode);
        }
        else{
            progressDialog.setMessage("Creating event...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean success = sendToServer(title, desc, date, start, end, location, category, session.matricNo(), mode);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(success){
                                fnCreateEvent(title, desc, date, start, end, location, category, mode);
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Event Created Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Event Create failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).start();
        }
    }

    private void fnCreateEvent(String title, String desc, String date, String start, String end, String location, String category, String mode) {
        //Code to CREATE
        if(mode.equals("create")) {
            int eventId = newbieDB.fnTotalRow(NewbieDB.eventTblName) + 1;
            String query = "INSERT INTO " + NewbieDB.eventTblName + " (" +
                    NewbieDB.colEventId + ", " +
                    NewbieDB.colEventTitle + ", " +
                    NewbieDB.colEventDesc + ", " +
                    NewbieDB.colEventDate + ", " +
                    NewbieDB.colEventStart + ", " +
                    NewbieDB.colEventEnd + ", " +
                    NewbieDB.colEventCategory + ", " +
                    NewbieDB.colEventHost + ", " +
                    NewbieDB.colEventColor + ", " +
                    NewbieDB.colEventLocation + ") VALUES (" +
                    eventId + ", '" +
                    title + "', '" +
                    desc + "','" +
                    date + "', '" +
                    start + "', '" +
                    end + "', '" +
                    category + "', '" +
                    session.matricNo() + "', '" +
                    R.color.event_color_01 + "', '" +
                    location + "');";

            boolean success = newbieDB.fnExecuteSql(query, null);
            if (success) {
                Toast.makeText(AddEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddEventActivity.this, "Event create failed", Toast.LENGTH_SHORT).show();
                Log.d("Error", "fnCreateEvent: " + query);
                eventDescription.setText(query);
            }
        //Code to UPDATE
        }
        else if (mode.equals("update") ){
            final String query = "UPDATE "+ NewbieDB.eventTblName +" SET "+
                    NewbieDB.colEventTitle + " = '"+title+"', "+
                    NewbieDB.colEventDesc + " = '"+desc+"', "+
                    NewbieDB.colEventDate + " = '"+date+"', "+
                    NewbieDB.colEventStart + " = '"+start+"', "+
                    NewbieDB.colEventEnd + " = '"+end+"', "+
                    NewbieDB.colEventCategory + " = '"+category+"', "+
                    NewbieDB.colEventLocation+" = '"+location+"' WHERE "+
                    NewbieDB.colEventId+" = "+id;
            final boolean success = newbieDB.fnExecuteSql(query,getApplicationContext());
            if(success){
                Intent i = new Intent(AddEventActivity.this, CalendarActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (success){
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Update Successfully",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d("SQL", "run: "+query);
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Update Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Log.d("Validate", "fnCreateEvent: some error occured");
        }
    }

    private boolean sendToServer(String title, String desc, String date, String start, String end, String location, String category, String user_matric, String mode){
        OkHttpClient client = new OkHttpClient();
        if(mode.equals("create")) {
            RequestBody body = new FormBody.Builder()
                    .add("title", title)
                    .add("desc", desc)
                    .add("date", date)
                    .add("start", start)
                    .add("end", end)
                    .add("location", location)
                    .add("category", category)
                    .add("user_matric", user_matric)
                    .add("email", session.email())
                    .build();
            Request request = new Request.Builder()
                    .url(Event.fcmUrl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                final String json = response.body().string();
                Log.v("myLog", "Response: " + json);
                if (json.equals("success")) {
                    Log.v("myLog", "Successful Code " + json);
                    return true;
                } else {
                    Log.v("myLog", "Unexpected code " + response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(AddEventActivity.this);
                            alertDialogBuider.setTitle("Can't create event");
                            alertDialogBuider.setMessage(json).setCancelable(false);
                            AlertDialog alertDialog = alertDialogBuider.create();
                            alertDialog.setCanceledOnTouchOutside(true);
                            alertDialog.show();
                        }
                    });
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(mode.equals("update")){
            RequestBody body = new FormBody.Builder()
                    .add("id",id)
                    .add("title", title)
                    .add("desc", desc)
                    .add("date", date)
                    .add("start", start)
                    .add("end", end)
                    .add("location", location)
                    .add("category", category)
                    .add("user_matric", user_matric)
                    .add("email", session.email())
                    .build();
            Request request = new Request.Builder()
                    .url(Event.fcmUrl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    Log.v("myLog", "Successful Code" + response.body().string());
                    return true;
                } else {
                    Log.v("myLog", "Unexpected code " + response);
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
