package com.mygdx.game;

public class Card {
	private String name;
	private int cost, id;
	
	public Card() {
		id = -1;
		name = "x";
		cost = 0;
	}
	
	public Card(int Id) {
		id = Id;
		
	}
	
	public String getName() {
		return name;
	}
	
	public int getCost() {
		return cost;
	}
}
