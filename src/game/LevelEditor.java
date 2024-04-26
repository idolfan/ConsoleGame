package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import base.KeyHandler;
import base.MouseMotionListener;
import base.RenderPanel;

public class LevelEditor {

    public static int[] camera_position = new int[2];
    public static Level visible_level = new Level();
    public static int selected_room_index = -1;
    public static int selected_area_index = -1;
    public static int selected_wall_index = -1;

    public static ArrayList<Integer> selected_room_indexes = new ArrayList<Integer>();
    public static ArrayList<Integer> selected_area_indexes = new ArrayList<Integer>();
    public static ArrayList<Integer> selected_wall_indexes = new ArrayList<Integer>();

    public static int creation_start[];
    public static int creation_end[];
    public static String creation_type = "AREA";

    public static int drag_selection_start[];
    public static int drag_selection_end[];
    public static Drag_Selection_Mode drag_selection_mode = Drag_Selection_Mode.WALL;

    enum Drag_Selection_Mode {
        AREA, WALL
    }

    public static void handleInputs() {
        int WIDTH = RenderPanel.WIDTH;
        int HEIGHT = RenderPanel.HEIGHT;
        int[] mouse_screen_pos = { MouseMotionListener.mouseX, MouseMotionListener.mouseY };
        int[] mouse_world_pos = { mouse_screen_pos[0] - WIDTH / 2 + camera_position[0],
                mouse_screen_pos[1] - HEIGHT / 2 + camera_position[1] };

        int[] new_selections = KeyHandler.expendKeys(new String[] { "A", "W", "D", "B" },
                new String[] { "SHIFT", "SHIFT", "SHIFT", "SHIFT"},
                new String[] { "C" });
        if (new_selections.length > 0) {
            switch (new_selections[0]) {
                case 0 -> creation_type = "AREA";
                case 1 -> creation_type = "WALL";
                case 2 -> creation_type = "DOOR";
                case 3 -> creation_type = "BUTTON";
            }
        }

        // Move Camera
        if (KeyHandler.useKeyForTick("W")) {
            camera_position[1] -= 800 / RenderPanel.tick;
        }
        if (KeyHandler.useKeyForTick("A")) {
            camera_position[0] -= 800 / RenderPanel.tick;
        }
        if (KeyHandler.useKeyForTick("S")) {
            camera_position[1] += 800 / RenderPanel.tick;
        }
        if (KeyHandler.useKeyForTick("D")) {
            camera_position[0] += 800 / RenderPanel.tick;
        }

        if (KeyHandler.useKeyForTick("0")) {
            camera_position[0] = 0;
            camera_position[1] = 0;
        }

        if (KeyHandler.consumeKey("R")) {
            visible_level.rooms.add(new game.Room());
            selected_room_index = visible_level.rooms.size() - 1;
            selected_area_index = -1;
            creation_start = null;
            creation_end = null;
        }
        if (KeyHandler.consumeKey("K")) {
            Level.save_level(visible_level, "test");
        }
        if (KeyHandler.consumeKey("L")) {
            selected_room_index = -1;
            selected_area_index = -1;
            visible_level = Level.load_level("test");
        }
        if (KeyHandler.consumeKey("T")) {
            if (drag_selection_mode == Drag_Selection_Mode.AREA) {
                drag_selection_mode = Drag_Selection_Mode.WALL;
            } else {
                drag_selection_mode = Drag_Selection_Mode.AREA;
            }
        }

        // Drag new Area
        if (KeyHandler.consumeKey("Q")) {
            creation_start = new int[] { mouse_world_pos[0], mouse_world_pos[1] };
        } else if (!KeyHandler.keyCurrentlyConsumed("Q") && creation_start != null && creation_end != null) {
            if (selected_room_index != -1) {
                Room room = visible_level.rooms.get(selected_room_index);
                switch (creation_type) {
                    case "AREA" -> room.add_area(creation_start, creation_end);
                    case "WALL" -> room.add_wall(creation_start, creation_end);
                    case "DOOR" -> room.add_door(creation_start, creation_end);
                    case "BUTTON" -> room.add_button(creation_start, creation_end);
                }
                selected_area_index = room.areas.size() - 1;
                selected_wall_index = room.walls.size() - 1;
            }
            creation_start = null;
            creation_end = null;
        }
        if (KeyHandler.keyCurrentlyConsumed("Q")) {
            if (creation_start != null)
                creation_end = new int[] { mouse_world_pos[0], mouse_world_pos[1] };
            if (KeyHandler.consumeKey("RIGHTCLICK")) {
                creation_start = null;
                creation_end = null;
            }
        }

        if (KeyHandler.keyCurrentlyConsumed("LEFTCLICK")) {
            if (drag_selection_start != null)
                drag_selection_end = new int[] { mouse_world_pos[0], mouse_world_pos[1] };
            if (KeyHandler.consumeKey("RIGHTCLICK")) {
                drag_selection_start = null;
                drag_selection_end = null;
            }
        }
        // Drag Selection
        if (KeyHandler.keyCurrentlyPressed("LEFTCLICK")) {
            drag_selection_start = new int[] { mouse_world_pos[0], mouse_world_pos[1] };
        } else if (!KeyHandler.keyCurrentlyConsumed("LEFTCLICK") &&
                drag_selection_start != null && drag_selection_end != null) {

            if (selected_room_index != -1) {
                selected_area_index = -1;
                selected_wall_index = -1;

                if (drag_selection_mode == Drag_Selection_Mode.AREA)
                    selected_area_indexes = drag_select_area(drag_selection_start, drag_selection_end);
                else
                    selected_wall_indexes = drag_select_wall(drag_selection_start, drag_selection_end);
            }

            drag_selection_start = null;
            drag_selection_end = null;
        }

        if (KeyHandler.consumeKey("RIGHT")) {
            selected_area_index = -1;
            selected_room_index += 1;
            if (selected_room_index >= visible_level.rooms.size()) {
                selected_room_index = 0;
            }
        }
        if (KeyHandler.consumeKey("LEFT")) {
            selected_area_index = -1;
            selected_room_index -= 1;
            if (selected_room_index < 0) {
                selected_room_index = visible_level.rooms.size() - 1;
            }
        }

        if (KeyHandler.consumeKey("LEFTCLICK")) {
            selected_area_indexes.clear();
            selected_wall_indexes.clear();
            selected_room_indexes.clear();

            int[] selected = select(mouse_world_pos);
            if (selected != null) {
                selected_room_index = selected[0];
                selected_area_index = selected[1];
                System.out.println("Selected: " + selected_room_index + " " + selected_area_index);
            }
        }
        if (KeyHandler.expendKeys(new String[] { "DELETE" }, new String[] { "SHIFT" },
                new String[] { "C" }).length > 0) {
            // Delete Room
            if (selected_room_index != -1) {
                visible_level.rooms.remove(selected_room_index);
                selected_room_index = -1;
                selected_area_index = -1;
            }
        }
        if (KeyHandler.consumeKey("DELETE")) {
            /*
             * if (selected_room_index != -1 && selected_area_index != -1) {
             * visible_level.rooms.get(selected_room_index).areas.remove(selected_area_index
             * );
             * selected_area_index = -1;
             * }
             */
            // sort selected indexes
            selected_area_indexes.sort(null);
            selected_wall_indexes.sort(null);

            for (int i = selected_area_indexes.size() - 1; i >= 0; i--) {

                visible_level.rooms.get(selected_room_index).areas.remove((int) selected_area_indexes.get(i));
            }
            for (int i = selected_wall_indexes.size() - 1; i >= 0; i--) {
                visible_level.rooms.get(selected_room_index).walls.remove((int) selected_wall_indexes.get(i));
            }

            selected_area_indexes.clear();
            selected_wall_indexes.clear();

        }
    }

