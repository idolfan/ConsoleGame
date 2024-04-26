package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import interactables.Button;
import interactables.Button.Orientation;

public class Room {
    /** Areas defined by two corner-cells */
    public ArrayList<int[][]> areas = new ArrayList<int[][]>();
    public int LevelEditor_color;
    /** Walls defined by two cells */
    public ArrayList<int[][]> walls = new ArrayList<int[][]>();
    public ArrayList<Door> doors = new ArrayList<Door>();
    public ArrayList<Button> buttons = new ArrayList<Button>();

    public Room(/* int[] origin */) {
        /* this.origin = origin; */
        int index = LevelEditor.visible_level.rooms.size();
        int color = ((50 + 60 * (index / 4)) % 255) << 16 | ((80 + 60 * index) % 255) << 8 | 200;
        this.LevelEditor_color = color;
    }

    public Room(int color) {
        this.LevelEditor_color = color;
    }

    public void add_area(int[] p1, int[] p2) {
        if (p1[0] == p2[0] || p1[1] == p2[1])
            return;

        int[][] area = to_area(p1, p2);
        areas.add(area);
        System.out.println("Area added: " + area[0][0] + " " + area[0][1] + " " + area[1][0] + " " + area[1][1]);
    }

    public void add_wall(int[] p1, int[] p2) {
        int[][] wall = to_wall(p1, p2);
        if (wall == null)
            return;

        walls.add(wall);
        System.out.println("Wall added: " + wall[0][0] + " " + wall[0][1] + " " + wall[1][0] + " " + wall[1][1]);

    }

    public void add_door(int[] p1, int[] p2) {
        int[][] door = to_door(p1, p2);
        if (door == null)
            return;

        doors.add(new Door(door, false));
        System.out.println("Door added: " + door[0][0] + " " + door[0][1] + " " + door[1][0] + " " + door[1][1]);
    }

    public void add_button(int[] p1, int[] p2) {
        int[][] position = to_button(p1, p2);
        System.out.println("Button: " + (p2[0] - p1[0]) + " " + (p2[1] - p1[1]));
        p1 = position[0];
        p2 = position[1];
        Orientation orientation = p1[0] < p2[0] ? Orientation.RIGHT :
            p1[0] > p2[0] ? Orientation.LEFT :
            p1[1] > p2[1] ? Orientation.UP : Orientation.DOWN;
        buttons.add(new Button(position[0][0], position[0][1], orientation));
        System.out.println("Button added: " + position[0][0] + " " + position[0][1] + " " + orientation.name());
    }

    public void LevelEditor_draw(Graphics2D g, boolean selected, int selected_area_index) {

        Color area_not_selected_color = selected ? new Color(LevelEditor_color + (140 << 24), true)
                : new Color(LevelEditor_color + (100 << 24), true);
        Color area_selected_color = new Color(LevelEditor_color + (180 << 24), true);

        ArrayList<Integer> selected_areas_indexes = LevelEditor.selected_area_indexes;

        for (int i = 0; i < areas.size(); i++) {
            int[][] area = areas.get(i);
            if (selected && (selected_area_index == i || selected_areas_indexes.contains(i)))
                g.setColor(area_selected_color);
            else
                g.setColor(area_not_selected_color);

            int[] left_top = new int[] { area[0][0] * Level.GRID_SIZE, area[0][1] * Level.GRID_SIZE };
            int width = (area[1][0] - area[0][0]) * Level.GRID_SIZE;
            int height = (area[1][1] - area[0][1]) * Level.GRID_SIZE;

            g.fillRect(left_top[0], left_top[1], width, height);
            g.setColor(new Color(0, 0, 0, 100));
            g.drawRect(left_top[0], left_top[1], width, height);

        }

        ArrayList<Integer> selected_walls_indexes = LevelEditor.selected_wall_indexes;

        for (int i = 0; i < walls.size(); i++) {
            int[][] wall = walls.get(i);
            g.setColor(new Color(255, 255, 255, 200));
            g.setStroke(new java.awt.BasicStroke(3));
            if (selected_walls_indexes.contains(i))
                g.setStroke(new java.awt.BasicStroke(5));

            int[] top_left = new int[] { wall[0][0] * Level.GRID_SIZE, wall[0][1] * Level.GRID_SIZE };
            int[] bottom_right = new int[] { wall[1][0] * Level.GRID_SIZE, wall[1][1] * Level.GRID_SIZE };

            g.drawLine(top_left[0], top_left[1], bottom_right[0], bottom_right[1]);
            g.setStroke(new java.awt.BasicStroke(1));
        }

        for (int i = 0; i < doors.size(); i++) {
            int[][] door = doors.get(i).position;
            g.setColor(new Color(139, 69, 19, 200));
            g.setStroke(new java.awt.BasicStroke(3));

            int[] top_left = new int[] { door[0][0] * Level.GRID_SIZE, door[0][1] * Level.GRID_SIZE };
            int[] bottom_right = new int[] { door[1][0] * Level.GRID_SIZE, door[1][1] * Level.GRID_SIZE };

            g.drawLine(top_left[0], top_left[1], bottom_right[0], bottom_right[1]);
            g.setStroke(new java.awt.BasicStroke(1));
        }

        for(int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            int x = button.x * Level.GRID_SIZE;
            int y = button.y * Level.GRID_SIZE;
            g.setColor(new Color(255, 0, 0, 200));
            g.fillOval(x - 10, y - 10, 20, 20);
            g.setColor(new Color(0, 0, 0, 200));
            g.drawOval(x - 10, y - 10, 20, 20);
        }

    }

