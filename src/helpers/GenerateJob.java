package helpers;

import java.io.File;

public class GenerateJob {
    private String songFileName;
    private String leftDSP;
    private String rightDSP;

    public GenerateJob(String songFileName, String leftDSP, String rightDSP) {
        this.songFileName = songFileName;
        this.leftDSP = leftDSP;
        this.rightDSP = rightDSP;
    }

    public String getSongFileName() {
        return songFileName;
    }

    public String getLeftDSP() {
        return leftDSP;
    }

    public String getRightDSP() {
        return rightDSP;
    }

    @Override
    public String toString() {
        return songFileName + " [" + new File(leftDSP).getName() + ", " + new File(rightDSP).getName() + "]";
    }
}