package com.mygdx.game;

abstract class Combatant {
	protected int health, maxHealth;
	private int block;
	private int[] statusEffects;
	
	public Combatant() {
		block = 0;
		statusEffects = new int[14];
		for(int i=0; i<14; i++) statusEffects[i] = 0;
	}
	
	public void damage(int damage) {
		if(statusEffects[0] > 0) damage = (int)(damage * 1.5);
		int diff = damage - block;
		if(diff > 0) {
			// damage overcame block
			block = 0;
			health -= diff;
		}
		else {
			// took no damage
			block = -diff;
		}
	}
	
	public void heal(int x) {
		health += x;
		if(health > maxHealth) health = maxHealth;
	}
	
	public void applyStatus(int id, int value) {
		statusEffects[id] += value;
	}
	
	public void gainBlock(int blk) {
		block += blk;
	}
	
	public void updateStatus() {
		int i;
		for(i=0; i<3; i++) statusEffects[i] -= 1; // decaying
		for(i=3; i<7; i++) statusEffects[i] = 0; // temporary
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public int getBlock() {
		return block;
	}
	
	public int getStatus(int id) {
		return statusEffects[id];
	}
	
	public int getAccuracy() {
		return statusEffects[4] + statusEffects[13];
	}
	
	public abstract void render();
}
