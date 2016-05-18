import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Breakout extends JPanel {
	public Ball ball;
	public int ballVel = 7;
	public int ballsLeft;
	
	public Paddle paddle;
	private int paddleVel = 20;

	private int blockRows = 5, blockCols = 10;
	public Block[][] block = new Block[blockCols][blockRows];
	public int blocksLeft;

	public int score = 0;

	private boolean running = false;
	private boolean paused = false;

	public int frameRate = 0;
	public int fps = frameRate;
	private int frameCount = 0;

	public int winw, winh;

	public TopPanel tp = new TopPanel(score, ballsLeft, blocksLeft, fps, ballVel);

	public Breakout(int wid, int ht) {
		super.setSize(wid, ht);
		setupGame(wid, ht);
		winw = wid;
		winh = ht;

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {
				float speed = 0;
				switch (ke.getID()) {
				case KeyEvent.KEY_PRESSED:
					if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
						if(running) {
							if(paddle.getX() > 0) {
								speed = -paddleVel;
								paddle.paddleMove(speed);
							}
						}
					} else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(running) {
							if(paddle.getX()+paddle.width() < getWidth()) {
								speed = paddleVel;
								paddle.paddleMove(speed);
							}
						};
					} else if (ke.getKeyCode() == KeyEvent.VK_R) {
						if(!running) {
							setupGame(winw, winh);
							running = true;
							runGame();
						}
					} else if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
						if(!running) {
							running = true;
							runGame();
						}
					} 
				}
				return false;
			}
		});
	}

	public void positionBlocks(int w, int h) {
		int windowWidth = w-20;
		int windowHeight = h;
		Color rowColor = Color.RED;
		int padding = 3; // padding b/w 2 blocks 
		for(int i=0; i<blockCols; i++) {
			for(int j=0; j<blockRows; j++) {
				if(j==0) { rowColor = new Color(230, 0, 0); }
				else if(j==1) { rowColor = Color.YELLOW; }
				else if(j==2) { rowColor = Color.GREEN; }
				else if(j==3) { rowColor = Color.BLUE; }
				else if(j==4) { rowColor = new Color(128, 0, 128); }
				block[i][j] = new Block(i*(windowWidth/10)+padding, j*(windowHeight/21)+padding, w/10-2, h/23, rowColor);

			}
		}
	}

	public void newBallPaddle(int w, int h) {
		int ballRadius = w/80;
		int paddleWidth = w/8;
		int paddleHeight = h/35;
		ball = new Ball(w/2, h-paddleHeight*2-(ballRadius*2), ballRadius, ballRadius, Color.BLACK);
		paddle = new Paddle((w/2)-(paddleWidth/2), h-paddleHeight*2, paddleWidth, paddleHeight, new Color(150, 150, 150));
	}

	public void setupGame(int w, int h) {
		blocksLeft = 50;
		ballsLeft = 3;
		score = 0;
		positionBlocks(w,h);
		newBallPaddle(w,h);
	}

	public void startGame() {
		running = true;
	}

	public void stopGame() {
		newBallPaddle(winw, winh);
		running = false;
	}

	public void checkWin(int blocksleft) {
		if(blocksleft == 0) {
			System.out.println("You Win!!!");
			setupGame(winw, winh);
			running = false;
		}
	}

	public void checkLose(int ballsleft) {
		if(ballsleft == 0) {
			System.out.println("You Lose :(");
			setupGame(winw, winh);
			running = false;
		}
	}

	public void runGame() {
		Thread myGame = new Thread() {
			public void run() {
			    game();
			}
		};
		myGame.start();
	}

	// This code for maintaining ball speed while changing FPS was inspired by http://www.java-gaming.org/index.php?topic=24220.0
	public void game() {
		final double TICKS = 30.0;
		final double TIME_BETWEEN_UPDATES = 1000000000 / TICKS;
		final int MAX_UPDATES_BEFORE_RENDER = 60;
		double lastUpdateTime = System.nanoTime();
		double lastRenderTime = System.nanoTime();
		
		final double TARGET_FPS = frameRate;
		final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

		int lastSecondTime = (int) (lastUpdateTime / 1000000000);
		
		while (running) {
			double now = System.nanoTime();
			int updateCount = 0;
			
			if (!paused) {
				while(now-lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER) {
					 ball.ballMove(block, paddle);
					 lastUpdateTime += TIME_BETWEEN_UPDATES;
					 updateCount++;
				}

				if (now-lastUpdateTime > TIME_BETWEEN_UPDATES) {
					lastUpdateTime = now-TIME_BETWEEN_UPDATES;
				}
		 
				float interp = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES) );
				ball.interpolation = interp;
				repaint();
				lastRenderTime = now;
		 
				int thisSecond = (int) (lastUpdateTime / 1000000000);
				if (thisSecond > lastSecondTime) {
					 fps = frameCount;
					 frameCount = 0;
					 lastSecondTime = thisSecond;
				}

				while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
					 Thread.yield();
					 try {Thread.sleep(1);} catch(Exception e) {} 
				
					 now = System.nanoTime();
				}
			}
			updateTopPanel(tp);
		}
	}

	public void updateTopPanel(TopPanel t) {
		t.score = score;
		t.balls = ballsLeft;
		t.blocks = blocksLeft;
		t.fps = frameRate;
		t.ballspeed = ballVel;
		t.repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		ball.draw(g);
		paddle.draw(g);
		for(int i=0; i<10; i++)
			for(int j=0; j<5; j++)
				block[i][j].draw(g);
	}

	class Block {
		public Color color;
		public int xCoord, yCoord;
		public int width, height;
		public boolean hit;

		public Block(int X, int Y, int W, int H, Color C) {
			xCoord = X;
			yCoord = Y;
			width = W;
			height = H;
			color = C;
			hit = false;
		}

		public void draw(Graphics g) {
			if(!hit){
				g.setColor(color);
				g.fill3DRect(xCoord,yCoord,width,height,true);
			}
		}
	}

	private class Ball {
		public float xCoord, yCoord, prevX, prevY;
		public int width, height;
		public float dirX, dirY;

		public Color color;
		public float interpolation;
		public float variance;
			
		public int lastDrawX, lastDrawY;
			
		public Ball(float X, float Y, int W, int H, Color C) {
			xCoord = prevX = X;
			yCoord = prevY = Y;
			width = W;
			height = H;
			
			dirX = (float) 0;
			dirY = (float) ballVel;
			color = C;
			variance = 0.05f;
		}

		public void ballMove(Block[][] block, Paddle paddle) {
			prevX = xCoord;
			prevY = yCoord;
			
			xCoord += dirX;
			yCoord += dirY;
			
			int radius = width/2;
			if (xCoord+radius >= getWidth()) {
				dirX *= -1;
				xCoord = getWidth() - radius;
			} else if (xCoord-radius <= 0) {
				dirX *= -1;
				xCoord = radius;
			}
			
			if (yCoord+radius >= getHeight()) {
				dirY *= -1;
				yCoord = getHeight() - radius;
				ballsLeft--;
				stopGame();
				checkLose(ballsLeft);
			} else if (yCoord-radius <= 0) {
				dirY *= -1;
				yCoord = radius;
			}

			checkCollision();
		}

		public void checkCollision() {
			// 1 - right hit, 2 - left hit, 3 - top hit, 4 - bottom
			float r = (float)width/2;
			int reboundPaddle = hit(xCoord, yCoord, prevX, prevY, (float)paddle.width, (float)paddle.height, (float)paddle.xCoord, (float)paddle.yCoord);

			int rebound = 0;
			boolean collision = false;
			for (int i = 0; i < blockCols; i++) {
				for (int j = 0; j < blockRows; j++) {
					Block temp = block[i][j];
					if (temp.hit)
						continue;
					rebound = hit(xCoord, yCoord, prevX, prevY, (float)temp.width, (float)temp.height, (float)temp.xCoord, (float)temp.yCoord);
					if (rebound > 0) {
						block[i][j].hit = true;
						collision = true;
						blocksLeft--;
						score += 5;
						checkWin(blocksLeft);
						break;
					}
				}
				if (collision == true)
					break;
			}

			float reboundAng = 0;
			if (reboundPaddle > 0) {
				rebound = reboundPaddle;
				reboundAng = (xCoord - (paddle.xCoord+((float)paddle.width/2))) * variance + (float) (Math.random()-0.5) * variance;
			}

			if (rebound == 1 || rebound == 2) {
				dirX *= -1;
				xCoord = prevX;
			} else if (rebound == 3 || rebound == 4) {
				dirY *= -1;
				yCoord = prevY;
				dirX += reboundAng;
			}
		}
			
		public void draw(Graphics g) {
			int radius = width/2;
			g.setColor(color);
			int x = (int) ((xCoord - prevX) * interpolation + prevX - radius);
			int y = (int) ((yCoord - prevY) * interpolation + prevY - radius);
			g.fillOval(x, y, width, height);
			
			lastDrawX = x;
			lastDrawY = y;
			// TopPanel panel = new TopPanel(game.score, game.ballsLeft, game.blocksLeft, game.fps, game.ballVel);
			frameCount++;
		}
	}

	class Paddle {
		public float xCoord, yCoord, prevX, prevY;
		public int width, height;
		public float speed;
		public Color color;
		public float interpolation;
			
		public int lastDrawX, lastDrawY;
			
		public Paddle(float X, float Y, int W, int H, Color C) {
			xCoord = prevX = X;
			yCoord = prevY = Y;
			width = W;
			height = H;
			speed = 25;
			color = C;
		}
		
		public float getX() {
			return xCoord;
		}

		public int width() {
			return width;
		}

		public void paddleMove(float speed) {
			prevX = xCoord;
			prevY = yCoord;

			xCoord += speed;
		}
			
		public void draw(Graphics g) {
			g.setColor(color);
			int x = (int) xCoord;
			int y = (int) yCoord;
			g.fill3DRect(x, y, width, height, true);
			
			lastDrawX = x;
			lastDrawY = y;
		}
	}

	public int hit(float X, float Y, float PrevX, float PrevY, float width, float height, float xCoord, float yCoord) {
		//
		//         xCoord,yCoord ----------- xCoord+width,yCoord
		//                       |         |
		// xCoord, yCoord+height ----------- xCoord+width, yCoord+height
		//
		// return 0 - no hit, 1 - right hit, 2 - left hit, 3 - top hit, 4 - bottom, all else WTF Bro!!!
		int retVal = 0;
		if ((Y>=yCoord) && (Y <= yCoord+height)) {
			// +/- 1 for left/right padding
			if ((X >= xCoord) && (X <= xCoord+width)) {
				// hit at top
				if (PrevX > xCoord+width)
					return 1;
				// hit at bottom
				if (PrevX < xCoord)
					return 2;
				// hit on the right
				if (PrevY > yCoord+height)
					return 3;
				// hit at left
				if (PrevY < yCoord)
					return 4;
			}
		}
		return 0;
	}
}
