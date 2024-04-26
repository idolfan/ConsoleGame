package interactables;

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import game.Event;
import game.Level;

public class Button {

    public int x, y;
    public Orientation orientation;
    public ArrayList<Event> events = new ArrayList<>();

    public Button(int x, int y, Orientation orientation) {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }

    public void press() {
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Event.execute_event(event.type, event.arguments);
            if (event.onetime) {
                events.remove(i);
                i--;
            }
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(java.awt.Color.RED);
        int[] cell_position = new int[] { x*Level.GRID_SIZE, y*Level.GRID_SIZE };
        int margin = Level.GRID_SIZE / 4;
        int width = Level.GRID_SIZE - margin * 2;
        int height = Level.GRID_SIZE / 4;
        switch(orientation) {
            case UP:
                g2d.fillRect(cell_position[0] + margin, cell_position[1], width, height);
                break;
            case DOWN:
                g2d.fillRect(cell_position[0] + margin, cell_position[1] + Level.GRID_SIZE - height, width, height);
                break;
            case LEFT:
                g2d.fillRect(cell_position[0], cell_position[1] + margin, height, width);
                break;
            case RIGHT:
                g2d.fillRect(cell_position[0] + Level.GRID_SIZE - height, cell_position[1] + margin, height, width);
                break;
        }
    }

    public void save(ObjectOutputStream out) {
        try {
            out.writeInt(x);
            out.writeInt(y);
            out.writeUTF(orientation.name());
            out.writeInt(events.size());
            for (Event event : events) {
                event.save(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Button load(ObjectInputStream in) {
        try {
            int x = in.readInt();
            int y = in.readInt();
            Orientation orientation = Orientation.valueOf(in.readUTF());
            int eventCount = in.readInt();
            Button button = new Button(x, y, orientation);
            for (int i = 0; i < eventCount; i++) {
                Event event = Event.load(in);
                button.events.add(event);
            }
            return button;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public enum Orientation {
        UP, DOWN, LEFT, RIGHT
    }
}
