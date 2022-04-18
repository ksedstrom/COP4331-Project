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
	int cursorPosition = 0;

	public MainMenu(final MyGdxGame game) {
		this.game = game;

		newGameButton = new Texture(Gdx.files.internal("newGameButton.png"));
		loadGameButton = new Texture(Gdx.files.internal("loadGameButton.png"));
		logInButton = new Texture(Gdx.files.internal("logInButton.png"));
		createAccountButton = new Texture(Gdx.files.internal("createAccountButton.png"));
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
		game.batch.draw(loadGameButton, 100, 400, loadGameButton.getWidth(), loadGameButton.getHeight());
		game.batch.draw(logInButton, 100, 250, logInButton.getWidth(), logInButton.getHeight());
		game.batch.draw(createAccountButton, 100, 100, createAccountButton.getWidth(), createAccountButton.getHeight());
		game.batch.draw(menuCursor, 500, 550-(cursorPosition * 150), menuCursor.getWidth(), menuCursor.getHeight());
		game.batch.end();

		// process user input
		if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			if(cursorPosition != 0){
				cursorPosition--;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			if(cursorPosition != 3){
				cursorPosition++;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			if(cursorPosition == 0){
				// Start a new game
				game.setScreen(new Combat(game, new RunData()));
				dispose();
			}
			if(cursorPosition == 1){
				// Load a game from a save, either online or offline
				// Should also tell user if no save is available
			}
			if(cursorPosition == 2){
				// Prompt user to log in
			}
			if(cursorPosition == 3){
				// Prompt user to create account
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