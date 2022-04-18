package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class Combat implements Screen {
	final MyGdxGame game;

	OrthographicCamera camera;

	final int cardWidth = 128;
	final int cardHeight = 192;

	private Enemy enemy;
	private Player player;
	private CardStack drawPile, discardPile, brokenPile, hand;
	private int turn = 1, empower = 0;
	private final int maxHandSize = 12;

	Texture healthBar;
	Texture healthBarOutline;

	public Combat(final MyGdxGame game, final RunData data) {

		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

		// initialize textures
		healthBar = new Texture(Gdx.files.internal("HealthBar.png"));
		healthBarOutline = new Texture(Gdx.files.internal("HealthBarOutline.png"));

		// initialize all card stacks
		drawPile = new CardStack();
		discardPile = new CardStack();
		brokenPile = new CardStack();
		hand = new CardStack();
		// copy the deck to the draw pile and then shuffle
		CardStack deck = data.getDeck();
		for(int i=0; i<deck.getSize(); i++) {
			drawPile.insert(deck.getCard(i));
		}
		drawPile.shuffle();
		// create enemy and player
		enemy = generateEnemy(data.getLevel(), data.getSeed());
		player = new Player(data.getMaxHealth(), data.getHealth());

		startTurn();
	}
	@Override
	public void render(float delta) {
		ScreenUtils.clear(100, 100, 100, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		// Render current hand
		for(int i = hand.getSize()-1; i >= 0; i--) {
			if (hand.getSize() < 10) {
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), 130*i+(576-64*hand.getSize()), 45, cardWidth, cardHeight);
			}
			if (hand.getSize() == 10) {
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), 125*i+10, 45+i*2, cardWidth, cardHeight);
			}
			if(hand.getSize() == 11){
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), 114*i+5, 45+i*2, cardWidth, cardHeight);
			}
			if(hand.getSize()== 12){
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), 104*i+5, 45+i*2, cardWidth, cardHeight);
			}
		}
		// Render health bar
		game.batch.draw(healthBarOutline, 0, 0, 1010,40);
		game.batch.draw(healthBar, 5, 5, player.getHealth()*10, 30);
		String hpDisplay = "HP: " + player.getHealth();
		game.font.draw(game.batch, hpDisplay, 10, 25);
		game.batch.end();
	}

	public void draw(int x) {
		Card card;
		for(int i=0; i<x; i++) {
			// if drawPile is empty, shuffle in the discardPile
			if(drawPile.getSize() == 0) {
				while(discardPile.getSize() > 0) {
					drawPile.insert(discardPile.remove(0));
				}
				drawPile.shuffle();
			}
			card = drawPile.remove(0); // draw card
			if(hand.getSize() == maxHandSize) {
				// hand is full; put drawn card into discard pile
				discard(card);
			}
			else {
				// put drawn card into hand
				hand.insert(card);
			}
		}
	}
	
	public void breakCard(Card card) {
		brokenPile.insert(card);
	}
	
	public void discard(Card card) {
		discardPile.insert(card);
	}
	
	private Enemy generateEnemy(int level, float seed) {
		// pseudo-randomly generate id from level and seed
		int id = 0; // temporary
		return new Enemy(id);
	}
	
	private void startTurn() {
		// upkeep
		draw(4 + player.getStatus(3) + player.getStatus(12)); // draw next turn and capacity up
		player.updateStatus();
		enemy.updateStatus();
		enemy.determineAction(turn);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Enemy getEnemy() {
		return enemy;
	}
	
	public int getEmpower() {
		return empower;
	}
	
	public boolean combatantDied() {
		if(enemy.getHealth() <= 0 || player.getHealth() <= 0) return true;
		return false;
	}

	// used to apply Unique Effects
	public boolean drawPileEmpty(){
		if(drawPile.getSize() <= 0){
			return true;
		}
		else{
			return false;
		}
	}
	public void applyOverclockEffect(){
		for(int i = 0; i < 5; i++){
			if(!drawPileEmpty()){
				drawPile.remove(0);
			}
		}
	}
	public void applyDeconstructEffect(){
		// does not need to check hand size because playing deconstruct will inherently remove 2 cards from hand
		Card newCell = new Card(0);
		hand.insert(newCell);
		hand.insert(newCell);
	}
	public void applyRefreshEffect(){
		while(discardPile.getSize() > 0) {
			drawPile.insert(discardPile.remove(0));
		}
		drawPile.shuffle();
	}

	@Override
	public void show() {

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {

	}
}
