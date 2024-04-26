package networking;

import game.Event;
import game.Game;

/** Provides static-methods which (de)construct messages. */
public class Message {

    public static String exampleWriteServer() {
        String type = "EXAMPLE";
        NetworkManager manager = new NetworkManager();
        manager.messageType = type;
        manager.addPackage("INFORMATION", "This is an example message from the server.");
        manager.addPackage("INFORMATION", "You can add as many packages as you want. Even with the same name.");

        return manager.constructMessage();
    }

    public static boolean exampleReadClient(NetworkManager manager) {
        String[] information = manager.getPackagesByName("INFORMATION");
        System.out.println("These are example messages from the server: \n");
        for (String info : information) {
            System.out.println(info);
        }
        return true;
    }

    public static String send_open_door(int x, int y, boolean open) {
        String type = "OPEN_DOOR";
        NetworkManager manager = new NetworkManager();
        manager.messageType = type;
        manager.addPackage("X", String.valueOf(x));
        manager.addPackage("Y", String.valueOf(y));
        manager.addPackage("OPEN", String.valueOf(open));
        return manager.constructMessage();
    }

    public static void receive_open_door(NetworkManager manager) {
        int x = Integer.parseInt(manager.getPackagesByName("X")[0]);
        int y = Integer.parseInt(manager.getPackagesByName("Y")[0]);
        boolean open = Boolean.parseBoolean(manager.getPackagesByName("OPEN")[0]);
        System.out.println("Received a request to open door at: " + x + " " + y + " " + open);

        Game.doors.get(x + " " + y).set_open(open);
    }

    public static String send_disconnect() {
        String type = "DISCONNECT";
        NetworkManager manager = new NetworkManager();
        manager.messageType = type;
        return manager.constructMessage();
    }

    public static void receive_disconnect(NetworkManager manager) {
        System.out.println("Received a disconnect message.");
        // TODO: Implement
    }

    public static String send_player_position(int x, int y) {
        String type = "PLAYER_POSITION";
        NetworkManager manager = new NetworkManager();
        manager.messageType = type;
        manager.addPackage("X", String.valueOf(x));
        manager.addPackage("Y", String.valueOf(y));
        manager.addPackage("ID", Client.name);
        return manager.constructMessage();
    }

    public static void receive_player_position(NetworkManager manager) {
        int x = Integer.parseInt(manager.getPackagesByName("X")[0]);
        int y = Integer.parseInt(manager.getPackagesByName("Y")[0]);
        String id = manager.getPackagesByName("ID")[0];
        System.out.println("Received a player position message: " + id + " " + x + " " + y);
        Game.players.put(id, new int[] {x, y});
    }

    public static String send_event(String event, String[] args) {
        String type = "EVENT";
        NetworkManager manager = new NetworkManager();
        manager.messageType = type;
        manager.addPackage("TYPE", event);
        for (int i = 0; i < args.length; i++) {
            manager.addPackage("ARG", args[i]);
        }
        return manager.constructMessage();
    }

    public static void receive_event(NetworkManager manager) {
        String event = manager.getPackagesByName("TYPE")[0];
        String[] args = manager.getPackagesByName("ARG");
        System.out.println("Received an event: " + event);
        Event.execute_event(event, args);
    }
}
