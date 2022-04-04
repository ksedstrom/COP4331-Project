package com.mygdx.game;

public class Combat {
	private Enemy enemy;
	private Player player;
	private CardStack drawPile;
	private CardStack discardPile;
	private CardStack brokenPile;
	private CardStack hand;
	private int turn = 1;
	private final int maxHandSize = 12;
	
	public Combat(final RunData data) {
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
		player = new Player(data.getMaxHp(), data.getHp());
		// start combat
		startCombat();
	}
	
	public void startCombat() {
		// make calls to startTurn
		// can divide startTurn into playerTurn and enemyTurn
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
			card = drawPile.remove(0);
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
		
		// get data from file
		
	}
	
	private void startTurn() {
		// upkeep
		player.updateStatus();
		enemy.updateStatus();
		draw(6 + player.getStatus(3) + player.getStatus(12));
		enemy.determineAction(turn);
		// card play
		
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Enemy getEnemy() {
		return enemy;
	}
}
