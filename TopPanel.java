import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TopPanel extends JPanel {
	public int score, balls, blocks, fps, ballspeed;
	public TopPanel(int score, int ballsleft, int blocksleft, int fps, int ballvel) {
		score = score;
		balls = ballsleft;
		blocks = blocksleft;
		fps = fps;
		ballspeed = ballvel;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		g.drawString("SCORE: " + score, 5, 10);
		g.drawString("BALLS LEFT: " + balls, 100, 10);
		g.drawString("BLOCKS LEFT: " + blocks, 240, 10);
		g.drawString("FPS: " + fps, 400, 10);
		g.drawString("SPEED: " + ballspeed, 500, 10);
	}
}