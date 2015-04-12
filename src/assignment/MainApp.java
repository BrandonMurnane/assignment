package assignment;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;


public class MainApp extends PApplet {
  public int SCREEN_SIZE = 700;
	
	//Paddle
	Paddle paddle;
	PVector paddleSize = new PVector(SCREEN_SIZE/7, SCREEN_SIZE/30);
	float CONTRACTED_PADDLE_WIDTH = SCREEN_SIZE/7;
	float EXTENDED_PADDLE_WIDTH = SCREEN_SIZE/4; 
	//The timer for the extended paddle, as well as the maximum time that it will remain extended
	int extendedPaddleTimer = 0, PADDLE_EXTEND_TIME = 1000;

	//Balls
	ArrayList <Ball> balls; 
	int BALL_SIZE = SCREEN_SIZE/55; 
	int DEFAULT_BALL_SPEED = SCREEN_SIZE/100; 
	int HORIZANTAL_SPEED_SENSITIVITY = 8; 
	//The minimum time before the next ball can be hit - this is to stop a glitch that allows the ball to hit two bricks at once
	int MINIMUM_TIME_BEFORE_NEXT_HIT = 1;
	boolean stickBallToPaddle = true; 
	
	//Bricks
	ArrayList <Brick> bricks;
	PVector BRICK_SIZE = new PVector(SCREEN_SIZE*9/100, SCREEN_SIZE*3/100); 
	
	
	PFont f = createFont("Arial", 20, true);
	int ballsLeft = 3; 
	int currentLevel = 1; 

	boolean displayedIntroduction = false; 
	boolean pauseState = false;
	boolean gameOverState = false;
	
	//powerups
	ArrayList <PowerUp> numPowerUps; 
	final int CHANCE_OF_POWER_UP = 6; //the chance of a powerup dropping when a block is destroyed -e.g., 1 is a 100% chance, where as 2 is a 50% chance
	final int TYPES_OF_POWER_UPS = 2; 
	final int POWER_UP_SIZE = SCREEN_SIZE/30; 
	final int POWER_UP_SPEED = SCREEN_SIZE/300; 
	int currentAmountOfBalls; 

public void setup()
	{
	  size(SCREEN_SIZE, SCREEN_SIZE);
	  smooth();
	  noCursor();
	  balls = new ArrayList();
	  paddle = new Paddle();
	  bricks = new ArrayList();
	  numPowerUps = new ArrayList(); 
	  spawnLevel(currentLevel); //Spawns the first level
	  balls.add(new Ball(paddle.paddleLocation.x + paddleSize.x/2, paddle.paddleLocation.y - BALL_SIZE/2, 0, DEFAULT_BALL_SPEED)); //Spawns the first ball
	}
	 
	 
public void draw()
	{
	  background(0);

	  //Rendering of objects
	  for (int i = 0; i < balls.size(); i++) balls.get(i).renderBall(i);
	  for (int i = 0; i < bricks.size(); i++) bricks.get(i).display(); //Renders all bricks
	  for (int i = 0; i < numPowerUps.size(); i++) numPowerUps.get(i).renderPowerUp(i); //Renders all power ups
	  paddle.display(); //Renders the player's paddle
	  renderHUD(); //Renders the player's HUD

	    //Checks and acts on the amount of balls
	  if (balls.size() == 0) { //If there are no balls left
		  ballsLeft--;
	    stickBallToPaddle = true; //Stick the next spawned ball to the paddle
	    numPowerUps.clear(); //Removes all power-ups in motion
	    extendedPaddleTimer = 0; //Contracts the paddle
	    balls.add(new Ball(paddle.paddleLocation.x + paddleSize.x/2, paddle.paddleLocation.y - BALL_SIZE/2, 0, DEFAULT_BALL_SPEED)); //Adds a ball
	  }
	  if (ballsLeft <= 0) gameOverState = true; //If there are no lives left, show the game over screen

	  //Checks and acts on the amount of bricks
	  if (bricks.size() == 0 && currentLevel <=5 /*AVAILIBLE_LEVELS*/) { //If there are no bricks left and the user has not completed all levels
	    numPowerUps.clear(); //Removes all power-ups in motion
	    balls.clear(); //Removes all balls
	    extendedPaddleTimer = 0; //Contracts the paddle
	    balls.add(new Ball(paddle.paddleLocation.x + paddleSize.x/2, paddle.paddleLocation.y - BALL_SIZE/2, 0, DEFAULT_BALL_SPEED)); //Adds a ball
	    stickBallToPaddle = true; //Sticks the ball back to the paddle
	    currentLevel++; //Increment current level by one
	    spawnLevel(currentLevel); //Spawns the next level
	  }
	}
	 	 
	 
class Paddle
	{ //Class for the paddle

