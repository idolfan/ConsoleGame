package base;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import game.Game;
import networking.VoiceClient;

public class SoundRecorder {

    final int bufSize = 16384;

    public Capture capture = new Capture();

    public HashMap<String, Playback> playbacks = new HashMap<>();

    public SoundRecorder() {
    }

    public void startRecording() {
        capture.start();
    }

    public void stopRecording() {
        capture.stop();
    }

    /**
     * Write data to the OutputChannel.
     */
    public class Playback implements Runnable {

        public long last_playback_time = 0;


        public SourceDataLine line;

        Thread thread;

        public byte[] voice_buffer = new byte[bufSize * 8];

        public int voice_buffer_read_index = 0;
        public int voice_buffer_write_index = 0;

        public int bytes_per_execution = 8192;

        public String name;

        public Playback(String name) {
            this.name = name;
        }

        public void start() {
            thread = new Thread(this);
            thread.setName("Playback");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        public void run() {

            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
            float rate = 16000.0f;
            int channels = 2;
            int sampleSize = 16;
            boolean bigEndian = true;

            AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
                    * channels, rate, bigEndian);

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                return;
            }

            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);

                FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);

                gain.setValue(-5);
                
            } catch (LineUnavailableException ex) {
                return;
            }

            line.start();

            while (thread != null) {
                try {
                    if(last_playback_time < System.currentTimeMillis() - 1000) {
                        stop();
                        Game.soundRecorder.playbacks.remove(name);
                    }
                    
                    int write_index = voice_buffer_write_index;
                    int available;
                    if(voice_buffer_read_index < write_index) {
                        available = write_index - voice_buffer_read_index;
                    } else {
                        available = write_index + voice_buffer.length - voice_buffer_read_index;
                    }
                    if(available > bytes_per_execution*3) {
                        voice_buffer_read_index += bytes_per_execution;;
                    }
                    if(available > bytes_per_execution*16) {
                        System.out.println("Catching up");
                        voice_buffer_read_index = write_index - bytes_per_execution*3;
                    }
                    if(voice_buffer_read_index >= voice_buffer.length) {
                        voice_buffer_read_index -= voice_buffer.length;
                    }
                    if(voice_buffer_read_index < 0) {
                        voice_buffer_read_index += voice_buffer.length;
                    }


                    if(voice_buffer_read_index + bytes_per_execution > voice_buffer.length) {
                        int first_half = voice_buffer.length - voice_buffer_read_index;
                        int second_half = bytes_per_execution - first_half;
                        line.write(voice_buffer, voice_buffer_read_index, first_half);
                        line.write(voice_buffer, voice_buffer_read_index, second_half);
                    } else {
                        line.write(voice_buffer, voice_buffer_read_index, bytes_per_execution);
                    }
                } catch (Exception e) {
                    break;
                }
            }
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
        }
    }

    class Capture implements Runnable {

        TargetDataLine line;

        Thread thread;

        boolean running = false;

        public void start() {
            if (running == true) {
                return;
            }
            thread = new Thread(this);
            thread.start();
        }

        public void stop() {
            thread = null;
            running = false;
        }

        public void run() {

            running = true;

            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
            float rate = 16000;
            int channels = 2;
            int sampleSize = 16;
            boolean bigEndian = true;

            AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
                    * channels, rate, bigEndian);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info))
                return;

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException ex) {
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

            line.start();

            while (thread != null) {
                byte[] voice_data = new byte[bufferLengthInBytes+6];
                line.read(voice_data, 6, bufferLengthInBytes);
                VoiceClient.write_voice_data(voice_data);

            }

            line.stop();
            line.close();
            line = null;

            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
            }

        }

    } // End class Capture
}