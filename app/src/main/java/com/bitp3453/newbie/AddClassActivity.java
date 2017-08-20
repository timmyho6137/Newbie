package com.bitp3453.newbie;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AddClassActivity extends AppCompatActivity {

    EditText classLocation, classRoom, classStart, classEnd;
    NewbieDB newbieDB;
    Spinner subName, classType, classDay, remindHours;
    String classId;
    int subId, currDay, currHour, currMin;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        newbieDB = new NewbieDB(getApplicationContext());
        classRoom = (EditText) findViewById(R.id.classRoom);
        classLocation = (EditText) findViewById(R.id.classLocation);
        classStart = (EditText) findViewById(R.id.classStart);
        classEnd = (EditText) findViewById(R.id.classEnd);
        classDay = (Spinner) findViewById(R.id.classDay);
        classType = (Spinner) findViewById(R.id.classType);
        subName = (Spinner) findViewById(R.id.spinner);
        remindHours = (Spinner) findViewById(R.id.sRemind);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        progressDialog = new ProgressDialog(this);

        currDay = getIntent().getIntExtra("day",3);
        currHour = getIntent().getIntExtra("hour",0);
        currMin = 0;

        //Populate all subject name
        subName.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, subjectList()){
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return  view;
            }
        });

        switch (currDay){
            //Monday
            case 2:
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(0);
                    }
                });
                break;
            //Tuesday
            case 3:
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(1);
                    }
                });
                break;
            //Wednesday
            case 4:
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(2);
                    }
                });
                break;
            //Thursday
            case 5:
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(3);
                    }
                });
                break;
            //Friday
            case 6:
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(4);
                    }
                });
                break;
            //Saturday
            case 7:
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(5);
                    }
                });
                break;
            //Sunday
            case 1:
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(6);
                    }
                });
                break;
        }

        //Opens new activity when selected 'Add New Subject'
        subName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long row_id) {
                if(position == adapterView.getCount()-1){
                    startActivity(new Intent(AddClassActivity.this, AddSubjectActivity.class));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Check whether activity is used for updating or deleting class
        classId = getIntent().getStringExtra("eventId");
        if(classId!=null){
            setTitle("Edit Class Details");
            btnSave.setText("Update");
            subName.post(new Runnable() {
                @Override
                public void run() {
                    subName.setSelection(loadEditData(classId));
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.setMessage("Updating class...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    fnUpdate();
                }
            });
        }
        else {
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.setMessage("Adding new class...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    fnSave();
                }
            });
        }

        TimePicker picker = new TimePicker(getApplicationContext());
        picker.setCurrentHour(currHour);
        picker.setCurrentMinute(0);

        //Reusable TimePickerListener
        View.OnClickListener showTimePicker = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String time = ((EditText) view).getText().toString();
                int hour =0, min=0;
                if(!time.isEmpty()){
                    String [] seperated = time.split(":");
                    hour = Integer.parseInt(seperated[0]);
                    min = Integer.parseInt(seperated[1]);
                }
                else {
                    hour = currHour;
                    min = currMin;
                }

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddClassActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                        currHour = selectedHour;
                        currMin = selectedMinute;
                    }
                }, hour, min, false); //Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        };
        classStart.setOnClickListener(showTimePicker);
        classEnd.setOnClickListener(showTimePicker);
    }

    private void setAlarm( int alarmCode, int remindBefore) {
        if(remindBefore>0){
            String[] separated = classStart.getText().toString().split(":");
            int hour = Integer.parseInt(separated[0].trim());
            int minute = Integer.parseInt(separated[1].trim());

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, resolvedDay(classDay.getSelectedItem().toString().trim()));
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MINUTE, -remindBefore);

            if(System.currentTimeMillis()<calendar.getTimeInMillis()){
                Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
                alertIntent.putExtra("subject", subName.getSelectedItem().toString());
                alertIntent.putExtra("time", classStart.getText().toString().trim());
                alertIntent.putExtra("location", classRoom.getText().toString().trim()+" in "+classLocation.getText().toString().trim());
                int remainder = remindBefore/60;
                String msg;
                if(remainder<1)
                    msg = "30 minutes";
                else if(remainder == 1)
                    msg = "1 hour";
                else
                    msg = remainder+" hours";
                alertIntent.putExtra("remaining", msg);

                //Use classId as the unique alarm code
                alarmCode = Integer.parseInt("1"+String.valueOf(alarmCode));
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmCode, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7, pendingIntent);
            }
            else
                Toast.makeText(getApplicationContext(), "Inappropriate time to remind", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm(int alarmCode) {
        alarmCode = Integer.parseInt("1"+String.valueOf(alarmCode));
        Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmCode, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private int resolvedDay(String strDay) {
        int day = 1;
        switch (strDay){
            case "Sunday":
                day = 1;
                return day;
            case "Monday":
                day = 2;
                return day;
            case "Tuesday":
                day = 3;
                return day;
            case "Wednesday":
                day = 4;
                return day;
            case "Thursday":
                day = 5;
                return day;
            case "Friday":
                day = 6;
                return day;
            case "Saturday":
                day = 7;
                return day;
            default:
                return day;
        }
    }

    private int loadEditData(String id) {
        Cursor c = newbieDB.getReadableDatabase().rawQuery(
                "SELECT * FROM "+NewbieDB.classTblName+" NATURAL JOIN "+NewbieDB.subTblName+" WHERE "+NewbieDB.colClassId+" =?", new String[]{id});
        if(c.moveToNext()){
            classRoom.setText(c.getString(c.getColumnIndex(NewbieDB.colClassRoom)));
            classLocation.setText(c.getString(c.getColumnIndex(NewbieDB.colClassLocation)));
            classStart.setText(c.getString(c.getColumnIndex(NewbieDB.colClassStart)));
            classEnd.setText(c.getString(c.getColumnIndex(NewbieDB.colClassEnd)));
            subId = Integer.parseInt(c.getString(c.getColumnIndex(NewbieDB.colSubId)));

            //Filter Class Type
            String selectedType = c.getString(c.getColumnIndex(NewbieDB.colClassType));
            if(selectedType.equals("Lecture")){
                classType.post(new Runnable() {
                    @Override
                    public void run() {
                        classType.setSelection(0);
                    }
                });
            }
            else if(selectedType.equals("Lab")){
                classType.post(new Runnable() {
                    @Override
                    public void run() {
                        classType.setSelection(1);
                    }
                });
            }

            //Filter Class Day
            String selectedDay = c.getString(c.getColumnIndex(NewbieDB.colClassDay));
            if(selectedDay.equals("Monday")){
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(0);
                    }
                });
            }
            else if(selectedDay.equals("Tuesday")){
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(1);
                    }
                });
            }
            else if(selectedDay.equals("Wednesday")){
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(2);
                    }
                });
            }
            else if(selectedDay.equals("Thursday")){
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(3);
                    }
                });
            }
            else if(selectedDay.equals("Friday")){
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(4);
                    }
                });
            }
            else if(selectedDay.equals("Saturday")){
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(5);
                    }
                });
            }
            else if(selectedDay.equals("Sunday")){
                classDay.post(new Runnable() {
                    @Override
                    public void run() {
                        classDay.setSelection(6);
                    }
                });
            }

            //Remind Hours Before
            final int hPosition = c.getInt(c.getColumnIndex(NewbieDB.colClassRemindHour));
            remindHours.post(new Runnable() {
                @Override
                public void run() {
                    remindHours.setSelection(hPosition);
                }
            });

            //Filter Subject Selected
            String subject = c.getString(c.getColumnIndex(NewbieDB.colSubName));
            int position = 0;
            for (String s : subjectList()){
                if(s.equals(subject)){
                    subName.setSelection(position);
                    return position;
                }
                position++;
            }
        }
        c.close();
        return 0;
    }

    public void fnSave() {
        if(TextUtils.isEmpty(classRoom.getText().toString())){
            classRoom.setError("Please enter this field.");
        }
        else if(TextUtils.isEmpty(classLocation.getText().toString())){
            classLocation.setError("Please enter this field");
        }
        else if(TextUtils.isEmpty(classStart.getText().toString())){
            classStart.setError("Please enter this field");
        }
        else if(TextUtils.isEmpty(classEnd.getText().toString())){
            classEnd.setError("Please enter this field");
        }
        else if(subName.getSelectedItem().toString().equals("Subject Name")){
            Toast.makeText(getApplicationContext(), "Please select a subject first", Toast.LENGTH_SHORT).show();
        }
        else {
            Cursor c = newbieDB.getReadableDatabase().rawQuery(
                    "SELECT * FROM " + NewbieDB.subTblName + " WHERE " + NewbieDB.colSubName + " = ?",
                    new String[]{subName.getSelectedItem().toString().trim()});
            if (c.moveToNext()) {
                subId = c.getInt(c.getColumnIndex(NewbieDB.colSubId));
            }
            else {
                //Default value (Optional)
                subId = newbieDB.fnTotalRow(NewbieDB.subTblName) + 1;
            }
            c.close();

            classId = String.valueOf(newbieDB.fnTotalRow(NewbieDB.classTblName) + 1);
            String query = "INSERT INTO " + NewbieDB.classTblName + " (" +
                    NewbieDB.colClassId + ", " +
                    NewbieDB.colClassStart + ", " +
                    NewbieDB.colClassEnd + ", " +
                    NewbieDB.colClassDay + ", " +
                    NewbieDB.colClassType + ", " +
                    NewbieDB.colClassLocation + ", " +
                    NewbieDB.colClassRoom + ", " +
                    NewbieDB.colClassRemindHour + ", " +
                    NewbieDB.colClassColor + ", " +
                    NewbieDB.colSubId + ") VALUES (" +
                    classId + ", '" +
                    classStart.getText().toString() + "', '" +
                    classEnd.getText().toString() + "','" +
                    classDay.getSelectedItem().toString() + "', '" +
                    classType.getSelectedItem().toString() + "','" +
                    classLocation.getText().toString().trim() + "', '" +
                    classRoom.getText().toString().trim() + "', '" +
                    remindHours.getSelectedItemPosition() + "', '" +
                    R.color.event_color_02 + "', '" +
                    subId + "');";
            final boolean success = newbieDB.fnExecuteSql(query, getApplicationContext());
            if (success) {
                progressDialog.dismiss();
                setAlarm(Integer.parseInt(classId), time_filter(remindHours.getSelectedItemPosition()));
                Toast.makeText(getApplicationContext(), "Class Added Successfully", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(AddClassActivity.this, CalendarActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
            else {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Class Added Failed", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void fnUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String query = "UPDATE "+ NewbieDB.classTblName +" SET "+
                        NewbieDB.colClassStart + " = '"+classStart.getText().toString().trim()+"', "+
                        NewbieDB.colClassEnd + " = '"+classEnd.getText().toString().trim()+"', "+
                        NewbieDB.colClassDay + " = '"+classDay.getSelectedItem().toString()+"', "+
                        NewbieDB.colClassType + " = '"+classType.getSelectedItem().toString()+"', "+
                        NewbieDB.colClassLocation + " = '"+classLocation.getText().toString().trim()+"', "+
                        NewbieDB.colClassRoom + " = '"+classRoom.getText().toString().trim()+"', "+
                        NewbieDB.colClassRemindHour + " = '"+remindHours.getSelectedItemPosition()+"', "+
                        NewbieDB.colSubId+" = '"+subId+"' WHERE "+
                        NewbieDB.colClassId+" = "+classId;
                final boolean success = newbieDB.fnExecuteSql(query,getApplicationContext());
                if(success){
                    int alarmCode = Integer.parseInt(classId);
                    cancelAlarm(alarmCode);
                    setAlarm(alarmCode, time_filter(remindHours.getSelectedItemPosition()));
                    Intent i = new Intent(AddClassActivity.this, CalendarActivity.class);
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
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Update Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private String [] subjectList(){
        Cursor subjects = newbieDB.getReadableDatabase().rawQuery("Select * from "+NewbieDB.subTblName, null);
        ArrayList<String> items = new ArrayList<String>();
        items.add("Subject Name");
        while (subjects.moveToNext()){
            items.add(subjects.getString(subjects.getColumnIndex(NewbieDB.colSubName)));
        }
        items.add("Add New Subject");
        subjects.close();
        return items.toArray(new String[0]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subName.setSelection(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(classId!=null){
            getMenuInflater().inflate(R.menu.delete, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_delete){
            if(classId!=null) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddClassActivity.this);
                alert.setTitle("Confirm Delete");
                alert.setMessage("Are you sure want to delete this class?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String query = "DELETE FROM " + NewbieDB.classTblName + " WHERE " + NewbieDB.colClassId + " = " + classId;
                        boolean success = newbieDB.fnExecuteSql(query, getApplicationContext());
                        if (success) {
                            Toast.makeText(getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddClassActivity.this, CalendarActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            classRoom.setText(query);
                            Toast.makeText(getApplicationContext(), "Delete Failed", Toast.LENGTH_SHORT).show();
                        }
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
            else
                Toast.makeText(getApplicationContext(), "Delete option not available", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
