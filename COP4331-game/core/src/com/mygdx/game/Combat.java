package com.mygdx.game;

public class Combat {
	private Enemy enemy;
	private Player player;
	private CardStack drawPile;
	private CardStack discardPile;
	private CardStack exilePile;
	private CardStack hand;
	private int turn;
	
	public Combat() {
		// default constructor; might remove later
	}
	
	public Combat(CardStack deck) {
		// initialize all card stacks
		int size = deck.getSize();
		drawPile = new CardStack(size);
		discardPile = new CardStack(size);
		exilePile = new CardStack(size);
		hand = new CardStack(10);
		// copy the deck to the draw pile and then shuffle
		for(int i=0; i<size; i++) {
			drawPile.insert(deck.getCard(i));
		}
		drawPile.shuffle();
		// need code for creating enemy and player
		
	}
	
	public void draw(int x) {
		for(int i=0; i<x; i++) {
			if(hand.getSize() == 10) {
				// hand is full; put drawn card into discard pile
				discardPile.insert(drawPile.remove());
			}
			else {
				// put drawn card into hand
				hand.insert(drawPile.remove());
			}
		}
	}
	
	// need code for managing turns and more for handling card stacks
}
