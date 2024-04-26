package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import game.Game;


public class Client implements Runnable {

    public static Socket client;
    public static Socket voice_client;
    public static String lastInput;
    public static DataInputStream input;
    public static DataOutputStream output;

    public static String name;
    public static Game game;

    /** Initializes the a client with an ip and port.
     * <p> Creates a new Thread and starts it.
     * @param ip
     * @param port
     */
    private Client(String ip, int port) {
        try {
            client = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(this);
        thread.start();

    }

    /** Starts the client. */
    public static Client startClient(String ip, int port) {
        Client client = new Client(ip, port);
        return client;
    }



    @Override
    public void run() {
        try {

            output = new DataOutputStream(client.getOutputStream());
            output.writeUTF("" + client.getLocalSocketAddress());

            input = new DataInputStream(client.getInputStream());

            name = input.readUTF();

            while (true) {
                String message = input.readUTF();
                process(message);
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /** Tries to process a received <code>message</code> and adjust the game-state accordingly.
     * @param message String to process. Received from the server.
     */
    public void process(String message) {
        System.out.println("--------------------");
        System.out.println("Client received a message:");
        System.out.println("--------------------");

        NetworkManager manager = NetworkManager.deconstructMessage(message);
        String type = manager.messageType;

        switch(type) {
            case "OPEN_DOOR" -> Message.receive_open_door(manager);
            case "PLAYER_POSITION" -> Message.receive_player_position(manager);
            case "EVENT" -> Message.receive_event(manager);
            default -> System.out.println("Unknown message type: " + type);
        }

    }

    public static void writeMessage(String message) {
        try {
            if(output == null) {
                System.out.println("No connection to server.");
                return;
            }
            output.writeUTF(message);
            System.out.println("Client sent: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
