package com.mygdx.game;

import java.io.Serializable;
import java.util.Random;

public class RunData{
	private long seed;
	private int health;
	private int maxHealth; // constant?
	private int currentLevel;
	private String deckList;
	private boolean combatClear = true;
	private CardStack deck;
	
	public RunData(String deckList) {
		// Assign Default values
		this.deckList = deckList;
		maxHealth = 75;
		health = maxHealth;
		currentLevel = 0;

		// Generate run seed
		Random rd = new Random();
		seed = rd.nextLong();
		// Create Starting Deck
		deck = new CardStack();
		for(int i = 0; i < deckList.length(); i++){
			deck.insert(new Card(deckList.charAt(i) - 'A'));
		}
	}

	public RunData(long seed, int health, int maxHealth, int currentLevel, String deckList, boolean combat){
		this.seed = seed;
		this.health = health;
		this.maxHealth = maxHealth;
		this.currentLevel = currentLevel;
		this.deckList = deckList;
		this.combatClear = combat;
		deck = new CardStack();
		for(int i = 0; i < deckList.length(); i++){
			deck.insert(new Card(deckList.charAt(i) - 'A'));
		}
	}
	
	public void addCard(Card card) {
		deck.insert(card);
		char newCard = (char)(card.getId() + 65);
		deckList = deckList + newCard;
	}
	
	public Card removeCard(int index) {
		return deck.remove(index);
	}
	
	public CardStack getDeck() {
		return deck;
	}

	public void setCombatClear(boolean input){
		combatClear = input;
	}

	public boolean getCombatClear(){return combatClear;}

	public String getDeckList(){
		return deckList;
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
