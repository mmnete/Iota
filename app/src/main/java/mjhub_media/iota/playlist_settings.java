package mjhub_media.iota;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class playlist_settings extends AppCompatActivity {


    FirebaseAuth firebaseAuth;

    PlayListCollection playListCollection;

    playlist_item playlist_item;

    playListSettingsAdapter playListSettingsAdapter;

    ListView songList;

    TextView playListName;



    private MusicService musicSrv;
    private boolean musicBound;

    private final  int  MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES = 0;
    private boolean viewingFilesPermissions = false;


    ProgressDialog progressDialog;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_playlist_settings);


        firebaseAuth = FirebaseAuth.getInstance();

        playListCollection = new PlayListCollection(this,getSongList());

        songList = (ListView) findViewById(R.id.playlist_main_menu_listView);

        playListName = (TextView) findViewById(R.id.textView10);

        Gson gS = new Gson();
        String target = getIntent().getStringExtra("selected_playlist");
        playlist_item = gS.fromJson(target, playlist_item.class);

        checkIfUserIsLoggedOut();

        bindMethod();


        progressDialog = new ProgressDialog(playlist_settings.this);
        progressDialog.setMessage("Upload in progress... please wait...");
        progressDialog.setMessage("The bigger the playlist... the longer the time!");


    }


    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserIsLoggedOut();

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(playlist_settings.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            updateDisplay();
            if(!musicBound){
                bindMethod();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfUserIsLoggedOut();

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(playlist_settings.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            updateDisplay();
            if(!musicBound){
                bindMethod();
            }
        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkIfUserIsLoggedOut();

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(playlist_settings.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            updateDisplay();
            if(!musicBound){
                bindMethod();
            }
        }


    }

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
        Collections.sort(songList, new Comparator<SongItem>(){
            public int compare(SongItem a, SongItem b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        musicCursor.close();

        return songList;
    }


    public boolean checkMemoryOfAllSongs(){

        Toast.makeText(this,"Checking file sizes...",Toast.LENGTH_LONG).show();

        for(SongItem i: playlist_item.songs){

          if(memory(i.getFilePath()) > 600){
              return false;
          }

        }


        Toast.makeText(this,"You are good! Starting upload now!",Toast.LENGTH_LONG).show();

        return true;
    }

    public float memory(String selectedFile){
        File file = new File(selectedFile);
        return (float) ((file.length() / 1024.0) / 1024.0);
    }




    public void uploadPlayListName(String playlistName){



        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("playlistName", playlistName));
        nameValuePairs.add(new BasicNameValuePair("user", firebaseAuth.getCurrentUser().getEmail()));

        String res = "";
        try{

            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://mohamedmnete.com/upload_playlist_details.php");

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();


            res = "Working...";
        } catch (MalformedURLException e) {
            e.printStackTrace();

            res = "Uploading Error";

        } catch (IOException e) {
            e.printStackTrace();

            res = "Uploading Error";
       }

        Toast.makeText(this,res,Toast.LENGTH_LONG).show();

    }


    public  void insert_user_of_playlist_details(String songName){



        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("playlist_name", this.playlist_item.name));
        nameValuePairs.add(new BasicNameValuePair("song_name", songName));
        nameValuePairs.add(new BasicNameValuePair("playlist_name", this.playlist_item.name));
        nameValuePairs.add(new BasicNameValuePair("user", firebaseAuth.getCurrentUser().getEmail()));

        String res = "";
        try{

            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://mohamedmnete.com/insert_song.php");

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();


            res = "Working...";
        } catch (MalformedURLException e) {
            e.printStackTrace();

            res = "Uploading Error";

        } catch (IOException e) {
            e.printStackTrace();

            res = "Uploading Error";
        }

        Toast.makeText(this,res,Toast.LENGTH_LONG).show();

    }


    public void uploadAudioFile(String selectedFilePath, final int fileNumber){


        int serverResponseCode = 0;


        HttpURLConnection connection;
      DataOutputStream dataOutputStream;
     String lineEnd = "\r\n";
       String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
       byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
       File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   Toast.makeText(playlist_settings.this,"Some file is missing..",Toast.LENGTH_LONG).show();
                    }
                });
            return;
           }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
               URL url = new URL("http://mohamedmnete.com/upload_auido_file.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
               connection.setRequestProperty("uploaded_file",selectedFilePath);

                insert_user_of_playlist_details(fileName);

                //creating new dataoutputstream
               dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
               dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                   bytesRead = fileInputStream.read(buffer,0,bufferSize);
                    }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
               String serverResponseMessage = connection.getResponseMessage();

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                       @Override
                        public void run() {
                          Toast.makeText(playlist_settings.this,"Uploaded "+fileNumber+" / "+playlist_item.songs.size(),Toast.LENGTH_LONG).show();
                           }
                        });
                    }

                //closing the input and output streams
               fileInputStream.close();
               dataOutputStream.flush();
                dataOutputStream.close();

                } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(playlist_settings.this,"File Not Found",Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(playlist_settings.this, "URL error!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(playlist_settings.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                }

           return;
            }

        }

    public void updateDisplay(){
        playListSettingsAdapter = new playListSettingsAdapter(this,playlist_item.songs);
     songList.setAdapter(playListSettingsAdapter);
        playListName.setText(playlist_item.name);

    }

    public void playPlayList(View v){
        if(musicSrv != null){
            if(playlist_item.songs.size() > 0){
                musicSrv.currPlayList = playlist_item;
                musicSrv.playSong(0);
                musicSrv.setShuffle();
            }else{
                Toast.makeText(this,"The playlist has no songs!",Toast.LENGTH_LONG).show();
            }

        }
    }

    public void checkIfUserIsLoggedOut(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            logOut();
        }
    }

    public void logOut(){
        firebaseAuth.signOut();
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }

    public void move2playlist_list(View v){
        Intent i = new Intent(this, playlist_list.class);
        startActivity(i);
    }


    public void addSongs2playList(View v){
        Gson gS = new Gson();
        String target = gS.toJson(playlist_item);
        Intent i = new Intent(this, addSongs2playList_fromSettings.class);
        i.putExtra("selected_playlist", target);
        startActivity(i);
    }


    public void deletePlayList(View v){

        if(!playlist_item.name.equals("default")){
            int x_index = 0;
            playlist_item currplaylist = playListCollection.playlists.get(0);

            while(!currplaylist.name.equals(playlist_item.name)){
                x_index++;
                currplaylist = playListCollection.playlists.get(x_index);
            }

            playListCollection.playlists.remove(x_index);
            playListCollection.savePlayListCollection(this,playListCollection.playlists);


            Intent i = new Intent(this, playlist_list.class);
            startActivity(i);

        }else{
            Toast.makeText(this,"You cannot delete the default playList!",Toast.LENGTH_LONG).show();
        }

    }

    public void removeSelectedAudio(View v){

        if(!playlist_item.name.equals("default")) {

            if(playListSettingsAdapter.selectedSongs.size() > 0){
                ArrayList<SongItem> temp = playlist_item.songs;
                for(SongItem i : playListSettingsAdapter.selectedSongs){
                    temp.remove(i);
                }
                playlist_item newplaylist = playlist_item;
                newplaylist.songs = temp;
                int x_index = 0;

                playlist_item currplaylist = playListCollection.playlists.get(0);
                while(!currplaylist.name.equals(playlist_item.name)){
                    x_index++;
                    currplaylist = playListCollection.playlists.get(x_index);
                }

                playListCollection.playlists.set(x_index,newplaylist);
                playListCollection.savePlayListCollection(this,playListCollection.playlists);
                Toast.makeText(this,"Done!",Toast.LENGTH_LONG).show();
                Intent i = new Intent(this, playlist_list.class);
                startActivity(i);
            }else{
                Toast.makeText(this,"Please select atleast one audio file to remove!",Toast.LENGTH_LONG).show();
            }

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




    public void uploadPlayList(View v){



        if(!playlist_item.name.equals("default")) {

            if(checkMemoryOfAllSongs()){

                //check for memory size

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {



                    progressDialog.show();


                    Thread tu = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();

                            uploadPlayListName(playlist_item.name);

                            Looper.loop();
                        }
                    });

                    tu.start();

                    if(playlist_item.songs.size() > 0){


                        Thread t = new Thread(new Runnable() {

                            @Override

                            public void run() {

                                Looper.prepare();

                                int fileNumber = 1;
                                for(SongItem i: playlist_item.songs){

                                    uploadAudioFile(i.getFilePath(), fileNumber);
                                    fileNumber++;

                                }


                                Looper.loop();




                            }

                        });



                        t.start();




                    }



                    progressDialog.dismiss();





                } else{

                    Toast.makeText(playlist_settings.this,"No Internet Connection",Toast.LENGTH_LONG).show();

                }




            }else{

                Toast.makeText(playlist_settings.this,"One of your audio files is too big.. limit is 600 MB.",Toast.LENGTH_LONG).show();

            }






        }else{
            Toast.makeText(playlist_settings.this,"You cannot upload the default playlist, please create your custom one and upload that one!", Toast.LENGTH_LONG).show();
        }




    }


    private String getMimeType(String path) {



        String extension = MimeTypeMap.getFileExtensionFromUrl(path);



        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

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
            if (ActivityCompat.shouldShowRequestPermissionRationale(playlist_settings.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // updateSongList();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(playlist_settings.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);
                }


            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(playlist_settings.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);

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
                                ask4permission(playlist_settings.this);
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
