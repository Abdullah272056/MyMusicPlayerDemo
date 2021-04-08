package com.example.mymusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;

import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.marcinmoskala.arcseekbar.ArcSeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String[]items;
    ListView listView;
    public static final int PERMISSION_READ = 0;
    ArrayList<File> arrayList1;


    CardView btnPlay,btnNext,btnPrev;
    ImageButton btnFf,btnFr;
    TextView txtSName,txtSStart,txtSStop;
    ArcSeekBar seekMusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sName;
    public static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        seekMusic=findViewById(R.id.seekBarId);
      //  visualizer=findViewById(R.id.blast);


        arrayList1=new ArrayList<>();
       // runTimePermission();

        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // data receive
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        mySongs=(ArrayList) bundle.getParcelableArrayList("songs");

        String songName=intent.getStringExtra("songName");
        position=bundle.getInt("pos",0);
        txtSName.setSelected(true);

        Uri uri=Uri.parse(mySongs.get(position).toString());
        sName=mySongs.get(position).getName();
        txtSName.setText(sName);

        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();


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




        btnPlay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.play_ic);
                    mediaPlayer.pause();
                }else {
                    btnPlay.setBackgroundResource(R.drawable.pause_ic);
                    mediaPlayer.start();
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();

                position=(position+1)%mySongs.size();

                Uri uri=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                sName=mySongs.get(position).getName();
                txtSName.setText(sName);
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.pause_ic);
                startAnimation(imageView);

//                int audioSessionId=mediaPlayer.getAudioSessionId();
//                if (audioSessionId!=-1){
//                    visualizer.setAudioSessionId(audioSessionId);
//                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri uri=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                sName=mySongs.get(position).getName();
                txtSName.setText(sName);
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.pause_ic);
                startAnimation(imageView);


//                int audioSessionId=mediaPlayer.getAudioSessionId();
//                if (audioSessionId!=-1){
//                    visualizer.setAudioSessionId(audioSessionId);
//                }


            }
        });


    }







    // animation create
    public void startAnimation(View view){
        ObjectAnimator animator=ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

}