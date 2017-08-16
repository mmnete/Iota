package mjhub_media.iota;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class addSongs2playList_fromSettings extends AppCompatActivity {


    FirebaseAuth firebaseAuth;

    playlist_item playlist_item;


    TextView playListName;
    ListView songList;

    AddingSongsToUpdatePlayListAdapter addingSongsToUpdatePlayListAdapter;

    PlayListCollection playListCollection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs2play_list_from_settings);

        firebaseAuth = FirebaseAuth.getInstance();

        Gson gS = new Gson();
        String target = getIntent().getStringExtra("selected_playlist");
        playlist_item = gS.fromJson(target, playlist_item.class);

        checkIfUserIsLoggedOut();

        playListName = (TextView) findViewById(R.id.textView11);
        songList = (ListView)findViewById(R.id.listView);
        playListCollection = new PlayListCollection(this,getSongList());

        updateDisaplay();

    }


    @Override
    protected void onStart() {
        super.onStart();
        updateDisaplay();
        checkIfUserIsLoggedOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisaplay();
        checkIfUserIsLoggedOut();
    }


    public void updateDisaplay(){
        playListName.setText(playlist_item.name);
        addingSongsToUpdatePlayListAdapter = new AddingSongsToUpdatePlayListAdapter(this,getSongList());
        songList.setAdapter(addingSongsToUpdatePlayListAdapter);
    }

    public void addNewSongs(View v){
        if(addingSongsToUpdatePlayListAdapter.selectedSongs.size() > 0){

            ArrayList<SongItem> temp = playlist_item.songs;
            for(SongItem i : addingSongsToUpdatePlayListAdapter.selectedSongs){
                if(!temp.contains(i)){
                    temp.add(i);
                }
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


        }else{
            Toast.makeText(this,"Please select atleast one song!",Toast.LENGTH_LONG).show();
        }
    }

    public void back2playlist_list(View v){
        Intent i = new Intent(this, playlist_list.class);
        startActivity(i);
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
}
