package com.mygdx.game;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class Combat implements Screen {
	final MyGdxGame game;
	private RunData runData;
	OrthographicCamera camera;

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

	private Texture combatCursor;
	private Texture background;

	public Combat(final MyGdxGame game, final RunData data) {
		this.game = game;
		runData = data;
		runData.incrementLevel();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

		// initialize textures
		combatCursor = new Texture(Gdx.files.internal("combatCursor.png"));
		if(runData.getLevel() <= 5){
			background = new Texture(Gdx.files.internal("DesertBackground.png"));
		}
		else{
			background = new Texture(Gdx.files.internal("ForestBackground.png"));
		}

		// initialize all card stacks
		drawPile = new CardStack();
		discardPile = new CardStack();
		brokenPile = new CardStack();
		hand = new CardStack();
		// copy the deck to the draw pile and then shuffle
		CardStack deck = runData.getDeck();
		for(int i=0; i<deck.getSize(); i++) drawPile.insert(deck.getCard(i));
		drawPile.shuffle();
		
		// create enemy and player
		enemy = generateEnemy(runData.getLevel(), runData.getSeed());
		player = new Player(runData.getMaxHealth(), runData.getHealth());

		startTurn();
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(100, 100, 100, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		// decrement playCardDelay
		if (playCardDelay != 0) playCardDelay--;

		game.batch.begin();

		// Render background, be sure to resize any new background images to 1280x720
		game.batch.draw(background, 0, 0);

		// Render current hand and cursor
		for(int i = hand.getSize()-1; i >= 0; i--) {
			int x = 0;
			int y = 45;
			if(hand.getCard(i).pitching) y = y+20; // height offset for pitching cards
			// calculate card position
			if (hand.getSize() < 10) x = 130*i+(576-64*hand.getSize());
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
				y += i*2;
			}
			hand.getCard(i).render(x, y, game, 1); // render card
			if(cursorPos == i) game.batch.draw(combatCursor, x, y+185, 128, 128); // render cursor
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
		if (selectedCard != null) selectedCard.render(600, 360, game, 1.5);
		// Check if selected card is able to be played
		if (selectedCard != null && !cardReady && selectedCard.getCost() <= pitch){
			cardReady = true;
			playCardDelay = 15;
		}

		// Render player and enemy
		player.render(0, 0, game);
		enemy.render(270, 680, game);
		
		game.batch.end();

		// Process User Input

		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && canAct){
			endTurn();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && canAct){
			if(cursorPos > 0){
				cursorPos--;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && canAct){
			if(cursorPos < hand.getSize()-1){
				cursorPos++;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && canAct){

			// PLAY A CARD
			// Check if selected card is ready to be played
			if(cardReady && playCardDelay == 0){
				selectedCard.play(this);
				// only discard the card if it is not fragile
				if(!selectedCard.getFragile()) {
					discardPile.insert(selectedCard);
				}
				selectedCard = null;
				// send selected cards to discard pile
				for(int i = 0; i < hand.getSize(); i++){
					if(hand.getCard(i).pitching){
						hand.getCard(i).pitching = false;
						drawPile.tuck(hand.remove(i));
						i--;
					}
				}
				pitch = 0;
				empower = 0;
				cardReady = false;

				// If player is dead, proceed to game over screen
				if (player.getHealth() < 1){

				}

				// If enemy is dead, proceed to combat rewards
				if (enemy.getHealth() < 1){
					game.setScreen(new Rewards(game, runData));
					dispose();
				}

				// End the turn if hand is empty
				if(hand.getSize() == 0){
					endTurn();
				}
			}
			// PITCH A CARD
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
						// if the pitched card is an unstable cell, make the selected card fragile
						if(hand.getCard(cursorPos).getId() == 4){
							selectedCard.setFragile(true);
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
		} // end of SPACE inputs

		// ESC key will serve as the "b button"
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && canAct){
			// deselect current card
			if(selectedCard != null){
				selectedCard.setFragile(false);
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
		if (Gdx.input.isKeyJustPressed(Input.Keys.K)){
			enemy.damage(100);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)){
			draw(1);
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
				card.pitching = false;
				card.updateDescription(this);
				hand.insert(card);
			}
		}
	}
	
	private Enemy generateEnemy(int level, long seed) {
		Random rng = new Random(seed);
		// Adjust randomizer based on levels cleared
		for (int i = 0; i < level; i++){
			rng.nextInt();
		}
		int id;
		// assign random id by area
		if(level > 10) id = rng.nextInt(6) + 16; // id: 16-21
		else if (level > 5) id = rng.nextInt(7) + 8; // id: 8-14
		else id = rng.nextInt(7); // id: 0-6
		// override id if boss stage
		switch(level) {
		case 5: id = 7; // sand wyrm
			break;
		case 10: id = 15; // the silent hunter
			break;
		case 15: id = 22; // hypercore beast
			break;
		}
		int levelBonus = level * 2 + rng.nextInt(5);
		return new Enemy(id, levelBonus);
	}
	
	private void startTurn() {
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
		if(selectedCard != null){
			discardPile.insert(selectedCard);
			selectedCard = null;
		}
		enemy.act(this);
		player.resetBlock();
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
		if(drawPile.getSize() <= 0) return true;
		else return false;
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
		// TODO: dispose of textures associated with enemy and player
		// Do NOT dispose of any card textures since those are stored in RunData and will be used in future combats
		combatCursor.dispose();
		background.dispose();
	}
}
