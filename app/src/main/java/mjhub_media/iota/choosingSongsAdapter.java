package mjhub_media.iota;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mmnet on 2017-08-08.
 */
public class choosingSongsAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<SongItem> list = new ArrayList<SongItem>();
    private Context context;

    public ArrayList<SongItem> newPlayList = new ArrayList<SongItem>();



    public choosingSongsAdapter(ArrayList<SongItem> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return list.get(pos).getID();
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.songs2add2playlist_item, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position).getTitle());

        //Handle buttons and add onClickListeners
        final Button addBtn = (Button)view.findViewById(R.id.add_btn);

        addBtn.setOnClickListener(new View.OnClickListener(){



            @Override
            public void onClick(View v) {

                if(addBtn.getText().toString().equals("ADD")){
                    addBtn.setText("ADDED");
                }else{
                    addBtn.setText("ADD");
                }

                if(!newPlayList.contains(list.get(position))){

                    newPlayList.add(list.get(position));
                }


            }
        });

        return view;
    }
}
