package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

public class MainMenu implements Screen {

	final MyGdxGame game;

	final String defaultDeckList = "AAFFFFGGGGH";

	OrthographicCamera camera;
	Texture newGameButton;
	Texture loadGameButton;
	Texture logInButton;
	Texture createAccountButton;
	Texture menuCursor;
	Texture logOutButton;
	Texture leaderboardbutton;
	int cursorPosition = 0;
	int cursorPositionX = 0;
	RunData loadedData;
	long loadedseed;
	int loadedhealth;
	int loadedmaxHealth;
	int loadedlevel;
	String loadeddeck;
	boolean loadedcombatcleared;
	boolean gameloaded = false;
	boolean nosave;

	public MainMenu(final MyGdxGame game) {
		this.game = game;
		if(game.serverConnected) {
			testConnection();
		}
		configSocketEvents();
		if(game.userID != 0){
			game.socket.emit("load_runs", game.userID);
		}

		newGameButton = new Texture(Gdx.files.internal("newGameButton.png"));
		loadGameButton = new Texture(Gdx.files.internal("loadGameButton.png"));
		logInButton = new Texture(Gdx.files.internal("logInButton.png"));
		createAccountButton = new Texture(Gdx.files.internal("createAccountButton.png"));
		logOutButton = new Texture(Gdx.files.internal("logOutButton.png"));
		menuCursor = new Texture(Gdx.files.internal("menuCursor.png"));
		leaderboardbutton = new Texture(Gdx.files.internal("leaderboardbutton.png"));

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);
		configSocketEvents();
	}
	public void testConnection(){
		System.out.println("testing connection");
		game.serverConnected = false;
		game.socket.emit("test_connection");
	}
	public void configSocketEvents(){
		game.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener(){
			@Override
			public void call(Object... args){
				System.out.println("Connected to server");
			}
		}).on("game_loaded", new Emitter.Listener(){
			@Override
			public void call(Object... args){

				JSONObject data = (JSONObject) args[0];
				try{
					long seed = data.getLong("seed");
					int health = data.getInt("health");
					int maxHealth = data.getInt("maxHealth");
					int level = data.getInt("currentLevel");
					String deck = data.getString("deckString");
					int combatCleared = data.getInt("combatCleared");
					//loadedData = new RunData(seed, health, maxHealth, level, deck, combatCleared);
					System.out.println("Loaded seed:" + seed);
					System.out.println("Loaded health:" + health);
					System.out.println("Loaded maxHealth:" + maxHealth);
					System.out.println("Loaded level:" + level);
					System.out.println("Loaded deck:" + deck);
					System.out.println("Loaded combatCleared:" + combatCleared);

					loadGame(seed, health, maxHealth, level, deck, combatCleared);

				}catch(JSONException e){
					System.out.println("Didn't create");
				}
			}
		}).on("no_save_found", new Emitter.Listener(){
			@Override
			public void call(Object... args){
				System.out.println("no save found");
				nosavefound();
			}
		}).on("loaded_runscomplete", new Emitter.Listener(){
			@Override
			public void call(Object... args){
				JSONObject data = (JSONObject) args[0];
				try{
					setRunCompleted(data.getInt("runscomplete"));
				}catch(JSONException e){
					System.out.println("Json exception at runscomplete on main menu");
				}
			}
		}).on("server_connected", new Emitter.Listener() {
			@Override
			public void call(Object... args){
				System.out.println("received connection message");
				setServerConnected(true);
				turnOffServerConnected();
			}
		});
	}
	public void turnOffServerConnected(){
		game.socket.off("server_connected");
	}
	public void setServerConnected(boolean connected){
		game.serverConnected = connected;
	}
	public void turnOffListeners(){
		game.socket.off("game_loaded");
		game.socket.off("no_save_found");
		game.socket.off("loaded_runscomplete");
		game.socket.off("server_connected");
	}
	public void setRunCompleted(int runscomplete){
		game.runscompleted = runscomplete;
	}
	public void nosavefound(){
		nosave = true;
	}
	public void loadGame(long seed, int health, int maxhealth, int level, String deck, int combatCleared){
		loadedseed = seed;
		loadedhealth = health;
		loadedmaxHealth = maxhealth;
		loadedlevel = level;
		loadeddeck = deck;
		if(combatCleared == 1) {
			loadedcombatcleared = true;
		}
		else{
			loadedcombatcleared = false;
		}
		gameloaded = true;
	}
	@Override
	public void render(float delta) {
		ScreenUtils.clear(1, 1, 1, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.batch.draw(newGameButton, 100, 550, newGameButton.getWidth(), newGameButton.getHeight());
		game.batch.draw(leaderboardbutton, 100, 100, leaderboardbutton.getWidth(), leaderboardbutton.getHeight());
		if(nosave){
			game.fontLarge.draw(game.batch, "No Save \n Found", 35, 455);
		}
		if(!game.serverConnected){
			game.fontLarge.draw(game.batch, "Not Connected to the Server", 1075, 50);
		}
		if(game.serverConnected){
			game.fontLarge.draw(game.batch, "Connected to the Server", 1100, 50);
		}
		if(game.userID != 0){
			game.fontLarge.draw(game.batch, "Runs Completed: " + game.runscompleted, 1050, 50);
		}
		if(game.userID == 0) {
			game.batch.draw(logInButton, 100, 400, logInButton.getWidth(), logInButton.getHeight());
			game.batch.draw(createAccountButton, 100, 250, createAccountButton.getWidth(), createAccountButton.getHeight());
		}
		else{
			game.batch.draw(loadGameButton, 100, 400, loadGameButton.getWidth(), loadGameButton.getHeight());
			game.batch.draw(logOutButton, 100, 250, logOutButton.getWidth(), logOutButton.getHeight());
		}
		game.batch.draw(menuCursor, 500 + (cursorPositionX*400), 550-(cursorPosition * 150), menuCursor.getWidth(), menuCursor.getHeight());
		game.fontHuge.draw(game.batch, "Change Font Size", 700, 550);
		game.batch.end();
		if(gameloaded){
			turnOffListeners();
			if(loadedcombatcleared){
				game.setScreen(new Combat(game, new RunData(loadedseed, loadedhealth, loadedmaxHealth, loadedlevel,
						loadeddeck, loadedcombatcleared)));
			}
			else{
				game.setScreen(new Rewards(game, new RunData(loadedseed, loadedhealth, loadedmaxHealth, loadedlevel,
						loadeddeck, loadedcombatcleared)));
			}
		}
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
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			if(cursorPositionX > 0){
				cursorPositionX--;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			if(cursorPositionX < 1){
				cursorPositionX++;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			if(cursorPositionX == 1){
				game.toggleLargeFont();
			}
			else if(cursorPosition == 0){
				// Start a new game
				turnOffListeners();
				game.setScreen(new Combat(game, new RunData(defaultDeckList)));
				dispose();
			}
			else if(cursorPosition == 1 && game.userID != 0){
				game.socket.emit("load_game", game.userID);
				// Load a game from a save, either online or offline
				// Should also tell user if no save is available
			}
			else if(cursorPosition == 1 && game.userID == 0){
				game.setScreen(new LogIn(game));
				dispose();
				// Prompt user to log in
			}
			else if(cursorPosition == 2 && game.userID == 0){
				// Create an account
				game.setScreen(new CreateAccount(game));
				dispose();
			}
			else if(cursorPosition == 2 && game.userID !=0){
				game.userID = 0;
				game.runscompleted = 0;
			}
			else if(cursorPosition == 3){
				game.setScreen(new Leaderboard(game));
				dispose();
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
		leaderboardbutton.dispose();
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