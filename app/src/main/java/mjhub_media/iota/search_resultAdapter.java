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
 * Created by mmnet on 2017-08-13.
 */
public class search_resultAdapter extends BaseAdapter {

    private ArrayList<searchResult> searchResults;
    private LayoutInflater songInf;

    public search_resultAdapter(Context c, ArrayList<searchResult> searchResults){
        this.searchResults = searchResults;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return searchResults.size();
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
                (R.layout.search_result_item, parent, false);
        //get title and artist views
        TextView resultlistView = (TextView)songLay.findViewById(R.id.searched_playlist);
        //get song using position
        searchResult currResult = searchResults.get(position);
        //get title and artist strings
        resultlistView.setText(currResult.display_text);
        //set position as tag
        songLay.setTag(position);

        return songLay;
    }

}
