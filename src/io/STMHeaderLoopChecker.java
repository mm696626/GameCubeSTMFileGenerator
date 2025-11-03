package io;

import constants.DSPFileConstants;
import constants.STMFileNames;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class STMHeaderLoopChecker {

    public static boolean isValidLoopStart(File dspChannel) {
        byte[] loopStartBytes = new byte[DSPFileConstants.LOOP_START_LENGTH_IN_BYTES];
        try (RandomAccessFile dspRaf = new RandomAccessFile(dspChannel, "r")) {
            dspRaf.seek(DSPFileConstants.LOOP_START_OFFSET);
            dspRaf.read(loopStartBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] validLoopStartBytes = {
                (byte) 0x02, (byte) 0x42, (byte) 0x82, (byte) 0xC2
        };

        byte loopStartByte = loopStartBytes[DSPFileConstants.LOOP_START_LENGTH_IN_BYTES - 1];

        for (byte validLoopStartByte : validLoopStartBytes) {
            if (loopStartByte == validLoopStartByte) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSongNonLooping(File stmFile) throws IOException {
        int nonLoopingValue;

        try (RandomAccessFile stmRaf = new RandomAccessFile(stmFile, "r")) {
            stmRaf.seek(0x0C);
            nonLoopingValue = stmRaf.readInt();
        }

        return nonLoopingValue == 0xFFFFFFFF;
    }

    public static boolean isSongNonLoopingNonExisting(String fileName) {
        for (int i=0; i<STMFileNames.NONLOOPING_FILE_NAMES.length; i++) {
            if (fileName.equals(STMFileNames.NONLOOPING_FILE_NAMES[i])) {
                return true;
            }
        }

        return false;
    }
}
