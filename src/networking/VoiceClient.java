package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import base.SoundRecorder;
import base.SoundRecorder.Playback;
import game.Game;

public class VoiceClient implements Runnable {

    public static Socket client;
    public static String lastInput;
    public static DataInputStream input;
    public static DataOutputStream output;
    public static String unitUUID;
    public static String name;
    public static String previous_name;

    /**
     * Initializes the a client with an ip and port.
     * <p>
     * Creates a new Thread and starts it.
     * 
     * @param ip
     * @param port
     */
    private VoiceClient(String ip, int port) {
        try {
            client = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(this);
        thread.start();

    }

    /** Starts the client. */
    public static VoiceClient startClient(String ip, int port) {
        VoiceClient client = new VoiceClient(ip, port);
        return client;
    }

    public static void stopClient() {
        try {
            client.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {

            output = new DataOutputStream(client.getOutputStream());
            output.writeUTF("" + client.getLocalSocketAddress());

            input = new DataInputStream(client.getInputStream());

            name = input.readUTF();

            while (true) {
                int available = input.available();
                if (available > 0) {
                    byte[] bytes = input.readNBytes(6);
                    String name = new String(bytes);
                    if (name.startsWith("name:")) {
                        process_voice_data(name.substring(5), input, null);
                        System.out.println("Received name: " + name);
                    } else {
                        process_voice_data(null, input, bytes);
                    }
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * Tries to process a received <code>message</code> and adjust the game-state
     * accordingly.
     * 
     * @param message String to process. Received from the server.
     */
    public void process_voice_data(String name, DataInputStream input, byte[] firstBytes) {

        if(name == null) {
            name = previous_name;
        } else {
            previous_name = name;
        }

        try {
            SoundRecorder soundRecorder = Game.soundRecorder;
            Playback playback = soundRecorder.playbacks.get(name);
            if (playback == null) {
                playback = soundRecorder.new Playback(name);
                soundRecorder.playbacks.put(name, playback);
                playback.start();
            }

            playback.last_playback_time = System.currentTimeMillis();

            int write_index = playback.voice_buffer_write_index;
            /* System.out.println("write_index: " + write_index); */
            int available = input.available();

            if (firstBytes != null) {
                System.out.println("firstBytes: " + new String(firstBytes));
                /* input.read(firstBytes, 0, 6);
                write_index += 6;
                if (write_index >= playback.voice_buffer.length) {
                    write_index = 0;
                }    */
            }

            if (write_index + available <= playback.voice_buffer.length) {
                int read = input.read(playback.voice_buffer, write_index, available);
                write_index += read;
                if (write_index >= playback.voice_buffer.length) {
                    write_index = 0;
                }
            } else {
                int first_half = playback.voice_buffer.length - write_index;
                int second_half = input.available() - first_half;
                input.read(playback.voice_buffer, write_index, first_half);
                input.read(playback.voice_buffer, 0, second_half);
                write_index = second_half;
            }
            /* System.out.println("next write_index: " + write_index); */
            playback.voice_buffer_write_index = write_index;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void write_voice_data(byte[] data) {
        try {

            byte[] name = ("name:" + VoiceClient.name).getBytes();
            System.out.println("Sending name: " + name.length + " " + VoiceClient.name + " ");
            System.arraycopy(name, 0, data, 0, name.length);
            output.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
