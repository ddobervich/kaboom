
import model.AudioReader;
import processing.core.PApplet;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class WavePlotter extends PApplet {
    Plot plt;
    int time = 0;
    ByteArrayOutputStream out;
    AudioReader reader;
    ArrayList<Integer> raw = new ArrayList<>();

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        plt = new TimeSeriesPlot(0, 0, 800, 800, 200);

        plt.set(Plot.Setting.show_axes, true);
        plt.set(Plot.Setting.show_border, true);

        //----------------
        reader = AudioReader.getMicStream(5000);
        //reader = AudioReader.getAudioStreamFor("music/01 - Outkast - Hey Ya.wav");
        out = reader.getOutputStream();
    }

    public void draw() {
        byte b[] = out.toByteArray();
        out.reset();
        background(255);

        for (int i = 0; i < b.length; i++) {
            plt.plot(0, time, b[i]).strokeColor("red").style("-");
            time++;
        }

        plt.draw(this);
    }

    public static void main(String[] args) {
        PApplet.main("WavePlotter");
    }
}
