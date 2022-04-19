package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Stack;

public class Combat implements Screen {
	final MyGdxGame game;

	OrthographicCamera camera;

	final int cardWidth = 128;
	final int cardHeight = 192;

	private Enemy enemy;
	private Player player;
	private CardStack drawPile, discardPile, brokenPile, hand;
	// turn starts at 0 but immediately goes to 1 when StartTurn is called for the first time
	private int turn = 0, empower = 0, pitch = 0;
	private final int maxHandSize = 12;
	private boolean canAct = false; // true if player can take actions

	// UI Handling Variables
	private int cursorPos = 0;
	private Card selectedCard = null;
	private int playCardDelay = 0;
	private boolean cardReady = false;

	Texture enemyHealthBar;
	Texture playerHealthBar;
	Texture healthBarOutline;
	Texture combatCursor;
	Texture enemyImage;

	public Combat(final MyGdxGame game, final RunData data) {

		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

		// initialize textures
		enemyHealthBar = new Texture(Gdx.files.internal("HealthBar.png"));
		playerHealthBar = new Texture(Gdx.files.internal("playerHP.png"));
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

		//enemyImage = new Texture(Gdx.files.internal(enemy.getImageName()));
		//placeholder while enemy image assets are being made
		enemyImage = new Texture(Gdx.files.internal("tempEnemy.png"));

		startTurn();
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(100, 100, 100, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		// downtick playCardDelay
		if (playCardDelay != 0){
			playCardDelay--;
		}

		game.batch.begin();
		// Render current hand and cursor
		for(int i = hand.getSize()-1; i >= 0; i--) {
			int x = 0;
			int y = 45;
			if(hand.getCard(i).pitching){
				y = y+20;
			}
			if (hand.getSize() < 10) {
				x = 130*i+(576-64*hand.getSize());
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), x, y, cardWidth, cardHeight);
				game.fontMedium.draw(game.batch, hand.getCard(i).getDescription(), x, y + 75, cardWidth, 1, true);
				if(cursorPos == i){
					// height and width are both equal to cardWidth for the cursor
					game.batch.draw(combatCursor, x, y+185, 128, 128);
				}
			}
			else {
				switch(hand.getSize()) {
				case 10:
					x = 125*i+10;
					break;
				case 11:
					x = 114*i+5;
					break;
				case 12:
					x = 104*i+5;
					break;
				}
				game.batch.draw(new Texture(Gdx.files.internal(hand.getCard(i).getImageName())), x, y+i*2, cardWidth, cardHeight);
				game.fontMedium.draw(game.batch, hand.getCard(i).getDescription(), x, y+75+i*2, cardWidth, 1, true);
				if(cursorPos == i){
					// height and width are both equal to cardWidth for the cursor
					game.batch.draw(combatCursor, x, y+185+i*2, 128, 128);
				}
			}
		}

		// Render discard and draw pile hud elements
		// TODO: need a simple deck icon to make it so this isn't just floating text
		String drawPileDisplay = "Draw Pile Size: " + drawPile.getSize();
		game.fontLarge.draw(game.batch, drawPileDisplay, 0, 250);
		String discardPileDisplay = "Discard Pile Size: " + discardPile.getSize();
		game.fontLarge.draw(game.batch, discardPileDisplay, 0, 200);
		//game.fontLarge.draw(game.batch, String.valueOf(discardPile.getSize()), 0, 300);

		// pressing ENTER key will end the turn, but an End Turn button could maybe be added in the future

		// Render selected card
		if (selectedCard != null){
			game.batch.draw(new Texture(Gdx.files.internal(selectedCard.getImageName())), 640, 360, (int)(cardWidth * 1.5), (int)(cardHeight*1.5));
			game.fontLarge.draw(game.batch, selectedCard.getDescription(), 640, 480, (int)(cardWidth*1.5), 1, true);
		}
		// Check if selected card is able to be played
		if (selectedCard != null && !cardReady && selectedCard.getCost() == pitch){
			cardReady = true;
			playCardDelay = 15;
		}

		// currently, enemyImage is loading tempEnemy.png
		game.batch.draw(enemyImage, 1000, 380, 252, 252);

		// TODO: pretty this up with some icons like slay the spire
		// Render enemy's next action.
		String temp = "";
		if(enemy.getNextAction()[2] == 1){
			temp = "Damage: " + enemy.getNextAction()[1] + "\n";
		}
		if(enemy.getNextAction()[2] > 1){
			temp = "Damage: " + enemy.getNextAction()[1] + "x" + enemy.getNextAction()[2] + "\n";
		}
		if(enemy.getNextAction()[3] > 0){
			temp = temp + "Block: " + enemy.getNextAction()[3] + "\n";
		}
		if(enemy.getNextAction()[6] != -1){
			// Can probably be made more specific when adding icons
			temp = temp + "Status will be applied";
		}
		game.fontMedium.draw(game.batch, temp, 800, 500);

		// TODO: health bar for player and enemy should also have a segment showing how much block each one has

