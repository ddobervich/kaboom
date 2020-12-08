
import model.AudioReader;
import processing.core.PApplet;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class WavePlotter extends PApplet {
    Plot plt;
    int time = 0;
    ByteArrayOutputStream out;
    AudioReader reader;

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        plt = new TimeSeriesPlot(0, 0, 800, 800, 200);

        plt.set(Plot.Setting.show_axes, true);
        plt.set(Plot.Setting.show_border, true);

    }

    public void draw() {

        // TODO: use AudioReader to get an audio stream
        // TODO: plot raw data

        plt.plot(0, time, mouseX).strokeColor("red").style("-");

        plt.draw(this);
    }

    public static void main(String[] args) {
        PApplet.main("WavePlotter");
    }
}
