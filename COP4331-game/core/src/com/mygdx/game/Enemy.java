package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Random;

public class Enemy extends Combatant{
	private String name, imageName;
	private int nextAction, prevAction, numActions;
	private boolean lt5Damage, healthTrigger;
	private int[][] behavior;

	public Enemy(int id) {
		super();
		lt5Damage = false;
		healthTrigger = false;

		// parse enemyData for given id
		JsonReader reader = new JsonReader();
		JsonValue enemyData = reader.parse(Gdx.files.internal("EnemyData.json")).get(id);

		// construct values using json data
		name = enemyData.getString("name");
		imageName = enemyData.getString("imageName");
		health = enemyData.getInt("maxHealth");
		maxHealth = enemyData.getInt("maxHealth");
		numActions = enemyData.getInt("numActions");

		// construct behavior array of form [actionID][actionData]
		// action data contains 8 values in order: %, damage, damageMultiplier, Block, triggerType, triggerValue, statusType, statusValue
		behavior = new int[numActions][8];
		int j;
		for(int i=0; i<numActions; i++){
			for(j=0; j<8; j++){
				behavior[i][j] = enemyData.get("behavior").get(i).get(j).asInt();
			}
		}
	}
	
	public void determineAction(int turn) {
		nextAction = checkTriggers(turn);
		if(nextAction == -1) {
			// no triggers; roll for action
			Random random = new Random();
			int randInt = random.nextInt(100);
			int behaviorChance = 0;
			for(int i=0; i<numActions; i++){
				behaviorChance = behaviorChance + behavior[i][0];
				if(randInt < behaviorChance){
					nextAction = i;
				}
			}
		}
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
				if(!healthTrigger && health < maxHealth/2) trigger = i;
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
	
	public void act(Combat combat) {
		prevAction = nextAction;
		// similar to Card.Play()
		
	}
	
	@Override
	public void render(int x, int y) {
		// display image
		super.render(x, y);
	}
}