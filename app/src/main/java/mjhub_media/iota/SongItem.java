package mjhub_media.iota;

/**
 * Created by mmnet on 2017-08-08.
 */
public class SongItem {
    private long id;
    private String title;
    private String artist;
    private String filePath;
    public boolean online;


    public SongItem(long songID, String songTitle, String songArtist, String filePath, boolean online) {
        this.id=songID;
        this.title=songTitle;
        this.artist=songArtist;
        this.filePath = filePath;
        this.online = online;
    }


    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getFilePath(){return filePath;}
}
