package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import base.KeyHandler;
import base.RenderPanel;
import base.SoundRecorder;
import interactables.Button;
import networking.Client;
import networking.Message;
import networking.Server;
import networking.VoiceClient;
import networking.VoiceServer;

public class Game {

    public static Level loaded_level;
    /** Width and height of the level in cells */
    public static boolean[][] horizontal_wall_map;
    public static boolean[][] vertical_wall_map;
    public static HashMap<String, Door> doors = new HashMap<String, Door>();
    public static HashMap<String, int[]> players = new HashMap<String, int[]>();
    public static HashMap<String, Button> buttons = new HashMap<String, Button>();

    public static int[][] bounds;
    public static int WIDTH;
    public static int HEIGHT;
    public static int GRID_SIZE;
    public static int left_camera_edge;
    public static int right_camera_edge;
    public static int top_camera_edge;
    public static int bottom_camera_edge;

    public static SoundRecorder soundRecorder = new SoundRecorder();

    public static int[] camera_position = new int[] { Level.GRID_SIZE / 2, Level.GRID_SIZE / 2 };
    public static int[] player_cell_position = new int[] { 10, 10 };



    public static void handleInputs() {

        if (KeyHandler.consumeKey("W"))
            move_up: {
                boolean blocked_path = horizontal_wall_map[player_cell_position[0]][player_cell_position[1]];
                if (blocked_path)
                    break move_up;
                player_cell_position[1]--;
                Client.writeMessage(Message.send_player_position(player_cell_position[0], player_cell_position[1]));
            }
        if (KeyHandler.consumeKey("A"))
            move_left: {
                boolean blocked_path = vertical_wall_map[player_cell_position[0]][player_cell_position[1]];
                if (blocked_path)
                    break move_left;
                player_cell_position[0]--;
                Client.writeMessage(Message.send_player_position(player_cell_position[0], player_cell_position[1]));
            }
        if (KeyHandler.consumeKey("S"))
            move_down: {
                boolean blocked_path = horizontal_wall_map[player_cell_position[0]][player_cell_position[1] + 1];
                if (blocked_path)
                    break move_down;
                player_cell_position[1]++;
                Client.writeMessage(Message.send_player_position(player_cell_position[0], player_cell_position[1]));
            }
        if (KeyHandler.consumeKey("D"))
            move_right: {
                boolean blocked_path = vertical_wall_map[player_cell_position[0] + 1][player_cell_position[1]];
                if (blocked_path)
                    break move_right;
                player_cell_position[0]++;
                Client.writeMessage(Message.send_player_position(player_cell_position[0], player_cell_position[1]));
            }
        if (KeyHandler.consumeKey("E"))
            open_door: {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        int[] door_pos = new int[] { player_cell_position[0] + i, player_cell_position[1] + j };
                        String key = door_pos[0] + " " + door_pos[1];

                        Door door = doors.get(key);
                        if (door != null) {
                            Client.writeMessage(Message.send_open_door(door_pos[0], door_pos[1], !door.open));
                            break open_door;

                        }
                    }
                }
            }

        if (KeyHandler.useKeyForTick("0")) {
            player_cell_position = new int[] { 0, 0 };
        }

        if (KeyHandler.consumeKey("L")) {
            loaded_level = Level.load_level("test");

            boolean[][][] wall_maps = loaded_level.get_wall_maps();
            horizontal_wall_map = wall_maps[0];
            vertical_wall_map = wall_maps[1];

            HashMap<String, Door> door_map = loaded_level.get_door_map();
            System.out.println("Doors: " + door_map.size());
            doors = door_map;
            buttons = loaded_level.get_button_map();

            bounds = loaded_level.get_bounds();

        }

        if(KeyHandler.consumeKey("C")) {
            Client.startClient("idolfan.ddns.net", 8888);
        }

        if(KeyHandler.consumeKey("H")) host : {
            if (loaded_level == null)
                break host;
            Server.startServer(new int[] { 8888 });
        }


        // Voice
        if (KeyHandler.consumeKey("V")) {
            soundRecorder.startRecording();
        }
        if (KeyHandler.consumeKey("B")) {
            soundRecorder.stopRecording();
        }

        if (KeyHandler.consumeKey("O")) {
            VoiceServer.startServer(new int[] { 8889 });
        }
        if (KeyHandler.consumeKey("P")) {
            VoiceClient.startClient("idolfan.ddns.net", 8889);
        }

    }

    public static void tick() {
        handleInputs();

    }

    public static void draw(Graphics2D g) {
        if (loaded_level == null)
            return;

        WIDTH = RenderPanel.WIDTH;
        HEIGHT = RenderPanel.HEIGHT;
        GRID_SIZE = Level.GRID_SIZE;

        camera_position = new int[] { player_cell_position[0] * GRID_SIZE + GRID_SIZE / 2,
                player_cell_position[1] * GRID_SIZE + GRID_SIZE / 2 };

        left_camera_edge = (camera_position[0] - RenderPanel.WIDTH / 2) / GRID_SIZE - 1;
        right_camera_edge = (camera_position[0] + RenderPanel.WIDTH / 2) / GRID_SIZE + 1;
        top_camera_edge = (camera_position[1] - RenderPanel.HEIGHT / 2) / GRID_SIZE - 1;
        bottom_camera_edge = (camera_position[1] + RenderPanel.HEIGHT / 2) / GRID_SIZE + 1;

        double[] observer_pos = new double[] { camera_position[0] / (double) GRID_SIZE,
                camera_position[1] / (double) GRID_SIZE };


        int[] camera_translation = new int[] { -camera_position[0] + RenderPanel.WIDTH / 2,
                -camera_position[1] + RenderPanel.HEIGHT / 2 };

        g.translate(camera_translation[0], camera_translation[1]);

        ArrayList<int[][]> polygons = new ArrayList<int[][]>();
        // Horizontal walls
        for (int x = left_camera_edge; x < right_camera_edge; x++) {
            for (int y = top_camera_edge; y < bottom_camera_edge; y++) {
                if (x < 0 || y < 0 || x >= horizontal_wall_map.length || y >= horizontal_wall_map[0].length)
                    continue;
                if (horizontal_wall_map[x][y]) {
                    int[] top_left = new int[] { x * GRID_SIZE, y * GRID_SIZE };
                    int[] bottom_right = new int[] { (x + 1) * GRID_SIZE, (y + 1) * GRID_SIZE };
                    g.setColor(new Color(255, 255, 255, 200));
                    g.setStroke(new BasicStroke(3));
                    g.drawLine(top_left[0], top_left[1], bottom_right[0], top_left[1]);
                    g.setStroke(new BasicStroke(1));

                    int[][] polygon = get_shadow_polygon(new int[][] { { x, y }, { x + 1, y } }, observer_pos);
                    if (polygon != null) {
                        polygons.add(polygon);
                    }
                }

            }
        }
        // Vertical walls
        for (int x = left_camera_edge; x < right_camera_edge; x++) {
            for (int y = top_camera_edge; y < bottom_camera_edge; y++) {
                if (x < 0 || y < 0 || x >= vertical_wall_map.length || y >= vertical_wall_map[0].length)
                    continue;
                if (vertical_wall_map[x][y]) {
                    int[] top_left = new int[] { x * GRID_SIZE, y * GRID_SIZE };
                    int[] bottom_right = new int[] { (x + 1) * GRID_SIZE, (y + 1) * GRID_SIZE };
                    g.setColor(new Color(255, 255, 255, 200));
                    g.setStroke(new BasicStroke(3));
                    g.drawLine(top_left[0], top_left[1], top_left[0], bottom_right[1]);
                    g.setStroke(new BasicStroke(1));

                    int[][] polygon = get_shadow_polygon(new int[][] { { x, y }, { x, y + 1 } }, observer_pos);
                    if (polygon != null) {
                        polygons.add(polygon);
                    }
                }
            }
        }

        // Draw doors
        for (Door door : doors.values()) {
            door.draw(g);
        }

        // Draw buttons
        for (Button button : buttons.values()) {
            button.draw(g);
        }

        g.translate(-camera_translation[0], -camera_translation[1]);

        int offset[] = { (camera_position[0] - WIDTH / 2) % Level.GRID_SIZE + Level.GRID_SIZE,
                (camera_position[1] - HEIGHT / 2) % Level.GRID_SIZE + Level.GRID_SIZE };
        g.translate(-offset[0], -offset[1]);

        // Draw grid
        g.setColor(new Color(255, 255, 255, 50));
        for (int x = 0; x < WIDTH + 1 * Level.GRID_SIZE; x += Level.GRID_SIZE) {
            g.drawLine(x, 0, x, HEIGHT + Level.GRID_SIZE);
        }
        for (int y = 0; y < HEIGHT + 1 * Level.GRID_SIZE; y += Level.GRID_SIZE) {
            g.drawLine(0, y, WIDTH + Level.GRID_SIZE, y);
        }

        g.translate(offset[0], offset[1]);
        g.translate(camera_translation[0], camera_translation[1]);

        // Draw Players

        HashMap<String, int[]> players = new HashMap<String, int[]>(Game.players);
        for (int[] player_pos : players.values()) {
            g.setColor(new Color(0, 255, 0, 255));
            g.fillOval(player_pos[0] * GRID_SIZE + GRID_SIZE/2 - 5, player_pos[1] * GRID_SIZE + GRID_SIZE/2 - 5, 10, 10);
        }

        // Draw shadows
        for (int[][] polygon : polygons) {
            g.setColor(new Color(0, 0, 0, 250));
            g.fillPolygon(polygon[0], polygon[1], polygon[0].length);
        }

        /* // Draw Observer
        g.setColor(new Color(255, 0, 0, 255));
        g.fillOval((int) (observer_pos[0] * GRID_SIZE) - 5, (int) (observer_pos[1] * GRID_SIZE) - 5, 10, 10); */

        /* g.translate(-wall_translation[0], -wall_translation[1]); */
        g.translate(-camera_translation[0], -camera_translation[1]);

    }

    public static int[][] get_shadow_polygon(int[][] points, double[] observer_pos) {
        int[] point1 = points[0];
        int[] point2 = points[1];

        double[][] left_border = new double[][] { { left_camera_edge, top_camera_edge },
                { left_camera_edge, bottom_camera_edge } };
        double[][] right_border = new double[][] { { right_camera_edge, top_camera_edge },
                { right_camera_edge, bottom_camera_edge } };
        double[][] top_border = new double[][] { { left_camera_edge, top_camera_edge },
                { right_camera_edge, top_camera_edge } };
        double[][] bottom_border = new double[][] { { left_camera_edge, bottom_camera_edge },
                { right_camera_edge, bottom_camera_edge } };

        double[][][] lines = new double[][][] { {
                { observer_pos[0], observer_pos[1] }, { point1[0], point1[1] } },
                { { observer_pos[0], observer_pos[1] }, { point2[0], point2[1] } } };

        int[][] polygon;

        double[][] polygon_corners = new double[2][2];

        int[][] prefered_corners = new int[2][2];

        for (int i = 0; i < 2; i++) {
            double[][] line = lines[i];
            double[] vector = new double[] { line[1][0] - line[0][0], line[1][1] - line[0][1] };
            double[][] intersections = new double[2][];

            boolean faces_right = vector[0] > 0;
            boolean faces_up = vector[1] < 0;

            if (faces_right)
                intersections[0] = lines_intersection(line, right_border);
            else
                intersections[0] = lines_intersection(line, left_border);
            if (faces_up)
                intersections[1] = lines_intersection(line, top_border);
            else
                intersections[1] = lines_intersection(line, bottom_border);

            if (intersections[0] == null && intersections[1] == null) {
                return null;
            }

            prefered_corners[i][0] = faces_right ? right_camera_edge : left_camera_edge;
            prefered_corners[i][1] = faces_up ? top_camera_edge : bottom_camera_edge;

            if (intersections[0] == null) {
                polygon_corners[i] = intersections[1];
                continue;
            } else if (intersections[1] == null) {
                polygon_corners[i] = intersections[0];
                continue;
            }

            double[][] observer_to_intersections = {
                    { intersections[0][0] - observer_pos[0], intersections[0][1] - observer_pos[1] },
                    { intersections[1][0] - observer_pos[0], intersections[1][1] - observer_pos[1] } };

            double[] lengths = {
                    length(observer_to_intersections[0]),
                    length(observer_to_intersections[1]) };

            if (lengths[0] < lengths[1])
                polygon_corners[i] = intersections[0];
            else
                polygon_corners[i] = intersections[1];

        }

        boolean same_corner = polygon_corners[0][0] == prefered_corners[1][0]
                && polygon_corners[0][1] == prefered_corners[1][1];
        if (same_corner)
            polygon = new int[][] {
                    {
                            points[0][0] * GRID_SIZE, points[1][0] * GRID_SIZE,
                            (int) (polygon_corners[1][0] * GRID_SIZE), prefered_corners[0][0] * GRID_SIZE,
                            (int) (polygon_corners[0][0] * GRID_SIZE) },
                    {
                            points[0][1] * GRID_SIZE, points[1][1] * GRID_SIZE,
                            (int) (polygon_corners[1][1] * GRID_SIZE), prefered_corners[0][1] * GRID_SIZE,
                            (int) (polygon_corners[0][1] * GRID_SIZE) } };
        else
            polygon = new int[][] {
                    {
                            points[0][0] * GRID_SIZE, points[1][0] * GRID_SIZE,
                            (int) (polygon_corners[1][0] * GRID_SIZE), prefered_corners[1][0] * GRID_SIZE,
                            prefered_corners[0][0] * GRID_SIZE, (int) (polygon_corners[0][0] * GRID_SIZE) },
                    {
                            points[0][1] * GRID_SIZE, points[1][1] * GRID_SIZE,
                            (int) (polygon_corners[1][1] * GRID_SIZE), prefered_corners[1][1] * GRID_SIZE,
                            prefered_corners[0][1] * GRID_SIZE, (int) (polygon_corners[0][1] * GRID_SIZE) } };

        return polygon;

    }

    public static double[] lines_intersection(double[][] line1, double[][] line2) {
        double x1 = line1[0][0];
        double y1 = line1[0][1];
        double x2 = line1[1][0];
        double y2 = line1[1][1];
        double x3 = line2[0][0];
        double y3 = line2[0][1];
        double x4 = line2[1][0];
        double y4 = line2[1][1];

        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0)
            return null;

        double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

        return new double[] { xi, yi };
    }

    private static double length(double[] vector) {
        return Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
    }

}
