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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class user extends AppCompatActivity{





    public FirebaseAuth firebaseAuth;

    private MusicService musicSrv;
    private boolean musicBound;

    private final  int  MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES = 0;
    private boolean viewingFilesPermissions = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);


        firebaseAuth = FirebaseAuth.getInstance();
        //Creating SendMail object


        checkIfUserIsLoggedOut();


        ImageButton search = (ImageButton) findViewById(R.id.searchView);
        ImageButton playList = (ImageButton) findViewById(R.id.playlistView);
        ImageButton user = (ImageButton) findViewById(R.id.userView);
        ImageButton song = (ImageButton) findViewById(R.id.songView);
        TextView currFragView = (TextView) findViewById(R.id.currFrag);
        ImageView userImage = (ImageView) findViewById(R.id.imageView);

        String currFrag = "User";
        currFragView.setText(currFrag);

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

        // Read your drawable from somewhere
        dr = getResources().getDrawable(R.drawable.untitled);
        bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
// Set your new, scaled drawable "d"

        userImage.setImageDrawable(d);


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(user.this, search.class);
                startActivity(i);

            }

        });

        playList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (musicSrv != null) {

                    if (musicSrv.currPlayList != null) {

                        Intent i = new Intent(user.this, playlist.class);
                        startActivity(i);

                    } else {

                        Toast.makeText(user.this, "Please either search for a playlist or choose from your local in the users section!", Toast.LENGTH_LONG).show();

                    }
                }

            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(user.this, user.class);
                startActivity(i);

            }
        });

        song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (musicSrv != null) {
                    if (musicSrv.currSong != null) {
                        Intent i = new Intent(user.this, song.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(user.this, "Please select a song from playlist, or search for one", Toast.LENGTH_LONG).show();
                    }
                }


            }
        });


        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(user.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            bindMethod();
        }





    }


    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserIsLoggedOut();

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(user.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            bindMethod();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkIfUserIsLoggedOut();

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(user.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            bindMethod();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfUserIsLoggedOut();

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(user.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            bindMethod();
        }

    }


    public void move2audioList(View v){
        Intent i = new Intent(this, song_list.class);
        startActivity(i);
    }

    public void move2playlist_List(View v){
        Intent i = new Intent(this, playlist_list.class);
        startActivity(i);
    }



    public void move2onlinePlaylists(View v){
        Intent i = new Intent(this, OnlinePlaylist.class);
        startActivity(i);
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
Bind and connect to music service
 */

    public void bindMethod(){
        Intent i = new Intent(this,MusicService.class);
        startService(i);
        bindService(i, musicConnection, Context.BIND_AUTO_CREATE);
    }

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



    public void resetPassword(View v){

        FirebaseAuth.getInstance().sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(user.this,"An a reset link has been sent to your email!",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(user.this,"Couldn't send Reset Email.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void logOut(View v){
        if(musicSrv != null){
            musicSrv.clearPlayer();
            musicSrv.mRunning = false;
            musicSrv.currSong = null;
            musicSrv.currPlayList= null;
            musicSrv.stopService(new Intent(this, MusicService.class));
        }

        firebaseAuth.signOut();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);

    }



    /*
  Ask for permissions...
   */
    public void ask4permission(Context context){

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(user.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // updateSongList();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(user.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);
                }


            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(user.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);

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
                                ask4permission(user.this);
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

