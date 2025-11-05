package helpers;

import java.io.File;

public class GenerateJob {
    private String songName;
    private String leftDSP;
    private String rightDSP;

    public GenerateJob(String songName, String leftDSP, String rightDSP) {
        this.songName = songName;
        this.leftDSP = leftDSP;
        this.rightDSP = rightDSP;
    }

    public String getSongName() {
        return songName;
    }

    public String getLeftDSP() {
        return leftDSP;
    }

    public String getRightDSP() {
        return rightDSP;
    }

    @Override
    public String toString() {
        return songName + " [" + new File(leftDSP).getName() + ", " + new File(rightDSP).getName() + "]";
    }
}