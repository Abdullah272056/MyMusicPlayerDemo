package com.example.mymusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mymusicplayer.Service.NotificationActionService;


public class CreateNotification {
    public static final String CHANNEL_ID="channel1";
    public static final String ACTION_PREVIOUS="actionprevious";
    public static final String ACTION_PLAY="actionplay";
    public static final String ACTION_NEXT="actionnext";
    public static Notification notification;
    public static void createNotification(Context context, Track track, int playButton, int pos, int size ){

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat= NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat=new MediaSessionCompat(context,"tag");
            Bitmap icon= BitmapFactory.decodeResource(context.getResources(),track.getImage());

            PendingIntent pendingIntentPrevious;
            int drwPrevious;
//            if (pos==0){
//                pendingIntentPrevious=null;
//                drwPrevious=0;
//            }
//            else {
//                Intent intentPrevious=new Intent(context, NotificationActionService.class).setAction(ACTION_PREVIOUS);
//                pendingIntentPrevious=PendingIntent.getBroadcast(context,0,intentPrevious,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//                // icon change
//                drwPrevious=R.drawable.ic_baseline_skip_previous_24;
//            }


            // Action previous
            Intent intentPrevious=new Intent(context, NotificationActionService.class).setAction(ACTION_PREVIOUS);
            pendingIntentPrevious=PendingIntent.getBroadcast(context,0,intentPrevious,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // icon change
            drwPrevious=R.drawable.ic_baseline_skip_previous_24;


    // Action Play
            Intent intentPlay=new Intent(context, NotificationActionService.class).setAction(ACTION_PLAY);
           PendingIntent pendingIntentPlay=PendingIntent.getBroadcast(context,0,
                   intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


            PendingIntent pendingIntentNext;
            int drwNext;
//            if (pos==size){
//                pendingIntentNext=null;
//                drwNext=0;
//            }
//            else {
//              Intent intentNext=new Intent(context, NotificationActionService.class).setAction(ACTION_NEXT);
////                pendingIntentNext=PendingIntent.getBroadcast(context,0,intentNext,
////                        PendingIntent.FLAG_UPDATE_CURRENT);
////                // icon change
////                drwNext=R.drawable.ic_baseline_skip_next_24;
//            }

            // Action Next
            Intent intentNext=new Intent(context, NotificationActionService.class).setAction(ACTION_NEXT);
                pendingIntentNext=PendingIntent.getBroadcast(context,0,intentNext,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                // icon change
                drwNext=R.drawable.ic_baseline_skip_next_24;

                // notification to activity
                Intent intent=new Intent(context,MainActivity.class);
                PendingIntent resultPendingIntent= PendingIntent.getActivity(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            notification=new NotificationCompat.Builder(context,CHANNEL_ID).
                    setSmallIcon(R.drawable.ic_baseline_music_note_24)
                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(drwPrevious,"previous",pendingIntentPrevious)
                    .addAction(playButton,"play",pendingIntentPlay)
                    .addAction(drwNext,"next",pendingIntentNext)
                    .setContentIntent(resultPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0,1,2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1,notification);
        }
    }
}
