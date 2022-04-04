package com.mygdx.game;

public class Enemy extends Combatant{
	private String name;
	private int nextAction, prevAction, numActions;
	private boolean lt5Damage, healthTrigger;
	private int[][] behavior;
	
	public Enemy(int id) {
		super();
		lt5Damage = false;
		healthTrigger = false;
		// get data from file using id
		// temporary construction:
		name = "bob";
		numActions = 5;
		behavior = new int[numActions][8];
		for(int i=0; i<numActions; i++) {
			// fill in action data
		}
	}
	
	public void determineAction(int turn) {
		nextAction = checkTriggers(turn);
		if(nextAction == -1) {
			// no triggers; roll for action
			
		}
	}
	
	private int checkTriggers(int turn) {
		int trigger = -1;
		// iterate through triggers
		for(int i=0; i<numActions; i++) {
			switch(behavior[i][4]) {
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
	public void render() {
		
	}
}
