package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Random;

public class Enemy extends Combatant{
	private String name;
	private int nextAction, prevAction, numActions;
	private boolean lt5Damage, healthTrigger;
	private int[][] behavior;
	
	// GUI stuff
	private Texture image;

	public Enemy(int id, int levelBonus) {
		super();
		lt5Damage = false;
		healthTrigger = false;
		healthBar = new Texture(Gdx.files.internal("HealthBar.png"));

		nextAction = 0; // default value for UI purposes, should never actually get rendered

		// parse enemyData for given id
		JsonReader reader = new JsonReader();
		JsonValue enemyData = reader.parse(Gdx.files.internal("EnemyData.json")).get(id);

		// construct values using json data
		name = enemyData.getString("name");
		image = new Texture(Gdx.files.internal(enemyData.getString("imageName")));
		health = enemyData.getInt("maxHealth") + levelBonus;
		maxHealth = enemyData.getInt("maxHealth") + levelBonus;
		numActions = enemyData.getInt("numActions");
		hpDisplay = "HP: " + health;

		// construct behavior array of form [actionID][actionData]
		// action reference: 0:%, 1:dmg, 2:dmgMult, 3:blk, 4:trigType, 5:trigVal, 6:statType, 7:statVal
		behavior = new int[numActions][8];
		int j;
		for(int i=0; i<numActions; i++){
			for(j=0; j<8; j++){
				behavior[i][j] = enemyData.get("behavior").get(i).get(j).asInt();
			}
		}
	}

	public String getName(){
		return name;
	}

	public int[] getNextAction(){
		return behavior[nextAction];
	}
	
	public void determineAction(int turn) {
		statusEffects[13] += statusEffects[9]; // ritual
		nextAction = checkTriggers(turn);
		if(nextAction == -1) {
			// no triggers; roll for action
			Random random = new Random();
			int randInt = random.nextInt(100);
			int behaviorChance = 0;
			for(int i=0; i<numActions; i++){
				behaviorChance += behavior[i][0];
				if(randInt < behaviorChance){
					nextAction = i;
					break;
				}
			}
		}
		/*
		actionDesc = "";
		if(behavior[nextAction][2] == 1){
			actionDesc = "Damage: " + behavior[nextAction][1] + "\n";
		}
		if(behavior[nextAction][2] > 1){
			actionDesc = "Damage: " + behavior[nextAction][1] + "x" + behavior[nextAction][2] + "\n";
		}
		if(behavior[nextAction][3] > 0){
			actionDesc = actionDesc + "Block: " + behavior[nextAction][3] + "\n";
		}
		if(behavior[nextAction][6] != -1){
			// Can probably be made more specific when adding icons
			actionDesc = actionDesc + "Status will be applied";
		}
		*/
	}
	
	private int checkTriggers(int turn) {
		int trigger = -1;
		// iterate through triggers
		for(int i=0; i<numActions; i++) {
			// behavior[i][4] is the triggerType
			// behavior[i][5] is the triggerValue
			switch(behavior[i][4]) {
			case -1:
				continue;
			case 0:
				if(lt5Damage) trigger = i;
				break;
			case 1:
				if(turn == 1) trigger = i;
				break;
			case 2:
				if(!healthTrigger && health < maxHealth/2) {
					trigger = i;
					healthTrigger = true;
				}
				break;
			case 3:
				if(prevAction == behavior[i][5]) trigger = i;
				break;
			case 4:
				if(turn % 3 == behavior[i][5]) trigger = i;
				break;
			case 5:
				if(turn % 4 == behavior[i][5]) trigger = i;
				break;
			case 6:
				if(turn % 5 == behavior[i][5]) trigger = i;
				break;
			}
			if(trigger >= 0) break; // trigger found; exit loop
		}
		return trigger;
	}
	
