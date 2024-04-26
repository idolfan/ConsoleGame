package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

public class Server implements Runnable {

    public static ServerSocket server;
    public static int port = 8888;
    /** List of all Clients connected to the server. */
    public static HashMap<String, Connection> connections = new HashMap<>();
    public static int[] config;
    /**
     * Used to accumulate messages, which should be send at a specific time to all
     * clients.
     */
    public static LinkedList<String> messageBuffer = new LinkedList<String>();
    public static boolean running = false;

    /**
     * Creates an instance of a server.
     * 
     * @param configuration int[] with port, and other configurations <br>
     *                      <code>configuration[0] == port </code>
     */
    private Server(int[] configuration) {
        try {
            config = configuration;
            server = new ServerSocket(configuration[0]);
            Thread thread = new Thread(this);
            thread.start();
            Thread distributer = new Thread(new Distributer());
            distributer.start();
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
                String name = connections.size()+"";
                Server.connections.put(name, new Connection(name, client, input, output));
                output.writeUTF(name);

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

                String message = connection.input.readUTF();
                NetworkManager manager = NetworkManager.deconstructMessage(message);

                if (manager.messageType.equals("DISCONNECT")) {
                    connection.disconnect();
                    connections.remove(connection.name);
                    continue;
                }

                for(Connection c : connections.values()){
                        c.output.writeUTF(message);
                }

            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset")) {
                    System.out.println("Client disconnected");
                    connections.get(connection.name).disconnect();
                    connections.remove(connection.name);
                }
                e.printStackTrace();

            }
        }
    }

    /** Starts the Server. */
    public static Server startServer(int[] configuration) {
        Server.port = configuration[0];
        Server s = new Server(configuration);
        return s;
    }

    /** Sends the buffer to every connection and clears it. */
    public static void sendBufferedMessages() {
        String concatinatedMessage = "";
        while (messageBuffer.size() > 0)
            concatinatedMessage += messageBuffer.pollFirst();
        if (concatinatedMessage.length() > 0) {
            for (String clientName : connections.keySet()) {
                writeDataToClient(clientName, concatinatedMessage);
            }
        }
    }

    /**
     * Writes data to specific client
     * 
     * @param clientName name of the client
     * @param data       String to send
     */
    private static void writeDataToClient(String clientName, String data) {
        DataOutputStream output = connections.get(clientName).output;
        try {
            output.writeUTF(data);
        } catch (IOException e) {
            if (e.getMessage().equals("Connection reset")) {
                System.out.println("Client disconnected");
                connections.get(clientName).disconnect();
                connections.remove(clientName);
            }
        }
    }

    public class Distributer implements Runnable {

        @Override
        public void run() {
            while (running) {
                process();
            }

        }
    }

}
