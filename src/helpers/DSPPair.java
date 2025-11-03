package helpers;

import io.STMHeaderLoopChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DSPPair {

    private File left;
    private File right;
    private String displayName;

    public DSPPair(File left, File right, String displayName) {
        this.left = left;
        this.right = right;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }


    public static ArrayList<DSPPair> detectDSPPairs(File folder) {
        ArrayList<DSPPair> pairs = new ArrayList<>();

        File[] files = folder.listFiles((_, name) -> name.toLowerCase().endsWith(".dsp"));
        if (files == null) return pairs;

        Map<String, File> fileMap = new HashMap<>();
        for (File f : files) {
            fileMap.put(f.getName(), f);
        }

        for (File f : files) {
            String name = f.getName();
            DSPPair pair = null;

            if (name.endsWith("_L.dsp")) {
                String rightName = name.replace("_L.dsp", "_R.dsp");
                if (fileMap.containsKey(rightName)) {
                    pair = new DSPPair(f, fileMap.get(rightName), name.replace("_L.dsp", ""));
                }
            } else if (name.endsWith("_l.dsp")) {
                String rightName = name.replace("_l.dsp", "_r.dsp");
                if (fileMap.containsKey(rightName)) {
                    pair = new DSPPair(f, fileMap.get(rightName), name.replace("_l.dsp", ""));
                }
            } else if (name.endsWith("(channel 0).dsp")) {
                String rightName = name.replace("(channel 0).dsp", "(channel 1).dsp");
                if (fileMap.containsKey(rightName)) {
                    pair = new DSPPair(f, fileMap.get(rightName), name.replace(" (channel 0).dsp", ""));
                }
            }

            if (pair != null) {
                if (STMHeaderLoopChecker.isValidLoopStart(pair.getLeft()) &&
                        STMHeaderLoopChecker.isValidLoopStart(pair.getRight())) {
                    pairs.add(pair);
                }
            }
        }

        return pairs;
    }

    public File getLeft() {
        return left;
    }

    public File getRight() {
        return right;
    }
}