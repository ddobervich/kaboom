package model;

import org.tritonus.sampled.convert.PCM2PCMConversionProvider;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/***
 * Substantial sections of this code adapted from https://github.com/wsieroci/audio-recognizer
 *
 * Provides two factory methods for returning AudioReader objects which is a wrapper for a ByteArrayOutputStream for
 * either a mic or an audiofile.  Only supports .wav and (I think) .aif but not .mp3.
 *
 * Basic use:
 *     AudioReader reader = AudioReader.getAudioStreamFor("music/01 - Outkast - Hey Ya.wav");
 *     out = reader.getOutputStream();
 *
 * You can wait until the file is fully read into the buffer
 * (though note that you may need to wait until reader.isRunning() is true before you start the loop):
 *
 *     while (reader.isRunning()) {
 *         Thread.sleep(100)
 *         System.out.print(".");
 *     }
 *     byte[] b = out.toByteArray();
 *
 * Or you can read from the stream in a loop
 *
 *     while (true) {
 *         byte[] b = out.toByteArray();
 *         if (b.length > 0) {
 *             out.reset();  // clear what's been read
 *         }
 *
 *         // do something with b
 *     }
 *
 * OutputStream will automatically close when file is done or mic time limit reached
 * so you don't need to close it yourself.
 */
public class AudioReader {
    private ByteArrayOutputStream out;
    private boolean running = false;

    /***
     * Return AudioReader with OutputStream for mic.  Only active for maxTime.  Unfortunately the units for
     * maxTime are read loops in the reading thread.  This should be changed in the future.  1000 is maybe 5-10 sec.
     * @param maxTime number of loops to read from the mic before stopping
     * @return AudioReader object
     */
    public static AudioReader getMicStream(int maxTime) {
        AudioReader reader = new AudioReader();
        TargetDataLine line;

        try {
            final AudioFormat format = getFormat(); //Fill AudioFormat with the wanted settings
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            reader.out = reader.getOutputStreamFor(line, maxTime);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reader;
    }

    /***
     * Return AudioReader with OutputStream for file located at filePath.  Works for .wav and (maybe) .aif
     * @return AudioReader object
     */
    public static AudioReader getAudioStreamFor(String filePath) {
        AudioReader reader = new AudioReader();
        PCM2PCMConversionProvider conversionProvider = new PCM2PCMConversionProvider();

        try {
            AudioInputStream in;
            AudioInputStream din;

            File file = new File(filePath);
            in = AudioSystem.getAudioInputStream(file);

            AudioFormat baseFormat = in.getFormat();

            System.out.println(baseFormat.toString());

            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                    false);

            din = AudioSystem.getAudioInputStream(decodedFormat, in);

            if (!conversionProvider.isConversionSupported(getFormat(),
                    decodedFormat)) {
                System.out.println("Conversion is not supported");
            }

            System.out.println(decodedFormat.toString());

            AudioInputStream outDin = conversionProvider.getAudioInputStream(getFormat(), din);

            reader.out = reader.getOutputStreamForAudioSource(outDin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reader;
    }

    public ByteArrayOutputStream getOutputStream() {
        return out;
    }

    private ByteArrayOutputStream getOutputStreamFor(TargetDataLine line, int maxTime) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Thread listeningThread = new Thread(new Runnable() {
            public void run() {
                byte[] buffer = new byte[(int)1024];
                running = true;
                int n = 0;

                try {
                    while (running) {
                        n++;
                        if (n > maxTime)
                            break;

                        int count = line.read(buffer, 0, 1024);
                        if (count > 0) {
                            out.write(buffer, 0, count);
                        }
                    }
                    out.flush();
                    out.close();
                    line.close();
                } catch (IOException e) {
                    System.err.println("I/O problems: " + e);
                    System.exit(-1);
                }

                running = false;
            }
        });
        listeningThread.start();

        return out;
    }

    private ByteArrayOutputStream getOutputStreamForAudioSource(AudioInputStream line) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Thread listeningThread = new Thread(new Runnable() {
            public void run() {
                running = true;
                byte[] buffer = new byte[(int)1024];

                long songLength = line.getFrameLength();
                System.out.println("Length is: " + songLength);

                long n = 0;
                try {
                    while (true) {
                        int count = line.read(buffer, 0, 1024);
                        if (count > 0) {
                            out.write(buffer, 0, count);
                        } else if (count == -1) {   // reached end of stream
                            break;
                        }
                    }
                    out.flush();
                    out.close();
                    line.close();
                } catch (IOException e) {
                    System.err.println("I/O problems: " + e);
                    System.exit(-1);
                }

                running = false;
            }
        });
        listeningThread.start();

        return out;
    }

    public boolean isRunning() {
        return running;
    }

    private static AudioFormat getFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 8;
        int channels = 1; //mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public byte[] readAllData() {
        ByteArrayOutputStream out = this.getOutputStream();

        while (!this.isRunning()) {
            System.out.println("Waiting for audio stream...");
        }

        System.out.println("Loading audio.");
        while (this.isRunning()) {
            System.out.print(".");
        }

        return out.toByteArray();
    }
}