	    //Initialization of the paddle's variables
	  PVector paddleLocation; //Location of the paddle

	  Paddle() { //Creates a new paddle
	    this.paddleLocation = new PVector(SCREEN_SIZE/2, SCREEN_SIZE - paddleSize.y * 2); //Moves the paddle to the middle of the x-axis, and to just above the bottom of the screen
	  }

	  void display() { //Method for rendering the paddle
	    //Movement of the paddle
	    if (!pauseState) //If the game is not paused
	      this.paddleLocation.x = mouseX - paddleSize.x / 2; //Moves the paddle to the mouse

	    //Extended properties of the paddle
	    if (extendedPaddleTimer > 0) //If the timer states that the paddle should be extended
	      paddleSize.x = EXTENDED_PADDLE_WIDTH; //Extend the paddle
	    else
	      paddleSize.x = CONTRACTED_PADDLE_WIDTH; //Contract the paddle
	    if (!pauseState) extendedPaddleTimer--; //Decreases the time for extending the paddle if the game is not paused

	    //Drawing of the paddle
	    fill(255); //Colors white
	    rect(this.paddleLocation.x, this.paddleLocation.y, paddleSize.x, paddleSize.y); //Draws the paddle
	  }
	}
	
public class Ball {

		  PVector ballLocation; 
		  PVector ballSpeed; 
		  int canNextBallBeHitTimer = 0;
		private int ballColor; 
		  Ball(float ballSpawnLocationX, float ballSpawnLocationY, float inputBallSpeedX, float inputBallSpeedY) { //Takes the nessessary inputs and creates a new ball with these properties

		    this.ballLocation = new PVector(ballSpawnLocationX, ballSpawnLocationY); //Sets the inputed location to the new ball
		    this.ballSpeed = new PVector(inputBallSpeedX, inputBallSpeedY); //Sets the speed of the ball
		  }

