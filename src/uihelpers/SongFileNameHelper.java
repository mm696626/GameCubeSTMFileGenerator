package uihelpers;

import constants.STMFileNames;

public class SongFileNameHelper {

    public static String getFileNameFromSong(String selectedGame, String songName) {
        Song[] songArray;

        switch (selectedGame) {
            case "Cubivore: Survival of the Fittest":
                songArray = STMFileNames.CUBIVORE_FILE_NAMES;
                break;
            case "Fire Emblem: Path of Radiance":
                songArray = STMFileNames.FIRE_EMBLEM_POR_FILE_NAMES;
                break;
            default:
                songArray = STMFileNames.PAPER_MARIO_TTYD_FILE_NAMES;
                break;
        }

        for (Song song : songArray) {
            if (song.getSongDisplayName().equals(songName)) {
                return song.getSongFileName();
            }
        }

        return null;
    }
}
