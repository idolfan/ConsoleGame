package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;

public class VoiceServer implements Runnable {

    public static ServerSocket server;
    public static int port = 8889;
    /** List of all Clients connected to the server. */
    public static HashMap<String, Connection> connections = new HashMap<>();
    public static int[] config;
    /**
     * Used to accumulate messages, which should be send at a specific time to all
     * clients.
     */
    public static LinkedList<String> messageBuffer = new LinkedList<String>();
    public static boolean running = false;

    /** Creates an instance of a server.
     * @param configuration int[] with port, and other configurations <br>
     * <code>configuration[0] == port </code>
     */
    private VoiceServer(int[] configuration) {
        try {
            config = configuration;
            server = new ServerSocket(configuration[0]);
            Thread thread = new Thread(this);
            thread.start();
        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                server.setSoTimeout(100000);
                System.out.println("Waiting for client at " + server.getLocalPort());

                Socket client = server.accept();
                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());
                System.out.println(client.getRemoteSocketAddress() + " connected.");

                input.readUTF().split(" ");
                // name = char at index connection.size
                String name = Character.toString((char) (connections.size() + 65));
                output.writeUTF(name);
                VoiceServer.connections.put(name, new Connection(name, client, input, output));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Process all inputs from all clients */
    public static void process() {
        for (Connection connection : connections.values()) {
            try {
                if (connection.input.available() <= 0)
                    continue;

                byte[] data = new byte[connection.input.available()];
                connection.input.read(data);
                distribute_voice_data(connection.name, data);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Starts the Server. */
    public static VoiceServer startServer(int[] configuration) {
        VoiceServer.port = configuration[0];
        VoiceServer s = new VoiceServer(configuration);
        return s;
    }

    public static void distribute_voice_data(String name, byte[] data){
        for (Connection connection : connections.values()) {
            if(connection.name.equals(name)) continue;
            try {
                connection.output.write(data);
            } catch (IOException e) {
                if(e instanceof SocketException){
                    connections.remove(connection.name);
                    System.out.println(connection.name + " disconnected.");
                }
            }
        }
    }

}