    public void saveRoom(ObjectOutputStream out) {
        try {
            out.writeUTF("{ \n");
            out.writeUTF("areas: [ \n ");
            for (int[][] area : areas) {
                out.writeUTF("[" + area[0][0] + ", " + area[0][1] + ", " + area[1][0] + ", " + area[1][1] + "], \n");
            }
            out.writeUTF("] \n");
            out.writeUTF("walls: [ \n ");
            for (int[][] wall : walls) {
                out.writeUTF("[" + wall[0][0] + ", " + wall[0][1] + ", " + wall[1][0] + ", " + wall[1][1] + "], \n");
            }
            out.writeUTF("] \n");
            out.writeUTF("doors: [ \n ");
            for (Door door : doors) {
                door.save(out);
            }
            out.writeUTF("] \n");
            out.writeInt(buttons.size());
            out.writeUTF("buttons: [ \n ");
            for (Button button : buttons) {
                button.save(out);
            }
            out.writeUTF("] \n");
            out.writeUTF("} \n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadRoom(ObjectInputStream ois) {
        System.out.println("Loading room");
        try {
            ois.readUTF(); // areas: [
            while (true) {
                String s = ois.readUTF(); // [x1, y1, x2, y2] || ]
                System.out.println(s);
                if (s.equals("] \n")) {
                    break;
                }
                s = s.substring(s.indexOf("[") + 1, s.indexOf("]")); // x1, y1, x2, y2
                String[] coords = s.split(", ");
                int[] p1 = { Integer.parseInt(coords[0]), Integer.parseInt(coords[1]) };
                int[] p2 = { Integer.parseInt(coords[2]), Integer.parseInt(coords[3]) };
                areas.add(new int[][] { p1, p2 });
                System.out.println("Area loaded: " + p1[0] + " " + p1[1] + " " + p2[0] + " " + p2[1]);
            }
            ois.readUTF(); // walls: [
            while (true) {
                String s = ois.readUTF(); // [x1, y1, x2, y2] || ]
                if (s.equals("] \n")) {
                    break;
                }
                s = s.substring(s.indexOf("[") + 1, s.indexOf("]")); // x1, y1, x2, y2
                String[] coords = s.split(", ");
                int[] p1 = { Integer.parseInt(coords[0]), Integer.parseInt(coords[1]) };
                int[] p2 = { Integer.parseInt(coords[2]), Integer.parseInt(coords[3]) };
                walls.add(new int[][] { p1, p2 });
                System.out.println("Wall loaded: " + p1[0] + " " + p1[1] + " " + p2[0] + " " + p2[1]);
            }
            ois.readUTF(); // doors: [
            while (true) {
                String s = ois.readUTF(); // [x1, y1, x2, y2, open] || ]
                if (s.equals("] \n")) {
                    break;
                }
                s = s.substring(s.indexOf("[") + 1, s.indexOf("]")); // x1, y1, x2, y2, open
                String[] data = s.split(", ");
                int[] p1 = { Integer.parseInt(data[0]), Integer.parseInt(data[1]) };
                int[] p2 = { Integer.parseInt(data[2]), Integer.parseInt(data[3]) };
                boolean open = Boolean.parseBoolean(data[4]);
                doors.add(new Door(new int[][] { p1, p2 }, open));
                System.out.println("Door loaded: " + p1[0] + " " + p1[1] + " " + p2[0] + " " + p2[1]);
            }
            int button_count = ois.readInt();
            ois.readUTF(); // buttons: [
            for (int i = 0; i < button_count; i++) {
                Button button = Button.load(ois);
                buttons.add(button);
            }
            ois.readUTF(); // ]
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[][] to_area(int[] p1, int[] p2) {
        int greater_x = (int) Math.ceil((p1[0] > p2[0] ? p1[0] : p2[0]) / (double) Level.GRID_SIZE);
        int lesser_x = (int) Math.floor((p1[0] < p2[0] ? p1[0] : p2[0]) / (double) Level.GRID_SIZE);
        int greater_y = (int) Math.ceil((p1[1] > p2[1] ? p1[1] : p2[1]) / (double) Level.GRID_SIZE);
        int lesser_y = (int) Math.floor((p1[1] < p2[1] ? p1[1] : p2[1]) / (double) Level.GRID_SIZE);
        return new int[][] { { lesser_x, lesser_y }, { greater_x, greater_y } };
    }

    public static int[][] to_wall(int[] p1, int[] p2) {
        int smaller_x = p1[0] < p2[0] ? p1[0] : p2[0];
        int smaller_y = p1[1] < p2[1] ? p1[1] : p2[1];
        int greater_x = p1[0] > p2[0] ? p1[0] : p2[0];
        int greater_y = p1[1] > p2[1] ? p1[1] : p2[1];

        int mean_x = (smaller_x + greater_x) / 2;
        int mean_y = (smaller_y + greater_y) / 2;
        int difference_x = Math.abs(smaller_x - greater_x);
        int difference_y = Math.abs(smaller_y - greater_y);

        int constant_axis = difference_x < difference_y ? mean_x : mean_y;
        int[] variable_axis = difference_x < difference_y ? new int[] { smaller_y, greater_y } : new int[] { smaller_x, greater_x };

        constant_axis = (int) Math.round(constant_axis / (double) Level.GRID_SIZE);
        variable_axis[0] = (int) Math.round(variable_axis[0] / (double) Level.GRID_SIZE);
        variable_axis[1] = (int) Math.round(variable_axis[1] / (double) Level.GRID_SIZE);
        if (variable_axis[0] == variable_axis[1])
            return null;

        if (difference_x < difference_y)
            return new int[][] { { constant_axis, variable_axis[0] }, { constant_axis, variable_axis[1] } };
        else
            return new int[][] { { variable_axis[0], constant_axis }, { variable_axis[1], constant_axis } };
    }

    public static int[][] to_door(int[] p1, int[] p2) {
        int smaller_x = p1[0];
        int smaller_y = p1[1];
        int greater_x = p2[0];
        int greater_y = p2[1];

        int mean_x = (smaller_x + greater_x) / 2;
        int mean_y = (smaller_y + greater_y) / 2;
        int difference_x = Math.abs(smaller_x - greater_x);
        int difference_y = Math.abs(smaller_y - greater_y);

        int constant_axis = difference_x < difference_y ? mean_x : mean_y;
        int[] variable_axis = difference_x < difference_y ? new int[] { smaller_y, greater_y } : new int[] { smaller_x, greater_x };

        constant_axis = (int) Math.round(constant_axis / (double) Level.GRID_SIZE);
        variable_axis[0] = (int) Math.round(variable_axis[0] / (double) Level.GRID_SIZE);
        variable_axis[1] = (int) Math.round(variable_axis[1] / (double) Level.GRID_SIZE);
        if (variable_axis[0] == variable_axis[1])
            return null;

        if (difference_x < difference_y)
            return new int[][] { { constant_axis, variable_axis[0] }, { constant_axis, variable_axis[1] } };
        else
            return new int[][] { { variable_axis[0], constant_axis }, { variable_axis[1], constant_axis } };
    }

    public static int[][] to_button(int[] p1, int[] p2) {
        return new int[][] { { (int)Math.floor(p1[0] / (double)Level.GRID_SIZE), (int)Math.floor(p1[1] / (double)Level.GRID_SIZE) },
            { (int)Math.floor(p2[0] / (double)Level.GRID_SIZE), (int)Math.floor(p2[1] / (double)Level.GRID_SIZE) } };
    }
}
