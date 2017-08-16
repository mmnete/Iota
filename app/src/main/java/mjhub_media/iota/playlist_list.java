package mjhub_media.iota;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class playlist_list extends AppCompatActivity {


    FirebaseAuth firebaseAuth;

    private final  int  MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES = 0;

    private ArrayList<SongItem> songList = new ArrayList<SongItem>();

    public ListView playList_listview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_playlist_list);

        playList_listview = (ListView) findViewById(R.id.playList);


        firebaseAuth = FirebaseAuth.getInstance();


    }


    @Override
    protected void onStart() {
        super.onStart();
        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(playlist_list.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlist
            updatePlayLists();
        }
        checkIfUserIsLoggedOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(playlist_list.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listbiew of the playlist
            updatePlayLists();
        }
        checkIfUserIsLoggedOut();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            permissionCheck = ContextCompat.checkSelfPermission(playlist_list.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionCheck == -1){
            ask4permission(this.getApplicationContext());
        }else{
            //update the listview of the playlists....
            updatePlayLists();
        }
        checkIfUserIsLoggedOut();
    }



    public void ask4permission(Context context){

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(playlist_list.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


            } else {

                // No explanation needed, we can request the permission.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(playlist_list.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES);
                }

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }





    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_AUDIO_FILES: {
                // If request is cancelled, the result arrays are empty.
                boolean viewingFilesPermissions = false;
                viewingFilesPermissions = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if(viewingFilesPermissions){
                    //update the list view to view all the playlists...
                    updatePlayLists();
                }else{
                    Intent i = new Intent(playlist_list.this,user.class);
                    startActivity(i);
                }

                return;

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }




   public void updatePlayLists(){
        final PlayListCollection playListCollection = new PlayListCollection(this,getSongList());
        PlayListAdapter playlistAdt = new PlayListAdapter(this, playListCollection.getPlayList(this));
        playList_listview.setAdapter(playlistAdt);

       playList_listview.setOnItemClickListener(
               new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       /*
                       Pass the playlist to the settings activity...
                        */
                       playlist_item src = playListCollection.playlists.get(position);
                       Gson gS = new Gson();
                       String target = gS.toJson(src);
                       Intent i = new Intent(playlist_list.this, playlist_settings.class);
                       i.putExtra("selected_playlist", target);
                       startActivity(i);
                   }
               }
       );

    }


    public ArrayList<SongItem> getSongList(){
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

        if(musicCursor != null){
            musicCursor.close();
        }


        return songList;
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


    public void back2user1(View v){
        Intent i = new Intent(this, user.class);
        startActivity(i);
    }

    public void newPlaylist(View v){
        Intent i = new Intent(this, newPlaylist.class);
        startActivity(i);
    }


}
