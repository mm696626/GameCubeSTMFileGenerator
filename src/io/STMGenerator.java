package io;

import constants.DSPFileConstants;

import javax.swing.*;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class STMGenerator {

    public static boolean generateSTM(File leftChannel, File rightChannel, File outputSTMFile, String songFileName, String selectedGame) {

        boolean isMono = rightChannel == null;

        if (!isMono) {
            if (!STMHeaderLoopChecker.isValidLoopStart(leftChannel) || !STMHeaderLoopChecker.isValidLoopStart(rightChannel)) {
                JOptionPane.showMessageDialog(null, "One or both of your channels for " + songFileName + " has an invalid loop start for the STM format!");
                return false;
            }
        }
        else {
            if (!STMHeaderLoopChecker.isValidLoopStart(leftChannel)) {
                JOptionPane.showMessageDialog(null, "Your mono DSP channel for " + songFileName + " has an invalid loop start for the STM format!");
                return false;
            }
        }

        if (outputSTMFile.exists()) {
            outputSTMFile.delete();
        }

        try (RandomAccessFile stmRaf = new RandomAccessFile(outputSTMFile, "rw")) {

            //use left channel here (since it's two halves of identically sized audio)
            int audioChannelLength = (int)leftChannel.length() - DSPFileConstants.DSP_HEADER_LENGTH_IN_BYTES;
            int audioChannelWithPaddingLength = getAudioChannelWithPaddingLength(audioChannelLength);

            writeSTMHeader(leftChannel, stmRaf, audioChannelWithPaddingLength, isMono);

            writeDSPHeaders(leftChannel, rightChannel, stmRaf);

            //read and write left channel audio data
            writeAudioChannel(leftChannel, stmRaf);

            //align left channel audio to 0x20 boundary
            alignAudioData(audioChannelLength, stmRaf);

            //write 0x20 bytes of padding between channels
            writePaddingBytes(stmRaf, 0x20);

            if (!isMono) {
                //read and write right channel audio data
                writeAudioChannel(rightChannel, stmRaf);

                //align right channel audio to 0x20 boundary
                alignAudioData(audioChannelLength, stmRaf);
            }

            //write last 0x8000 bytes of padding all STM files have
            writePaddingBytes(stmRaf, 0x8000);

            logSongReplacement(songFileName, leftChannel, rightChannel, selectedGame);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
            return false;
        }
    }

    private static void writeAudioChannel(File audioChannel, RandomAccessFile stmRaf) throws IOException {
        byte[] audioChannelData;

        try (RandomAccessFile audioChannelRaf = new RandomAccessFile(audioChannel, "r")) {
            audioChannelRaf.seek(DSPFileConstants.AUDIO_DATA_OFFSET);
            long remainingBytes = audioChannelRaf.length() - DSPFileConstants.AUDIO_DATA_OFFSET;
            audioChannelData = new byte[(int) remainingBytes];
            audioChannelRaf.readFully(audioChannelData);
        }

        stmRaf.write(audioChannelData);
    }

    private static void writeDSPHeaders(File leftChannel, File rightChannel, RandomAccessFile stmRaf) throws IOException {
        //read and write DSP channel headers
        byte[] leftChannelHeader = new byte[DSPFileConstants.DSP_HEADER_LENGTH_IN_BYTES];
        try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
            leftChannelRaf.seek(0x00);
            leftChannelRaf.read(leftChannelHeader);
        }

        stmRaf.write(leftChannelHeader);

        if (rightChannel != null) {
            byte[] rightChannelHeader = new byte[DSPFileConstants.DSP_HEADER_LENGTH_IN_BYTES];
            try (RandomAccessFile rightChannelRaf = new RandomAccessFile(rightChannel, "r")) {
                rightChannelRaf.seek(0x00);
                rightChannelRaf.read(rightChannelHeader);
            }

            stmRaf.write(rightChannelHeader);
        }
        else {
            writePaddingBytes(stmRaf, DSPFileConstants.DSP_HEADER_LENGTH_IN_BYTES);
        }
    }

    private static int getAudioChannelWithPaddingLength(int audioChannelLength) {
        int audioChannelWithPaddingLength;

        if (audioChannelLength % 0x20 != 0) {
            audioChannelWithPaddingLength = audioChannelLength + (0x20 - (audioChannelLength % 0x20));
        }
        else {
            audioChannelWithPaddingLength = audioChannelLength;
        }
        return audioChannelWithPaddingLength;
    }

    private static void writeSTMHeader(File leftChannel, RandomAccessFile stmRaf, int audioChannelWithPaddingLength, boolean isMono) throws IOException {
        //version number (always 512)
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

        if (!isMono) {
            //will mostly be stereo, so write 2
            stmRaf.writeInt(2);
        }
        else {
            //if it's mono, then write 1
            stmRaf.writeInt(1);
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

        long loopStartSTM = (loopStartDSP - 2)/2;

        stmRaf.writeInt(audioChannelWithPaddingLength);
        stmRaf.writeInt((int)loopStartSTM);

        stmRaf.writeInt(audioChannelWithPaddingLength);
        stmRaf.writeInt(audioChannelWithPaddingLength);

        stmRaf.writeInt((int)loopStartSTM);
        stmRaf.writeInt((int)loopStartSTM);

        //write last 0x20 bytes of padding for STM header
        writePaddingBytes(stmRaf, 0x20);
    }

    private static void alignAudioData(int leftChannelAudioLength, RandomAccessFile stmRaf) throws IOException {
        if (leftChannelAudioLength % 0x20 != 0) {
            long bytesToWrite = 0x20 - (leftChannelAudioLength % 0x20);
            writePaddingBytes(stmRaf, (int)bytesToWrite);
        }
    }

    private static void writePaddingBytes(RandomAccessFile stmRaf, int bytes) throws IOException {
        for (int i=0; i<bytes; i++) {
            stmRaf.write(0);
        }
    }

    private static void logSongReplacement(String songFileName, File leftChannel, File rightChannel, String selectedGame) {
        File songReplacementsFolder = new File("song_replacements");
        if (!songReplacementsFolder.exists()) {
            songReplacementsFolder.mkdirs();
        }

        File logFile = new File("song_replacements", selectedGame + ".txt");

        Map<String, String> songMap = new TreeMap<>();

        if (logFile.exists()) {
            try (Scanner inputStream = new Scanner(new FileInputStream(logFile))) {
                while (inputStream.hasNextLine()) {
                    String line = inputStream.nextLine();
                    String[] parts = line.split("\\|");

                    if (parts.length >= 3) {
                        String existingSongFileName = parts[0];
                        String left = parts[1];
                        String right = parts[2];
                        songMap.put(existingSongFileName, left + "|" + right);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (rightChannel != null) {
            songMap.put(songFileName, leftChannel.getName() + "|" + rightChannel.getName());
        }
        else {
            songMap.put(songFileName, leftChannel.getName() + "|" + "N/A");
        }

        try (PrintWriter outputStream = new PrintWriter(new FileOutputStream(logFile))) {
            for (Map.Entry<String, String> entry : songMap.entrySet()) {
                outputStream.println(entry.getKey() + "|" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
