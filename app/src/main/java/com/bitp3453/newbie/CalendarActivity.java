package com.bitp3453.newbie;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private WeekView weekView;
    NewbieDB newbieDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        weekView = (WeekView) findViewById(R.id.weekView);
        newbieDB = new NewbieDB(getApplicationContext());
        // Lets change some dimensions to best fit the view.
        setWeekView();
    }

    private void setWeekView(){
        if(weekView != null){
            weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
                @Override
                public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                    return populateEvents(newYear, newMonth);
                }
            });

            weekView.setOnEventClickListener(new WeekView.EventClickListener() {
                @Override
                public void onEventClick(WeekViewEvent event, RectF eventRect) {
                    char firstChar = String.valueOf(event.getId()).charAt(0);
                    if( firstChar == '1'){
                        //This is Class
                        Intent i = new Intent(getApplicationContext(), AddClassActivity.class);
                        i.putExtra("eventId", String.valueOf(event.getId()).substring(1));
                        i.putExtra("eventColor", event.getColor());
                        startActivity(i);
                    }
                    else if(firstChar == '2'){
                        //This is Event
                        Intent i = new Intent(getApplicationContext(), EventDetailActivity.class);
                        i.putExtra("eventId", String.valueOf(event.getId()).substring(1));
                        startActivity(i);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Invalid event", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            weekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
                @Override
                public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                }
            });

            weekView.setEmptyViewLongPressListener(new WeekView.EmptyViewLongPressListener() {
                @Override
                public void onEmptyViewLongPress(Calendar time) {
                }
            });

            weekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
                @Override
                public void onEmptyViewClicked(Calendar time) {
                    Intent i = new Intent(CalendarActivity.this, AddClassActivity.class);
                    i.putExtra("day", time.get(Calendar.DAY_OF_WEEK));
                    i.putExtra("hour", time.get(Calendar.HOUR_OF_DAY));
                    i.putExtra("date", time.get(Calendar.DATE));
                    i.putExtra("month", time.get(Calendar.MONTH));
                    i.putExtra("year", time.get(Calendar.YEAR));
                    startActivity(i);
                }
            });
        }
    }

    private List<WeekViewEvent> populateEvents(int newYear, int newMonth){
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        //TODO SELECT FROM CLASS
        String strQuery = "select * from "+NewbieDB.classTblName+" NATURAL JOIN "+NewbieDB.subTblName;
        Cursor cursor = newbieDB.getReadableDatabase().rawQuery(strQuery, null);
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                //split start time
                String start = cursor.getString(cursor.getColumnIndex(NewbieDB.colClassStart));
                String[] separated = start.split(":");
                int sHour = Integer.parseInt(separated[0].trim());
                int sMin = Integer.parseInt(separated[1].trim());
                //split end time
                String end = cursor.getString(cursor.getColumnIndex(NewbieDB.colClassEnd));
                separated = end.split(":");
                int eHour = Integer.parseInt(separated[0].trim());
                int eMin = Integer.parseInt(separated[1].trim());
                int day = resolvedDay(cursor.getString(cursor.getColumnIndex(NewbieDB.colClassDay)));

                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, sHour);
                startTime.set(Calendar.MINUTE, sMin);
                startTime.set(Calendar.DAY_OF_WEEK, day);
                startTime.set(Calendar.MONTH, newMonth);
                startTime.set(Calendar.YEAR, newYear);
                Calendar endTime = (Calendar) startTime.clone();
                endTime.set(Calendar.HOUR_OF_DAY, eHour);
                endTime.set(Calendar.MINUTE, eMin);
                endTime.set(Calendar.MONTH, newMonth);
                long myId = Long.parseLong("1"+cursor.getString(cursor.getColumnIndex(NewbieDB.colClassId)));

                Calendar lastDay = Calendar.getInstance();
                lastDay.set(Calendar.MONTH, 11);
                lastDay.set(Calendar.YEAR, newYear);
                lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));

                int q = 0;
                do {
                Calendar cal1 = (Calendar) startTime.clone();
                Calendar cal2 = (Calendar) endTime.clone();
                    WeekViewEvent event = new WeekViewEvent(myId, cursor.getString(cursor.getColumnIndex(NewbieDB.colSubName)), cal1, cal2);
                    event.setColor(getResources().getColor(cursor.getInt(cursor.getColumnIndex(NewbieDB.colClassColor))));
                    event.setLocation(cursor.getString(
                            cursor.getColumnIndex(NewbieDB.colClassType)) + ", \n" +
                            cursor.getString(cursor.getColumnIndex(NewbieDB.colClassRoom)) + ", \n" +
                            cursor.getString(cursor.getColumnIndex(NewbieDB.colClassLocation)) + ", \n" +
                            cursor.getString(cursor.getColumnIndex(NewbieDB.colSubLecturer)));

                    events.add(event);
                    startTime.add(Calendar.DATE, 7);
                    endTime.add(Calendar.DATE, 7);
                    q++;
                    Log.d("Calendar", "populateEvents: "+q);
                    Log.d("Calendar", "populateEvents: "+startTime.getTimeInMillis());
                }
                while(startTime.getTimeInMillis()<lastDay.getTimeInMillis());
            }
        }
        cursor.close();

        //TODO SELECT FROM EVENT
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        strQuery = "select * from "+NewbieDB.eventTblName+ " where "+NewbieDB.colEventDate+" >= '"+sdf.format(Calendar.getInstance().getTime())+"'";
        Log.d("EC", "populateEvents: "+newMonth);

        cursor = newbieDB.getReadableDatabase().rawQuery(strQuery,null);
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){
                //split date
                String [] separatedDate = cursor.getString(cursor.getColumnIndex(NewbieDB.colEventDate)).split("-");
                int year = Integer.parseInt(separatedDate[0].trim());
                int month = Integer.parseInt(separatedDate[1].trim());
                int day = Integer.parseInt(separatedDate[2].trim());

                //TODO CHECK IF AND ONLY IF THE EVENT MONTH MATCHES WITH THE MONTH PASSED BY THE onMonthChange()
                if(month==newMonth && year==newYear) {

                    //split start time
                    String start = cursor.getString(cursor.getColumnIndex(NewbieDB.colEventStart));
                    String[] separated = start.split(":");
                    int sHour = Integer.parseInt(separated[0].trim());
                    int sMin = Integer.parseInt(separated[1].trim());
                    //split end time
                    String end = cursor.getString(cursor.getColumnIndex(NewbieDB.colEventEnd));
                    separated = end.split(":");
                    int eHour = Integer.parseInt(separated[0].trim());
                    int eMin = Integer.parseInt(separated[1].trim());

                    Log.d("Date", "populateEvents: " + year + " " + month + " " + day);
                    Calendar startTime = Calendar.getInstance();
                    startTime.set(Calendar.HOUR_OF_DAY, sHour);
                    startTime.set(Calendar.MINUTE, sMin);
                    startTime.set(Calendar.DAY_OF_MONTH, day);
                    startTime.set(Calendar.MONTH, month - 1);
                    startTime.set(Calendar.YEAR, year);
                    Calendar endTime = (Calendar) startTime.clone();
                    endTime.set(Calendar.HOUR_OF_DAY, eHour);
                    endTime.set(Calendar.MINUTE, eMin);
                    endTime.set(Calendar.DAY_OF_MONTH, day);
                    endTime.set(Calendar.MONTH, month - 1);
                    long myId = Long.parseLong("2"+cursor.getString(cursor.getColumnIndex(NewbieDB.colEventId)));
                    WeekViewEvent event = new WeekViewEvent(myId, cursor.getString(cursor.getColumnIndex(NewbieDB.colEventTitle)), startTime, endTime);
                    event.setColor(getResources().getColor(R.color.event_color_01));
                    event.setLocation(cursor.getString(
                            cursor.getColumnIndex(NewbieDB.colEventLocation)) + ", \n" +
                            cursor.getString(cursor.getColumnIndex(NewbieDB.colEventCategory)));
                    events.add(event);
                }
            }
        }
        cursor.close();

        return events;
    }

    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case  R.id.action_today:
                weekView.goToToday();
                return true;
            case R.id.action_day_view:
                item.setChecked(true);
                weekView.setNumberOfVisibleDays(1);
                return true;
            case R.id.action_three_day_view:
                item.setChecked(true);
                weekView.setNumberOfVisibleDays(3);
                return true;
            case R.id.action_week_view:
                item.setChecked(true);
                weekView.setNumberOfVisibleDays(7);
                // Lets change some dimensions to best fit the view.
                weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                return true;
        }
        return super.onOptionsItemSelected(item);
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
}