    public static void tick() {
        handleInputs();
        /*
         * System.out.println("RoomCount:" + visible_level.rooms.size() +
         * " SelectedRoom:" + selected_room_index);
         */
    }

    public static void draw(Graphics2D g2d) {
        int WIDTH = RenderPanel.WIDTH;
        int HEIGHT = RenderPanel.HEIGHT;
        int offset[] = { (camera_position[0] - WIDTH / 2) % Level.GRID_SIZE,
                (camera_position[1] - HEIGHT / 2) % Level.GRID_SIZE };

        g2d.translate(-camera_position[0] + WIDTH / 2, -camera_position[1] + HEIGHT / 2);
        visible_level.LevelEditor_draw(g2d, selected_room_index, selected_area_index);
        // Draw new area
        if (creation_start != null && creation_end != null) {
            if (creation_type.equals("AREA")) {
                g2d.setColor(new Color(200, 100, 100, 100));
                int[][] area = Room.to_area(creation_start, creation_end);
                g2d.fillRect(area[0][0] * Level.GRID_SIZE, area[0][1] * Level.GRID_SIZE,
                        (area[1][0] - area[0][0]) * Level.GRID_SIZE, (area[1][1] - area[0][1]) * Level.GRID_SIZE);
            } else if (creation_type.equals("WALL")) {
                g2d.setColor(new Color(100, 100, 200, 200));
                int[][] wall = Room.to_wall(creation_start, creation_end);
                if (wall != null) {
                    g2d.setStroke(new java.awt.BasicStroke(3));
                    g2d.drawLine(wall[0][0] * Level.GRID_SIZE, wall[0][1] * Level.GRID_SIZE,
                            wall[1][0] * Level.GRID_SIZE,
                            wall[1][1] * Level.GRID_SIZE);
                    g2d.setStroke(new java.awt.BasicStroke(1));
                }
            } else if (creation_type.equals("DOOR")) {
                g2d.setColor(new Color(100, 100, 200, 200));
                int[][] door = Room.to_wall(creation_start, creation_end);
                if (door != null) {
                    g2d.setStroke(new java.awt.BasicStroke(3));
                    g2d.drawLine(door[0][0] * Level.GRID_SIZE, door[0][1] * Level.GRID_SIZE,
                            door[1][0] * Level.GRID_SIZE,
                            door[1][1] * Level.GRID_SIZE);
                    g2d.setStroke(new java.awt.BasicStroke(1));
                }
            }
        }
        // Draw drag selection
        if (drag_selection_start != null && drag_selection_end != null) {
            g2d.setColor(new Color(100, 200, 100, 100));
            g2d.fillRect(drag_selection_start[0], drag_selection_start[1],
                    drag_selection_end[0] - drag_selection_start[0],
                    drag_selection_end[1] - drag_selection_start[1]);
        }
        g2d.translate(camera_position[0] - WIDTH / 2, camera_position[1] - HEIGHT / 2);

        // Draw Grid
        g2d.setColor(new Color(255, 255, 255, 50));
        for (int x = 0; x < WIDTH + Level.GRID_SIZE; x += Level.GRID_SIZE) {
            g2d.drawLine(x - offset[0], 0, x - offset[0], HEIGHT);
        }
        for (int y = 0; y < HEIGHT + Level.GRID_SIZE; y += Level.GRID_SIZE) {
            g2d.drawLine(0, y - offset[1], WIDTH, y - offset[1]);
        }

        displayDebugInfo(g2d);
    }

