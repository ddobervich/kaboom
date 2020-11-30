import model.Complex;
import model.FFT;
import model.AudioReader;
import processing.core.PApplet;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

// TODO:  why the long pause at the start??

public class FrequencyPlotter extends PApplet {
    private static final int CHUNK_SIZE = 4096;
    Plot plt;         // TODO: add ability to be fixed size; new points bump off old points
    int time = 0;
    ByteArrayOutputStream out;
    AudioReader reader;
    private boolean logModeEnabled = false;
    int currentCol = 0;
    ArrayList<Complex[]> fftResults = new ArrayList<Complex[]>();

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        //----------------
        reader = AudioReader.getAudioStreamFor("music/01 - Outkast - Hey Ya.wav");
        out = reader.getOutputStream();
        //out = MicReader.getMicStream();
    }

    public void draw() {
        byte b[] = out.toByteArray();

        if (b.length > CHUNK_SIZE) {
            out.reset();
            FFT.performFFT(b, CHUNK_SIZE, fftResults);
        }

        if (fftResults.size() > 0) {
            background(255);
            Complex[] results = fftResults.remove(0);

            if (results.length > 0) {
                // add points to plot
                plt = new ScatterPlot(0, 0, 800, 800);
                plt.set(Plot.Setting.show_axes, true);
                plt.set(Plot.Setting.show_border, true);
                plt.setYDataRange(0, 10);
                plt.set(Plot.Setting.freeze_y_scale, true);

                // higher frequencies are higher in the array, so we're only displaying lower ones
                // element 0 is the average signal intensity
                for (int f = 1; f < results.length / 4; f++) {
                    double magnitude = Math.log10(results[f].abs() + 1);

                    plt.plot(f, magnitude).style("-");
                }

                plt.draw(this);
            }
        }

        if (fftResults.size() == 0 && !reader.isRunning()) {
            background(255);
            fill(0);
            stroke(0);
            textSize(64);
            textAlign(CENTER, CENTER);
            text("done!", width/2, height/2);
        }
    }

    public static void main(String[] args) {
        PApplet.main("AudioFrequencyPlotter");
    }
}
