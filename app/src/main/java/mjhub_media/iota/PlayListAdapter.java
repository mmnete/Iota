package mjhub_media.iota;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mmnet on 2017-08-08.
 */
public class PlayListAdapter extends BaseAdapter {

    private ArrayList<playlist_item> playlists;
    private LayoutInflater songInf;

    public PlayListAdapter(Context c, ArrayList<playlist_item> thePlaylists){
        playlists = thePlaylists;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return playlists.size();
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
                (R.layout.playlist_item, parent, false);
        //get title and artist views
        TextView playlistView = (TextView)songLay.findViewById(R.id.playlistName);
        //get song using position
        playlist_item currPlaylist = playlists.get(position);
        //get title and artist strings
        playlistView.setText(currPlaylist.name);
        //set position as tag
        songLay.setTag(position);

        return songLay;
    }

}
