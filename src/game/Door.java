package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.ObjectOutputStream;

public class Door {

    public static Color color = new Color(139, 69, 19, 255);

    public final int[][] position;
    public final int[][] wall_positions;
    public final int length;

    public boolean open;
    public boolean locked = false;

    public Door(int[][] position, boolean open) {
        this.position = position;
        this.open = open;
        this.length = Math.abs(position[0][0] - position[1][0]) + Math.abs(position[0][1] - position[1][1]);
        int smaller_x = position[0][0] < position[1][0] ? position[0][0] : position[1][0];
        int smaller_y = position[0][1] < position[1][1] ? position[0][1] : position[1][1];

        wall_positions = new int[length][2];
        for(int i = 0; i < length; i++) {
            wall_positions[i] = new int[] {smaller_x + (position[0][0] == position[1][0] ? 0 : i), smaller_y + (position[0][1] == position[1][1] ? 0 : i)};
        }

    }

    public void save(ObjectOutputStream oos) {
        try {
            oos.writeUTF("[" + position[0][0] + ", " + position[0][1] + ", " + position[1][0] + ", " + position[1][1]
                    + ", " + open + "], \n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.setStroke(new java.awt.BasicStroke(5));
        if (open) {
            int x_direction = position[0][0] == position[1][0] ? 0 : (position[0][0] < position[1][0] ? 1 : -1);
            int y_direction = position[0][1] == position[1][1] ? 0 : (position[0][1] < position[1][1] ? 1 : -1);
            int[] inneredge_1 = position[0];
            double[] outeredge_1 = { inneredge_1[0] + (-x_direction - y_direction) * length * 0.35,
                    inneredge_1[1] + (x_direction - y_direction) * length * 0.35 };
            int[] inneredge_2 = position[1];
            double[] outeredge_2 = { inneredge_2[0] + (x_direction - y_direction) * length * 0.35,
                    inneredge_2[1] + (x_direction + y_direction) * length * 0.35 };
            g.drawLine(inneredge_1[0] * Level.GRID_SIZE, inneredge_1[1] * Level.GRID_SIZE,
                    (int) (outeredge_1[0] * Level.GRID_SIZE), (int) (outeredge_1[1] * Level.GRID_SIZE));
            g.drawLine(inneredge_2[0] * Level.GRID_SIZE, inneredge_2[1] * Level.GRID_SIZE,
                    (int) (outeredge_2[0] * Level.GRID_SIZE), (int) (outeredge_2[1] * Level.GRID_SIZE));
        } else {
            g.drawLine(position[0][0] * Level.GRID_SIZE, position[0][1] * Level.GRID_SIZE,
                    position[1][0] * Level.GRID_SIZE, position[1][1] * Level.GRID_SIZE);
        }
        
        g.setStroke(new java.awt.BasicStroke(1));
    }

    public void set_open(boolean open) {
        if(locked) return;
        boolean horizontal = position[0][0] != position[1][0];
        if (horizontal)
            for (int[] wall_position : wall_positions) {
                Game.horizontal_wall_map[wall_position[0]][wall_position[1]] = !open;
            }
        if (!horizontal)
            for (int[] wall_position : wall_positions) {
                Game.vertical_wall_map[wall_position[0]][wall_position[1]] = !open;
            }
        this.open = open;
    }

}
