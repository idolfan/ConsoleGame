package base;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import game.Game;
import game.LevelEditor;
import networking.VoiceServer;

public class RenderPanel extends JPanel implements Runnable {
	private Thread thread;
	private boolean running = false;
	public boolean paused = false;
	public final static int WIDTH = 1920;
	public final static int HEIGHT = 1080;
	public static final int fps = 165;
	public static double tick = 64;

	public static Mode mode = Mode.GAME;

	enum Mode {
		GAME, LEVELEDITOR, MENU
	}

	public RenderPanel() {
		/** Insert current app */
		/***********************/
		this.setFocusable(true);
		this.addKeyListener(new KeyHandler());
		this.addMouseListener(new MouseListener());
		this.addMouseMotionListener(new MouseMotionListener());
		this.addMouseWheelListener(new MouseWheelListener());

		thread = new Thread(this);
		thread.start();
	}

	public void init() {
	}

	public void start() {
		running = true;
		thread.start();
	}

	public void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void tick() {
		KeyHandler.tick();
		VoiceServer.process();

		if (KeyHandler.consumeKey("F1")) {
			if (mode == Mode.GAME) {
				mode = Mode.LEVELEDITOR;
			} else {
				mode = Mode.GAME;
			}
		}

		switch (mode) {
			case GAME -> {
				Game.tick();
			}
			case LEVELEDITOR ->
				LevelEditor.tick();
			case MENU -> {
			}
		}

		/** Insert current app */

		/***********************/
	}

	public void render() {
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(0, 0, 0));
		g2d.fillRect(0, 0, WIDTH, HEIGHT);

		switch (mode) {
			case GAME -> {
				Game.draw(g2d);
			}
			case LEVELEDITOR -> {
				LevelEditor.draw(g2d);
			}
			case MENU -> {
			}

		}
	}

	public void setMode(Mode mode) {
	}

	public void run() {
		init();

		int tickTime = 0;
		int tickCounter = 0;
		int renderTime = 0;
		int renderCounter = 0;
		running = true;
		long initialTime = System.nanoTime();
		final double timeF = 1000000000 / fps;
		double deltaT = 0, deltaF = 0;
		int frames = 0, ticks = 0;
		long timer = System.currentTimeMillis();
		while (running) {
			double timeT = 1000000000 / tick;
			if (!paused && running) {
				long currentTime = System.nanoTime();
				deltaT += (currentTime - initialTime) / timeT;
				deltaF += (currentTime - initialTime) / timeF;
				initialTime = currentTime;

				if (deltaT >= 1) {
					tickCounter += 1;
					tickTime -= System.nanoTime();
					tick();
					tickTime += System.nanoTime();
					ticks++;
					deltaT--;
				}

				if (deltaF >= 1) {
					renderCounter += 1;
					renderTime -= System.nanoTime();
					render();
					renderTime += System.nanoTime();
					frames++;
					deltaF--;
				}

				if (System.currentTimeMillis() - timer > 1000) {
					// Test-Counter every sec
					System.out.println("TPS:" + ticks + "(" + tickTime / tickCounter / 1000 + " micS)" + "FPS:"
							+ frames + " (" + renderTime / renderCounter / 1000 + " micS)");
					/*
					 * System.out.println("testCounter: " + (double) testCounter[0] / fps + " "
					 * + (double) testCounter[1] / ticks);
					 */
					tickCounter = 0;
					tickTime = 0;
					renderCounter = 0;
					renderTime = 0;
					frames = 0;
					ticks = 0;
					timer += 1000;
				}
			}
		}
	}
}
