package mjhub_media.iota;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class OnlinePlaylist extends AppCompatActivity {


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    private ListView onlinePlayLists;

    public List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    public List<NameValuePair> nameValuePairs1 = new ArrayList<NameValuePair>();
    String res1 = "";
    boolean searchAttempt1 = false;
    boolean searchAttempt = false;
    String res = "";
    private MusicService musicSrv;
    private boolean musicBound;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_playlist);


      onlinePlayLists = (ListView)findViewById(R.id.listView3);



  checkIfUserIsLoggedOut();

        checkForInternetConnection();

        bindMethod();



            updateDisplay();



    }


    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserIsLoggedOut();
        checkForInternetConnection();

        bindMethod();


            updateDisplay();


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfUserIsLoggedOut();
        checkForInternetConnection();

        bindMethod();


            updateDisplay();


    }

    public void updateDisplay(){

        Thread r = new Thread(new Runnable() {
            @Override
            public void run() {




                nameValuePairs.clear();
                nameValuePairs.add(new BasicNameValuePair("user",firebaseAuth.getCurrentUser().getEmail()));

                //get the list of songs
                try{



                    HttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost("http://mohamedmnete.com/fetch_user_onlinePlaylist.php");

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    String htmlResponse = EntityUtils.toString(entity);


                    res = htmlResponse;

                    searchAttempt = true;


                } catch (MalformedURLException e) {
                    e.printStackTrace();

                    res = "";

                    searchAttempt = false;
                } catch (IOException e) {
                    e.printStackTrace();

                    res = "";
                    searchAttempt = false;
                }

                if(res.equals("")){
                    searchAttempt = false;
               }



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(searchAttempt){

                            search_resultAdapter search_resultAdapter = new search_resultAdapter(OnlinePlaylist.this,prepareSearchResults(res));
                            onlinePlayLists.setAdapter(search_resultAdapter);
                            onlinePlayLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    Toast.makeText(OnlinePlaylist.this,prepareSearchResults(res).get(position).display_text,Toast.LENGTH_LONG).show();



                                    nameValuePairs1.clear();
                                    nameValuePairs1.add(new BasicNameValuePair("playlist_name",prepareSearchResults(res).get(position).display_text));
                                    nameValuePairs1.add(new BasicNameValuePair("user",prepareSearchResults(res).get(position).info));


                                    Thread r = new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            //get the list of songs
                                            try{

                                                HttpClient httpClient1 = new DefaultHttpClient();

                                                HttpPost httpPost1 = new HttpPost("http://mohamedmnete.com/load_playlist.php");

                                                httpPost1.setEntity(new UrlEncodedFormEntity(nameValuePairs1));

                                                HttpResponse response = httpClient1.execute(httpPost1);

                                                HttpEntity entity1 = response.getEntity();

                                                String htmlResponse1 = EntityUtils.toString(entity1);


                                                res1 = htmlResponse1;

                                                searchAttempt1 = true;


                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();

                                                res1 = "Search Error";

                                                searchAttempt1 = false;
                                            } catch (IOException e) {
                                                e.printStackTrace();

                                                res1 = "Search Error";
                                                searchAttempt1 = false;
                                            }

                                            if(searchAttempt1){
                                                ArrayList<SongItem> newSongList = prepareOnlinePlaylist(res1);
                                                playlist_item newPlayList = new playlist_item("Online",newSongList);
                                                musicSrv.currPlayList = newPlayList;
                                                musicSrv.playSong(0);
                                                Intent i = new Intent(OnlinePlaylist.this,song.class);
                                                startActivity(i);
                                                searchAttempt1 = false;
                                            }


                                        }
                                    });
                                    r.start();

                                }
                            });
                            searchAttempt = false;

                        }

                    }
                });


            }
        });


        r.start();





    }


    public ArrayList<SongItem> prepareOnlinePlaylist(String onlineResult){
        ArrayList<SongItem> temp = new ArrayList<SongItem>();


        if(onlineResult.length() > 1){


            String[] resultItem = onlineResult.split("-uuuuujjjjuuuuuu-");

            for(String i: resultItem){
                String[] temp2 = i.split("----kkkkkkkkkkk----");
                if(temp2.length > 1){

                    SongItem temp3 = new SongItem(100,temp2[1],"Online","http://mmnete.000webhostapp.com/"+temp2[0],true);
                    temp.add(temp3);

                }

            }

        }



        return temp;
    }

    public ArrayList<searchResult> prepareSearchResults(String s){

        ArrayList<searchResult> temp = new ArrayList<searchResult>();


        if(s.length() > 1){

            String[] resultLines = s.split("-uuuuujjjjuuuuuu-");


            for(String i : resultLines){
                String[] temp2 = i.split("----kkkkkkkkkkk----");
                if(temp2.length > 1){

                    searchResult searchResult = new searchResult(temp2[0],temp2[1]);
                    temp.add(searchResult);

                }

            }


        }



        return temp;
    }

    public void back(View v){
        Intent i = new Intent(this, user.class);
        startActivity(i);
    }




    public void logOut(){
        firebaseAuth.signOut();
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }



    public void checkIfUserIsLoggedOut(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            logOut();
        }
    }


    public void checkForInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {


        } else{

            Toast.makeText(OnlinePlaylist.this,"There is no internet connection",Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, user.class);
            startActivity(i);

        }
    }



          /*
Bind and connect to music service
 */

    public void bindMethod(){
        Intent i = new Intent(this,MusicService.class);
        startService(i);
        bindService(i, musicConnection, Context.BIND_AUTO_CREATE);
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            musicBound = true;

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
    }





}
