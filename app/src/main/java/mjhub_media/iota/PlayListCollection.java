package mjhub_media.iota;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by mmnet on 2017-08-08.
 */
public class PlayListCollection {

    public ArrayList<playlist_item> playlists;
    private Context context;



    public PlayListCollection(Context context, ArrayList<SongItem> allaudio ){
        this.context = context;
        preparePlayLists(context, allaudio);
    }

    public void addPlayList(playlist_item playlist_item){
        playlists.add(playlist_item);
        savePlayListCollection(this.context,this.playlists);
    }

    public void deletePlayList(playlist_item playlist_item){
        playlists.remove(playlist_item);
        savePlayListCollection(this.context,this.playlists);
    }


    public void savePlayListCollection(Context context, ArrayList<playlist_item> temp){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();

        String json = gson.toJson(temp);

        editor.putString("playListCollection", json);
        editor.apply();
        editor.commit();
    }

    public  ArrayList<playlist_item> getPlayList(Context context){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString("playListCollection", null);
        Type type = new TypeToken<ArrayList<playlist_item>>() {}.getType();
        ArrayList<playlist_item> arrayList = gson.fromJson(json, type);

        return arrayList;
    }

    public void preparePlayLists(Context context, ArrayList<SongItem> allAudio){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      if(preferences.contains("playListCollection")){
            /*
            Get from prepared statement and get the first playlist and update it with the new list of songs..
             */

            this.playlists = getPlayList(context);

        }else{
            //app has not been in this phone before.....
            //prepare the default playlist...
            //this is only when the user allows us to access the songs...

            playlist_item playlist_item = new playlist_item("default",allAudio);

            ArrayList<playlist_item> temp = new ArrayList<playlist_item>();
            temp.add(playlist_item);

            savePlayListCollection(context,temp);

            this.playlists = getPlayList(context);



        }
    }

}
