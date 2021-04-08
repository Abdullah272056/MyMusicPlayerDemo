package com.example.mymusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongListActivity extends AppCompatActivity {
    String[]items;
    ListView listView;
    ArrayList<File> arrayList1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        listView=findViewById(R.id.listView);
        arrayList1=new ArrayList<>();
        runTimePermission();
    }

    public void runTimePermission(){
        Dexter.withContext(SongListActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySong();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    ArrayList<File> findSong(File file){
        ArrayList<File>  arrayList=new ArrayList<>();
        File[] files=file.listFiles();
        for (File singleFile:files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                arrayList.addAll(findSong(singleFile));
            }else {
                if (singleFile.getName().endsWith(".mp3")  || singleFile.getName().endsWith(".wav")){
                    arrayList.add(singleFile);
                }
            }
        }

        arrayList1.addAll(arrayList);
        return arrayList;
    }
    void displaySong(){
        final ArrayList<File> mySongs=findSong(Environment.getExternalStorageDirectory());
        items=new String[mySongs.size()];
        if (mySongs.size()>0){
            for (int i=0;i<mySongs.size();i++){
                items[i]=mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");
            }
        }

        SongListActivity.CustomAdapter customAdapter=new SongListActivity.CustomAdapter();
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String songName= (String) listView.getItemAtPosition(position);

                startActivity(new Intent(SongListActivity.this,MainActivity.class)
                        .putExtra("songs",mySongs)
                        .putExtra("songName",songName)
                        .putExtra("pos",position));

            }
        });
    }
    class CustomAdapter extends BaseAdapter {

        ArrayList<File> mySongs=findSong(Environment.getExternalStorageDirectory());
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView=getLayoutInflater().inflate(R.layout.list_item,null);
            TextView songNameTextView=myView.findViewById(R.id.songNameTextViewId);
            TextView durationTextView=myView.findViewById(R.id.songDurationTextViewId);
            songNameTextView.setSelected(true);
            songNameTextView.setText(items[position]);


            Uri uri=Uri.parse(mySongs.get(position).toString());

            mmr.setDataSource(SongListActivity.this, uri);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String title =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            //String image =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE);

            int millSecond = Integer.parseInt(duration);
            durationTextView.setSelected(true);
            durationTextView.setText(title +"\t"+createTime(millSecond));

            return myView ;
        }
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
}