		  void renderBall(int ballNumber) { //Renders the ball

		    //Movement of the ball
		    if (!pauseState) { //If the game isn't paused
		      this.ballLocation.x += this.ballSpeed.x; //Moves ball horizantally by the horizantal speed
		      this.ballLocation.y += this.ballSpeed.y; //Moves ball vertically by the vertical speed
		    }

		    //Collision of the ball with the walls
		    if (this.ballLocation.x < BALL_SIZE && this.ballSpeed.x < 0) this.ballSpeed.x = -this.ballSpeed.x; //If the ball moves to the left and hits the left wall, reflect the ball
		    if (this.ballLocation.x > SCREEN_SIZE - BALL_SIZE && this.ballSpeed.x > 0) this.ballSpeed.x = -this.ballSpeed.x; //If the ball moves to the right and hits the right wall, reflect the ball
		    if (this.ballLocation.y < BALL_SIZE && this.ballSpeed.y < 0) this.ballSpeed.y = -this.ballSpeed.y; //If the ball moves up and hits the top wall, reflect the ball
		    if (this.ballLocation.y - BALL_SIZE > SCREEN_SIZE) balls.remove(ballNumber); //If the ball goes past the bottom of the screen, remove it

		    //Collision of the ball with the paddle
		    if (this.ballLocation.x + BALL_SIZE/2 > paddle.paddleLocation.x //If the ball is right of the left side of the paddle
		    && this.ballLocation.x - BALL_SIZE/2 < paddle.paddleLocation.x + paddleSize.x //If the ball is on the left of the right side of the paddle
		    && this.ballLocation.y + BALL_SIZE/2 > paddle.paddleLocation.y //If the ball is past the top of the paddle
		    && this.ballLocation.y + BALL_SIZE/2 < paddle.paddleLocation.y + paddleSize.y //If the ball is above the bottom of the paddle
		    && this.ballSpeed.y > 0) { //If the ball is moving downwards (as to stop a glitch which causes the ball to vibrate when bouncing off the paddle)
		      if (this.ballLocation.x > paddle.paddleLocation.x + paddleSize.x/2) //If the ball hits on the right side of the paddle
		        this.ballSpeed.x = dist(paddle.paddleLocation.x + paddleSize.x/2, 0, this.ballLocation.x, 0)/HORIZANTAL_SPEED_SENSITIVITY; //Angle towards the right side in proportion to the distance of the ball with the middle of the paddle
		      else //If the ball hits on the left side of the paddle
		      this.ballSpeed.x = -dist(paddle.paddleLocation.x + paddleSize.x/2, 0, this.ballLocation.x, 0)/HORIZANTAL_SPEED_SENSITIVITY; //Angle towards the left side in proportion to the distance of the ball with the middle of the paddle
		      this.ballSpeed.y = -this.ballSpeed.y; //Reflect the ball vertically
		    }

		    //Collision of the ball with a brick
		    for (int brickNumber = 0; brickNumber < bricks.size(); brickNumber++) { //Checks all currently rendering bricks
		      if (this.ballLocation.x + BALL_SIZE/2 >= bricks.get(brickNumber).brickLocation.x //If the ball's right side is past the left side of the brick
		      && this.ballLocation.x - BALL_SIZE/2 <= bricks.get(brickNumber).brickLocation.x + BRICK_SIZE.x //If the ball's left side is past the right side of the brick
		      && this.ballLocation.y + BALL_SIZE/2 >= bricks.get(brickNumber).brickLocation.y //If the ball's bottom is past the top of the brick
		      && this.ballLocation.y - BALL_SIZE/2 <= bricks.get(brickNumber).brickLocation.y + BRICK_SIZE.y) { //If the ball's top is above the bottom of the brick

		        if ((this.ballLocation.x < bricks.get(brickNumber).brickLocation.x && this.ballSpeed.x > 0) //If the ball hits the left side of the brick
		        || (this.ballLocation.x > bricks.get(brickNumber).brickLocation.x + BRICK_SIZE.x && this.ballSpeed.x < 0)) this.ballSpeed.x = -this.ballSpeed.x; //Or if the ball hits the right side of the brick, reflect it back
		        if ((this.ballLocation.y < bricks.get(brickNumber).brickLocation.y && this.ballSpeed.y > 0) //If the ball hits the top side of the brick
		        || (this.ballLocation.y > bricks.get(brickNumber).brickLocation.y + BRICK_SIZE.y && this.ballSpeed.y < 0)) this.ballSpeed.y = -this.ballSpeed.y; //Or the ball hits the bottom of the brick, reflect the ball vertically

		          if (this.canNextBallBeHitTimer <= 0) { //If the timer allows another block to be destroyed
		        	  Brick b=(Brick) bricks.get(brickNumber);
		        	  b.deathcount-=1;
		          
		          if(b.deathcount==0)
		          {
		        	  int p=(int) random(CHANCE_OF_POWER_UP);
		        	  //The power-up drop code of the brick      
			          if (p == 0)
			          {//If the random chance happens in which a power-up should be dropped
			            numPowerUps.add(new PowerUp(b.brickLocation.x + BRICK_SIZE.x/2, 
			            b.brickLocation.y + BRICK_SIZE.y/2)); //Spawns a power-up at that brick's location
			          }
		        	  bricks.remove(brickNumber); //Removes the hit block if the ball is allowed to destroy a block
		        	  
		          }
		          this.canNextBallBeHitTimer = MINIMUM_TIME_BEFORE_NEXT_HIT; //Resets the hit timer
		        }
		      }
		    }

		    if (stickBallToPaddle) //If the ball must be stuck to the paddle
		        this.ballLocation = new PVector(paddle.paddleLocation.x + paddleSize.x/2, paddle.paddleLocation.y - BALL_SIZE/2); //Move the ball just above the paddle

		    canNextBallBeHitTimer--; //Decreases the hit timer by one

		    //Drawing of the ball
		    fill(255); //Colors white
		    ellipse(this.ballLocation.x, this.ballLocation.y, BALL_SIZE, BALL_SIZE); //Draws the ball
		  }
		}
public void mouseClicked() { //If the mouse is pressed
	  stickBallToPaddle = false; //Un-stick the ball from the paddle
	  displayedIntroduction = true; //Remove the introduction message
	}

public void keyReleased() { //If a key is released
	  if (key == 'Q' || key == 'q') exit(); //If the player presses 'q', quit the game
	  if (key == 'P' || key == 'p') pauseState = !pauseState; //If the player presses 'p', toggle the pause
	}

void renderHUD() { //Renders the player's HUD

    noCursor(); //Removes the user's cursor
  textAlign(LEFT); //Aligns the text to the left of co-ordinates
  textFont(f, SCREEN_SIZE*4/125); //Sets the text font
  text("Balls Left: ", SCREEN_SIZE/100, SCREEN_SIZE/25); //Writes the text "Balls Left"
  text("Current Level: " + currentLevel, SCREEN_SIZE/100, SCREEN_SIZE*7/80); //Writes out the current level
  for (int ballsToDraw = 0; ballsToDraw < ballsLeft; ballsToDraw++) ellipse(SCREEN_SIZE*2/11 + ballsToDraw * BALL_SIZE*3/2, SCREEN_SIZE/32, BALL_SIZE, BALL_SIZE); //Draws a ball for each life left

  textAlign(CENTER); //Aligns the text to the, center of co-ordinates
  textFont(f, SCREEN_SIZE/50); //Sets the text font
  if (!displayedIntroduction) text("Welcome to Brick Break!" + "\nTo let go of the ball, click the mouse." + "\nTo control the paddle, move the mouse." + "\nTo pause the game, press 'P'." + "\nTo quit the game, press 'Q'.", SCREEN_SIZE/2, SCREEN_SIZE*6/10); //Displays the introduction message

  if (pauseState) { //If the game is paused
    cursor(ARROW); //Sets the user's cursor to an arrow
    fill(0, 0, 0, 220); //Fills with a transparent black
    rect(0, 0, SCREEN_SIZE, SCREEN_SIZE); //Darkens the whole screen
    fill(255); //Colors white
    textFont(f, SCREEN_SIZE*4/125); //Sets the text font
    text("Game Paused", SCREEN_SIZE/2, SCREEN_SIZE/2); //Writes a pause message to the user
    textFont(f, SCREEN_SIZE/50); //Sets the text font
    text("Press 'P' to resume the game.", SCREEN_SIZE/2, SCREEN_SIZE*11/21); //Writes instructions regarding the pause to the user
  } 


  if (gameOverState) { //If the game has been lost
    background(0); //Colors the screen black, overlaying all the renderings
    textFont(f, SCREEN_SIZE*4/125); //Sets the text font
    text("Game Over", SCREEN_SIZE/2, SCREEN_SIZE/2); //Writes a game over message to the user
    textFont(f, SCREEN_SIZE/50); //Sets the text font
    text("Press 'Q' to exit the game.", SCREEN_SIZE/2, SCREEN_SIZE*11/21); //Writes instructions regarding the quit to the user
  }
}

void spawnLevel(int levelToSpawn) { //Spawns the level that should be spawned
	  switch(levelToSpawn) { //Checks the current level that was already incremented  before this was called
	  case 1: //If must spawn level one
	    for (float spawnYofBrick = SCREEN_SIZE*10/45; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*20; spawnYofBrick += BRICK_SIZE.y) //Arranges each row one block apart
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x) //Aranges each column one block apart
	        bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); //Spawns each brick
	    break;
	  case 2: //If must spawn level two
	    for (float spawnYofBrick = SCREEN_SIZE*8/45; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*15; spawnYofBrick += BRICK_SIZE.y) //Arranges each row one block apart
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x*2)
	        bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); //Spawns each brick
	    break;
	  case 3: //If must spawn level three
	    for (float spawnYofBrick = SCREEN_SIZE*14/45; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*15; spawnYofBrick += BRICK_SIZE.y) //Arranges each row one block apart
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x) //Aranges each column one block apart
	        if (spawnXofBrick < SCREEN_SIZE*3/8 || spawnXofBrick > SCREEN_SIZE/2) bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); //Spawns each brick, except in the middle column
	    break;
	  case 4: //If must spawn level four
	    for (float spawnYofBrick = SCREEN_SIZE*2/9; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*17; spawnYofBrick += BRICK_SIZE.y) //Arranges each row one block apart
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x) //Aranges each column one block apart
	        if(spawnXofBrick == BRICK_SIZE.x || spawnXofBrick >= SCREEN_SIZE - BRICK_SIZE.x*3 //If it is in one of the two columns
	        || spawnYofBrick == SCREEN_SIZE*2/9 || spawnYofBrick >= SCREEN_SIZE - BRICK_SIZE.y*18) //If it is in on of the two rows
	        bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); //Spawns bricks in a box shape
	    break;
	  case 5: //If must spawn level five (which is non-existent)
	    break;
	  }
	}
	 
