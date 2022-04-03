package com.mygdx.game;

public class Card {
	private String name;
	private int id, cost; // common
	private int damage, damageMult, block, blockMult; // damage and block
	private int draw, empower, uniqueEffect; // special
	private int[] status = new int[2]; // effect {id, value}
	
	public Card(int ID) {
		id = ID;
		// get card data from file by using the id
		
	}
	
	public int getCost() {
		return cost;
	}
	
	public void play(Combat combat, int power) {
		Player player = combat.getPlayer();
		Enemy enemy = combat.getEnemy();
		if(damage > 0) {
			int dmg = calcValue(damage + power + player.getStatus(4) + player.getStatus(13), player.getStatus(2), enemy.getStatus(0));
			for(int i=0; i<damageMult; i++)
				enemy.damage(dmg);
		}
		if(block > 0) player.block(blockMult * calcValue(block + power, player.getStatus(1), 0));
		if(draw > 0) combat.draw(draw);
		if(status[0] >= 0) player.applyStatus(status[0], status[1]);
		// handle unique effects
		switch(uniqueEffect) {
		
		}
	}
	
	private int calcValue(int base, int mult25, int mult50) {
		if(mult25 > 0) base = (int)(base * 0.75);
		if(mult50 > 0) base = (int)(base * 1.5);
		return base;
	}
	
	public void render(int x, int y) {
		
	}
}
