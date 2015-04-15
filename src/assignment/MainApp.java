package assignment;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
	int AVAILIBLE_LEVELS = 4;

	boolean displayedIntroduction = false; 
	boolean displayWinningMessage = false; 
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
	  AudioClip ac = getAudioClip(getDocumentBase(),"02 T-Shirt Weather.mp3");
	  ac.play();

	  for (int i = 0; i < balls.size(); i++) balls.get(i).renderBall(i);
	  for (int i = 0; i < bricks.size(); i++) bricks.get(i).display(); 
	  for (int i = 0; i < numPowerUps.size(); i++) numPowerUps.get(i).renderPowerUp(i); 
	  paddle.display(); 
	  renderHUD(); 

	  if (balls.size() == 0) { 
		  ballsLeft--;
	    stickBallToPaddle = true; 
	    numPowerUps.clear();
	    extendedPaddleTimer = 0; 
	    balls.add(new Ball(paddle.paddleLocation.x + paddleSize.x/2, paddle.paddleLocation.y - BALL_SIZE/2, 0, DEFAULT_BALL_SPEED)); 
	  }
	  if (ballsLeft <= 0) gameOverState = true; //If there are no lives left, show the game over screen

	  
	  if (bricks.size() == 0 && currentLevel <=AVAILIBLE_LEVELS) { 
	    numPowerUps.clear(); 
	    balls.clear(); 
	    extendedPaddleTimer = 0;
	    balls.add(new Ball(paddle.paddleLocation.x + paddleSize.x/2, paddle.paddleLocation.y - BALL_SIZE/2, 0, DEFAULT_BALL_SPEED)); 
	    stickBallToPaddle = true; 
	    currentLevel++; 
	    spawnLevel(currentLevel); 
	  }
	}
	 	 
	 
