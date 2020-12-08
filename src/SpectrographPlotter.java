import model.Complex;
import model.FFT;
import model.AudioReader;
import processing.core.PApplet;

import java.io.ByteArrayOutputStream;

public class SpectrographPlotter extends PApplet {
    private static final int WINDOW_SIZE = 4096;

    ByteArrayOutputStream out;
    AudioReader reader;
    private boolean logModeEnabled = false;
    int currentCol = 0;

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        //----------------
        //out = model.MicReader.getAudioStreamFor("D:\\JavaWorkspaces\\DavidDobervich\\Shazam\\data\\sweeplin.wav");
        reader = AudioReader.getMicStream(5000);
        //reader = AudioReader.getAudioStreamFor("music/01 - Outkast - Hey Ya.wav");
        out = reader.getOutputStream();
    }

    public void draw() {
        int blockSizeX = 1;
        int blockSizeY = 1;

        byte b[] = out.toByteArray();
        out.reset();

        if (b.length > 0) {
            Complex[][] results = FFT.performFFT(b, WINDOW_SIZE);

            for (int i = 0; i < results.length; i++) {
                int freq = 1;
                for (int line = 1; line < results[i].length/8; line++) {
                    double magnitude = Math.log(results[i][freq].abs() + 1);

                    int fillColor = color(0, (int) (magnitude * 10), (int) (magnitude * 20));
                    fill(fillColor);
                    stroke(fillColor);

                    float yval = map(line, 0, 4096/8, 800, 0);
                    rect(currentCol + i * blockSizeX, yval, blockSizeX, blockSizeY);
//                g2d.fillRect(i*blockSizeX, (size-line)*blockSizeY,blockSizeX,blockSizeY);

                    // I used a improviced logarithmic scale and normal scale:
                    if (logModeEnabled && (Math.log10(line) * Math.log10(line)) > 1) {
                        freq += (int) (Math.log10(line) * Math.log10(line));
                    } else {
                        freq++;
                    }
                }
            }
            currentCol += results.length * blockSizeX;

            if (currentCol > 800) {
                currentCol = 0;
                background(255);
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main("SpectrographPlotter");
    }
}
