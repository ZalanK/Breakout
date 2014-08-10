/* Implementation of Breakout
 * 
 * Created by Zalan Khan
 * 
 * Assignment # 3 of Stanford CS 106A.
 * 
 * Starter file and some logic used from hand-out.
 * 
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;


public class Breakout extends GraphicsProgram {
	
/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;
	
/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;
	
/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;
	
/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;
	
/** Separation between bricks */
	private static final int BRICK_SEP = 4;
	
/** Width of a brick */
	private static final int BRICK_WIDTH =
			(WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;
	
/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;
	
/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Ball velocity */
	private double vx, vy;

/** Random number generator for vx */	
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
/** Delay time in milliseconds */
	private static final int DELAY = 10;
	
/** Total score before starting. */	
	private int totalScore = 0;
	
/** Number of lives left */	
	private int livesLeft = 3; 

/** Total number of bricks */
	private int totalBricks = NBRICKS_PER_ROW * NBRICK_ROWS;
	
	public void init() {
		setSize(400, 600); 
	}
	
	public void run() {
		setup();
		play();
	}
	
    //Set up the game by building the bricks, paddle and ball.
	private void setup() {
		createBackground();
		createWelcomeMessage();
		createAllBricks();
		createPaddle();
		createBall();
		createPointCounter();
		createLifeCounter();
	}
	
	//Animation of paddle and ball.
	private void play() {
		waitForClick();
		remove(welcome);
		getballVelocity();
		while(true) {
			ball.move(vx, vy);
			checkForCollisions();
			if(totalBricks == 0) {
				remove(ball);
				remove(paddle);
				getWinner();
				break;
			}
			if(livesLeft == 0) {
				remove(ball);
				remove(paddle);
				getLoser();
				break;
			}
			pause(DELAY);
		}
	}
	
	// Black background.
	private void createBackground() {
		 setBackground(Color.BLACK);
	}
	
	private void createWelcomeMessage() {
		double textWidth, textHeight; //Width and height of text
		
		welcome = new GLabel("PREPARE FOR THE GREATEST CHALLENGE OF YOUR LIFE!");
		textWidth = welcome.getWidth();
		textHeight = welcome.getHeight();
		welcome.move(((WIDTH / 2) - (textWidth / 2)) ,((HEIGHT / 2) - textHeight));
		welcome.setColor(Color.YELLOW);
		add (welcome);
	}
	
	private void createAllBricks() {
		
		int i, j;
		double initialX, initialY;
		
		/* The bricks are centered in the window, with the leftover space divided equally on the left and right sides.
		 * Factoring in the:
		 * Width of each bricks
		 * Separation between each brick
		 */
		initialX = (WIDTH - (NBRICKS_PER_ROW * BRICK_WIDTH)
				- (NBRICKS_PER_ROW * BRICK_SEP)) / 2;
		
		initialY = BRICK_Y_OFFSET; // Vertical space to the first block from the very top.
		 
		for(i = 0; i < NBRICK_ROWS; i++){  //Creates all the rows.
			
			for(j = 0; j < NBRICKS_PER_ROW; j++){ //Creates a single row of bricks.
				
				brick = new GRect(initialX, initialY, BRICK_WIDTH, BRICK_HEIGHT);
				add(brick);
				brick.setFilled(true);
				
				//Set the colour of each brick which depends on its row. 
				if (i < 2) {
					brick.setColor(Color.RED);
				}
				else if (i == 2 || i == 3) {
					brick.setColor(Color.ORANGE);
				}
				else if (i == 4 || i == 5) {
					brick.setColor(Color.YELLOW);
				}
				else if (i == 6 || i == 7) {
					brick.setColor(Color.GREEN);
				}
				else if (i == 8 || i == 9) {
					brick.setColor(Color.CYAN);
				}
				
				/* Additional bricks needed to be added to create the row. 
				 * Thus we need to change the x-coordinates for the initialX for the loop.
				 * The y-coordinates remains the same.
				 * Each new brick position will be the sum of:
				 * The place where the last brick started from
				 * The width of the brick
				 * The separation between each brick
				 */
				initialX = initialX + BRICK_WIDTH + BRICK_SEP;
			}
			
			//The initial horizontal space for the new row is reset. Same as before.
			initialX = (WIDTH - (NBRICKS_PER_ROW * BRICK_WIDTH) - (NBRICKS_PER_ROW * BRICK_SEP)) / 2; 
			
			//The initial vertical space is increased by a brick width and brick space from the last brick location.
			initialY = initialY + BRICK_HEIGHT + BRICK_SEP; 	
		}
	}
	//Setting up the paddle
	private void createPaddle() {
		
		/* The middle of the program and the paddle being in those coordinates is not actually centralized.
		 * The x-coordinate of the paddle does not specify the center, but rather to the right by PADDLE_WIDTH.
		 * To centralize the paddle center coordinate, move the paddle to the left by PADDLE_WIDTH/2.
		 */
		double paddleX = (WIDTH / 2) - (PADDLE_WIDTH / 2); 
		double paddleY = HEIGHT - PADDLE_Y_OFFSET;  //The paddle starts from the bottom, not the top.
		
		paddle = new GRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT );
		paddle.setFilled(true);
		paddle.setColor(Color.WHITE);
		add(paddle);
		addMouseListeners();
	}
	
	//Making the paddle move
	public void mouseMoved(MouseEvent e) {
		
		double paddleX = e.getX() - PADDLE_WIDTH / 2; //Starts the cursor off at the center of the paddle.
		double paddleY = HEIGHT - PADDLE_Y_OFFSET;
		
		/* The paddle's restrictions:
		 * Must not go past the very end of the canvas (WIDTH - PADDLE_WIDTH),
		 * remember the paddle object starts at the left corner.
		 * Must not go past the beginning of the canvas (0).
		 * Where ever the mouse is moved, the height will remain the same, and
		 * the cursor will determine the paddle's x-coordinate.
		 */
		if (paddleX <= (WIDTH - PADDLE_WIDTH) && paddleX > 0){
			paddle.setLocation(paddleX, paddleY);
		}
	}
	
	private void createBall() {
		
		/* Keep in mind that the coordinates of the GOval do not specify the location of the center of
		 * the ball but rather its upper left corner. Thus shift the ball to the left and upwards by BALL_RADIUS/2.
		 */
		double centerX = (WIDTH / 2) - (BALL_RADIUS / 2) ;
		double centerY = (HEIGHT / 2) - (BALL_RADIUS / 2);	
		
		ball = new GOval(centerX, centerY, BALL_RADIUS, BALL_RADIUS);
		ball.setFilled(true);
		ball.setColor(Color.WHITE);
		add(ball);
	}
	
	//Individual score counter object.
	private GLabel score;
	
	private void createPointCounter() {
		score = new GLabel("Score: "+ totalScore +"");
		score.setColor(Color.WHITE);
		add(score, 5, (HEIGHT - 5));
	}
	
	//Individual live counter object.
	private GLabel lives;
	
	private void createLifeCounter() {
		lives = new GLabel("Lives: "+ livesLeft +"");
		lives.setColor(Color.WHITE);
		add(lives, WIDTH - 50, (HEIGHT - 5 ));
	}
	
	private void getballVelocity() {
		vy = 3.0;
		vx = rgen.nextDouble(1.0, 3.0);
		
		/* Sets vx to be a random double in the range 1.0 to 3.0 
		 * and then makes it negative half the time.
		 */
		if (rgen.nextBoolean(0.5)) {
			vx = -vx;
		}
	}

	private void checkForCollisions() {
		checkForWallCollision();
		checkForObjectCollision();
	}
	
	private void checkForWallCollision() {
		/* Bouncing around the world (not bricks and paddle)
		 * If the ball bounces off the top wall, vy changes signs.
		 * If the ball bounces off the side walls, vx changes signs.
		 * Remember that we are using the circle's top, left coordinates and not
		 * its center coordinates.
		 */
		
		//Condition for top wall
		if(ball.getY() < 0){
			vy = -vy;
		}
		
		//Condition for right wall	
		if ((ball.getX() + (BALL_RADIUS * 2)) > WIDTH){
			vx = -vx;
		}
		
		//Condition for left wall
		if(ball.getX() < 0){
			vx = -vx;
		}
		
		/* Terminating condition for bottom wall
		 */
		if(ball.getY() + (BALL_RADIUS * 2) > HEIGHT){
			updateLives();
			repositionBallToOrigin();
			waitForClick();
		}
	}
	
	private void repositionBallToOrigin() {
		double centerX = (WIDTH / 2) - (BALL_RADIUS / 2) ;
		double centerY = (HEIGHT / 2) - (BALL_RADIUS / 2);	
		
		ball.setLocation(centerX, centerY);
	}

	//Bouncing and interacting with the paddle and the bricks.
	private void checkForObjectCollision() {
		GObject collider = getCollidingObject();

		if(collider == paddle) {
			vy = -vy;
		}
		
		/* This is to ensure that the ball does not bounce off the score and
		 * life GLabels.
		 */
		else if(collider == score) {
			collider = null;
		}
		else if(collider == lives) { 
			collider = null;
		}
		
		// If the ball hits the brick, the brick disappears and the ball bounces off.
		else if(collider != null) {
			collider.getColor(); //Colour of brick is recorded for the score
			remove(collider); //Brick is removed
			vy = -vy;
			
			/* Counter to tell us when there are no more bricks left.
			 * The user wins the game.
			 */
			totalBricks = totalBricks - 1;
			
			/* Depending on the colour of the brick the ball breaks, the greater
			 * score the user achieves. Higher up bricks will be worth more.
			 */
			if(collider.getColor() == Color.CYAN) { //Score is updated in the program
				remove(score);
				totalScore = totalScore + 10;
				score = new GLabel("Score: "+ totalScore +"");
				score.setColor(Color.WHITE);
				add(score, 5, (HEIGHT - 5));
			}
			if(collider.getColor() == Color.GREEN){ //Score is updated in the program
				remove(score);
				totalScore = totalScore + 25;
				score = new GLabel("Score: "+ totalScore +"");
				score.setColor(Color.WHITE);
				add(score, 5, (HEIGHT - 5));
			}
			if(collider.getColor() == Color.YELLOW){ //Score is updated in the program
				remove(score);
				totalScore = totalScore + 50;
				score = new GLabel("Score: "+ totalScore +"");
				score.setColor(Color.WHITE);
				add(score, 5, (HEIGHT - 5));
			}
			if(collider.getColor() == Color.ORANGE){ //Score is updated in the program
				remove(score);
				totalScore = totalScore + 100;
				score = new GLabel("Score: "+ totalScore +"");
				score.setColor(Color.WHITE);
				add(score, 5, (HEIGHT - 5));
			}
			if(collider.getColor() == Color.RED){ //Score is updated in the program
				remove(score);
				totalScore = totalScore + 200;
				score = new GLabel("Score: "+ totalScore +"");
				score.setColor(Color.WHITE);
				add(score, 5, (HEIGHT - 5));
			}
		}
	}
	
	private void updateLives() {
		remove(lives);
		livesLeft = livesLeft - 1;
		lives = new GLabel("Lives: "+ livesLeft +"");
		lives.setColor(Color.WHITE);
		add(lives, WIDTH - 50, (HEIGHT - 5 ));
	}
	
	private void getWinner() {
		double textWidth, textHeight;; //Width and height of text
	
		winner = new GLabel("YOU WIN THIS TIME CHALLENGER!");
		textWidth = winner.getWidth();
		textHeight = welcome.getHeight();
		winner.move((WIDTH / 2) - (textWidth / 2), ((HEIGHT / 2) + textHeight));
		winner.setColor(Color.GREEN);
		add (winner);
	}

	private void getLoser() {
		double textWidth, textHeight; //Width and height of text
		
		gameOver = new GLabel("GAME OVER: DON'T START CRYING NOW CHALLENGER!");
		textWidth = gameOver.getWidth();
		textHeight = welcome.getHeight();
		gameOver.move(((WIDTH / 2) - (textWidth / 2)) ,((HEIGHT / 2) + textHeight));
		gameOver.setColor(Color.RED);
		add (gameOver);
	}
	
	private GObject getCollidingObject() {
		/* Remember that GOval is defined in terms of a bounding rectangle,
		 * where its upper left corner is its x,y coordinates.
		 * Essentially we are checking to see if the ball collides with an object
		 */
		
		if(getElementAt(ball.getX(), ball.getY()) != null){
			return(getElementAt(ball.getX(), ball.getY()));
		}
		else if((getElementAt((ball.getX() + 2 * BALL_RADIUS), ball.getY()) != null)){
			return((getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY())));
		}
		else if(getElementAt(ball.getX(), (ball.getY() + 2 * BALL_RADIUS)) != null){
			return((getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS)));
		}	
		else if(getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS) != null){
			return((getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS)));
		}
		else{
			return (null);
		}
	}
	
	/* Private instance variables */
	
	//Individual Welcome box.
	private GLabel welcome;
	
	//Individual brick object.
	private GRect brick;
	
	//Individual paddle object.
	private GRect paddle;
	
	//Individual ball object.
	private GOval ball;
	
	//Individual message label object.
	private GLabel winner;
	
	//Individual message label object.
	private GLabel gameOver;
}