    public static void displayDebugInfo(Graphics2D g2d) {
        //
        int height = 20;
        g2d.setColor(new Color(255, 255, 255));
        g2d.drawString("Selected Room: " + selected_room_index, 10, height);
        height += 20;
        g2d.drawString("Selected Area: " + selected_area_index, 10, height);
        height += 20;
        g2d.drawString("Drag Selection Mode: " + drag_selection_mode, 10, height);
        height += 20;
        g2d.drawString("Camera Position: " + camera_position[0] + " " + camera_position[1], 10, height);
        height += 20;
        g2d.drawString("Creation Type: " + creation_type, 10, height);
        height += 40;
        g2d.drawString("CONTROLS:", 10, height);
        height += 20;
        g2d.drawString("WASD: Move Camera", 10, height);
        height += 20;
        g2d.drawString("0: Reset Camera", 10, height);
        height += 20;
        g2d.drawString("Q: Start/End Creation", 10, height);
        height += 20;
        g2d.drawString("A / W / D: Select Creation Type (Area / Wall / Door)", 10, height);
        height += 20;
        g2d.drawString("R: New Room", 10, height);
        height += 20;
        g2d.drawString("L: Load Level", 10, height);
        height += 20;
        g2d.drawString("K: Save Level", 10, height);
        height += 20;
        g2d.drawString("Left/Right: Select Room", 10, height);
        height += 20;
        g2d.drawString("LeftClick: Select Area", 10, height);
        height += 20;
        g2d.drawString("Delete: Delete Room", 10, height);
        height += 20;
        g2d.drawString("Shift + Delete: Delete Area", 10, height);
        height += 20;
        g2d.drawString("T: Toggle Drag Selection Mode", 10, height);
        height += 20;
    }

