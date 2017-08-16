package mjhub_media.iota;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class song extends AppCompatActivity {





    public FirebaseAuth firebaseAuth;

    private String currFrag = "Audio";
    private TextView currFragView;

    private TextView currSong;
    private TextView currSongArtist;

    private ImageButton prev;
    private ImageButton pause;
    private ImageButton next;
    private Switch shuffle;
    private SeekBar seekBar;
    private Switch repeat;


    Drawable playButton,pauseButton;

    private MusicService musicSrv;
    private  boolean musicBound;

    private final  int  MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES = 0;
    private boolean viewingFilesPermissions = false;




    /*
    Setup the controls
     */
    private ImageButton search;
    private ImageButton playList;
    private ImageButton user;
    private ImageButton song;

    Thread changeDisplay;

    Thread t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);


        firebaseAuth = FirebaseAuth.getInstance();
        //Creating SendMail object


        checkIfUserIsLoggedOut();




        search = (ImageButton) findViewById(R.id.searchView);
        playList = (ImageButton) findViewById(R.id.playlistView);
        user = (ImageButton) findViewById(R.id.userView);
        song = (ImageButton) findViewById(R.id.songView);


        // Read your drawable from somewhere
        Drawable dr = getResources().getDrawable(R.drawable.search);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 90, 90, true));
// Set your new, scaled drawable "d"

        search.setImageDrawable(d);

        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.playlist);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 90, 90, true));
// Set your new, scaled drawable "d"

        playList.setImageDrawable(d);

        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.user);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 90, 90, true));
// Set your new, scaled drawable "d"

        user.setImageDrawable(d);

        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.song);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 90, 90, true));
// Set your new, scaled drawable "d"


        song.setImageDrawable(d);




        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(song.this, search.class);
                startActivity(i);

            }

        });

        playList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(musicSrv != null){

                    if(musicSrv.currPlayList != null){

                        Intent i = new Intent(song.this, playlist.class);
                        startActivity(i);

                    }else{

                        Toast.makeText(song.this,"Please either search for a playlist or choose from your local in the users section!",Toast.LENGTH_LONG).show();

                    }
                }


            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(song.this, user.class);
                startActivity(i);

            }
        });

        song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(song.this, song.class);
                startActivity(i);

            }
        });



        currSong = (TextView) findViewById(R.id.textView13);
        currSongArtist = (TextView) findViewById(R.id.currFrag);
        prev = (ImageButton) findViewById(R.id.imageButton);
        pause = (ImageButton) findViewById(R.id.imageButton2);
        next = (ImageButton) findViewById(R.id.imageButton3);
        shuffle = (Switch) findViewById(R.id.switch1);
        seekBar = (SeekBar) findViewById(R.id.seekBar);


        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.back);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 80, 80, true));
// Set your new, scaled drawable "d"


        prev.setImageDrawable(d);


        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.next);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 80, 80, true));
// Set your new, scaled drawable "d"


        next.setImageDrawable(d);

        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.playicon);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        playButton = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 90, 90, true));
// Set your new, scaled drawable "d"

        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.pauseicon);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        pauseButton = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 90, 90, true));
// Set your new, scaled drawable "d"


        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(song.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            if(musicSrv == null){
                bindMethod();
            }


            t = new Thread(new Runnable() {


                @Override
                public void run() {

                    while(true){


                        try {
                            Thread.sleep(100);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }




                }
            });



            t.start();
        }







    }


    @Override
    protected void onStart() {
        super.onStart();


        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(song.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //you have permissions...

            bindMethod();


            //listen to the control commands
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(musicSrv != null){
                        musicSrv.curr_song_index--;
                        if(musicSrv.curr_song_index < 0){
                            musicSrv.curr_song_index = musicSrv.currPlayList.length - 1;
                        }
                        musicSrv.playSong(musicSrv.curr_song_index);
                    }
                }
            });

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(musicSrv != null){
                        musicSrv.curr_song_index++;
                        if(musicSrv.curr_song_index >= musicSrv.currPlayList.length){
                            musicSrv.curr_song_index = 0;
                        }
                        musicSrv.playSong(musicSrv.curr_song_index);
                    }
                }
            });


            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(musicSrv != null){
                        musicSrv.pause();
                        updateDisplay();
                    }
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    musicSrv.setProgress(seekBar.getProgress());
                }
            });


            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    musicSrv.Shuffle = !musicSrv.Shuffle;
                    if(musicSrv.Shuffle){
                        Toast.makeText(song.this,"Shuffle On",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(song.this,"Shuffle Off",Toast.LENGTH_LONG).show();
                    }
                }
            });





            updateDisplay();






        }


        checkIfUserIsLoggedOut();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(song.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            bindMethod();

            updateDisplay();
        }

        checkIfUserIsLoggedOut();
    }

    @Override
    protected void onResume() {
        super.onResume();


        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(song.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            bindMethod();

            updateDisplay();
        }



        checkIfUserIsLoggedOut();
    }



    public void updateDisplay(){


    if(musicSrv != null && musicSrv.mRunning){


           if(musicSrv.currSong != null){

               currSong.setText(musicSrv.currSong.getTitle());
               currSongArtist.setText(musicSrv.currSong.getArtist());


           }




            if(musicSrv.Shuffle){
                shuffle.setChecked(true);
            }else{
                shuffle.setChecked(false);
            }



        if(musicSrv.isPlaying()){
            //change the pause image button
            pause.setImageDrawable(pauseButton);
            //update the seek bar
            seekBar.setMax(musicSrv.getDuration());
            seekBar.setProgress(musicSrv.getCurrDuration());
        }else{
            pause.setImageDrawable(playButton);
        }

    }








    }


    public void bindMethod(){
        Intent i = new Intent(this,MusicService.class);
        startService(i);
        bindService(i, musicConnection, Context.BIND_AUTO_CREATE);
    }





    public void logOut(){
        firebaseAuth.signOut();
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }


    public void checkIfUserIsLoggedOut(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            logOut();
        }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
    }



    /*
   Ask for permissions...
    */
    public void ask4permission(Context context){

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(song.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // updateSongList();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(song.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);
                }


            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(song.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);

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

                    bindMethod();

                } else {

                    boolean showRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE);
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
                                ask4permission(song.this);
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





}

