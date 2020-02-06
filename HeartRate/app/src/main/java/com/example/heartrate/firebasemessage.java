package com.example.heartrate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class firebasemessage extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("fire message came here");
        if (remoteMessage.getData().size() > 0){

            Log.d("fire message",remoteMessage.getMessageId());
            Intent i = new Intent(this,doctor.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_ONE_SHOT);
            Uri notificationsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Notification notifi = new NotificationCompat.Builder(this,"channel 1").setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("Heart rate alert")
                    .setAutoCancel(true)
                    .setSound(notificationsound)
                    .setColor(getResources().getColor(R.color.blue))
                    .setContentIntent(pendingIntent).build();
            NotificationManagerCompat notify = NotificationManagerCompat.from(this);
            notify.notify(0,notifi);
        }
    }
}
