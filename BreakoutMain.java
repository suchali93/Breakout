import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BreakoutMain {
	private BreakoutMain(int fpsArg, int speedArg) {

		JFrame f = new JFrame("Breakout"); // jframe is the app window
		f.setPreferredSize(new Dimension(600, 550));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Breakout game = new Breakout(600,500);
		f.add(game, BorderLayout.CENTER);

		TopPanel panel = game.tp;
		game.frameRate = fpsArg;
		game.ballVel = speedArg;
		f.add(panel, BorderLayout.NORTH);


		JButton startButton = new JButton("Start");
		JButton pauseButton = new JButton("Pause");
		JButton quitButton = new JButton("Quit");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// game.startGame();
			}
		});
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// game.stopGame();
			}
		});
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// game.exitGame();
			}
		});
		// panel.add(startButton);
		// panel.add(pauseButton);
		// panel.add(quitButton);

		// f.setResizable(false);
		f.pack();
		f.setVisible(true);

		game.setupGame(600,500);
		game.updateTopPanel(panel);
		game.startGame();
		game.runGame();
	}

	public static void main(String[] args) {
		int fpsArg=60, speedArg=7;
		if (args.length > 0) {
		    try {
		        fpsArg = Integer.parseInt(args[0]);
		        speedArg = Integer.parseInt(args[1]);
		    } catch (NumberFormatException e) {
		        System.err.println("Please enter an integer!");
		        System.exit(1);
		    }
		}
		new BreakoutMain(fpsArg, speedArg);
	}

}