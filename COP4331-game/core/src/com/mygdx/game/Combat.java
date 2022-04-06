package com.mygdx.game;

public class Combat {
	private Enemy enemy;
	private Player player;
	private CardStack drawPile, discardPile, brokenPile, hand;
	private int turn = 1, empower = 0;
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
		int id = 0;
		return new Enemy(id);
	}
	
	private boolean startTurn() {
		// upkeep
		draw(6 + player.getStatus(3) + player.getStatus(12)); // draw next turn and capacity up
		player.updateStatus();
		enemy.updateStatus();
		enemy.determineAction(turn);
		// card play
		
		// enemy turn
		
		return false; // both combatants are alive
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
}