class Paddle
	{ //Class for the paddle

	    
	  PVector paddleLocation; 
	  Paddle() { 
	    this.paddleLocation = new PVector(SCREEN_SIZE/2, SCREEN_SIZE - paddleSize.y * 2); 
	  }

	  void display() { //Method for rendering the paddle
	    
	    if (!pauseState) 
	      this.paddleLocation.x = mouseX - paddleSize.x / 2; 
	    //Extended properties of the paddle
	    if (extendedPaddleTimer > 0) 
	      paddleSize.x = EXTENDED_PADDLE_WIDTH; 
	    else
	      paddleSize.x = CONTRACTED_PADDLE_WIDTH; 
	    if (!pauseState) extendedPaddleTimer--; 
	    fill(255); 
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

		  void renderBall(int ballNumber) { 

		    //Movement of the ball
		    if (!pauseState) { 
		      this.ballLocation.x += this.ballSpeed.x; 
		      this.ballLocation.y += this.ballSpeed.y;
		    }

		    //Collision of the ball with the walls
		    if (this.ballLocation.x < BALL_SIZE && this.ballSpeed.x < 0) this.ballSpeed.x = -this.ballSpeed.x;
		    if (this.ballLocation.x > SCREEN_SIZE - BALL_SIZE && this.ballSpeed.x > 0) this.ballSpeed.x = -this.ballSpeed.x; 
		    if (this.ballLocation.y < BALL_SIZE && this.ballSpeed.y < 0) this.ballSpeed.y = -this.ballSpeed.y; 
		    if (this.ballLocation.y - BALL_SIZE > SCREEN_SIZE) balls.remove(ballNumber); 

		    //Collision of the ball with the paddle
		    if (this.ballLocation.x + BALL_SIZE/2 > paddle.paddleLocation.x 
		    && this.ballLocation.x - BALL_SIZE/2 < paddle.paddleLocation.x + paddleSize.x 
		    && this.ballLocation.y + BALL_SIZE/2 > paddle.paddleLocation.y 
		    && this.ballLocation.y + BALL_SIZE/2 < paddle.paddleLocation.y + paddleSize.y 
		    && this.ballSpeed.y > 0) { 
		      if (this.ballLocation.x > paddle.paddleLocation.x + paddleSize.x/2) 
		        this.ballSpeed.x = dist(paddle.paddleLocation.x + paddleSize.x/2, 0, this.ballLocation.x, 0)/HORIZANTAL_SPEED_SENSITIVITY;
		      else 
		      this.ballSpeed.x = -dist(paddle.paddleLocation.x + paddleSize.x/2, 0, this.ballLocation.x, 0)/HORIZANTAL_SPEED_SENSITIVITY; 
		      this.ballSpeed.y = -this.ballSpeed.y; 
		    }

		    //Collision of the ball with a brick
		    for (int brickNumber = 0; brickNumber < bricks.size(); brickNumber++) { 
		      if (this.ballLocation.x + BALL_SIZE/2 >= bricks.get(brickNumber).brickLocation.x 
		      && this.ballLocation.x - BALL_SIZE/2 <= bricks.get(brickNumber).brickLocation.x + BRICK_SIZE.x 
		      && this.ballLocation.y + BALL_SIZE/2 >= bricks.get(brickNumber).brickLocation.y 
		      && this.ballLocation.y - BALL_SIZE/2 <= bricks.get(brickNumber).brickLocation.y + BRICK_SIZE.y) { 

		        if ((this.ballLocation.x < bricks.get(brickNumber).brickLocation.x && this.ballSpeed.x > 0) 
		        || (this.ballLocation.x > bricks.get(brickNumber).brickLocation.x + BRICK_SIZE.x && this.ballSpeed.x < 0)) this.ballSpeed.x = -this.ballSpeed.x; 
		        if ((this.ballLocation.y < bricks.get(brickNumber).brickLocation.y && this.ballSpeed.y > 0) 
		        || (this.ballLocation.y > bricks.get(brickNumber).brickLocation.y + BRICK_SIZE.y && this.ballSpeed.y < 0)) this.ballSpeed.y = -this.ballSpeed.y; 

		          if (this.canNextBallBeHitTimer <= 0) { //If the timer allows another block to be destroyed
		        	  Brick b=(Brick) bricks.get(brickNumber);
		        	  b.deathcount-=1;
		          
		          if(b.deathcount==0)
		          {
		        	  int p=(int) random(CHANCE_OF_POWER_UP);
		        	    
			          if (p == 0)
			          {
			            numPowerUps.add(new PowerUp(b.brickLocation.x + BRICK_SIZE.x/2, 
			            b.brickLocation.y + BRICK_SIZE.y/2)); 
			          }
		        	  bricks.remove(brickNumber); 
		        	  
		          }
		          this.canNextBallBeHitTimer = MINIMUM_TIME_BEFORE_NEXT_HIT; 
		        }
		      }
		    }

		    if (stickBallToPaddle) 
		        this.ballLocation = new PVector(paddle.paddleLocation.x + paddleSize.x/2, paddle.paddleLocation.y - BALL_SIZE/2); 

		    canNextBallBeHitTimer--; 
		    fill(255); 
		    ellipse(this.ballLocation.x, this.ballLocation.y, BALL_SIZE, BALL_SIZE); 
		  }
		}
public void mouseClicked() { //If the mouse is pressed
	  stickBallToPaddle = false; 
	  displayedIntroduction = true; 
	}

public void keyReleased() { //If a key is released
	  if (key == 'Q' || key == 'q') exit(); 
	  if (key == 'P' || key == 'p') pauseState = !pauseState; 
	}

void renderHUD() { //Renders the player's HUD

    noCursor(); 
  textAlign(LEFT); 
  textFont(f, SCREEN_SIZE*4/125); 
  text("Balls Left: ", SCREEN_SIZE/100, SCREEN_SIZE/25); 
  text("Current Level: " + currentLevel, SCREEN_SIZE/100, SCREEN_SIZE*7/80); 
  for (int ballsToDraw = 0; ballsToDraw < ballsLeft; ballsToDraw++) ellipse(SCREEN_SIZE*2/11 + ballsToDraw * BALL_SIZE*3/2, SCREEN_SIZE/32, BALL_SIZE, BALL_SIZE);

  textAlign(CENTER); 
  textFont(f, SCREEN_SIZE/50); 
  if (!displayedIntroduction) text("Welcome to Brick Break!" + "\nTo let go of the ball, click the mouse." + "\nTo control the paddle, move the mouse." + "\nTo pause the game, press 'P'." + "\nTo quit the game, press 'Q'.", SCREEN_SIZE/2, SCREEN_SIZE*6/10);
  if (displayWinningMessage) text("Congratulations!" + "\nYou win!" + "\nPress 'Q' to quit the game.", SCREEN_SIZE/2, SCREEN_SIZE/2); 

  
  
  if (pauseState) { 
    cursor(ARROW); 
    fill(0, 0, 0, 220);
    rect(0, 0, SCREEN_SIZE, SCREEN_SIZE); 
    fill(255); 
    textFont(f, SCREEN_SIZE*4/125); 
    text("Game Paused", SCREEN_SIZE/2, SCREEN_SIZE/2); 
    textFont(f, SCREEN_SIZE/50); 
    text("Press 'P' to resume the game.", SCREEN_SIZE/2, SCREEN_SIZE*11/21); 
  } 


  if (gameOverState) { 
    background(0); 
    textFont(f, SCREEN_SIZE*4/125); 
    text("Game Over", SCREEN_SIZE/2, SCREEN_SIZE/2);
    textFont(f, SCREEN_SIZE/50);
    text("Press 'Q' to exit the game.", SCREEN_SIZE/2, SCREEN_SIZE*11/21); 
  }
}

void spawnLevel(int levelToSpawn) { 
	  switch(levelToSpawn) { 
	  case 1: //If must spawn level one
	    for (float spawnYofBrick = SCREEN_SIZE*10/45; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*20; spawnYofBrick += BRICK_SIZE.y) 
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x) 
	        bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); 
	    break;
	  case 2: //If must spawn level two
	    for (float spawnYofBrick = SCREEN_SIZE*8/45; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*15; spawnYofBrick += BRICK_SIZE.y) 
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x*2)
	        bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); 
	    break;
	  case 3: //If must spawn level three
	    for (float spawnYofBrick = SCREEN_SIZE*14/45; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*15; spawnYofBrick += BRICK_SIZE.y) 
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x) 
	        if (spawnXofBrick < SCREEN_SIZE*3/8 || spawnXofBrick > SCREEN_SIZE/2) bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); 
	    break;
	  case 4: //If must spawn level four
	    for (float spawnYofBrick = SCREEN_SIZE*2/9; spawnYofBrick < SCREEN_SIZE - BRICK_SIZE.y*17; spawnYofBrick += BRICK_SIZE.y) 
	      for (float spawnXofBrick = BRICK_SIZE.x; spawnXofBrick < SCREEN_SIZE - BRICK_SIZE.x*2; spawnXofBrick += BRICK_SIZE.x) 
	        if(spawnXofBrick == BRICK_SIZE.x || spawnXofBrick >= SCREEN_SIZE - BRICK_SIZE.x*3 
	        || spawnYofBrick == SCREEN_SIZE*2/9 || spawnYofBrick >= SCREEN_SIZE - BRICK_SIZE.y*18) 
	        bricks.add(new Brick(spawnXofBrick, spawnYofBrick,(int) random(3)+1)); 
	    break;
	  case 5: 
		  displayWinningMessage = true;
	    break;
	  }
	}
	 
