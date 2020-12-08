import model.Complex;
import model.FFT;
import model.AudioReader;
import processing.core.PApplet;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FrequencyPlotter extends PApplet {
    // ---- DISPLAY VARIABLES ----
    Plot plt;

    // ---- AUDIO VARIABLES ----
    final int WINDOW_SIZE = 4096;
    final boolean mic = false;
    ByteArrayOutputStream out;
    AudioReader reader;
    ArrayList<Complex[]> fftFrames = new ArrayList<>();

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

        if (fftFrames.size() > 0) {
            background(255);
            Complex[] frame = fftFrames.remove(0);

            // add points to plot
            plt = new ScatterPlot(0, 0, 800, 800);
            plt.set(Plot.Setting.show_axes, true);
            plt.set(Plot.Setting.show_border, true);
            plt.setYDataRange(0, 10);
            plt.set(Plot.Setting.freeze_y_scale, true);

            // higher frequencies are higher in the array,
            // so we're only displaying lower ones
            // Upper half of array is reflection of lower half so max val
            // should be frame.length / 2

            // element 0 is the average signal intensity
            for (int f = 1; f < frame.length / 16; f++) {
                double magnitude = Math.log10(frame[f].abs() + 1);

                plt.plot(f, magnitude).style("-");
            }

            plt.draw(this);
        }

        // --- display if done ---
        if (fftFrames.size() == 0 && !reader.isRunning()) {
            background(255);
            fill(0);
            stroke(0);
            textSize(64);
            textAlign(CENTER, CENTER);
            text("done!", width / 2, height / 2);
        }
    }

    public static void main(String[] args) {
        PApplet.main("FrequencyPlotter");
    }
}
