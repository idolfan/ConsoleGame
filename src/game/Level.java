package game;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import interactables.Button;

public class Level {

    public static int GRID_SIZE = 80;
    public ArrayList<Room> rooms = new ArrayList<Room>();

    public Level() {

    }

    public void LevelEditor_draw(java.awt.Graphics2D g2d, int selected_room_index, int selected_area_index) {
        for (int i = 0; i < rooms.size(); i++) {
            rooms.get(i).LevelEditor_draw(g2d, i == selected_room_index, selected_area_index);
        }
    }

    public static void save_level(Level level, String name) {
        try {
            // Use class
            OutputStream os = new FileOutputStream("src/levels/" + name + ".lvl");
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeUTF("rooms: [ \n");
            for (int i = 0; i < level.rooms.size(); i++) {
                level.rooms.get(i).saveRoom(out);
            }
            out.writeUTF("]");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Level load_level(String name) {
        Level level = new Level();
        try {
            InputStream is = Level.class.getResourceAsStream("/levels/" + name + ".lvl");

            ObjectInputStream in = new ObjectInputStream(is);
            in.readUTF();
            while (true) {
                String s = in.readUTF(); // {
                if (s.equals("]")) {
                    break;
                }
                Room r = new Room();
                r.loadRoom(in);
                level.rooms.add(r);
                in.readUTF(); // }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return level;
    }

    public int[][] get_bounds() {
        int most_left = Integer.MAX_VALUE;
        int most_right = Integer.MIN_VALUE;
        int most_top = Integer.MAX_VALUE;
        int most_bottom = Integer.MIN_VALUE;
        for (Room r : this.rooms) {
            for (int[][] area : r.areas) {
                if (area[0][0] < most_left) {
                    most_left = area[0][0];
                }
                if (area[0][1] < most_top) {
                    most_top = area[0][1];
                }
                if (area[1][0] > most_right) {
                    most_right = area[1][0];
                }
                if (area[1][1] > most_bottom) {
                    most_bottom = area[1][1];
                }
            }
        }

        return new int[][] { { most_left, most_top }, { most_right, most_bottom } };
    }

    /** @return <Code> { horizontal_map , vertical_map } */
    public boolean[][][] get_wall_maps() {
        int[][] bounds = this.get_bounds();
        int[] size = new int[] { bounds[1][0] - bounds[0][0] + 1, bounds[1][1] - bounds[0][1] + 1 };
        System.out.println("Bounds: " + bounds[0][0] + " " + bounds[0][1] + " " + bounds[1][0] + " " + bounds[1][1]);
        System.out.println("Size: " + size[0] + " " + size[1]);
        boolean[][] horizontal_wall_map = new boolean[size[0]][size[1]];
        boolean[][] vertical_wall_map = new boolean[size[0]][size[1]];
        for (Room r : this.rooms) {
            for (int[][] wall : r.walls) {
                int[] top_left = new int[] { wall[0][0] - bounds[0][0], wall[0][1] - bounds[0][1] };
                int[] bottom_right = new int[] { wall[1][0] - bounds[0][0], wall[1][1] - bounds[0][1] };
                System.out.println(
                        "Wall: " + top_left[0] + " " + top_left[1] + " " + bottom_right[0] + " " + bottom_right[1]);
                boolean horizontal = bottom_right[1] == top_left[1];
                boolean vertical = bottom_right[0] == top_left[0];
                if (vertical) {
                    for (int y = top_left[1]; y < bottom_right[1]; y++) {
                        vertical_wall_map[top_left[0]][y] = true;
                        System.out.println("Vertical wall at: " + top_left[0] + " " + y);
                    }
                } else if (horizontal) {
                    for (int x = top_left[0]; x < bottom_right[0]; x++) {
                        horizontal_wall_map[x][top_left[1]] = true;
                        System.out.println("Horizontal wall at: " + x + " " + top_left[1]);
                    }
                }

            }
        }
        return new boolean[][][] { horizontal_wall_map, vertical_wall_map };
    }

    public HashMap<String, Door> get_door_map() {
        HashMap<String, Door> door_map = new HashMap<String, Door>();
        int[] bounds = this.get_bounds()[0];
        for (Room r : this.rooms) {
            for (Door d : r.doors) {
                Door door = new Door(new int[][]{{d.position[0][0] - bounds[0], d.position[0][1] - bounds[1]}, {d.position[1][0] - bounds[0], d.position[1][1] - bounds[1]}}, d.open);
                int x_direction = d.position[0][0] < d.position[1][0] ? 1 : -1;
                int y_direction = d.position[0][1] < d.position[1][1] ? 1 : -1;
                int x_length = Math.abs(d.position[0][0] - d.position[1][0]);
                int y_length = Math.abs(d.position[0][1] - d.position[1][1]);
                for(int x = 0; x <= x_length; x++) {
                    for(int y = 0; y <= y_length; y++) {
                        int x_index = door.position[0][0] + x*x_direction;
                        int y_index = door.position[0][1] + y*y_direction;
                        door_map.put(x_index + " " + y_index, door);
                        System.out.println("Door at: " + x_index + " " + y_index);
                    }
                }
            }
        }
        return door_map;
    }

    public HashMap<String, Button> get_button_map() {
        HashMap<String, Button> button_map = new HashMap<String, Button>();
        int[] bounds = this.get_bounds()[0];
        for (Room r : this.rooms) {
            for (Button b : r.buttons) {
                Button button = new Button(b.x - bounds[0], b.y - bounds[1], b.orientation);
                button.events = b.events;
                button_map.put(button.x + " " + button.y, button);
                System.out.println("Button at: " + button.x + " " + button.y);
            }
        }
        return button_map;
    }

}
