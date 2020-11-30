import model.FingerprintLib;

import java.io.File;
import java.io.IOException;

public class IndexMusicLibrary {
    public static void main(String[] args) throws InterruptedException, IOException {
        String dataDir = "music/";

        saveFingerprintsForDir(dataDir);
    }

    private static void saveFingerprintsForDir(String dataDir) throws IOException, InterruptedException {
        File[] files = new File(dataDir).listFiles();
        for (File f : files) {
            if (f.getName().endsWith(".wav")) {
                System.out.println("Processing: " + f.getName());

                FingerprintLib.saveSongFingerPrint(dataDir + f.getName());

                System.out.println("*********************************************************************************");
            } else {
                System.out.println("Skipping file: " + f.getName());
            }
        }
    }


}
