package com.mygdx.game;

import java.util.Random;

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
	private int deckDisplayScroll = 0;

	private Texture combatCursor;
	private Texture background;
	private Texture animatedCard;
	private Texture drawIcon;
	private Texture discardIcon;

	public Combat(final MyGdxGame game, final RunData data) {
		this.game = game;
//		if(game.userID != 0){
//			configSocketEvents();
//		}
		runData = data;
		runData.incrementLevel();
		runData.setCombatClear(false);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

		// initialize textures
		combatCursor = new Texture(Gdx.files.internal("combatCursor.png"));
		animatedCard = new Texture(Gdx.files.internal("Base_Card_Template.png"));
		drawIcon = new Texture(Gdx.files.internal("DeckIcon.png"));
		discardIcon = new Texture(Gdx.files.internal("DiscardIcon.png"));
		if(runData.getLevel() <= 5){
			background = new Texture(Gdx.files.internal("DesertBackground.png"));
		}
		else if(runData.getLevel() <= 10){
			background = new Texture(Gdx.files.internal("ForestBackground.png"));
		}
		else{
			background = new Texture(Gdx.files.internal("ruinedcitybackground.jpg"));
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
		//enemy = new Enemy(22, 0); // Choose a specific enemy for debugging
		player = new Player(runData.getHealth(), runData.getMaxHealth());

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
		game.batch.draw(drawIcon, 30, 0);
		game.fontHuge.draw(game.batch, String.valueOf(drawPile.getSize()), 70, 25, 40, 1, false);
		game.batch.draw(discardIcon, 1170, 0);
		game.fontHuge.draw(game.batch, String.valueOf(discardPile.getSize()), 1210, 25, 40, 1, false);

		// pressing ENTER key will end the turn, but an End Turn button could maybe be added in the future

		// Render selected card
		if (selectedCard != null) selectedCard.render(600, 360, game, 1.5);
		// Check if selected card is able to be played
		if (selectedCard != null && !cardReady && selectedCard.getCost() <= pitch){
			cardReady = true;
			playCardDelay = 15;
		}

		// Render player and enemy
		player.render(140, 0, game, this);
		enemy.render(270, 680, game, this);

		// If TAB is being held, overwrite with background then render draw pile
		if (Gdx.input.isKeyPressed(Input.Keys.TAB)){
			canAct = false;
			game.batch.draw(background, 0, 0);
			drawPile.render(0,0,game,deckDisplayScroll,true);
		}
		game.batch.end();

		// Process User Input

		if (!Gdx.input.isKeyPressed(Input.Keys.TAB)){canAct=true;}
		else if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){deckDisplayScroll+=210;}
		else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){deckDisplayScroll-=210;}

		if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && canAct) endTurn();
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && canAct && cursorPos > 0) cursorPos--;
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && canAct && cursorPos < hand.getSize()-1) cursorPos++;
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && canAct) {
			// PLAY A CARD
			// Check if selected card is ready to be played
			if(cardReady && playCardDelay == 0){
				selectedCard.play(this); // play the card
				
				// check if a combatant is dead, 
				if (player.getHealth() < 1){
					game.setScreen(new GameOver(game)); // proceed to game over screen
					dispose();
				}
				if (enemy.getHealth() < 1){
					runData.setHealth(player.getHealth());
					if(runData.getLevel()==5){
						runData.setMaxHealth(85);
						runData.heal(40);
					}
					if(runData.getLevel()==10){
						runData.setMaxHealth(100);
						runData.heal(50);
					}
					if(runData.getLevel() == 15){
						game.setScreen(new Winning(game));
					}
					else {
						if(game.userID != 0) {
							saveGame();
						}
						game.setScreen(new Rewards(game, runData)); // proceed to combat rewards
						dispose();
					}
				}
				
				// move card to a pile
				if(selectedCard.getFragile()) brokenPile.insert(selectedCard); // break card
				else discardPile.insert(selectedCard); // discard card
				selectedCard = null;
				
				// pitch chosen cards to bottom of the draw pile
				for(int i = 0; i < hand.getSize(); i++){
					if(hand.getCard(i).pitching){
						hand.getCard(i).pitching = false;
						hand.getCard(i).unknown = false;
						hand.getCard(i).updateDescription(this);
						drawPile.insert(hand.remove(i));
						i--;
					}
				}
				pitch = 0;
				empower = 0;
				cardReady = false;

				// End the turn if hand is empty
				if(hand.getSize() == 0) endTurn();
				if(cursorPos > hand.getSize()){
					cursorPos = hand.getSize();
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
			// Every time the SPACE is pressed, update the card descriptions
			updateHand();
			if(selectedCard != null) selectedCard.updateDescription(this);
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
			updateHand();
		}


		// Debug Input
		if (Gdx.input.isKeyJustPressed(Input.Keys.K)){
			enemy.damage(100);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)){
			draw(1);
		}
	}
	public void saveGame(){
		//game.gameSaved = false;
		int x;
		if(runData.getCombatClear()){
			x = 1;
		}
		else{
			x = 0;
		}
		game.socket.emit("save_game", game.userID, runData.getSeed(), runData.getHealth(), runData.getMaxHealth(),
				runData.getLevel(), runData.getDeckList(), x);
	}
	public void draw(int x) {
		Card card;
		for(int i=0; i<x; i++) {
			// if drawPile is empty, shuffle in the discardPile
			if(drawPile.getSize() == 0) {
				if(discardPile.getSize() == 0) break; // if discardPile is also empty, break
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
				card.setFragile(false);
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
		player.removeStatus(3); // remove draw next turn
		enemy.determineAction(turn); // also handles ritual
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
		enemy.removeStatus(6); // remove burrow
		player.removeStatus(4); // remove temp accuracy
		enemy.actStage1(this); // action stage 1
		if(player.getHealth() <= 0){
			game.setScreen(new GameOver(game));
			dispose();
		}
		// update decaying status effects
		player.decayStatus();
		enemy.decayStatus();
		enemy.actStage2(this); // action stage 2
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
	
	private void updateHand() {
		for(int i = 0; i < hand.getSize(); i++) hand.getCard(i).updateDescription(this);
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
		hand.insert(new Card(0));
		hand.insert(new Card(0));
	}
	public void applyRefreshEffect(){
		while(discardPile.getSize() > 0) {
			drawPile.insert(discardPile.remove(0));
		}
		drawPile.shuffle();
	}
//	public void configSocketEvents() {
//		game.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
//			@Override
//			public void call(Object... args) {
//				System.out.println("Connected to server");
//			}
//		}).on("save_success", new Emitter.Listener() {
//			@Override
//			public void call(Object... args) {
//				System.out.println("Received save success");
//				setSaveSuccess(true);
//				turnOffListener();
//
//			}
//		});
//	}
//	public void turnOffListener(){
//		game.socket.off("save_success");
//	}
//	public void setSaveSuccess(boolean saved){
//		game.gameSaved = saved;
//	}
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
		//turnOffListener();
		combatCursor.dispose();
		background.dispose();
	}
}
