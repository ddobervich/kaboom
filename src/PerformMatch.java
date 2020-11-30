import model.*;

import javax.swing.plaf.synth.SynthToggleButtonUI;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PerformMatch {
    private static final int CHUNK_SIZE = 4096;

    public static void main(String[] args) {
        HashMap<Long, List<Match>> database = buildDatabase("prints/", 0);
        performMatch(database,0);
    }

    private static void performMatch(HashMap<Long, List<Match>> db, int spacing) {
        HashMap<String, Integer> songHits = new HashMap<>();

        int totalHits = 0;

        ArrayList<Complex[]> fftResults = new ArrayList<Complex[]>();
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
            byte b[] = out.toByteArray();

            // Load results List when ready
            if (b.length > CHUNK_SIZE) {
                out.reset();
                FFT.performFFT(b, CHUNK_SIZE, fftResults);
            }

            // Process results list
            if (fftResults.size() > 0) {
                Complex[] results = fftResults.remove(0);

                if (results.length > 0) {
                    int[] keyFreq = FingerprintLib.getKeyFrequenciesFor(results);
                    System.out.println(currentIndex + " : " + Arrays.toString(keyFreq));

                    keyFreqList.add(keyFreq);
                }
            }

            // build songHits list
            if (keyFreqList.size() > currentIndex+spacing) {
                int[] firstRow = keyFreqList.get(currentIndex);
                int[] secondRow = keyFreqList.get(currentIndex+spacing);

                Long hash = gethashFor(firstRow, secondRow);
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
        for (Match m:currentMatchList) {
            if (songHits.containsKey(m.getFileName())) {
                int n = songHits.get(m.getFileName());
                songHits.put(m.getFileName(), n+1);
            } else {
                songHits.put(m.getFileName(), 1);
            }
        }
    }

    private static HashMap<Long, List<Match>> buildDatabase(String dataDir, int spacing) {
        HashMap<Long, List<Match>> db = new HashMap<>();

        File[] files = new File(dataDir).listFiles();
        for (File f : files) {
            if (f.getName().endsWith(".csv")) {
                System.out.println("Processing: " + f.getName());

                List<int[]> keyFreqList = loadFreqList(f);
                System.out.println("Done parsing file.  Loading results into HashMap.");

                loadDbWithPrintsFrom(db, keyFreqList, f.getName(), spacing);

                System.out.println("********************LOADED!***************************");
            } else {
                System.out.println("Skipping file: " + f.getName());
            }
        }

        return db;
    }

    private static void loadDbWithPrintsFrom(HashMap<Long, List<Match>> db, List<int[]> keyFreqList, String songName, int spacing) {
        for (int i = 0; i < keyFreqList.size() - spacing - 1; i++) {
            int[] firstRow = keyFreqList.get(i);
            int[] secondRow = keyFreqList.get(i+spacing);

            Long hash = gethashFor(firstRow, secondRow);
            Match match = new Match(songName, i);
            store(db, hash, match);
        }
    }

    // TODO: cheap fast one; replace with better one later
    private static Long gethashFor(int[] firstRow, int[] secondRow) {
        String s1 = Arrays.toString(firstRow);
        String s2 = Arrays.toString(secondRow);
        String val = s1+s2;
        return new Long(val.hashCode());
    }

    private static void store(HashMap<Long, List<Match>> db, Long hash, Match match) {
        if (db.containsKey(hash)) {
            List<Match> matches = db.get(hash);
            matches.add(match);
        } else {
            List<Match> matches = new ArrayList<>();
            matches.add(match);
            db.put(hash, matches);
        }
    }

    private static List<int[]> loadFreqList(File f) {
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

    public static String readFile(String filePath) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath));) {

            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
                line = br.readLine();
            }

        } catch (Exception errorObj) {
            System.err.println("There was a problem reading the " + filePath);
        }

        return sb.toString();
    }

}