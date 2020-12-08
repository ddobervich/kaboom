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
        if (mic) {
            byte b[] = out.toByteArray();
            out.reset();

            if (b.length > 0) {
                FFT.performFFT(b, WINDOW_SIZE, fftFrames);
            }
        }

        if (fftFrames.size() > 0) {
            Complex[] frame = fftFrames.remove(0);
            displayFrameAtCol(frame, currentCol);
            currentCol += blockSizeX;

            if (currentCol > 800) {
                currentCol = 0;
                background(255);
            }
        }
    }

    private void displayFrameAtCol(Complex[] frame, int currentCol) {
        for (int freq = 1; freq < frame.length / 8; freq++) {
            double magnitude = Math.log(frame[freq].abs() + 1);
            float yval = map(freq, 0, 4096 / 8, 800, 0);

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


