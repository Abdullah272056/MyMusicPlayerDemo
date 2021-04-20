package com.example.mymusicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.Manifest;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.Service.OnClearFromRecentService;
import com.example.mymusicplayer.theme.ThemeDataBaseHelper;
import com.example.mymusicplayer.theme.ThemeNote;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;

import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.marcinmoskala.arcseekbar.ProgressListener;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Playable{
LinearLayout linearLayout;
    ListView listView;
    ArrayList<File> arrayList1;



    CardView btnPlay,btnNext,btnPrev;
    ImageButton btnFf,btnFr;
    TextView txtSName,txtSStart,txtSStop;
    ArcSeekBar seekMusic;

    BarVisualizer visualizer;
    ImageView imageView,playImageView;

    String sName;
    public static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekBar;
   Toolbar toolbar;


    NotificationManager notificationManager;


    boolean isPlaying=false;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer!=null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // toolbar
        toolbar=findViewById (R.id.toolbar);
        if (toolbar!=null){
            setSupportActionBar(toolbar);
        }
        //getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        // view finding
        linearLayout=findViewById(R.id.linearLayoutId);
        listView=findViewById(R.id.listView);
        btnPlay=findViewById(R.id.playButtonId);
        btnNext=findViewById(R.id.nextButtonId);
        btnPrev=findViewById(R.id.prevButtonId);
        btnFf=findViewById(R.id.FFButtonId);
        btnFr=findViewById(R.id.FRButtonId);
        txtSName=findViewById(R.id.songNameTextViewId);
        txtSStart=findViewById(R.id.sonStartTimeTextViewId);
        txtSStop=findViewById(R.id.songStopTextViewId);
        imageView=findViewById(R.id.imageViewId);
        playImageView=findViewById(R.id.playImageViewId);
        seekMusic=findViewById(R.id.seekBarId);
        visualizer=findViewById(R.id.blast);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createChannel();
            registerReceiver(broadcastReceiver,new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }
        // call setColor method for color set
        setColor();

        arrayList1=new ArrayList<>();

       // runTimePermission();

        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // data receive
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();

        if (bundle!=null){

            mySongs=(ArrayList) bundle.getParcelableArrayList("songs");
           // String songName=intent.getStringExtra("songName");
            position=bundle.getInt("pos",0);
        }

        txtSName.setSelected(true);
        Uri uri=Uri.parse(mySongs.get(position).toString());

        // create media player
        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        // call onTrackPlay method
        onTrackPlay();

       UpdateSeekBar();
       runNextSon();


//        //seekMusic set Listener
//        seekMusic.setOnProgressChangedListener(new ProgressListener() {
//            @Override
//            public void invoke(int i) {
//                mediaPlayer.seekTo(i);
//            }
//        });


        // time format change and set textView
        String endTime=createTime(mediaPlayer.getDuration());
        txtSStop.setText(endTime);

        // after one second change current time and
        // when user change seekBar progress then get current time and set textView

        final Handler handler=new Handler();
        final  int delay=1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime=createTime(mediaPlayer.getCurrentPosition());
                txtSStart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);


        btnPlay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    onTrackPause();
                }else {
                   onTrackPlay();
                }
            }
        });

        btnFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer!=null){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });


        btnFf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer!=null){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });




        int audioSessionId=mediaPlayer.getAudioSessionId();
        if (audioSessionId!=-1){
            visualizer.setAudioSessionId(audioSessionId);
        }


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onTrackNext();
            }
        });



        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onTrackPrevious();

            }
        });


    }

    private void createChannel(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "KOD Dev", NotificationManager.IMPORTANCE_LOW);

            notificationManager=getSystemService(NotificationManager.class);
            if (notificationManager!=null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
   // er vitor ja ja korbo seta hobe
    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getExtras().getString("actionName");
            switch (action){
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (mediaPlayer.isPlaying()){
                        onTrackPause();
                    }else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };





    // animation create
    public void startAnimation(View view){
        ObjectAnimator animator=ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    //time conversion int value to time format
    public String createTime(int duration) {
        String time="";
        int dur = (int) duration;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            time = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            time = String.format("%02d:%02d", mns, scs);
        }
        return time;
    }


    void UpdateSeekBar(){
        updateSeekBar=new Thread(){
            @Override
            public void run() {
                int totalDuration=mediaPlayer.getDuration();
                int currentPosition=0;
                while (currentPosition<totalDuration){
                    try {
                        sleep(500);
                        currentPosition=mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(currentPosition);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                super.run();
            }
        };

        //seekMusic.setMax(mediaPlayer.getDuration());
        seekMusic.setMaxProgress(mediaPlayer.getDuration());
        updateSeekBar.start();
    }
    public Bitmap getAlbumart(Long album_id)
    {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = this.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        return bm;
    }

    // current song after end next son start
    void runNextSon(){
        final Handler handler2=new Handler();
        final  int delay1=2000;
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                // after current song ended then next song start
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        btnNext.performClick();

                    }
                });
                handler2.postDelayed(this,delay1);
            }
        },delay1);
    }

    void setColor(){
      int colorStatus;
      ThemeDataBaseHelper  themeDataBaseHelper=new ThemeDataBaseHelper(MainActivity.this);
        themeDataBaseHelper.getWritableDatabase();
        List<ThemeNote> themeStatusData  = new ArrayList<>();
        themeStatusData = themeDataBaseHelper.getAllNotes();
        colorStatus =themeStatusData.get(0).getThemeStatus();
        if (colorStatus==1){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color1));
           txtSName.setTextColor(getResources().getColor(R.color.color9));
           txtSStart.setTextColor(getResources().getColor(R.color.color9));
           txtSStop.setTextColor(getResources().getColor(R.color.color9));
          visualizer.setColor(getResources().getColor(R.color.color7));
        }
        else  if (colorStatus==2){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color2));

        }
        else  if (colorStatus==3){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color3));

        }
        else  if (colorStatus==4){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color4));
        }
        else  if (colorStatus==5){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color5));
        }
        else  if (colorStatus==6) {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color6));
        }
        else  if (colorStatus==7){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color7));
            visualizer.setColor(getResources().getColor(R.color.color11));
        }
        else  if (colorStatus==8){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color8));
            visualizer.setColor(getResources().getColor(R.color.color11));
        }
        else  if (colorStatus==9){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color9));
        }
        else  if (colorStatus==10){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color10));
        }
        else  if (colorStatus==11){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color11));
        }
        else  if (colorStatus==12){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color12));
        }
        else  if (colorStatus==13){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color13));
        }
        else  if (colorStatus==14){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color14));
        }
        else  if (colorStatus==15){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color15));
        }
        else  if (colorStatus==16){
            linearLayout.setBackgroundColor(getResources().getColor(R.color.color16));
        }


    }



    // override Playable class method
    @Override
    public void onTrackPrevious() {
        mediaPlayer.stop();
        mediaPlayer.release();
        position=((position-1)<0)?(mySongs.size()-1):(position-1);
        Uri uri=Uri.parse(mySongs.get(position).toString());
        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        sName=mySongs.get(position).getName();
        txtSName.setText(sName);


        txtSStop.setText(createTime(mediaPlayer.getDuration()));
        UpdateSeekBar();

        mediaPlayer.start();
        playImageView.setImageResource(R.drawable.pause_ic);
        startAnimation(imageView);


        int audioSessionId=mediaPlayer.getAudioSessionId();
        if (audioSessionId!=-1){
            visualizer.setAudioSessionId(audioSessionId);
        }

        sName=mySongs.get(position).getName();
        txtSName.setText(sName);
        CreateNotification.createNotification(MainActivity.this,new Track(sName,sName,R.drawable.song_img),
                R.drawable.pause_ic,position,mySongs.size()-1,mySongs);
    }

    @Override
    public void onTrackPlay() {
//        CreateNotification.createNotification(MainActivity.this,tracks.get(position),
//                R.drawable.ic_baseline_pause_24,position,mySongs.size()-1);
        sName=mySongs.get(position).getName();
        txtSName.setText(sName);
        CreateNotification.createNotification(MainActivity.this,new Track(sName,sName,R.drawable.song_img),
                R.drawable.pause_ic,position,mySongs.size()-1,mySongs);

        playImageView.setImageResource(R.drawable.pause_ic);
        mediaPlayer.start();
    }

    @Override
    public void onTrackPause() {
        sName=mySongs.get(position).getName();
        txtSName.setText(sName);
        CreateNotification.createNotification(MainActivity.this,new Track(sName,sName,R.drawable.song_img),
                R.drawable.play_ic,position,mySongs.size()-1,mySongs);

        playImageView.setImageResource(R.drawable.play_ic);
        mediaPlayer.pause();
    }

    @Override
    public void onTrackNext() {
        mediaPlayer.stop();
        mediaPlayer.release();
        position=(position+1)%mySongs.size();
        Uri uri=Uri.parse(mySongs.get(position).toString());
        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        sName=mySongs.get(position).getName();
        txtSName.setText(sName);
        txtSStop.setText(createTime(mediaPlayer.getDuration()));
        UpdateSeekBar();

        mediaPlayer.start();
        playImageView.setImageResource(R.drawable.pause_ic);
        startAnimation(imageView);

        int audioSessionId=mediaPlayer.getAudioSessionId();
        if (audioSessionId!=-1){
            visualizer.setAudioSessionId(audioSessionId);
        }

        sName=mySongs.get(position).getName();
        txtSName.setText(sName);
        CreateNotification.createNotification(MainActivity.this,new Track(sName,sName,R.drawable.song_img),
                R.drawable.pause_ic,position,mySongs.size()-1,mySongs);
    }




}