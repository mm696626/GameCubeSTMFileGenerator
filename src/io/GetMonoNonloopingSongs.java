package io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GetMonoNonloopingSongs {

    public static boolean isSongMono(File stmFile) throws IOException {
        int valueAt4;

        try (RandomAccessFile stmRaf = new RandomAccessFile(stmFile, "r")) {
            stmRaf.seek(0x04);
            valueAt4 = stmRaf.readInt();
        }

        return valueAt4 == 1;
    }

    public static boolean isSongNonLooping(File stmFile) throws IOException {
        int nonLoopingValue0C;
        int nonLoopingValue18;
        int nonLoopingValue1C;

        try (RandomAccessFile stmRaf = new RandomAccessFile(stmFile, "r")) {
            stmRaf.seek(0x0C);
            nonLoopingValue0C = stmRaf.readInt();
            stmRaf.seek(0x18);
            nonLoopingValue18 = stmRaf.readInt();
            stmRaf.seek(0x1C);
            nonLoopingValue1C = stmRaf.readInt();
        }

        if (nonLoopingValue0C != 0xFFFFFFFF) {
            return false;
        }

        return nonLoopingValue18 == 0 && nonLoopingValue1C == 0;
    }
}
