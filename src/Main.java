// GameCube STM File Generator by Matt McCullough
// A tool to generate STM files for GameCube games (such as Paper Mario TTYD)

import ui.GamecubeSTMFileGeneratorUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        GamecubeSTMFileGeneratorUI gamecubeSTMFileGeneratorUI = new GamecubeSTMFileGeneratorUI();
        gamecubeSTMFileGeneratorUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gamecubeSTMFileGeneratorUI.pack();
        gamecubeSTMFileGeneratorUI.setVisible(true);
    }
}