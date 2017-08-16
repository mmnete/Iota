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
 * Created by mmnet on 2017-08-09.
 */
public class AddingSongsToUpdatePlayListAdapter extends BaseAdapter {

    private ArrayList<SongItem> songs;
    private LayoutInflater songInf;

    public ArrayList<SongItem> selectedSongs;

    public AddingSongsToUpdatePlayListAdapter(Context c, ArrayList<SongItem> theSongs){

        songs=theSongs;
        songInf=LayoutInflater.from(c);
        selectedSongs = new ArrayList<SongItem>();

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return songs.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.adding_songs_to_acreated_playlist_item, parent, false);
        //get title and artist views
        TextView songView = (TextView)songLay.findViewById(R.id.adding_song);
        final Button selectBtn = (Button)songLay.findViewById(R.id.select_btn1);

        //get song using position
        SongItem currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectBtn.getText().toString().equals("SELECT")){
                    selectedSongs.add(songs.get(position));
                    selectBtn.setText("SELECTED");
                }else{
                    selectedSongs.remove(songs.get(position));
                    selectBtn.setText("SELECT");
                }
            }
        });
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

}