package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGdxGame extends Game {
	public SpriteBatch batch;
	public BitmapFont fontSmall, fontMedium, fontLarge, fontHuge;

	public void create() {
		batch = new SpriteBatch();
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
}
