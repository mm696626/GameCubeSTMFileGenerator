package io;

import constants.DSPFileConstants;

import java.io.File;
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
}
