package base;

import java.awt.event.MouseEvent;

/**
 * Stores static variables which track mouseMovement
 * <p>
 * {@link #mouseX}
 * <p>
 * {@link #mouseY}
 */
public class MouseMotionListener implements java.awt.event.MouseMotionListener {
	public static int mouseX;
	public static int mouseY;

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

}
