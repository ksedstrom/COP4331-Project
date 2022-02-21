package com.mygdx.game;

import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Snake extends ApplicationAdapter {
   private Texture back1Image;
   private Texture back2Image;
   private Texture snakeImage;
   private Texture headImage;
   private Texture foodImage;
   private SpriteBatch batch;
   private OrthographicCamera camera;
   private Array<Rectangle> grid;
   private long lastMoveTime;
   private int lastMoveDir;
   private int direction = 1;
   private boolean[] gridToSnake;
   private Queue<Integer> snakeToGrid;
   private int gridWidth = 17;
   private int gridHeight = 17;
   private int gridArea;
   private int headPos;
   private int fruitPos;
   private long moveTimeInc = 200000000;
   private int snakeSize = 0;

   @Override
   public void create() {
      // load images
      back1Image = new Texture(Gdx.files.internal("back1.png"));
      back2Image = new Texture(Gdx.files.internal("back2.png"));
      snakeImage = new Texture(Gdx.files.internal("snake.png"));
      headImage = new Texture(Gdx.files.internal("head.png"));
      foodImage = new Texture(Gdx.files.internal("food.png"));

      // create the camera and the SpriteBatch
      camera = new OrthographicCamera();
      camera.setToOrtho(false, 600, 600);
      batch = new SpriteBatch();
      
      // create the grid and snake
      gridArea = gridWidth * gridHeight;
      grid = new Array<Rectangle>();
      gridToSnake = new boolean[gridArea];
      snakeToGrid = new LinkedList<>();
      genGrid();
      genSnake();
      
      // start the game events
      move();
      spawnFruit();
   }
   
   private void genGrid() {
	  int widthOffset = (600 - 32 * gridWidth) / 2;
	  int heightOffset = (600 - 32 * gridHeight) / 2;
	  for (int i=0; i<gridArea; i++) {
		  Rectangle space = new Rectangle();
		  space.x = (i % gridWidth) * 32 + widthOffset;
		  space.y = (i / gridWidth) * 32 + heightOffset;
		  space.width = 32;
		  space.height = 32;
		  grid.add(space);
		  gridToSnake[i] = false;
	  }
   }
   
   private void genSnake() {
	   int x = gridArea / 2;
	   addSegment(x - gridWidth * 3);
	   addSegment(x - gridWidth * 2);
	   addSegment(x - gridWidth);
	   addSegment(x);
   }
   
   private void addSegment(int x) {
	   if (gridToSnake[x] == true) {
		   endGame();
		   return;
	   }
	   snakeToGrid.add(x);
	   gridToSnake[x] = true;
	   headPos = x;
	   snakeSize++;
   }
   
   private void removeSegment() {
	   int x = snakeToGrid.remove();
	   snakeSize--;
	   if(snakeSize > 0) {
		   if(x != snakeToGrid.peek())
			   gridToSnake[x] = false;
	   }
	   else gridToSnake[x] = false;
   }
   
   private void move() {
	   if (direction == 0) {
		   if (headPos % gridWidth == 0) {
			   endGame();
			   return;
		   }
		   else addSegment(headPos - 1);
	   }
	   else if (direction == 1) {
		   if (headPos >= gridArea - gridWidth) {
			   endGame();
			   return;
		   }
		   else addSegment(headPos + gridWidth);
	   }
	   else if (direction == 2) {
		   if (headPos < gridWidth) {
			   endGame();
			   return;
		   }
		   else addSegment(headPos - gridWidth);
	   }
	   else {
		   if ((headPos + 1) % gridWidth == 0) {
			   endGame();
			   return;
		   }
		   else addSegment(headPos + 1);
	   }
	   lastMoveTime = TimeUtils.nanoTime();
	   lastMoveDir = direction;
	   if (headPos == fruitPos) spawnFruit();
	   else removeSegment();
   }
   
   private void endGame() {
	   moveTimeInc = 200000000;
	   while (snakeSize > 0) removeSegment();
	   genSnake();
	   spawnFruit();
	   direction = 1;
   }
   
   private void spawnFruit() {
	   do {
		   fruitPos = MathUtils.random(0, gridArea - 1);
	   } while (gridToSnake[fruitPos]);
	   if (moveTimeInc > 6000000) moveTimeInc -= 2000000;
   }

   @Override
   public void render() {
      // clear the screen with a dark blue color
      ScreenUtils.clear(0, 0, 0.2f, 1);

      // tell the camera to update its matrices
      camera.update();

      // tell the SpriteBatch to render in the coordinate system specified by the camera
      batch.setProjectionMatrix(camera.combined);

      // begin a new batch and draw the board
      batch.begin();
      int i = 0;
      for(Rectangle space: grid) {
    	  if (i == fruitPos) batch.draw(foodImage, space.x, space.y);
    	  else if (i == headPos) batch.draw(headImage, space.x, space.y);
    	  else if (gridToSnake[i]) batch.draw(snakeImage, space.x, space.y);
    	  else if (i % 2 == 0) batch.draw(back1Image, space.x, space.y);
    	  else batch.draw(back2Image, space.x, space.y);
    	  i++;
      }
      batch.end();

      // process user input
      if(Gdx.input.isKeyPressed(Keys.LEFT) && lastMoveDir != 3) direction = 0;
      else if(Gdx.input.isKeyPressed(Keys.UP) && lastMoveDir != 2) direction = 1;
      else if(Gdx.input.isKeyPressed(Keys.DOWN) && lastMoveDir != 1) direction = 2;
      else if(Gdx.input.isKeyPressed(Keys.RIGHT) && lastMoveDir != 0) direction = 3;

      // check if the snake needs to move
      if(TimeUtils.nanoTime() - lastMoveTime > moveTimeInc) move();
   }

   @Override
   public void dispose() {
      // dispose of all the native resources
      back1Image.dispose();
      back2Image.dispose();
      snakeImage.dispose();
      headImage.dispose();
      foodImage.dispose();
      batch.dispose();
   }
}