    public static int[] select(int[] mouse_world_pos) {
        for (int i = 0; i < visible_level.rooms.size(); i++) {
            Room room = visible_level.rooms.get(i);
            for (int j = 0; j < room.areas.size(); j++) {
                int[][] area = room.areas.get(j);
                int[] p1 = area[0];
                int[] p2 = area[1];
                if (mouse_world_pos[0] > p1[0] && mouse_world_pos[0] < p2[0] && mouse_world_pos[1] > p1[1]
                        && mouse_world_pos[1] < p2[1]) {
                    return new int[] { i, j };
                }
            }
        }
        return null;
    }

    /**
     * @return Returns an Array containing all indexes of areas which intersect the
     *         drag selection.
     */
    public static ArrayList<Integer> drag_select_area(int[] p1_ds, int[] p2_ds) {

        ArrayList<Integer> selected_areas = new ArrayList<Integer>();

        int greater_x = p1_ds[0] > p2_ds[0] ? p1_ds[0] : p2_ds[0];
        int smaller_x = p1_ds[0] < p2_ds[0] ? p1_ds[0] : p2_ds[0];
        int greater_y = p1_ds[1] > p2_ds[1] ? p1_ds[1] : p2_ds[1];
        int smaller_y = p1_ds[1] < p2_ds[1] ? p1_ds[1] : p2_ds[1];

        Room room = visible_level.rooms.get(selected_room_index);
        for (int j = 0; j < room.areas.size(); j++) {
            int[][] area = room.areas.get(j);
            int[] p1 = new int[] { area[0][0] * Level.GRID_SIZE, area[0][1] * Level.GRID_SIZE };
            int[] p2 = new int[] { area[1][0] * Level.GRID_SIZE, area[1][1] * Level.GRID_SIZE };

            boolean selection_is_left = p1[0] > smaller_x && p1[0] > greater_x;
            boolean selection_is_right = p2[0] < smaller_x && p2[0] < greater_x;
            boolean selection_is_top = p1[1] > smaller_y && p1[1] > greater_y;
            boolean selection_is_bottom = p2[1] < smaller_y && p2[1] < greater_y;

            if (selection_is_left || selection_is_right || selection_is_top || selection_is_bottom)
                continue;

            selected_areas.add(j);

        }

        return selected_areas;
    }

    /**
     * @return Returns an Array containing all indexes of areas which intersect the
     *         drag selection.
     */
    public static ArrayList<Integer> drag_select_wall(int[] p1_ds, int[] p2_ds) {

        ArrayList<Integer> selected_walls = new ArrayList<Integer>();

        int greater_x = p1_ds[0] > p2_ds[0] ? p1_ds[0] : p2_ds[0];
        int smaller_x = p1_ds[0] < p2_ds[0] ? p1_ds[0] : p2_ds[0];
        int greater_y = p1_ds[1] > p2_ds[1] ? p1_ds[1] : p2_ds[1];
        int smaller_y = p1_ds[1] < p2_ds[1] ? p1_ds[1] : p2_ds[1];

        Room room = visible_level.rooms.get(selected_room_index);
        for (int j = 0; j < room.walls.size(); j++) {
            int[][] wall = room.walls.get(j);
            int[] p1 = new int[] { wall[0][0] * Level.GRID_SIZE, wall[0][1] * Level.GRID_SIZE };
            int[] p2 = new int[] { wall[1][0] * Level.GRID_SIZE, wall[1][1] * Level.GRID_SIZE };

            boolean selection_is_left = p1[0] > smaller_x && p1[0] > greater_x;
            boolean selection_is_right = p2[0] < smaller_x && p2[0] < greater_x;
            boolean selection_is_top = p1[1] > smaller_y && p1[1] > greater_y;
            boolean selection_is_bottom = p2[1] < smaller_y && p2[1] < greater_y;

            if (selection_is_left || selection_is_right || selection_is_top || selection_is_bottom)
                continue;

            selected_walls.add(j);
        }

        return selected_walls;
    }

}
