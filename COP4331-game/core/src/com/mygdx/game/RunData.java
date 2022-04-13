package com.mygdx.game;

public class RunData {
	private long seed;
	private int health;
	private int maxHealth; // constant?
	private int currentLevel;
	//private int lastState; can be reserved for save file management
	private CardStack deck;
	
	public RunData() {
		
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
	
	public void heal(int x) {
		health += x;
		if(health > maxHealth) health = maxHealth;
	}
	
	public void setHealth(int hp) {
		health = hp;
	}
	
	
}
