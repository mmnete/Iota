package mjhub_media.iota;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class song_list extends AppCompatActivity {



    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    //song list variables
    private ArrayList<SongItem> songList;
    private ListView songView;

    private FirebaseAuth firebaseAuth;


    private final  int  MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_song_list);

        songView = (ListView)findViewById(R.id.songList);
        songList = new ArrayList<SongItem>();





        firebaseAuth = FirebaseAuth.getInstance();

        checkIfUserIsLoggedOut();

        ask4permission(this.getApplicationContext());

        if(!musicBound){
            bindMethod();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(song_list.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            updateSongList();
        }
       if(!musicBound){
           bindMethod();
       }
        checkIfUserIsLoggedOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(song_list.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            updateSongList();
        }
        checkIfUserIsLoggedOut();
        if(!musicBound){
            bindMethod();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(song_list.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            updateSongList();
        }
        checkIfUserIsLoggedOut();
        if(!musicBound){
            bindMethod();
        }
    }

    public void bindMethod(){
        Intent i = new Intent(this,MusicService.class);
        startService(i);
        bindService(i, musicConnection, Context.BIND_AUTO_CREATE);
    }

    public void updateSongList(){

        getSongList();
        Collections.sort(songList, new Comparator<SongItem>(){
            public int compare(SongItem a, SongItem b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
             if(musicSrv != null){


                 musicSrv.currPlayList = new playlist_item("default",songList);
                 musicSrv.playSong(position);

             }else{

             }

            }
        });

    }

    public void getSongList(){
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String path = musicCursor.getString(pathColumn);
                if(songList.size() > 0){
                    boolean put = true;
                    for(SongItem i: songList){
                        if(i.getTitle().equals(thisTitle)){
                            put = false;
                        }
                    }
                    if(put){
                        songList.add(new SongItem(thisId, thisTitle, thisArtist, path, false));
                    }
                }else{
                    songList.add(new SongItem(thisId, thisTitle, thisArtist, path, false));
                }

            }
            while (musicCursor.moveToNext());
        }

        if(musicCursor != null){
            musicCursor.close();
        }

    }



    public void back2user(View v){
        Intent i = new Intent(this, user.class);
        startActivity(i);
    }


    public void checkIfUserIsLoggedOut(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            logOut();
        }
    }


    public void ask4permission(Context context){

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

             // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(song_list.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                 // Show an explanation to the user *asynchronously* -- don't block
                 // this thread waiting for the user's response! After the user
                 // sees the explanation, try again to request the permission.

               // updateSongList();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(song_list.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);
                }


            } else {

                // No explanation needed, we can request the permission.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(song_list.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);
                }

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                 // app-defined int constant. The callback method gets the
                 // result of the request.
                 }
        }





    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
             case MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES: {
                 // If request is cancelled, the result arrays are empty.
                 if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                     boolean viewingFilesPermissions = true;

                     updateSongList();

                     } else {

                     boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);
                     if (!showRationale) {
                         Intent intent = new Intent();
                         intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                         Uri uri = Uri.fromParts("package", getPackageName(), null);
                         intent.setData(uri);
                         startActivity(intent);
                     }else{

                         //show alert dialog
                         AlertDialog.Builder builder = new AlertDialog.Builder(this);
                         builder.setMessage("This app needs to use your storage. We neeed to access the audio files!");
                         builder.setTitle("Location Services");
                         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 ask4permission(song_list.this);
                             }
                         });

                         builder.show();


                    }


                    }

                 return;

                }

            // other 'case' lines to check for other
             // permissions this app might request
            }
    }

    public void logOut(){
        firebaseAuth.signOut();
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }



    /*
    Connect to music service
     */
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
}
