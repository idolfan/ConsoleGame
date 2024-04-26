package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/** Represents a Client connected to the server. */
public class Connection {

    // Serverside
    public Socket client;
    public DataInputStream input;
    public DataOutputStream output;
    // ClientSide
    public String name;

    /** SERVER
     */
    public Connection(String name, Socket client, DataInputStream input, DataOutputStream output) {
        this.name = name;
        this.client = client;
        this.input = input;
        this.output = output;
    }

    /** Disconnects it from the server */
    public void disconnect() {
        try {
            input.close();
            output.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}