package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

public class MyGdxGame extends Game {
	public SpriteBatch batch;
	public BitmapFont fontSmall, fontMedium, fontLarge, fontHuge;
	public boolean largeFont = false;
	public Socket socket;
	public int userID = 0;
	public int runscompleted;
	public boolean serverConnected = false;
	public Preferences prefs;
	//public boolean gameSaved = false;
	public void create() {
		batch = new SpriteBatch();
		connectSocket();
		configSocketEvents();
		prefs = Gdx.app.getPreferences("cop4331savedata");
		fontSmall = new BitmapFont(Gdx.files.internal("Arial8px.fnt"));
		fontMedium = new BitmapFont(Gdx.files.internal("Arial12px.fnt"));
		fontLarge = new BitmapFont(Gdx.files.internal("Arial18px.fnt"));
		fontHuge = new BitmapFont(Gdx.files.internal("Arial24px.fnt"));
		this.setScreen(new MainMenu(this));
	}

	public void render() {
		super.render(); // important!
	}

	public void dispose() {
		batch.dispose();
		fontSmall.dispose();
		fontMedium.dispose();
		fontLarge.dispose();
		fontHuge.dispose();
	}

	public void toggleLargeFont(){
		if(largeFont){
			largeFont = false;
			fontSmall = new BitmapFont(Gdx.files.internal("Arial8px.fnt"));
			fontMedium = new BitmapFont(Gdx.files.internal("Arial12px.fnt"));
			fontLarge = new BitmapFont(Gdx.files.internal("Arial18px.fnt"));
			fontHuge = new BitmapFont(Gdx.files.internal("Arial24px.fnt"));
		}
		else{
			largeFont = true;
			fontSmall = new BitmapFont(Gdx.files.internal("Arial12px.fnt"));
			fontMedium = new BitmapFont(Gdx.files.internal("Arial18px.fnt"));
			fontLarge = new BitmapFont(Gdx.files.internal("Arial24px.fnt"));
			fontHuge = new BitmapFont(Gdx.files.internal("Arial32px.fnt"));
		}
	}

	public void connectSocket(){
		try{
			System.out.println("trying to connect to socket");
			socket = IO.socket("http://localhost:8080");
			socket.connect();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	public void configSocketEvents(){
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener(){
			@Override
			public void call(Object... args){
				setServerConnected(true);
				System.out.println("Connected to server");

			}
		});
	}

	public void setServerConnected(boolean connected){
		this.serverConnected = connected;
	}


}
