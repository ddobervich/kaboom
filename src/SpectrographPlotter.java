import model.Complex;
import model.FFT;
import model.AudioReader;
import processing.core.PApplet;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SpectrographPlotter extends PApplet {
    // ---- DISPLAY VARIABLES ----
    int blockSizeX = 2;
    int blockSizeY = 2;
    int currentCol = 0;

    // ---- AUDIO VARIABLES ----
    private static final int WINDOW_SIZE = 4096;
    ByteArrayOutputStream out;
    AudioReader reader;
    ArrayList<Complex[]> fftFrames;
    boolean mic = true;

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        if (mic) {
            reader = AudioReader.getMicStream(5000);
            out = reader.getOutputStream();
            fftFrames = new ArrayList<>();
        } else {
            reader = AudioReader.getAudioStreamFor("music/01 - Outkast - Hey Ya.wav");
            fftFrames = loadAllDataFrom(reader);
        }
    }

    public void draw() {
        if (mic) {                          // read next set of frames from the mic
            byte b[] = out.toByteArray();
            out.reset();

            if (b.length > 0) {
                FFT.performFFT(b, WINDOW_SIZE, fftFrames);
            }
        }

        if (fftFrames.size() > 0) {                            // if undisplayed frames left
            Complex[] frame = fftFrames.remove(0);       // remove oldest frame and display
            displayFrameAtCol(frame, currentCol, 1);
            currentCol += blockSizeX;

            if (currentCol > this.width) {                     // screen wrap display
                currentCol = 0;
                background(255);
            }
        }
    }

    /***
     * Display frequencies for Complex[] frame in window at column currentCol
     * @param frame output fft frequency data
     * @param currentCol column to display output at
     */
    private void displayFrameAtCol(Complex[] frame, int currentCol, double maxPercentFreqForDisplay) {
        // upper half of frame[] is reflection of lower half.  So don't include it in calculation.
        int maxFreq = (int)((frame.length/2)*maxPercentFreqForDisplay);

        // frame[0] contains average signal strength; not a freq... so start at 1
        for (int freq = 1; freq < maxFreq; freq++) {
            double magnitude = Math.log(frame[freq].abs() + 1);
            float yval = map(freq, 0, maxFreq, 800, 0);  // map from freq range to y-coord range

            int fillColor = color(0, (int) (magnitude * 10), (int) (magnitude * 20));
            fill(fillColor);
            stroke(fillColor);
            rect(currentCol, yval, blockSizeX, blockSizeY);
        }
    }

    private ArrayList<Complex[]> loadAllDataFrom(AudioReader reader) {
        ByteArrayOutputStream out = reader.getOutputStream();

        while (!reader.isRunning()) {
            System.out.println("Waiting for audio stream...");
        }

        System.out.println("Loading audio.");
        while (reader.isRunning()) {
            System.out.print(".");
        }

        byte b[] = out.toByteArray();

        ArrayList<Complex[]> outputFrames = new ArrayList<>();
        FFT.performFFT(b, WINDOW_SIZE, outputFrames);
        return outputFrames;
    }

    public static void main(String[] args) {
        PApplet.main("SpectrographPlotter");
    }
}


