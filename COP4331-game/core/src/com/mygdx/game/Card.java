package com.mygdx.game;

public class Card {
	private String name, description;
	private int id, cost;
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
	
	public int getEmpower() {
		return empower;
	}
	
	// order of card effects might need to be rearranged
	public void play(Combat combat) {
		Player player = combat.getPlayer();
		Enemy enemy = combat.getEnemy();
		int power = combat.getEmpower();
		if(damage > 0) {
			int dmg = calcValue(damage + power + player.getAccuracy(), player.getStatus(2));
			for(int i=0; i<damageMult; i++) {
				player.damage(enemy.damage(dmg)); // damage enemy and take spikey damage
				if(combat.combatantDied()) return; // check if a combatant died from the damage
			}
		}
		if(block > 0) player.gainBlock(blockMult * calcValue(block + power, player.getStatus(1)));
		if(draw > 0) combat.draw(draw);
		if(status[0] >= 0) player.applyStatus(status[0], status[1]);
		// handle unique effects
		switch(uniqueEffect) {
		
		}
	}
	
	private int calcValue(int base, int minus25) {
		if(minus25 > 0) base -= (int)(base * 0.25); // corroded or disoriented
		return base;
	}
	
	public void render(int x, int y, Combat combat) {
		Player player = combat.getPlayer();
		Enemy enemy = combat.getEnemy();
		int power = combat.getEmpower();
		
	}
}
