package mjhub_media.iota;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class newPlaylist extends AppCompatActivity {


    FirebaseAuth firebaseAuth;

    ListView choosingAudio;

    choosingSongsAdapter choosingSongsAdapter;

    PlayListCollection playListCollection;

    EditText playListName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_new_playlist);

        choosingAudio = (ListView) findViewById(R.id.selectingSongs);
        playListName = (EditText) findViewById(R.id.playListName1);
        playListCollection = new PlayListCollection(this,getSongList());



        firebaseAuth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateDisplay();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        updateDisplay();
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
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

    public void back2playlist(View v){
        Intent i = new Intent(this,playlist_list.class);
        startActivity(i);
    }

    public void updateDisplay(){
         choosingSongsAdapter = new choosingSongsAdapter(getSongList(),this);
        choosingAudio.setAdapter(choosingSongsAdapter);
    }

    public void createNewPlayList(View v){

        if(!playListName.getText().toString().equals("")){

            if(choosingSongsAdapter.newPlayList.size() != 0){

                if(playListName.getText().length() < 60){


                    if(!doesPlayListNameExist()){




                        if(doesntContainFunnyBusiness(playListName.getText().toString())){
                            playlist_item playlist_item = new playlist_item(playListName.getText().toString(),choosingSongsAdapter.newPlayList);
                            playListCollection.addPlayList(playlist_item);

                            Intent i = new Intent(this, playlist_list.class);
                            startActivity(i);
                        }else{

                            Toast.makeText(this,"Only alpha numeric charcters allowed.",Toast.LENGTH_LONG).show();
                        }





                    }else{
                        Toast.makeText(this,"The playlist name is use!",Toast.LENGTH_LONG).show();
                    }






                }else{
                    Toast.makeText(this,"Playlist name too long...",Toast.LENGTH_LONG).show();
                }


            }else{
                Toast.makeText(this,"Please choose atleast 1 song!",Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(this,"Please type in PlayList Name!",Toast.LENGTH_LONG).show();
        }

    }


    private boolean doesntContainFunnyBusiness(String str){

        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c))
                return false;
        }

        return true;

    }


    private boolean doesPlayListNameExist(){
        String name = playListName.getText().toString();
        for(playlist_item i : playListCollection.playlists){
            if(i.name.equals(name)){
                return true;
            }
        }
        return false;
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

}
