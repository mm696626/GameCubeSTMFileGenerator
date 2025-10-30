package ui;

import io.STMGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GamecubeSTMFileGeneratorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, generateSTM, fixNonLoopingSTMHeader;
    private String leftChannelPath = "";
    private String rightChannelPath = "";

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;

    public GamecubeSTMFileGeneratorUI() {
        setTitle("GameCube STM File Generator");
        generateUI();
    }

    private void generateUI() {
        JPanel stmGeneratorPanel = new JPanel();
        stmGeneratorPanel.setLayout(new BoxLayout(stmGeneratorPanel, BoxLayout.Y_AXIS));

        JPanel stmPanel = new JPanel(new GridBagLayout());
        GridBagConstraints stmGBC = new GridBagConstraints();
        stmGBC.insets = new Insets(5, 5, 5, 5);
        stmGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        generateSTM = new JButton("Generate STM");
        generateSTM.addActionListener(this);

        fixNonLoopingSTMHeader = new JButton("Fix Nonlooping STM Header");
        fixNonLoopingSTMHeader.addActionListener(this);

        stmGBC.gridx = 0; stmGBC.gridy = 0;
        stmPanel.add(pickLeftChannel, stmGBC);
        stmGBC.gridx = 1;
        stmPanel.add(leftChannelLabel, stmGBC);

        stmGBC.gridx = 0; stmGBC.gridy = 1;
        stmPanel.add(pickRightChannel, stmGBC);
        stmGBC.gridx = 1;
        stmPanel.add(rightChannelLabel, stmGBC);

        stmGBC.gridx = 0; stmGBC.gridy = 2;
        stmPanel.add(generateSTM, stmGBC);

        stmGBC.gridx = 1; stmGBC.gridy = 2;
        stmPanel.add(fixNonLoopingSTMHeader, stmGBC);

        stmGeneratorPanel.add(stmPanel);

        setLayout(new BorderLayout());
        add(stmGeneratorPanel, BorderLayout.CENTER);
    }

    private void chooseDSP(boolean isLeft) {
        JFileChooser dspFileChooser = new JFileChooser();

        if (isLeft) {
            dspFileChooser.setDialogTitle("Select DSP Left Channel");
        }
        else {
            dspFileChooser.setDialogTitle("Select DSP Right Channel");
        }

        dspFileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
        dspFileChooser.setFileFilter(dspFilter);

        int userSelection = dspFileChooser.showOpenDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = dspFileChooser.getSelectedFile();

        if (isLeft) {
            leftChannelPath = selectedFile.getAbsolutePath();
            leftChannelLabel.setText(selectedFile.getName());
        }

        else {
            rightChannelPath = selectedFile.getAbsolutePath();
            rightChannelLabel.setText(selectedFile.getName());
        }

        File otherChannel = detectOtherChannel(selectedFile, isLeft);
        if (otherChannel != null) {
            if (isLeft) {
                rightChannelPath = otherChannel.getAbsolutePath();
                rightChannelLabel.setText(otherChannel.getName());
            }
            else {
                leftChannelPath = otherChannel.getAbsolutePath();
                leftChannelLabel.setText(otherChannel.getName());
            }
        }
    }

    private File detectOtherChannel(File selectedFile, boolean isLeftSelected) {
        String fileName = selectedFile.getName();
        File parentDir = selectedFile.getParentFile();

        String otherChannelName = null;

        if (fileName.endsWith("_L.dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("_L.dsp", "_R.dsp");
        } else if (fileName.endsWith("_R.dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("_R.dsp", "_L.dsp");
        } else if (fileName.endsWith("(channel 0).dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("(channel 0).dsp", "(channel 1).dsp");
        } else if (fileName.endsWith("(channel 1).dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("(channel 1).dsp", "(channel 0).dsp");
        }

        if (otherChannelName != null) {
            File otherChannelFile = new File(parentDir, otherChannelName);
            if (otherChannelFile.exists()) {
                return otherChannelFile;
            }
        }

        return null;
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_ ]", "");
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickLeftChannel) {
            chooseDSP(true);
        }

        if (e.getSource() == pickRightChannel) {
            chooseDSP(false);
        }

        if (e.getSource() == generateSTM) {
            if (leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel wasn't chosen!");
                return;
            }

            File leftChannelFile = new File(leftChannelPath);
            File rightChannelFile = new File(rightChannelPath);

            if (!leftChannelFile.exists() || !rightChannelFile.exists()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel doesn't exist!");
                return;
            }

            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setDialogTitle("Save STM File");
            saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            saveFileChooser.setAcceptAllFileFilterUsed(false);
            saveFileChooser.setFileFilter(new FileNameExtensionFilter("STM Files", "stm"));

            int userSelection = saveFileChooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File selectedFile = saveFileChooser.getSelectedFile();
            String sanitizedFileName = sanitizeFileName(selectedFile.getName());

            if (!sanitizedFileName.toLowerCase().endsWith(".stm")) {
                sanitizedFileName += ".stm";
            }

            File outputSTMFile = new File(selectedFile.getParentFile(), sanitizedFileName);
            STMGenerator.generateSTM(leftChannelFile, rightChannelFile, outputSTMFile);
        }

        if (e.getSource() == fixNonLoopingSTMHeader) {

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "This is intended only for nonlooping STM files. Are you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            JFileChooser stmFileChooser = new JFileChooser();
            stmFileChooser.setDialogTitle("Choose STM File");
            stmFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            stmFileChooser.setAcceptAllFileFilterUsed(false);
            stmFileChooser.setFileFilter(new FileNameExtensionFilter("STM Files", "stm"));

            int userSelection = stmFileChooser.showOpenDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File stmFile = stmFileChooser.getSelectedFile();

            STMGenerator.fixNonLoopingSTMHeader(stmFile);
        }
    }
}