package io;

import constants.DSPFileConstants;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;

public class STMGenerator {

    public static void generateSTM(File leftChannel, File rightChannel, File outputSTMFile) {

        if (!isValidLoopStart(leftChannel) || !isValidLoopStart(rightChannel)) {
            JOptionPane.showMessageDialog(null, "One or both of your channels has an invalid loop start for the STM format!");
            return;
        }

        try (RandomAccessFile stmRaf = new RandomAccessFile(outputSTMFile, "rw")) {

            //version number (always 2)
            stmRaf.write(0x02);
            stmRaf.write(0x00);

            //grab sample rate from DSP
            byte[] stmSampleRate = new byte[2];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.SAMPLE_RATE_OFFSET + 2); //only need lower 2 bytes
                leftChannelRaf.read(stmSampleRate);
            }

            //write sample rate
            stmRaf.write(stmSampleRate);

            //will always be stereo, so write 2
            stmRaf.writeInt(2);

            int leftChannelAudioLength = (int)leftChannel.length() - 0x60;
            int leftChannelBlockLength;

            if (leftChannelAudioLength % 0x20 != 0) {
                leftChannelBlockLength = leftChannelAudioLength + (0x20 - (leftChannelAudioLength % 0x20));
            }
            else {
                leftChannelBlockLength = leftChannelAudioLength;
            }

            //grab loop start from DSP
            byte[] newLoopStart = new byte[DSPFileConstants.LOOP_START_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.LOOP_START_OFFSET);
                leftChannelRaf.read(newLoopStart);
            }

            long loopStartDSP = ((newLoopStart[0] & 0xFF) << 24) |
                    ((newLoopStart[1] & 0xFF) << 16) |
                    ((newLoopStart[2] & 0xFF) << 8)  |
                    (newLoopStart[3] & 0xFF);

            long loopStartSTM = (loopStartDSP-2)/2;

            stmRaf.writeInt((int)leftChannelBlockLength);
            stmRaf.writeInt((int)loopStartSTM);

            stmRaf.writeInt((int)leftChannelBlockLength);
            stmRaf.writeInt((int)leftChannelBlockLength);

            stmRaf.writeInt((int)loopStartSTM);
            stmRaf.writeInt((int)loopStartSTM);

            //write last 0x20 bytes of padding for STM header
            for (int i=0; i<0x20; i++) {
                stmRaf.write(0x00);
            }

            //read and write DSP channel headers
            byte[] leftChannelHeader = new byte[0x60];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(0x0);
                leftChannelRaf.read(leftChannelHeader);
            }

            byte[] rightChannelHeader = new byte[0x60];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(rightChannel, "r")) {
                leftChannelRaf.seek(0x0);
                leftChannelRaf.read(rightChannelHeader);
            }

            stmRaf.write(leftChannelHeader);
            stmRaf.write(rightChannelHeader);

            //read and write left channel audio data
            byte[] newDSPLeftChannelAudio;

            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(0x60);
                long remainingBytes = leftChannelRaf.length() - 0x60;
                newDSPLeftChannelAudio = new byte[(int) remainingBytes];
                leftChannelRaf.readFully(newDSPLeftChannelAudio);
            }

            stmRaf.write(newDSPLeftChannelAudio);

            //pad left channel audio to 0x20 boundary
            if (leftChannelAudioLength % 0x20 != 0) {
                long bytesToWrite = 0x20 - (leftChannelAudioLength % 0x20);

                for (int i=0; i<bytesToWrite; i++) {
                    stmRaf.write(0);
                }
            }

            //write 0x20 bytes of padding between channels
            for (int i=0; i<0x20; i++) {
                stmRaf.write(0);
            }

            //read and write right channel audio data
            byte[] newDSPRightChannelAudio;

            try (RandomAccessFile rightChannelRaf = new RandomAccessFile(rightChannel, "r")) {
                rightChannelRaf.seek(0x60);
                long remainingBytes = rightChannelRaf.length() - 0x60;
                newDSPRightChannelAudio = new byte[(int) remainingBytes];
                rightChannelRaf.readFully(newDSPRightChannelAudio);
            }

            stmRaf.write(newDSPRightChannelAudio);

            //pad right channel audio to 0x20 boundary
            if (leftChannelAudioLength % 0x20 != 0) {
                long bytesToWrite = 0x20 - (leftChannelAudioLength % 0x20);

                for (int i=0; i<bytesToWrite; i++) {
                    stmRaf.write(0);
                }
            }

            //write last 0x8000 bytes of padding all STM files have
            for (int i=0; i<0x8000; i++) {
                stmRaf.write(0);
            }

            JOptionPane.showMessageDialog(null, "STM file generated successfully!");

        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }

    public static void fixNonLoopingSTMHeader(File stmFile) {
        try (RandomAccessFile stmRaf = new RandomAccessFile(stmFile, "rw")) {
            stmRaf.seek(0x0C);

            stmRaf.write(0xFF);
            stmRaf.write(0xFF);
            stmRaf.write(0xFF);
            stmRaf.write(0xFF);

            stmRaf.seek(0x18);

            stmRaf.write(0x00);
            stmRaf.write(0x00);
            stmRaf.write(0x00);
            stmRaf.write(0x00);

            stmRaf.seek(0x1C);

            stmRaf.write(0x00);
            stmRaf.write(0x00);
            stmRaf.write(0x00);
            stmRaf.write(0x00);

            JOptionPane.showMessageDialog(null, "STM header fixed successfully!");

        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }


    private static boolean isValidLoopStart(File dspChannel) {
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