	public void actStage1(Combat combat) {
		 // block next turn
		if(statusEffects[5] > 0) {
			gainBlock(statusEffects[5], combat);
			statusEffects[5] = 0;
		}
		// action reference: 0:%, 1:dmg, 2:dmgMult, 3:blk, 4:trigType, 5:trigVal, 6:statType, 7:statVal
		prevAction = nextAction;
		Player player = combat.getPlayer();
		if(behavior[nextAction][1] > 0) {
			int dmg = calcValue(behavior[nextAction][1] + getAccuracy(), statusEffects[2]);
			for(int i=0; i<behavior[nextAction][2]; i++) {
				player.damage(dmg); // damage player
				if(combat.combatantDied()) return; // check if the player died from the damage
			}
		}
		if(behavior[nextAction][3] > 0) gainBlock(behavior[nextAction][3], combat); // block
	}
	
	public void actStage2(Combat combat) {
		// action reference: 0:%, 1:dmg, 2:dmgMult, 3:blk, 4:trigType, 5:trigVal, 6:statType, 7:statVal
		if(behavior[nextAction][6] >= 0) {
			if(behavior[nextAction][6] <= 3) combat.getPlayer().applyStatus(behavior[nextAction][6], behavior[nextAction][7]);
			else applyStatus(behavior[nextAction][6], behavior[nextAction][7]);
		}
	}
	
	private int calcValue(int base, int minus25) {
		if(minus25 > 0) base -= (int)(base * 0.25); // corroded or disoriented
		return base;
	}
	
	@Override
	public void render(int x, int y, final MyGdxGame game, Combat combat) {
		String temp = null;
		super.render(x, y, game, combat); // health bar
		if(block > 0) {
			game.batch.draw(blkIcon, x-40, y); // block icon
			game.fontHuge.draw(game.batch, blkDisplay, x-40, y+25, 40, 1, false); // block value
		}

		// Render Status Effect Icons
		for(int i = 0; i < 7; i++){
			if(statusEffects[i] != 0){
				game.batch.draw(effectTextures[i], 1000+i*40, 340);
				game.fontHuge.draw(game.batch, String.valueOf(statusEffects[i]), 1000+i*40, 365, 40, 1, false);
			}
		}
		for(int i = 7; i < 14; i++){
			if(statusEffects[i] != 0){
				game.batch.draw(effectTextures[i], 1000+(i-7)*40, 300);
				game.fontHuge.draw(game.batch, String.valueOf(statusEffects[i]), 1000+(i-7)*40, 325, 40, 1, false);
			}
		}

		game.fontLarge.draw(game.batch, name, x+10, y-10); // name
		game.batch.draw(image, 1000, 380, 252, 252); // image

		// Render next action
		// Calculate expected damage output
		int displayDamage = calcValue(behavior[nextAction][1] + getAccuracy(), getStatus(2));
		if(combat.getPlayer().getStatus(0)>0) displayDamage = (int)(displayDamage * 1.5);
		if(behavior[nextAction][2] == 1){
			game.batch.draw(dmgIcon, 960, 530);
			game.fontHuge.draw(game.batch, String.valueOf(displayDamage), 960, 555, 40, 1, false);
		}
		if(behavior[nextAction][2] > 1){
			game.batch.draw(dmgIcon, 960, 530);
			temp = displayDamage + "x" + behavior[nextAction][2];
			game.fontLarge.draw(game.batch, temp, 960, 555, 40, 1, false);
		}
		if(behavior[nextAction][3] > 0){
			//actionDesc = actionDesc + "Block: " + behavior[nextAction][3] + "\n";
			game.batch.draw(blkIcon, 960, 490);
			game.fontHuge.draw(game.batch, String.valueOf(behavior[nextAction][3]), 960, 515, 40, 1, false);
		}
		if(behavior[nextAction][6] != -1){
			if(behavior[nextAction][6] == 102){
				game.batch.draw(specialEffectTexture, 960, 450);
			}
			else{
				game.batch.draw(effectTextures[behavior[nextAction][6]], 960, 450);
				game.fontHuge.draw(game.batch, String.valueOf(behavior[nextAction][7]), 960, 475, 40, 1, false);
			}

		}
	}
}
