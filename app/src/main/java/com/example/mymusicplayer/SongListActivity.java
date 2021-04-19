package com.example.mymusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.theme.ThemeDataBaseHelper;
import com.example.mymusicplayer.theme.ThemeNote;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

public class SongListActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99 ;
    String[]items;
    ListView listView;
    ArrayList<File> arrayList1;

    Toolbar toolbar;
    ThemeDataBaseHelper themeDataBaseHelper;
    LinearLayout linearLayout;

    int colorStatus;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private List<ThemeNote> themeStatusData;

    TextView header;
    View headerview;

    CardView color1,color2,color3,color4,color5,color6,color7,color8,
             color9,color10,color11,color12,color13,color14,color15,color16;
    Button saveButton ,cancelButton;
    View selectColor;

    List<Track> tracks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        toolbar=findViewById (R.id.toolbarId);
        drawerLayout=findViewById (R.id.drawerLayoutId);
        if (toolbar!=null){
            setSupportActionBar (toolbar);
        }



        listView=findViewById(R.id.listView);
        arrayList1=new ArrayList<>();

        runTimePermission();
        navigationView=findViewById (R.id.myNavigationViewId);


        headerview=navigationView.getHeaderView(0);
        header=headerview.findViewById(R.id.navigationSubHeaderId);
        // call ThemeDataBaseHelper class
        themeDataBaseHelper=new ThemeDataBaseHelper(SongListActivity.this);
        themeDataBaseHelper.getWritableDatabase();
        themeStatusData  = new ArrayList<>();
        if (themeDataBaseHelper.getAllNotes().size()<1){
            for (int i = 0; i <=1; i++) {
                int id=themeDataBaseHelper.insertData(new ThemeNote(1));
                if (id!=0){
                    Toast.makeText(this, "success "+String.valueOf(i), Toast.LENGTH_SHORT).show();
                }
            }
        }

        setColor();

        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(
                SongListActivity.this,drawerLayout,toolbar,R.string.open,R.string.closed){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Toast.makeText (SongListActivity.this, "Open", Toast.LENGTH_SHORT).show ();
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Toast.makeText (SongListActivity.this, "Closed", Toast.LENGTH_SHORT).show ();
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);





        navigationView.setNavigationItemSelectedListener (new NavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId ()){
                    case R.id.aboutItemIdId:

                        break;

                    case R.id.themeItemIdId:
                        CustomAdapterForColorChange();
                        break;
                    case R.id.permissionItemId:
                            runTimePermission();
                            drawerLayout.closeDrawers();
                        break;
                    case R.id.shareItemIdId:
//                        Intent sendIntent = new Intent();
//                        sendIntent.setAction(Intent.ACTION_SEND);
//                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Link");
//                        sendIntent.setType("text/plain");
//                        Intent shareIntent = Intent.createChooser(sendIntent, null);
//                        startActivity(shareIntent);
                        break;
                    case R.id.contactItemId:


                        break;
                }
                return false;
            }
        });


    }

    public void runTimePermission(){
            Dexter.withContext(SongListActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            if(multiplePermissionsReport.areAllPermissionsGranted()){
                                    displaySong();
                            }else {
                              //  runTimePermission();
                                Toast.makeText(SongListActivity.this, "Please check permission ", Toast.LENGTH_LONG).show();
                            }


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
                //////////////////////////////////////////
                tracks=new ArrayList<>();

                items[i]=mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");
            }
        }

        if (items.length>0){
            SongListActivity.CustomAdapter customAdapter=new SongListActivity.CustomAdapter();
            listView.setAdapter(customAdapter);
        }
        else {
            Toast.makeText(this, "item is not found", Toast.LENGTH_SHORT).show();
        }


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

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView=getLayoutInflater().inflate(R.layout.list_item,null);
            TextView songNameTextView=myView.findViewById(R.id.songNameTextViewId);
            TextView durationTextView=myView.findViewById(R.id.songDurationTextViewId);
            ImageView img=myView.findViewById(R.id.img);
            songNameTextView.setSelected(true);
            songNameTextView.setText(items[position]);




            Uri uri=Uri.parse(mySongs.get(position).toString());

            mmr.setDataSource(SongListActivity.this, uri);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String title =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
             mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String image =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE);

           Bitmap bm = StringToBitMap(image);
            if (bm!=null){
                img.setImageBitmap(bm);
            //MyPhoto is image control
            }



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
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public void CustomAdapterForColorChange(){
        AlertDialog.Builder builder     =new AlertDialog.Builder(SongListActivity.this);
        LayoutInflater layoutInflater   =LayoutInflater.from(SongListActivity.this);
        final View view                       =layoutInflater.inflate(R.layout.color_box,null);
        builder.setView(view);
        final AlertDialog alertDialog   = builder.create();
        color1=view.findViewById(R.id.color1);
        color2=view.findViewById(R.id.color2);
        color3=view.findViewById(R.id.color3);
        color4=view.findViewById(R.id.color4);
        color5=view.findViewById(R.id.color5);
        color6=view.findViewById(R.id.color6);
        color7=view.findViewById(R.id.color7);
        color8=view.findViewById(R.id.color8);
        color9=view.findViewById(R.id.color9);
        color10=view.findViewById(R.id.color10);
        color11=view.findViewById(R.id.color11);
        color12=view.findViewById(R.id.color12);
        color13=view.findViewById(R.id.color13);
        color14=view.findViewById(R.id.color14);
        color15=view.findViewById(R.id.color15);
        color16=view.findViewById(R.id.color16);
        selectColor=view.findViewById(R.id.selectColor);
        cancelButton=view.findViewById(R.id.cancelButton);
        saveButton=view.findViewById(R.id.okButton);

        themeStatusData  = new ArrayList<>();
       themeStatusData = themeDataBaseHelper.getAllNotes();
       colorStatus =themeStatusData.get(0).getThemeStatus();
       if (colorStatus==1){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color1));
       }
       else  if (colorStatus==2){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color2));
       }
       else  if (colorStatus==3){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color3));
       }
       else  if (colorStatus==4){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color4));
       }
       else  if (colorStatus==5){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color5));
       }
       else  if (colorStatus==6){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color6));

       }
       else  if (colorStatus==7){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color7));

       }
       else  if (colorStatus==8){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color8));

       }
       else  if (colorStatus==9){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color9));
       }
       else  if (colorStatus==10){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color10));
       }
       else  if (colorStatus==11){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color11));
       }
       else  if (colorStatus==12){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color12));
       }
       else  if (colorStatus==13){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color13));
       }
       else  if (colorStatus==14){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color14));
       } else  if (colorStatus==15){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color15));
       }
       else  if (colorStatus==16){
           selectColor.setBackgroundColor(getResources().getColor(R.color.color16));
       }




        color1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=1;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color1));
            }
        });
        color2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=2;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color2));
            }
        });
        color3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=3;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color3));
            }
        });
        color4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=4;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color4));
            }
        });
        color5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=5;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color5));
            }
        });
        color6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=6;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color6));
            }
        });
        color7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=7;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color7));
            }
        });
        color8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=8;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color8));
            }
        });
        color9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=9;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color9));
            }
        }); color10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=10;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color10));
            }
        });
        color11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=11;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color11));
            }
        });
        color12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=12;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color12));
            }
        });
        color13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=13;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color13));
            }
        }); color14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=14;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color14));
            }
        }); color15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=15;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color15));
            }
        }); color16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorStatus=16;
                selectColor.setBackgroundColor(getResources().getColor(R.color.color16));
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id=themeDataBaseHelper.updateThemeData(new ThemeNote(1,colorStatus));
                if (id==1){
                    setColor();
                    Toast.makeText(getApplicationContext(), "Change  theme", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "change fail", Toast.LENGTH_SHORT).show();
                }

                alertDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

 void setColor(){
    themeStatusData  = new ArrayList<>();
    themeStatusData = themeDataBaseHelper.getAllNotes();
    colorStatus =themeStatusData.get(0).getThemeStatus();
    if (colorStatus==1){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color1));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color1));
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
        };
        int[] colors = new int[] {
                Color.BLACK,
        };
        ColorStateList myList = new ColorStateList(states, colors);
        navigationView.setItemTextColor(myList);
        navigationView.setItemIconTintList(myList);
    }
    else  if (colorStatus==2){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color2));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color2));
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
        };
        int[] colors = new int[] {
                Color.BLACK,
        };
        ColorStateList myList = new ColorStateList(states, colors);
        navigationView.setItemTextColor(myList);
        navigationView.setItemIconTintList(myList);
    }
    else  if (colorStatus==3){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color3));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color3));
        texColorChange();

    }
    else  if (colorStatus==4){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color4));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color4));

        texColorChange();
    }
    else  if (colorStatus==5){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color5));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color5));
        texColorChange();
    }
    else  if (colorStatus==6){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color6));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color6));
        texColorChange();
    }
    else  if (colorStatus==7){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color7));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color7));
        texColorChange();
    }
    else  if (colorStatus==8){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color8));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color8));
        texColorChange();
    }
    else  if (colorStatus==9){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color9));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color9));
        texColorChange();
    }
    else  if (colorStatus==10){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color10));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color10));
        texColorChange();
    }
    else  if (colorStatus==11){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color11));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color11));
        texColorChange();
    }
    else  if (colorStatus==12){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color12));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color12));
        texColorChange();
    }
    else  if (colorStatus==13){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color13));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color13));
        texColorChange();
    }
    else  if (colorStatus==14){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color14));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color14));
        texColorChange();
    }
    else  if (colorStatus==15){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color15));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color15));
        texColorChange();
    }
    else  if (colorStatus==16){
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color16));
        navigationView.setBackgroundColor(getResources().getColor(R.color.color16));
        texColorChange();
    }


}
  void texColorChange(){
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_enabled}, // enabled
            };
            int[] colors = new int[] {
                    Color.WHITE,
            };
            ColorStateList myList = new ColorStateList(states, colors);
            navigationView.setItemTextColor(myList);
            navigationView.setItemIconTintList(myList);
        }
        ////


}