		// Render player health bar
		game.batch.draw(healthBarOutline, 0, 0, 1010,40);
		game.batch.draw(playerHealthBar, 5, 5, player.getHealth()*10, 30);
		String hpDisplay = "HP: " + player.getHealth();
		game.fontLarge.draw(game.batch, hpDisplay, 10, 25);

		// Render enemy health bar
		// hpMult is used to scale the size of enemy health bars to fit on the screen.
		int hpMult = 10;
		if(enemy.getMaxHealth() > 128){
			hpMult = 9;
		}
		if(enemy.getName().equals("The Silent Hunter")){
			hpMult = 6;
		}
		if(enemy.getName().equals("Hypercore Beast")){
			hpMult = 2;
		}
		game.batch.draw(healthBarOutline, 1280 - enemy.getMaxHealth()*hpMult-10, 680, enemy.getMaxHealth()*hpMult+10, 40);
		game.batch.draw(enemyHealthBar, 1275 - enemy.getHealth()*hpMult, 685, enemy.getHealth()*hpMult, 30);
		String enemyDisplay = "HP: " + enemy.getHealth();
		game.fontLarge.draw(game.batch, enemyDisplay, 1280 - enemy.getMaxHealth()*hpMult, 705);
		game.fontLarge.draw(game.batch, enemy.getName(), 1280 - enemy.getMaxHealth()*hpMult, 670);
		game.batch.end();

		// Process User Input

		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && canAct){
			endTurn();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && canAct){
			if(cursorPos != 0){
				cursorPos--;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && canAct){
			if(cursorPos != hand.getSize()-1){
				cursorPos++;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && canAct){
			// Check if selected card is ready to be played
			if(cardReady && playCardDelay == 0){
				selectedCard.play(this);
				discardPile.insert(selectedCard);
				selectedCard = null;
				for(int i = 0; i < hand.getSize(); i++){
					if(hand.getCard(i).pitching){
						hand.getCard(i).pitching = false;
						drawPile.tuck(hand.remove(i));
					}
				}
				pitch = 0;
				empower = 0;
				cardReady = false;
				// End the turn if hand is empty
				if(hand.getSize() == 0){
					endTurn();
				}
			}
			// check cursor is pointing to a card
			else if(cursorPos >= 0 && cursorPos < hand.getSize()){
				// if a card is currently selected, toggle pitch of current card
				if(selectedCard != null){
					// select a card to be pitched
					if(!hand.getCard(cursorPos).pitching){
						hand.getCard(cursorPos).pitching = true;
						pitch++;
						// if the pitched card is a heavy cell, add one more to the pitch value
						if(hand.getCard(cursorPos).getId() == 3){
							pitch++;
						}
						empower = empower + hand.getCard(cursorPos).getEmpower(this);
					}
				}
				// if no card is currently selected, select the current card
				else if(selectedCard == null){
					selectedCard = hand.remove(cursorPos);
					if(cursorPos == hand.getSize() && hand.getSize() != 0){cursorPos--;}
				}
			}
			//  Every time the SPACE is pressed, update the card descriptions
			for(int i = 0; i < hand.getSize()-1; i++){
				hand.getCard(i).updateDescription(this);
			}
			if(selectedCard != null){
				selectedCard.updateDescription(this);
			}
		}
		// ESC key will serve as the "b button"
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && canAct){
			// deselect current card
			if(selectedCard != null){
				hand.insert(selectedCard);
				selectedCard = null;
				pitch = 0;
				empower = 0;
				cardReady = false;
				for(int i = 0; i < hand.getSize(); i++){
					hand.getCard(i).pitching = false;
				}
			}
			for(int i = 0; i < hand.getSize()-1; i++){
				hand.getCard(i).updateDescription(this);
			}
		}

		// Debug Input
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)){
			hand.insert(new Card(7));
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)){
			draw(3);
		}
	}

	public void draw(int x) {
		Card card;
		for(int i=0; i<x; i++) {
			// if drawPile is empty, shuffle in the discardPile
			if(drawPile.getSize() == 0) {
				// if discardPile is also empty, break
				if(discardPile.getSize() == 0){
					break;
				}
				while(discardPile.getSize() > 0) {
					drawPile.insert(discardPile.remove(0));
				}
				drawPile.shuffle();
			}
			card = drawPile.remove(0); // draw card
			if(hand.getSize() == maxHandSize) {
				// hand is full; put drawn card into discard pile
				discardPile.insert(card);
			}
			else {
				// put drawn card into hand
				card.updateDescription(this);
				hand.insert(card);
			}
		}
	}
	
	private Enemy generateEnemy(int level, float seed) {
		// pseudo-randomly generate id from level and seed
		int id = 0; // temporary
		return new Enemy(id);
	}
	
	private void startTurn() {
		// upkeep
		turn++;
		draw(6 + player.getStatus(3) + player.getStatus(12)); // draw next turn and capacity up
		player.updateStatus();
		enemy.updateStatus();
		enemy.determineAction(turn);
		canAct = true;
	}

	private void endTurn(){
		canAct = false;
		while(hand.getSize() != 0){
			discardPile.insert(hand.remove(0));
		}
		enemy.act(this);
		startTurn();
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
