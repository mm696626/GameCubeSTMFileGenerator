package ui;

import constants.STMFileNames;
import io.STMGenerator;
import uihelpers.GenerateJob;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

public class GamecubeSTMFileGeneratorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, generateSTM, fixNonLoopingSTMHeader;
    private String leftChannelPath = "";
    private String rightChannelPath = "";

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;

    private JComboBox<String> gameSelector;
    private JComboBox<String> songSelector;

    private DefaultListModel<GenerateJob> jobQueueModel;
    private JList<GenerateJob> jobQueueList;
    private JButton addToQueueButton, removeQueueButton, clearQueueButton, runBatchButton;


    public GamecubeSTMFileGeneratorUI() {
        setTitle("GameCube STM File Generator");
        generateUI();
    }

    private void generateUI() {
        JPanel stmGeneratorPanel = new JPanel();
        stmGeneratorPanel.setLayout(new BoxLayout(stmGeneratorPanel, BoxLayout.Y_AXIS));

        JPanel gameSongPanel = new JPanel(new GridBagLayout());
        gameSongPanel.setBorder(BorderFactory.createTitledBorder("Game and Song Selection"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gameSongPanel.add(new JLabel("Game:"), gbc);


        gameSelector = new JComboBox<>(new String[]{"Paper Mario: The Thousand-Year Door", "Fire Emblem: Path of Radiance", "Cubivore"});
        gameSelector.addActionListener(e -> updateSongList());

        songSelector = new JComboBox<>(STMFileNames.PAPER_MARIO_TTYD_FILE_NAMES);

        gbc.gridx = 1;
        gameSongPanel.add(gameSelector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gameSongPanel.add(new JLabel("Song:"), gbc);

        gbc.gridx = 1;
        gameSongPanel.add(songSelector, gbc);

        JPanel stmPanel = new JPanel(new GridBagLayout());
        stmPanel.setBorder(BorderFactory.createTitledBorder("DSP Selection/STM Generation"));
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

        stmGeneratorPanel.add(gameSongPanel);
        stmGeneratorPanel.add(Box.createVerticalStrut(10));
        stmGeneratorPanel.add(stmPanel);

        setLayout(new BorderLayout());
        add(stmGeneratorPanel, BorderLayout.CENTER);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Batch Generation Job Queue"));

        jobQueueModel = new DefaultListModel<>();
        jobQueueList = new JList<>(jobQueueModel);
        jobQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(jobQueueList);

        JPanel queueButtonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        addToQueueButton = new JButton("Add");
        removeQueueButton = new JButton("Remove");
        clearQueueButton = new JButton("Clear All");
        runBatchButton = new JButton("Run Batch");

        addToQueueButton.addActionListener(this);
        removeQueueButton.addActionListener(this);
        clearQueueButton.addActionListener(this);
        runBatchButton.addActionListener(this);

        queueButtonPanel.add(addToQueueButton);
        queueButtonPanel.add(removeQueueButton);
        queueButtonPanel.add(clearQueueButton);
        queueButtonPanel.add(runBatchButton);

        queuePanel.add(scrollPane, BorderLayout.CENTER);
        queuePanel.add(queueButtonPanel, BorderLayout.SOUTH);

        stmGeneratorPanel.add(Box.createVerticalStrut(10));
        stmGeneratorPanel.add(queuePanel);
    }

    private void updateSongList() {
        String[] songNameArray = getSongArrayForSelectedGame();

        if (songSelector == null) {
            songSelector = new JComboBox<>();
        }

        if (songNameArray != null) {
            Arrays.sort(songNameArray);
            songSelector.setModel(new DefaultComboBoxModel<>(songNameArray));
        } else {
            songSelector.setModel(new DefaultComboBoxModel<>(new String[]{}));
        }

        songSelector.revalidate();
        songSelector.repaint();
    }

    private String[] getSongArrayForSelectedGame() {
        String selectedGame = (String) gameSelector.getSelectedItem();
        if (selectedGame == null) return null;

        switch (selectedGame) {
            case "Cubivore":
                return STMFileNames.CUBIVORE_FILE_NAMES;
            case "Fire Emblem: Path of Radiance":
                return STMFileNames.FIRE_EMBLEM_POR_FILE_NAMES;
            default:
                return STMFileNames.PAPER_MARIO_TTYD_FILE_NAMES;
        }
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

    private void addToQueue() {
        String songFileName = (String) songSelector.getSelectedItem();
        if (songFileName == null || leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select song and both DSP channels before adding.");
            return;
        }

        jobQueueModel.addElement(new GenerateJob(songFileName, leftChannelPath, rightChannelPath));
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

            String selectedSong = (String) songSelector.getSelectedItem();
            if (selectedSong == null || selectedSong.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a song name before generating.");
                return;
            }

            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setDialogTitle("Select Output Folder");
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            folderChooser.setAcceptAllFileFilterUsed(false);

            int userSelection = folderChooser.showOpenDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File outputDir = folderChooser.getSelectedFile();

            File outputSTMFile = new File(outputDir, selectedSong);

            if (outputSTMFile.exists()) {
                outputSTMFile.delete();
            }

            boolean generatedSuccessfully = STMGenerator.generateSTM(leftChannelFile, rightChannelFile, outputSTMFile);

            if (generatedSuccessfully) {
                JOptionPane.showMessageDialog(null, "STM file generated successfully!");
            }
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

        if (e.getSource() == addToQueueButton) {
            addToQueue();
        }

        if (e.getSource() == removeQueueButton) {
            int selectedIndex = jobQueueList.getSelectedIndex();
            if (selectedIndex != -1) {
                jobQueueModel.remove(selectedIndex);
            }
        }

        if (e.getSource() == clearQueueButton) {
            jobQueueModel.clear();
        }

        if (e.getSource() == runBatchButton) {
            if (jobQueueModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Queue is empty!");
                return;
            }

            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setDialogTitle("Select Output Folder");
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            folderChooser.setAcceptAllFileFilterUsed(false);

            int userSelection = folderChooser.showOpenDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File outputDir = folderChooser.getSelectedFile();

            for (int i = 0; i < jobQueueModel.size(); i++) {
                GenerateJob generateJob = jobQueueModel.getElementAt(i);


                File leftDSP = new File(generateJob.getLeftDSP());
                File rightDSP = new File(generateJob.getRightDSP());

                if (!leftDSP.exists() || !rightDSP.exists()) {
                    JOptionPane.showMessageDialog(this, "DSP files for " + generateJob.getSongFileName() + " not found. Skipping.");
                    continue;
                }

                String selectedSong = generateJob.getSongFileName();
                File outputSTMFile = new File(outputDir, selectedSong);

                if (outputSTMFile.exists()) {
                    outputSTMFile.delete();
                }

                boolean generatedSuccessfully = STMGenerator.generateSTM(leftDSP, rightDSP, outputSTMFile);

                if (!generatedSuccessfully) {
                    JOptionPane.showMessageDialog(null, "Something went wrong with the job for " + generateJob.getSongFileName());
                }
            }

            JOptionPane.showMessageDialog(this, "Batch process completed.");
            jobQueueModel.clear();
        }
    }
}