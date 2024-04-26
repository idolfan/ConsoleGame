package game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Event {

    public String type;
    public String[] arguments;
    public boolean onetime;

    public Event(String type, String[] args){
        this(type, args, false);
    }

    public Event(String type, String[] args, boolean onetime){
        this.type = type;
        this.arguments = args;
        this.onetime = onetime;
    }

    public static void execute_event(String event, String[] args) {
        switch (event) {
            case "unlock_door" -> {
                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                Door door = Game.doors.get(x + " " + y);
                door.locked = false;
                System.out.println("The door is now unlocked.");
            }
            default -> System.out.println("Event not found.");
        }

    }

    public void save(ObjectOutputStream out) {
        try {
            out.writeUTF(type);
            out.writeInt(arguments.length);
            for(String arg : arguments) {
                out.writeUTF(arg);
            }
            out.writeBoolean(onetime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Event load(ObjectInputStream in) {
        try {
            String type = in.readUTF();
            int argLength = in.readInt();
            String[] args = new String[argLength];
            for(int i = 0; i < argLength; i++) {
                args[i] = in.readUTF();
            }
            boolean onetime = in.readBoolean();
            return new Event(type, args, onetime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
