package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

	// UI Handling Variables
	private int cursorPos = 0;
	private Card selectedCard;

	Texture healthBar;
	Texture healthBarOutline;
	Texture combatCursor;

	public Combat(final MyGdxGame game, final RunData data) {

		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

		// initialize textures
		healthBar = new Texture(Gdx.files.internal("HealthBar.png"));
		healthBarOutline = new Texture(Gdx.files.internal("HealthBarOutline.png"));
		combatCursor = new Texture(Gdx.files.internal("combatCursor.png"));

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

		int x = 0;

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		// Render current hand and cursor
		for(int i = hand.getSize()-1; i >= 0; i--) {
			x = 0;
			if (hand.getSize() < 10) {
				x = 130*i+(576-64*hand.getSize());
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), x, 45, cardWidth, cardHeight);
				game.fontMedium.draw(game.batch, hand.getCard(i).getDescription(), x, 120, cardWidth, 1, true);
				if(cursorPos == i){
					// height and width are both equal to cardWidth for the cursor
					game.batch.draw(combatCursor, x, 230, 128, 128);
				}
			}
			if (hand.getSize() == 10) {
				x = 125*i+10;
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), x, 45+i*2, cardWidth, cardHeight);
				game.fontMedium.draw(game.batch, hand.getCard(i).getDescription(), x, 120+i*2, cardWidth, 1, true);
				if(cursorPos == i){
					// height and width are both equal to cardWidth for the cursor
					game.batch.draw(combatCursor, x, 230+i*2, 128, 128);
				}
			}
			if(hand.getSize() == 11){
				x = 114*i+5;
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), x, 45+i*2, cardWidth, cardHeight);
				game.fontMedium.draw(game.batch, hand.getCard(i).getDescription(), x, 120+i*2, cardWidth, 1, true);
				if(cursorPos == i){
					// height and width are both equal to cardWidth for the cursor
					game.batch.draw(combatCursor, x, 230+i*2, 128, 128);
				}
			}
			if(hand.getSize() == 12){
				x = 104*i+5;
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), x, 45+i*2, cardWidth, cardHeight);
				game.fontMedium.draw(game.batch, hand.getCard(i).getDescription(), x, 120+i*2, cardWidth, 1, true);
				if(cursorPos == i){
					// height and width are both equal to cardWidth for the cursor
					game.batch.draw(combatCursor, x, 230+i*2, 128, 128);
				}
			}
		}

		// Render health bar
		game.batch.draw(healthBarOutline, 0, 0, 1010,40);
		game.batch.draw(healthBar, 5, 5, player.getHealth()*10, 30);
		String hpDisplay = "HP: " + player.getHealth();
		game.fontLarge.draw(game.batch, hpDisplay, 10, 25);
		game.batch.end();

		// Process User Input
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
			if(cursorPos != 0){
				cursorPos--;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
			if(cursorPos != hand.getSize()-1){
				cursorPos++;
			}
		}
		// Debug Input to draw cards for testing purposes
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)){
			draw(1);
		}
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
		draw(6 + player.getStatus(3) + player.getStatus(12)); // draw next turn and capacity up
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
