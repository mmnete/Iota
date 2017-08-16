package mjhub_media.iota;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.Random;

/**
 * Created by mmnet on 2017-08-09.
 */
public class MusicService extends Service implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener{




    int mNotificationId = 1;

    public boolean mRunning = false;


    private  NotificationManager mNotificationManager;

    public playlist_item currPlayList;
    public int curr_song_index = 0;
    public int length;
    public SongItem currSong;
    public boolean Shuffle = false;

    Random rand = new Random();

    AudioManager am;
    AudioManager.OnAudioFocusChangeListener afChangeListener;


    //media player
        private MediaPlayer player;

        //current position
        private int songPosn;
        //binder
        private final IBinder musicBind = new MusicBinder();




    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        player = new MediaPlayer();
        //initialize
        initMusicPlayer();




         am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);









// mNotificationId is a unique integer your app uses to identify the
// notification. For example, to cancel the notification, you can pass its ID
// number to NotificationManager.cancel().

    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);






    }


    public void prepareService( playlist_item temp){
        this.currPlayList = temp;
        if(temp.songs.size() > 0){
            this.currSong = temp.songs.get(this.curr_song_index);
            this.setSong(0);
        }
        this.length = temp.songs.size();
    }

    public void changePlayList(playlist_item temp){
        prepareService(temp);
    }


    public void playNext(){


            this.curr_song_index++;
            if(Shuffle){
                this.curr_song_index += rand.nextInt();
            }
            this.curr_song_index %= this.currPlayList.length;
            playSong(this.curr_song_index);







    }

    public void playPrev(){



            this.curr_song_index--;
            if(Shuffle){
                this.curr_song_index -= rand.nextInt();
            }
            this.curr_song_index %= this.currPlayList.length;
            playSong(this.curr_song_index);



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mRunning = true;



        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    if(player != null){
                        if(player.isPlaying()){
                            player.pause();
                        }
                    }
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    if(player != null){
                        if(player.isPlaying()){
                            player.pause();
                        }
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }



        return super.onStartCommand(intent, flags, startId);


    }



    //binder
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }




    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void clearPlayer(){

        if(player != null){
            if(player.isPlaying()){
                player.stop();
            }

        }
        this.stopSelf();
        if(mNotificationManager != null){
            mNotificationManager.cancelAll();
        }

    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public void setShuffle(){
        Shuffle = !Shuffle;
    }

    public void setProgress(int prog){
        player.seekTo(prog);
    }

    public int getCurrDuration(){
        return player.getCurrentPosition();
    }

    public void pause(){

        am.abandonAudioFocus(afChangeListener);

        if(player.isPlaying()){
            player.pause();
        }else{
            if(!player.isPlaying()){
                player.start();
            }else{
                playSong(0);
            }
        }
    }

    //play a song
    public void playSong(int songPosn){












            /*
            We got the permission to play the song
             */


            if(Shuffle){
                this.curr_song_index += rand.nextInt();

                this.curr_song_index %= this.currPlayList.length;


            }



            if(songPosn < 0){
                songPosn = 0;
            }

            if(songPosn > this.currPlayList.songs.size() - 1){
                songPosn = this.currPlayList.songs.size();
            }

            if(this.currPlayList.songs.size() > 0){


                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.launch)
                        .setContentTitle("I o t a").setContentText(this.currPlayList.songs.get(songPosn).getTitle());
                // Creates an explicit intent for an Activity in your app


                mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(mNotificationId, mBuilder.build());

                this.currSong = this.currPlayList.songs.get(songPosn);
                //play
                player.reset();
                //get song
                SongItem playSong = this.currPlayList.songs.get(songPosn);
                //get id
                if(playSong.online){

                    long currSong = playSong.getID();
                    //set uri
                    Uri trackUri = ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            currSong);
                    //set the data source
                    try{
                        player.setDataSource(playSong.getFilePath());
                    }
                    catch(Exception e){

                        playNext();
                    }
                    player.prepareAsync();

                }else{

                    long currSong = playSong.getID();
                    //set uri
                    Uri trackUri = ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            currSong);
                    //set the data source
                    try{
                        player.setDataSource(getBaseContext(), trackUri);
                    }
                    catch(Exception e){

                        playNext();
                    }
                    player.prepareAsync();


                }


            }















    }

    //set the song
    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub

           playNext();


    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
    }







    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mRunning  = false;
        if(mNotificationManager != null){
            mNotificationManager.cancelAll();
        }

    }








}


