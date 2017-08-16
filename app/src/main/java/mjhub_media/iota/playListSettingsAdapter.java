package mjhub_media.iota;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mmnet on 2017-08-08.
 */
public class playListSettingsAdapter extends BaseAdapter {

    private ArrayList<SongItem> playlist;
    private LayoutInflater songInf;

    public ArrayList<SongItem> selectedSongs = new ArrayList<SongItem>();

    public playListSettingsAdapter(Context c, ArrayList<SongItem> thePlaylist){
        playlist = thePlaylist;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return playlist.size();
    }
    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.playlist_settings_song_item, parent, false);
        //get title and artist views
        TextView playlistView = (TextView)songLay.findViewById(R.id.playlist_song_item);
        final Button selectBtn = (Button) songLay.findViewById(R.id.select_btn);
        //get song using position
        final SongItem currSong = playlist.get(position);
        //get title and artist strings
        playlistView.setText(currSong.getTitle());
        //set position as tag
        songLay.setTag(position);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectBtn.getText().toString().equals("SELECT")){
                    selectBtn.setText("SELECTED");
                    if(!selectedSongs.contains(currSong)){
                        selectedSongs.add(currSong);
                    }
                }else{
                    selectBtn.setText("SELECT");
                    selectedSongs.remove(currSong);
                }

            }
        });


        return songLay;
    }

}
