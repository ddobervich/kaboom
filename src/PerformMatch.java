import model.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class PerformMatch {
    private static final int WINDOW_SIZE = 4096;

    public static void main(String[] args) {
        HashMap<Integer, List<Match>> database = buildDatabase("prints/");
        performMatch(database);
    }

    private static void performMatch(HashMap<Integer, List<Match>> db) {
        HashMap<String, Integer> songHits = new HashMap<>();

        int totalHits = 0;

        ArrayList<Complex[]> fftFrames = new ArrayList<Complex[]>();
        ArrayList<int[]> keyFreqList = new ArrayList<>();

        AudioReader reader = AudioReader.getMicStream(5000);
        ByteArrayOutputStream out = reader.getOutputStream();

        System.out.println("Loading audio");
        while (!reader.isRunning()) {
            System.out.println("Waiting for audio stream...");
        }

        System.out.print("Reading audio");
        int currentIndex = 0;
        while (reader.isRunning()) {
            byte audioData[] = out.toByteArray();

            // Convert batch of audio data into FFT frames
            if (audioData.length > WINDOW_SIZE) {
                out.reset();
                FFT.performFFT(audioData, WINDOW_SIZE, fftFrames);
            }

            // Process results from fftFrames list
            if (fftFrames.size() > 0) {
                Complex[] results = fftFrames.remove(0);

                int[] keyFreq = FingerprintLib.getKeyFrequenciesFor(results);
                System.out.println(currentIndex + " : " + Arrays.toString(keyFreq));

                keyFreqList.add(keyFreq);
            }

            // build songHits list
            if (keyFreqList.size() > currentIndex) {
                int[] firstRow = keyFreqList.get(currentIndex);

                int hash = gethashFor(firstRow);
                if (db.containsKey(hash)) {
                    List<Match> currentMatchList = db.get(hash);
                    totalHits += currentMatchList.size();
                    recordMatch(songHits, currentMatchList);
                    printResultsSoFar(songHits);
                }

                currentIndex++;
            }
        }
    }

    private static void printResultsSoFar(HashMap<String, Integer> songHits) {
        for (String song : songHits.keySet()) {
            int numHits = songHits.get(song);
            System.out.println(song + "\t :: " + numHits);
        }

        System.out.println("*******************************************************");
    }

    private static void recordMatch(HashMap<String, Integer> songHits, List<Match> currentMatchList) {
        for (Match m : currentMatchList) {
            if (songHits.containsKey(m.getFileName())) {
                int n = songHits.get(m.getFileName());
                songHits.put(m.getFileName(), n + 1);
            } else {
                songHits.put(m.getFileName(), 1);
            }
        }
    }

    private static HashMap<Integer, List<Match>> buildDatabase(String dataDir) {
        HashMap<Integer, List<Match>> db = new HashMap<>();

        File[] files = new File(dataDir).listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".csv")) {
                System.out.println("Processing: " + file.getName());

                List<int[]> keyFreqList = loadFingerprintsFrom(file);
                loadDbWithPrints(db, keyFreqList, file.getName());

                System.out.println("********************LOADED!***************************");
            } else {
                System.out.println("Skipping file: " + file.getName());
            }
        }

        return db;
    }

    private static void loadDbWithPrints(HashMap<Integer, List<Match>> db, List<int[]> keyFreqList, String songName) {
        for (int i = 0; i < keyFreqList.size() - 1; i++) {
            int[] firstRow = keyFreqList.get(i);

            int hash = gethashFor(firstRow);
            Match match = new Match(songName, i);
            store(db, hash, match);
        }
    }

    // TODO: cheap easy to program hash; replace with better one later
    private static int gethashFor(int[] firstRow) {
        String s1 = Arrays.toString(firstRow);
        String val = s1;
        return val.hashCode();
    }

    private static void store(HashMap<Integer, List<Match>> db, int hash, Match match) {
        if (db.containsKey(hash)) {
            List<Match> matches = db.get(hash);
            matches.add(match);
        } else {
            List<Match> matches = new ArrayList<>();
            matches.add(match);
            db.put(hash, matches);
        }
    }

    private static List<int[]> loadFingerprintsFrom(File f) {
        List<int[]> out = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            String line = br.readLine();
            while (line != null) {
                int[] datarow = getDataRow(line);
                out.add(datarow);
                line = br.readLine();
            }

        } catch (Exception errorObj) {
            System.err.println("There was a problem reading the file");
        }

        return out;
    }

    private static int[] getDataRow(String line) {
        String[] vals = line.split(",");
        int[] out = new int[vals.length];

        for (int i = 0; i < vals.length; i++) {
            int val = Integer.parseInt(vals[i]);
            out[i] = val;
        }

        return out;
    }

}