package base;

import java.awt.event.MouseWheelEvent;

public class MouseWheelListener implements java.awt.event.MouseWheelListener {
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		KeyHandler.mouseScrolled(e);
	}

}
