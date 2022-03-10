package com.mygdx.game;

public class Card {
	private String name;
	private int cost, id;
	private int damage, block; // common combat values
	
	public Card() {
		// default constructor; creates empty card
		id = -1;
		name = "x";
		cost = 0;
		damage = 0;
		block = 0;
	}
	
	public Card(int Id) {
		id = Id;
		// get card data from file by using the id
		
	}
	
	public String getName() {
		return name;
	}
	
	public int getCost() {
		return cost;
	}
	
	// function for executing card effects
	public void play(Combatant player, Combatant enemy) {
		if(damage > 0) enemy.damage(damage + player.getAccuracy());
		if(block > 0) player.addBlock(block);
	}
}
