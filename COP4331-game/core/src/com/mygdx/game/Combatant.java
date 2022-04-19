package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

abstract class Combatant {
	protected int health, maxHealth;
	protected int block;
	protected int[] statusEffects;
	final protected String[] effectNames = {"Vulnerable", "Corroded", "Disoriented", "DrawNextTurn", "TempAccuracy",
			"BlockNextTurn", "Burrow", "Enrage", "Spikey", "Ritual", "QuickReflexes", "Overcharge", "CapacityUp", "Accuracy"};

	
	// GUI stuff
	protected Texture healthBarOutline;
	protected Texture healthBar;
	protected Texture blkIcon;
	protected String hpDisplay, blkDisplay;
	
	public Combatant() {
		block = 0;
		statusEffects = new int[14];
		for(int i=0; i<14; i++) statusEffects[i] = 0;
		healthBarOutline = new Texture(Gdx.files.internal("HealthBarOutline.png"));
		// TODO: set block icon
		// blkIcon = new Texture(Gdx.files.internal("BlockIcon.png"));
		blkDisplay = String.valueOf(block);
	}
	
	public int damage(int damage) {
		if(statusEffects[6] > 0) return 0; // burrow
		if(statusEffects[0] > 0) damage = (int)(damage * 1.5); // vulnerable
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
		hpDisplay = "HP: " + health;
		blkDisplay = String.valueOf(block);
		return statusEffects[8]; // spikey
	}
	
	public void heal(int x) {
		health += x;
		if(health > maxHealth) health = maxHealth;
	}
	
	public void applyStatus(int id, int value) {
		// Effects that apply multiple statues are set as over 100
		if(id > 100){
			if(id == 101){
				statusEffects[3] += 2;
				statusEffects[13] += 1;
			}
			if(id == 102){
				statusEffects[0] += value;
				statusEffects[1] += value;
				statusEffects[2] += value;
			}
		}
		else{
			statusEffects[id] += value;
		}
	}
	
	public void gainBlock(int blk) {
		block += blk;
		blkDisplay = String.valueOf(block);
	}
	
	public void updateStatus() {
		int i;
		block += statusEffects[5]; // block next turn
		statusEffects[13] += statusEffects[9]; // ritual
		for(i=0; i<3; i++) if(statusEffects[i] > 0) statusEffects[i] -= 1; // decaying
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
		return statusEffects[4] + statusEffects[13]; // temp accuracy + accuracy
	}
	
	public void render(int x, int y, final MyGdxGame game) {
		// health bar
		game.batch.draw(healthBarOutline, x, y, 1010, 40);
		game.batch.draw(healthBar, x + 5, y + 5, 1000 * health / maxHealth, 30);
		game.fontLarge.draw(game.batch, hpDisplay, x + 10, y + 25);
		
	};
}