class Brick
	{
		  PVector brickLocation;
		  int deathcount;
		  int c;

		  Brick(float brickSpawnLocationX, float brickSpawnLocationY, int deathcount) { 
		    this.brickLocation = new PVector(brickSpawnLocationX, brickSpawnLocationY); 
		   this.deathcount=deathcount;
		}
		  
		  void display()
		  {
		    stroke(0);
		    setColor();
		    fill(c);
		    rect(this.brickLocation.x, this.brickLocation.y, BRICK_SIZE.x, BRICK_SIZE.y);
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
	  
	  PVector powerUpLocation; 
	  int powerUpType; 
	  PowerUp(float powerUpSpawnLocationX, float powerUpSpawnLocationY) { 
	    this.powerUpLocation = new PVector(powerUpSpawnLocationX, powerUpSpawnLocationY); 
	    this.powerUpType = ((int) random(TYPES_OF_POWER_UPS)) ;
	    println(this.powerUpType );
	  }
	  void renderPowerUp(int powerUpNumber) { 
		  fill(255);
	    this.powerUpLocation.y += POWER_UP_SPEED; 

	   
	    if (this.powerUpLocation.y - POWER_UP_SIZE > SCREEN_SIZE) numPowerUps.remove(powerUpNumber); 

	    //Collision with the paddle
	    if (this.powerUpLocation.x + POWER_UP_SIZE/2 > paddle.paddleLocation.x 
	    && this.powerUpLocation.x - POWER_UP_SIZE/2 < paddle.paddleLocation.x + paddleSize.x 
	    && this.powerUpLocation.y + POWER_UP_SIZE/2 > paddle.paddleLocation.y 
	    && this.powerUpLocation.y + POWER_UP_SIZE/2 < paddle.paddleLocation.y + paddleSize.y) { 
	    
	      switch(this.powerUpType) { 
	      case 0: //If it is a x3 power up
	        currentAmountOfBalls = balls.size(); 
	        for (int i = 0; i < currentAmountOfBalls; i++) { 
	          balls.add(new Ball(balls.get(i).ballLocation.x, balls.get(i).ballLocation.y, balls.get(i).ballSpeed.x, -balls.get(i).ballSpeed.y)); 
	          balls.add(new Ball(balls.get(i).ballLocation.x, balls.get(i).ballLocation.y, -balls.get(i).ballSpeed.x, balls.get(i).ballSpeed.y)); 
	          balls.add(new Ball(balls.get(i).ballLocation.x, balls.get(i).ballLocation.y, -balls.get(i).ballSpeed.x, -balls.get(i).ballSpeed.y)); 
	        }
	        break;
	      case 1: //If it is an extended paddle power up
	        extendedPaddleTimer = PADDLE_EXTEND_TIME;
	        break;
	      }
	      numPowerUps.remove(powerUpNumber); 
	    }

	    ellipse(this.powerUpLocation.x, this.powerUpLocation.y, POWER_UP_SIZE, POWER_UP_SIZE); 

	    fill(255);
	    textFont(f, SCREEN_SIZE/50);
	    switch(this.powerUpType) { 
	    case 0: //If it is a x3 Power Up
	    	fill(0);
	      text("3", this.powerUpLocation.x - SCREEN_SIZE/800, this.powerUpLocation.y + SCREEN_SIZE*3/400); 
	      break;
	    case 1: //If it is an extended paddle power up
	    	fill(0);
	      text("E", this.powerUpLocation.x - SCREEN_SIZE/800, this.powerUpLocation.y + SCREEN_SIZE*3/400); 
	      break;
	    }
	  }
	}
}