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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class search extends AppCompatActivity{





    public FirebaseAuth firebaseAuth;

    private MusicService musicSrv;
    private boolean musicBound;

    private ListView results;
    private EditText search_query;
    public String res = "";
    public String res1 = "";
    public boolean searchAttempt = false;
    public boolean searchAttempt1 = false;
    public List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    public List<NameValuePair> nameValuePairs1 = new ArrayList<NameValuePair>();


    private final  int  MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES = 0;
    private boolean viewingFilesPermissions = false;

    Thread r;
    Thread tur;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        firebaseAuth = FirebaseAuth.getInstance();
        //Creating SendMail object

        checkIfUserIsLoggedOut();


        ImageButton search = (ImageButton) findViewById(R.id.searchView);
        ImageButton playList = (ImageButton) findViewById(R.id.playlistView);
        ImageButton user = (ImageButton) findViewById(R.id.userView);
        ImageButton song = (ImageButton) findViewById(R.id.songView);
        TextView currFragView = (TextView) findViewById(R.id.currFrag);

        String currFrag = "Search playlists on MJHUB-CLOUD";
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



        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(search.this, search.class);
                startActivity(i);

            }

        });

        playList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(musicSrv != null){

                    if(musicSrv.currPlayList != null){

                        Intent i = new Intent(search.this, playlist.class);
                        startActivity(i);

                    }else{

                        Toast.makeText(search.this,"Please either search for a playlist or choose from your local in the users section!",Toast.LENGTH_LONG).show();

                    }
                }


            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(search.this, user.class);
                startActivity(i);

            }
        });

        song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(musicSrv != null){
                    if(musicSrv.currSong != null){
                        Intent i = new Intent(search.this, song.class);
                        startActivity(i);
                    }else{
                        Toast.makeText(search.this,"Please select a song from playlist, or search for one",Toast.LENGTH_LONG).show();
                    }
                }


            }
        });



        search_query = (EditText) findViewById(R.id.search_query);
        results = (ListView) findViewById(R.id.resultListView);


        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(search.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
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
            permissionCheck = ContextCompat.checkSelfPermission(search.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
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
            permissionCheck = ContextCompat.checkSelfPermission(search.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
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
            permissionCheck = ContextCompat.checkSelfPermission(search.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            bindMethod();
        }

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


    public void search(View v){

        Toast.makeText(search.this,"Searching...",Toast.LENGTH_LONG).show();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {



            if(!search_query.getText().toString().equals("")){




                nameValuePairs.clear();
                nameValuePairs.add(new BasicNameValuePair("search", search_query.getText().toString()));



                tur = new Thread(new Runnable() {



                    @Override
                    public void run() {



                        while(true){

                            try {
                                Thread.sleep(1000);

                                try{

                                    HttpClient httpClient = new DefaultHttpClient();

                                    HttpPost httpPost = new HttpPost("http://mohamedmnete.com/search.php");

                                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                                    HttpResponse response = httpClient.execute(httpPost);

                                    HttpEntity entity = response.getEntity();

                                    String htmlResponse = EntityUtils.toString(entity);


                                    res = htmlResponse;

                                    searchAttempt = true;


                                } catch (MalformedURLException e) {
                                    e.printStackTrace();

                                    res = "Search Error";

                                } catch (IOException e) {
                                    e.printStackTrace();

                                    res = "Search Error";
                                }



                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if(searchAttempt){

                                            search_resultAdapter search_resultAdapter = new search_resultAdapter(search.this,prepareSearchResults(res));
                                            results.setAdapter(search_resultAdapter);
                                            results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                                    Toast.makeText(search.this,prepareSearchResults(res).get(position).display_text,Toast.LENGTH_LONG).show();



                                                    nameValuePairs1.clear();
                                                    nameValuePairs1.add(new BasicNameValuePair("playlist_name",prepareSearchResults(res).get(position).display_text));
                                                    nameValuePairs1.add(new BasicNameValuePair("user",prepareSearchResults(res).get(position).info));


                                                     r = new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            //get the list of songs
                                                            try{

                                                                HttpClient httpClient1 = new DefaultHttpClient();

                                                                HttpPost httpPost1 = new HttpPost("http://mohamedmnete.com/load_playlist.php");

                                                                httpPost1.setEntity(new UrlEncodedFormEntity(nameValuePairs1));

                                                                HttpResponse response = httpClient1.execute(httpPost1);

                                                                HttpEntity entity1 = response.getEntity();

                                                                String htmlResponse1 = EntityUtils.toString(entity1);


                                                                res1 = htmlResponse1;

                                                                searchAttempt1 = true;


                                                            } catch (MalformedURLException e) {
                                                                e.printStackTrace();

                                                                res1 = "Search Error";

                                                                searchAttempt1 = false;
                                                            } catch (IOException e) {
                                                                e.printStackTrace();

                                                                res1 = "Search Error";
                                                                searchAttempt1 = false;
                                                            }

                                                            if(searchAttempt1){
                                                                ArrayList<SongItem> newSongList = prepareOnlinePlaylist(res1);
                                                                playlist_item newPlayList = new playlist_item("Online",newSongList);
                                                                musicSrv.currPlayList = newPlayList;
                                                                musicSrv.playSong(0);
                                                                Intent i = new Intent(search.this,song.class);
                                                                startActivity(i);
                                                                searchAttempt1 = false;
                                                            }


                                                        }
                                                    });
                                                    r.start();


                                                }
                                            });
                                            searchAttempt = false;

                                        }
                                    }
                                });



                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }



                        }




                    }



                });

                tur.start();




            }else{
                Toast.makeText(this,"Please finish filling the form!",Toast.LENGTH_LONG).show();
            }



        } else{

            Toast.makeText(search.this,"No Internet Connection",Toast.LENGTH_LONG).show();

        }



    }


    public ArrayList<SongItem> prepareOnlinePlaylist(String onlineResult){



        ArrayList<SongItem> temp = new ArrayList<SongItem>();


        if(onlineResult.length() > 1){
            String[] resultItem = onlineResult.split("-uuuuujjjjuuuuuu-");

            for(String i: resultItem){
                String[] temp2 = i.split("----kkkkkkkkkkk----");
                if(temp2.length > 1){

                    SongItem temp3 = new SongItem(100,temp2[1],"Online","http://mohamedmnete.com/"+temp2[0],true);
                    temp.add(temp3);

                }

            }

        }


        return temp;
    }


    public ArrayList<searchResult> prepareSearchResults(String s){

        ArrayList<searchResult> temp = new ArrayList<searchResult>();


        if(s.length() > 1){

            String[] resultLines = s.split("-uuuuujjjjuuuuuu-");


            for(String i : resultLines){
                String[] temp2 = i.split("----kkkkkkkkkkk----");
                if(temp2.length > 1){
                    searchResult searchResult = new searchResult(temp2[0],temp2[1]);
                    temp.add(searchResult);
                }

            }


        }



        return temp;
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
            if(musicSrv.currPlayList == null){
                musicSrv.currPlayList = new playlist_item("default",getSongList());
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };



    public ArrayList<SongItem> getSongList(){

        ArrayList<SongItem> songList = new ArrayList<SongItem>();

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
                songList.add(new SongItem(thisId, thisTitle, thisArtist, path, false));
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(songList, new Comparator<SongItem>(){
            public int compare(SongItem a, SongItem b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        musicCursor.close();

        return songList;
    }



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
            if (ActivityCompat.shouldShowRequestPermissionRationale(search.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // updateSongList();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(search.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);
                }


            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(search.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);

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
                                ask4permission(search.this);
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

