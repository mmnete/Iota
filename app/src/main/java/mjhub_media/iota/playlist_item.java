package mjhub_media.iota;

import java.util.ArrayList;

/**
 * Created by mmnet on 2017-08-08.
 */
public class playlist_item {


    public String name;
    public  ArrayList<SongItem> songs;
    public int length = 0;

   public playlist_item(String name, ArrayList<SongItem> songs){
        this.name = name;
        this.songs = songs;
        this.length = songs.size();
    }

    public void add(SongItem song){
        this.songs.add(song);
    }

    public void deleteSong(SongItem songItem){
        this.songs.remove(this.songs.indexOf(songItem));
    }

}
