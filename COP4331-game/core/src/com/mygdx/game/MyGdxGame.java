package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MyGdxGame extends Game {
	public SpriteBatch batch;
	public BitmapFont fontSmall, fontMedium, fontLarge, fontHuge;
	public Socket socket;

	public void create() {
		batch = new SpriteBatch();
		connectSocket();
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

	public void connectSocket(){
		try{
			System.out.println("trying to connect to socket");
			socket = IO.socket("http://localhost:8080");
			socket.connect();
		} catch (Exception e){
			System.out.println(e);
		}
	}
}
