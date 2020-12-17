import model.Complex;
import model.FFT;
import model.AudioReader;
import model.FingerprintLib;
import processing.core.PApplet;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class SpectrographPlotter extends PApplet {
    // ---- DISPLAY VARIABLES ----
    int blockSizeX = 2;
    int blockSizeY = 2;
    int currentCol = 0;

    // ---- AUDIO VARIABLES ----
    private static final int WINDOW_SIZE = 4096;
    ByteArrayOutputStream out;
    AudioReader reader;
    ArrayList<Complex[]> fftFrames = new ArrayList<>();
    boolean mic = false;

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        if (mic) {
            reader = AudioReader.getMicStream(5000);
            out = reader.getOutputStream();
        } else {
            reader = AudioReader.getAudioStreamFor("music/01 - Outkast - Hey Ya.wav");
            byte[] data = reader.readAllData();
            FFT.performFFT(data, WINDOW_SIZE, fftFrames);
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

            int[] keyFrequencies = FingerprintLib.getKeyFrequenciesFor(frame);
            System.out.println(Arrays.toString(keyFrequencies));

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

        int[] keyFreq = FingerprintLib.getKeyFrequenciesFor(frame);
        highlightKeyFrequencies(keyFreq, maxFreq);
    }

    private void highlightKeyFrequencies(int[] keyFreq, int maxFreq) {
        for ( Integer freq : keyFreq ) {
            stroke(color(255, 0,0));
            fill(color(255,0,0));
            float yval = map(freq, 0, maxFreq, 800, 0);
            ellipse(currentCol, yval, 3, 3);
        }
    }

    public static void main(String[] args) {
        PApplet.main("SpectrographPlotter");
    }
}


