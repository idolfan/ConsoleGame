package base;

import java.awt.event.MouseEvent;

/** Listens for mousebutton-events and transfers them to the {@linkplain KeyHandler} */
public class MouseListener implements java.awt.event.MouseListener {

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		KeyHandler.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		KeyHandler.mouseReleased(e);

	}

}
