package helpers;

public class Song {
    private String songFileName;
    private String songDisplayName;

    public Song(String songFileName, String songDisplayName) {
        this.songFileName = songFileName;
        this.songDisplayName = songDisplayName;
    }

    public String getSongFileName() {
        return songFileName;
    }

    public String getSongDisplayName() {
        return songDisplayName;
    }
}