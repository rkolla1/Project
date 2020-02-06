package com.example.heartrate;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_1 = "channel 1";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationchannel();

    }

    private void createNotificationchannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1,"Cancel", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("this is heartrate");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

}
