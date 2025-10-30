package ui;

import constants.STMFileNames;
import io.STMGenerator;
import uihelpers.DSPPair;
import uihelpers.GenerateJob;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class GamecubeSTMFileGeneratorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, generateSTM, fixNonLoopingSTMHeader, fixNonLoopingSTMHeaderFolder;
    private String leftChannelPath = "";
    private String rightChannelPath = "";

    private File savedDSPFolder;
    private File defaultSavedDSPFolder;

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;
    private JLabel defaultDSPFolderLabel;

    private JComboBox<String> gameSelector;
    private JComboBox<String> songSelector;

    private DefaultListModel<GenerateJob> jobQueueModel;
    private JList<GenerateJob> jobQueueList;
    private JButton addToQueueButton, removeQueueButton, clearQueueButton, runBatchButton;

    private JCheckBox autoAddToQueue;


    public GamecubeSTMFileGeneratorUI() {
        setTitle("GameCube STM File Generator");
        initSettingsFile();
        loadSettingsFile();
        generateUI();
    }

    private void generateUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel stmGeneratorPanel = new JPanel();
        stmGeneratorPanel.setLayout(new BoxLayout(stmGeneratorPanel, BoxLayout.Y_AXIS));

        JPanel gameSongPanel = new JPanel(new GridBagLayout());
        gameSongPanel.setBorder(BorderFactory.createTitledBorder("Game and Song Selection"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gameSongPanel.add(new JLabel("Game:"), gbc);

        gameSelector = new JComboBox<>(new String[]{"Paper Mario: The Thousand-Year Door", "Fire Emblem: Path of Radiance", "Cubivore"});
        gameSelector.addActionListener(e -> updateSongList());
        gbc.gridx = 1;
        gameSongPanel.add(gameSelector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gameSongPanel.add(new JLabel("Song:"), gbc);

        songSelector = new JComboBox<>(STMFileNames.PAPER_MARIO_TTYD_FILE_NAMES);
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

        fixNonLoopingSTMHeaderFolder = new JButton("Fix Nonlooping STM Headers (Folder)");
        fixNonLoopingSTMHeaderFolder.addActionListener(this);

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
        stmGBC.gridx = 1;
        stmPanel.add(fixNonLoopingSTMHeader, stmGBC);
        stmGBC.gridx = 2;
        stmPanel.add(fixNonLoopingSTMHeaderFolder, stmGBC);

        autoAddToQueue = new JCheckBox("Automatically Add DSP Pairs from DSP Folder to Queue");
        stmGBC.gridx = 0; stmGBC.gridy = 3;
        stmPanel.add(autoAddToQueue, stmGBC);

        stmGeneratorPanel.add(gameSongPanel);
        stmGeneratorPanel.add(Box.createVerticalStrut(10));
        stmGeneratorPanel.add(stmPanel);

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

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints settingsGBC = new GridBagConstraints();
        settingsGBC.insets = new Insets(5, 5, 5, 5);
        settingsGBC.fill = GridBagConstraints.HORIZONTAL;

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 0;
        settingsPanel.add(new JLabel("Default DSP Folder:"), settingsGBC);

        defaultDSPFolderLabel = new JLabel(defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultDSPFolderLabel, settingsGBC);

        JButton chooseDefaultDSPButton = new JButton("Change");
        chooseDefaultDSPButton.addActionListener(e -> chooseDefaultDSPFolder());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultDSPButton, settingsGBC);

        JButton resetGeneratorSettingsButton = new JButton("Reset Generator Settings");
        resetGeneratorSettingsButton.addActionListener(e -> resetGeneratorSettings());
        settingsGBC.gridx = 0;
        settingsGBC.gridy = 1;
        settingsGBC.gridwidth = 3;
        settingsPanel.add(resetGeneratorSettingsButton, settingsGBC);

        tabbedPane.addTab("STM Generator", stmGeneratorPanel);
        tabbedPane.addTab("Settings", settingsPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initSettingsFile() {
        File settingsFile = new File("settings.txt");
        PrintWriter outputStream;
        if (!settingsFile.exists()) {
            try {
                outputStream = new PrintWriter(new FileOutputStream(settingsFile));
            }
            catch (FileNotFoundException f) {
                return;
            }

            outputStream.println("defaultSavedDSPFolder:None");
            outputStream.close();
        }
    }

    private void loadSettingsFile() {
        File settingsFile = new File("settings.txt");
        try (Scanner inputStream = new Scanner(new FileInputStream(settingsFile))) {
            while (inputStream.hasNextLine()) {
                String line = inputStream.nextLine();
                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;
                String key = parts[0];
                String value = parts[1];

                if (key.equals("defaultSavedDSPFolder")) {
                    if (!value.equals("None")) defaultSavedDSPFolder = new File(value);
                }
            }

            if (defaultSavedDSPFolder != null && defaultSavedDSPFolder.exists()) {
                savedDSPFolder = defaultSavedDSPFolder;
            }

        } catch (FileNotFoundException e) {
            return;
        }
    }

    private void chooseDefaultDSPFolder() {
        JFileChooser defaultDSPFolderChooser = new JFileChooser();
        defaultDSPFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        defaultDSPFolderChooser.setDialogTitle("Select Default DSP Folder");
        defaultDSPFolderChooser.setAcceptAllFileFilterUsed(false);
        int result = defaultDSPFolderChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            defaultSavedDSPFolder = defaultDSPFolderChooser.getSelectedFile();
            defaultDSPFolderLabel.setText(defaultSavedDSPFolder.getAbsolutePath());
            savedDSPFolder = defaultSavedDSPFolder;
            saveSettingsToFile();
        }
    }

    private void saveSettingsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:" + (defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + e.getMessage());
        }
    }

    private void resetGeneratorSettings() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset the generator settings?",
                "Confirm Reset Settings",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        defaultSavedDSPFolder = null;

        if (defaultDSPFolderLabel != null) {
            defaultDSPFolderLabel.setText("None");
        }

        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:None");
            JOptionPane.showMessageDialog(this, "Generator has been reset.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to reset generator: " + e.getMessage());
        }
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

    private void useSavedDSPFolder() {
        if (savedDSPFolder == null) return;

        ArrayList<DSPPair> dspPairs = DSPPair.detectDSPPairs(savedDSPFolder);

        if (dspPairs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching DSP pairs found in the saved folder.");
            savedDSPFolder = null;
            return;
        }

        DSPPair selectedPair = showSearchableDSPDialog(dspPairs);
        if (selectedPair != null) {
            leftChannelPath = selectedPair.getLeft().getAbsolutePath();
            rightChannelPath = selectedPair.getRight().getAbsolutePath();
            leftChannelLabel.setText(selectedPair.getLeft().getName());
            rightChannelLabel.setText(selectedPair.getRight().getName());

            if (autoAddToQueue.isSelected()) {
                addToQueue();
            }
        }
    }

    private DSPPair showSearchableDSPDialog(ArrayList<DSPPair> dspPairs) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select DSP Pair", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JTextField searchField = new JTextField();
        dialog.add(searchField, BorderLayout.NORTH);

        DefaultListModel<DSPPair> listModel = new DefaultListModel<>();
        dspPairs.forEach(listModel::addElement);

        JList<DSPPair> dspList = new JList<>(listModel);
        dspList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dspList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DSPPair pair) {
                    setText(pair.getLeft().getName() + " / " + pair.getRight().getName());
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dspList);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String filter = searchField.getText().trim().toLowerCase();
                listModel.clear();
                for (DSPPair pair : dspPairs) {
                    String name = pair.getLeft().getName().toLowerCase() + " " + pair.getRight().getName().toLowerCase();
                    if (name.contains(filter)) listModel.addElement(pair);
                }
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        final DSPPair[] selectedPair = {null};

        dspList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && dspList.getSelectedValue() != null) {
                    selectedPair[0] = dspList.getSelectedValue();
                    dialog.dispose();
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && dspList.getSelectedValue() != null) {
                    selectedPair[0] = dspList.getSelectedValue();
                    dialog.dispose();
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                int size = dspList.getModel().getSize();
                int index = dspList.getSelectedIndex();

                if (code == KeyEvent.VK_DOWN) {
                    if (size > 0) {
                        if (index < size - 1) dspList.setSelectedIndex(index + 1);
                        else dspList.setSelectedIndex(0);
                        dspList.ensureIndexIsVisible(dspList.getSelectedIndex());
                    }
                    e.consume();
                } else if (code == KeyEvent.VK_UP) {
                    if (size > 0) {
                        if (index > 0) dspList.setSelectedIndex(index - 1);
                        else dspList.setSelectedIndex(size - 1);
                        dspList.ensureIndexIsVisible(dspList.getSelectedIndex());
                    }
                    e.consume();
                }
            }
        });

        okButton.addActionListener(e -> {
            selectedPair[0] = dspList.getSelectedValue();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dspList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && dspList.getSelectedValue() != null) {
                    selectedPair[0] = dspList.getSelectedValue();
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
        return selectedPair[0];
    }

    private void chooseLeftChannelPath() {
        if (savedDSPFolder != null) {
            useSavedDSPFolder();
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Would you like to pick a folder of DSPs to select a song from?\n(Your choice will be remembered until closing the program or if you set a default folder)",
                "Choose DSP Folder",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            JFileChooser dspFolderChooser = new JFileChooser();
            dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dspFolderChooser.setDialogTitle("Select Folder with DSP Files");
            dspFolderChooser.setAcceptAllFileFilterUsed(false);

            int folderSelected = dspFolderChooser.showOpenDialog(this);
            if (folderSelected == JFileChooser.APPROVE_OPTION) {
                savedDSPFolder = dspFolderChooser.getSelectedFile();
                useSavedDSPFolder();
            }
        } else {
            chooseDSP(true);
        }
    }

    private void chooseRightChannelPath() {
        if (savedDSPFolder != null) {
            useSavedDSPFolder();
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Would you like to pick a folder of DSPs to select a song from?\n(Your choice will be remembered until closing the program or if you set a default folder)",
                "Choose DSP Folder",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            JFileChooser dspFolderChooser = new JFileChooser();
            dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dspFolderChooser.setDialogTitle("Select Folder with DSP Files");
            dspFolderChooser.setAcceptAllFileFilterUsed(false);

            int folderSelected = dspFolderChooser.showOpenDialog(this);
            if (folderSelected == JFileChooser.APPROVE_OPTION) {
                savedDSPFolder = dspFolderChooser.getSelectedFile();
                useSavedDSPFolder();
            }
        } else {
            chooseDSP(false);
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
        } else if (fileName.endsWith("_l.dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("_l.dsp", "_r.dsp");
        } else if (fileName.endsWith("_r.dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("_r.dsp", "_l.dsp");
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
            chooseLeftChannelPath();
        }

        if (e.getSource() == pickRightChannel) {
            chooseRightChannelPath();
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

            String selectedGame = (String) gameSelector.getSelectedItem();
            if (selectedGame == null || selectedGame.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a game before generating.");
                return;
            }

            selectedGame = selectedGame.replaceAll("[^a-zA-Z0-9]", "_");

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

            boolean generatedSuccessfully = STMGenerator.generateSTM(leftChannelFile, rightChannelFile, outputSTMFile, selectedSong, selectedGame);

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

            boolean successful = STMGenerator.fixNonLoopingSTMHeader(stmFile);

            if (successful) {
                JOptionPane.showMessageDialog(null, "STM header fixed successfully!");
            }
        }

        if (e.getSource() == fixNonLoopingSTMHeaderFolder) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "This is intended only for a folder of nonlooping STM files. Are you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            JFileChooser stmFolderChooser = new JFileChooser();
            stmFolderChooser.setDialogTitle("Select STM Folder");
            stmFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            stmFolderChooser.setAcceptAllFileFilterUsed(false);

            int userSelection = stmFolderChooser.showOpenDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File stmFolder = stmFolderChooser.getSelectedFile();
            File[] stmFiles = stmFolder.listFiles((_, name) -> name.toLowerCase().endsWith(".stm"));

            if (stmFiles == null || stmFiles.length == 0) {
                JOptionPane.showMessageDialog(this, "No STM files found in the selected folder.", "No Files", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (File stmFile : stmFiles) {
                try {
                    STMGenerator.fixNonLoopingSTMHeader(stmFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error processing file: " + stmFile.getName(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(null, "STM headers fixed successfully!");
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


                String selectedGame = (String) gameSelector.getSelectedItem();

                if (selectedGame == null || selectedGame.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select a game before generating.");
                    return;
                }

                selectedGame = selectedGame.replaceAll("[^a-zA-Z0-9]", "_");

                String selectedSong = generateJob.getSongFileName();
                File outputSTMFile = new File(outputDir, selectedSong);

                if (outputSTMFile.exists()) {
                    outputSTMFile.delete();
                }

                boolean generatedSuccessfully = STMGenerator.generateSTM(leftDSP, rightDSP, outputSTMFile, selectedSong, selectedGame);

                if (!generatedSuccessfully) {
                    JOptionPane.showMessageDialog(null, "Something went wrong with the job for " + generateJob.getSongFileName());
                }
            }

            JOptionPane.showMessageDialog(this, "Batch process completed.");
            jobQueueModel.clear();
        }
    }
}