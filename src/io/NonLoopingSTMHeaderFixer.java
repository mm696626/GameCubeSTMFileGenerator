package io;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;

public class NonLoopingSTMHeaderFixer {

    public static boolean fixNonLoopingSTMHeader(File stmFile) {
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

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
            return false;
        }
    }
}
