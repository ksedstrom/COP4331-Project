package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenu implements Screen {

	final MyGdxGame game;

	OrthographicCamera camera;
	Texture newGameButton;
	Texture loadGameButton;
	Texture logInButton;
	Texture createAccountButton;
	Texture menuCursor;
	Texture logOutButton;
	int cursorPosition = 0;

	public MainMenu(final MyGdxGame game) {
		this.game = game;

		newGameButton = new Texture(Gdx.files.internal("newGameButton.png"));
		loadGameButton = new Texture(Gdx.files.internal("loadGameButton.png"));
		logInButton = new Texture(Gdx.files.internal("logInButton.png"));
		createAccountButton = new Texture(Gdx.files.internal("createAccountButton.png"));
		logOutButton = new Texture(Gdx.files.internal("logOutButton.png"));
		menuCursor = new Texture(Gdx.files.internal("menuCursor.png"));

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0.2f, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.batch.draw(newGameButton, 100, 550, newGameButton.getWidth(), newGameButton.getHeight());

		if(game.userID == 0) {
			game.batch.draw(logInButton, 100, 400, logInButton.getWidth(), logInButton.getHeight());
			game.batch.draw(createAccountButton, 100, 250, createAccountButton.getWidth(), createAccountButton.getHeight());
		}
		else{
			game.batch.draw(loadGameButton, 100, 400, loadGameButton.getWidth(), loadGameButton.getHeight());
			game.batch.draw(logOutButton, 100, 250, logOutButton.getWidth(), logOutButton.getHeight());
		}
		game.batch.draw(menuCursor, 500, 550-(cursorPosition * 150), menuCursor.getWidth(), menuCursor.getHeight());
		game.batch.end();

		// process user input
		if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			if(cursorPosition != 0){
				cursorPosition--;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			if(cursorPosition != 2){
				cursorPosition++;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			if(cursorPosition == 0){
				// Start a new game
				game.setScreen(new Combat(game, new RunData()));
				dispose();
			}
			if(cursorPosition == 1 && game.userID != 0){
				// Load a game from a save, either online or offline
				// Should also tell user if no save is available
			}
			if(cursorPosition == 1 && game.userID == 0){
				game.setScreen(new LogIn(game));
				dispose();
				// Prompt user to log in
			}
			if(cursorPosition == 2 && game.userID == 0){
				// Create an account
				game.setScreen(new CreateAccount(game));
				dispose();
			}
			if(cursorPosition == 2 && game.userID !=0){
				game.userID = 0;
			}

		}
	}
	
	@Override
	public void resize(int width, int height) {
	}
	
	@Override
	public void resume() {
	}
	
	@Override
	public void dispose() {
		newGameButton.dispose();
		loadGameButton.dispose();
		logInButton.dispose();
		createAccountButton.dispose();
		menuCursor.dispose();
	}
	
	@Override
	public void hide() {
	}
	
	@Override
	public void show() {
	}
	
	@Override
	public void pause() {
	}
}