class Brick
	{
		 //Initialization of the bricks's variables
		  PVector brickLocation; //Location of the brick
		  int deathcount;
		  int c;

		  Brick(float brickSpawnLocationX, float brickSpawnLocationY, int deathcount) { //Takes neccessary inputs and creates a brick with those properties
		    this.brickLocation = new PVector(brickSpawnLocationX, brickSpawnLocationY); //Sets the brick to the location of the inputed variables
		   this.deathcount=deathcount;
		}
		  
		  void display()
		  {
		    stroke(0);
		    setColor();
		    fill(c);
		    rect(this.brickLocation.x, this.brickLocation.y, BRICK_SIZE.x, BRICK_SIZE.y); //Draws the brick
		  }
		   
		  void setColor()
		  {
		    switch(deathcount)
		    {
		      case 1:
		        c = color(255, 0, 0);
		        break;
		      case 2:
		        c = color(0, 255, 0);
		        break;
		      case 3:
		        c = color(0, 0, 255);
		        break;
		    }
		  }
		   
		  int getDeathCount()
		  {
		    return deathcount; 
		  }
		   
		  void setDeathCount(int dcount)
		  {
		    deathcount = dcount;
		  }
		}

class PowerUp { //Class for the power ups
	  //Initialization of the power up's variables
	  PVector powerUpLocation; 
	  int powerUpType; 
	  PowerUp(float powerUpSpawnLocationX, float powerUpSpawnLocationY) { //Creates a power-up with the inputed proportions
	    this.powerUpLocation = new PVector(powerUpSpawnLocationX, powerUpSpawnLocationY); //Sets the location to the inputed co-ordinates
	    this.powerUpType = ((int) random(TYPES_OF_POWER_UPS)) ;
	    println(this.powerUpType );
	  }
	  void renderPowerUp(int powerUpNumber) { //Renders the power up
	    //Movement of the power up
		  fill(255);
	    this.powerUpLocation.y += POWER_UP_SPEED; //Lowers the power up

	    //Collision with the bottom wall
	    if (this.powerUpLocation.y - POWER_UP_SIZE > SCREEN_SIZE) numPowerUps.remove(powerUpNumber); //If the powerup passes the bottom of the screen, remove it

	    //Collision with the paddle
	    if (this.powerUpLocation.x + POWER_UP_SIZE/2 > paddle.paddleLocation.x //If the powerup is right of the left side of the paddle
	    && this.powerUpLocation.x - POWER_UP_SIZE/2 < paddle.paddleLocation.x + paddleSize.x //If the powerup is on the left of the right side of the paddle
	    && this.powerUpLocation.y + POWER_UP_SIZE/2 > paddle.paddleLocation.y //If the powerup is past the top of the paddle
	    && this.powerUpLocation.y + POWER_UP_SIZE/2 < paddle.paddleLocation.y + paddleSize.y) { //If the powerup is above the bottom of the paddle
	    
	      switch(this.powerUpType) { //Checks the power up type
	      case 0: //If it is a x3 power up
	        currentAmountOfBalls = balls.size(); //Sets the current amount of balls to the size of the arraylist as to not run out of memory in a constant loop due to newly spawned balls
	        for (int i = 0; i < currentAmountOfBalls; i++) { //For every ball that is on the game...
	          balls.add(new Ball(balls.get(i).ballLocation.x, balls.get(i).ballLocation.y, balls.get(i).ballSpeed.x, -balls.get(i).ballSpeed.y)); //Creates a ball going diagonally top-right relative to the ball's direction
	          balls.add(new Ball(balls.get(i).ballLocation.x, balls.get(i).ballLocation.y, -balls.get(i).ballSpeed.x, balls.get(i).ballSpeed.y)); //Creates a ball going diagonally bottom-left relative to the ball's direction
	          balls.add(new Ball(balls.get(i).ballLocation.x, balls.get(i).ballLocation.y, -balls.get(i).ballSpeed.x, -balls.get(i).ballSpeed.y)); //Creates a ball going diagonally top-left relative to the ball's direction
	        }
	        break;
	      case 1: //If it is an extended paddle power up
	        extendedPaddleTimer = PADDLE_EXTEND_TIME; //Sets sticky time to maximum sticky time
	        break;
	      }
	      numPowerUps.remove(powerUpNumber); //Remove the power up
	    }

	    ellipse(this.powerUpLocation.x, this.powerUpLocation.y, POWER_UP_SIZE, POWER_UP_SIZE); //Draws the power up

	    fill(255);
	    textFont(f, SCREEN_SIZE/50); //Sets the text font
	    switch(this.powerUpType) { //Checks the type of the power up
	    case 0: //If it is a x3 Power Up
	    	fill(0);
	      text("3", this.powerUpLocation.x - SCREEN_SIZE/800, this.powerUpLocation.y + SCREEN_SIZE*3/400); //Writes the number 3 on the power up for recognition from the user
	      break;
	    case 1: //If it is an extended paddle power up
	    	fill(0);
	      text("E", this.powerUpLocation.x - SCREEN_SIZE/800, this.powerUpLocation.y + SCREEN_SIZE*3/400); //Writes the letter E on the power up for recognition from the user
	      break;
	    }
	  }
	}
}

