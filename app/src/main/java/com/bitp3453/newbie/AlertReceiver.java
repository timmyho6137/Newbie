package com.bitp3453.newbie;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Timmy Ho on 8/9/2017.
 */

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("subject")!=null){
            createNotification(context,
                    intent.getStringExtra("subject")+" class",
                    intent.getStringExtra("time"),
                    intent.getStringExtra("remaining"),
                    intent.getStringExtra("location"),
                    "Class Reminder");
        }
        else if(intent.getStringExtra("event")!=null) {
            createNotification(context,
                    intent.getStringExtra("event"),
                    intent.getStringExtra("time"),
                    intent.getStringExtra("remaining"),
                    intent.getStringExtra("location"),
                    "Event Reminder");
        }
    }

    private void createNotification(Context context, String title, String time, String remainingTime, String location, String msgAlert) {
        Intent destIntent = new Intent(context, CalendarActivity.class);
        PendingIntent notifyIntent = PendingIntent.getActivity(context, 0, destIntent, 0);

        NotificationCompat.Builder notificBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(" Starting in "+remainingTime)
                .setTicker(msgAlert)
                .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(
                        title+" will be started in "+remainingTime+" at "+time+" at "+location))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_launcher))
                .setSmallIcon(R.drawable.app_launcher);
        notificBuilder.setContentIntent(notifyIntent);
        notificBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        notificBuilder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificBuilder.build());
    }
}
