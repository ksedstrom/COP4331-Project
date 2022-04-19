package com.mygdx.game;

import java.util.Random;

public class RunData {
	private long seed;
	private int health;
	private int maxHealth; // constant?
	private int currentLevel;
	//private int lastState; can be reserved for save file management
	private CardStack deck;
	
	public RunData() {
		// Assign Default values
		health = 100;
		maxHealth = 100;
		currentLevel = 0;

		// Generate run seed
		Random rd = new Random();
		seed = rd.nextLong();
		// Create Starting Deck
		deck = new CardStack();
		deck.insert(new Card(7));
		deck.insert(new Card(0));
		deck.insert(new Card(0));
		for(int i = 0; i < 4; i++){
			deck.insert(new Card(5));
			deck.insert(new Card(6));
		}
	}
	
	public void addCard(Card card) {
		deck.insert(card);
	}
	
	public Card removeCard(int index) {
		return deck.remove(index);
	}
	
	public CardStack getDeck() {
		return deck;
	}
	
	public long getSeed() {
		return seed;
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public int getLevel() {
		return currentLevel;
	}

	public void incrementLevel(){
		currentLevel++;
	}

	public void heal(int x) {
		health += x;
		if(health > maxHealth) health = maxHealth;
	}
	
	public void setHealth(int hp) {
		health = hp;
	}
	
	
}
