package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;


public class Card {
	private String name, description, imageName;
	private int id, cost;
	private int damage, damageMult, block, blockMult; // damage and block
	private int draw, empower, uniqueEffect; // special
	private boolean fragile;
	private int[] status = new int[2]; // effect {id, value}
	private String returnDescription = "";
	public boolean pitching = false;

	public Card(int ID) {
		// get card data from file by using the id
		id = ID;

		// Parse json data associated with id
		JsonReader reader = new JsonReader();
		JsonValue cardData = reader.parse(Gdx.files.internal("CardData.json")).get(id);

		// Assign values using json data
		name = cardData.getString("name");
		description = cardData.getString("description");
		imageName = cardData.getString("imageName");
		cost = cardData.getInt("cost");
		damage = cardData.getInt("damage");
		damageMult = cardData.getInt("damageMult");
		block = cardData.getInt("block");
		blockMult = cardData.getInt(("blockMult"));
		draw = cardData.getInt("draw");
		empower = cardData.getInt("empower");
		uniqueEffect = cardData.getInt("uniqueEffect");
		// List of unique effects, numbered 1-7:
		// Overclock, Master Plan, Plasma Cannon, Deconstruct, Refresh, Reduce to Scrap, Critical Hit
		fragile = cardData.getBoolean("fragile");
		status[0] = cardData.getInt("statusId");
		status[1] = cardData.getInt("statusValue");
	}

	public int getId(){
		return id;
	}

	public int getCost() {
		return cost;
	}

	public String getName(){return name;}

	public int getEmpower(Combat combat) {
		if(id < 5){
			return empower + combat.getPlayer().getStatus(11);
		}
		return empower;
	}

	public String getImageName(){return imageName;}

	// order of card effects might need to be rearranged
	public void play(Combat combat) {
		Player player = combat.getPlayer();
		Enemy enemy = combat.getEnemy();
		int power = combat.getEmpower();
		int critMultiplier = 1;
		// handle unique effects
		switch(uniqueEffect) {
			case 0:
				break;
			case 1:
				combat.applyOverclockEffect();
				break;
			case 2:
				if(combat.drawPileEmpty()){
					combat.draw(3);
				}
				break;
			case 3:
				player.damage(2);
				break;
			case 4:
				combat.applyDeconstructEffect();
			case 5:
				combat.applyRefreshEffect();
			case 6:
				if(enemy.getHealth() <= 0)
				{
					player.heal(5);
				}
			case 7:
				critMultiplier = 3;
		}
		if(damageMult > 0) {
			int dmg = calcValue((damage + power + player.getAccuracy()) * critMultiplier, player.getStatus(2));
			for(int i=0; i<damageMult; i++) {
				player.damage(enemy.damage(dmg)); // damage enemy and take spikey damage
				if(combat.combatantDied()) return; // check if a combatant died from the damage
			}
		}
		if(blockMult > 0) player.gainBlock(blockMult * calcValue(block + power, player.getStatus(1)));
		if(draw > 0) combat.draw(draw);
		if(status[0] >= 0) {
			if(status[0] >= 3) player.applyStatus(status[0], status[1]);
			else enemy.applyStatus(status[0], status[1]);
		}
	}

	private int calcValue(int base, int minus25) {
		if(minus25 > 0) base -= (int)(base * 0.25); // corroded or disoriented
		return base;
	}

	public String getDescription(){
		return returnDescription;
	}

	public void updateDescription(Combat combat){
		String dmgString = null, blkString = null, powString = null;
		returnDescription = "";

		// Calculate dmgString
		int displayDamage = calcValue(damage + combat.getEmpower() + combat.getPlayer().getAccuracy(), combat.getPlayer().getStatus(2));
		if (combat.getEnemy().getStatus(0) > 0){
			displayDamage = (int)(displayDamage * 1.5);
		}
		if(combat.getEnemy().getStatus(6) > 0){
			displayDamage = 0;
		}
		if(displayDamage == damage){
			dmgString = String.valueOf(damage);
		}
		else{
			dmgString = displayDamage + "*";
		}

		// calculate blkString
		int displayBlock = calcValue(block + combat.getEmpower(), combat.getPlayer().getStatus(1));
		if(displayBlock == block){
			blkString = String.valueOf(block);
		}
		else{
			blkString = displayBlock + "*";
		}

		// calculate powString
		if(empower == getEmpower(combat)){
			powString = String.valueOf(empower);
		}
		else{
			powString = getEmpower(combat) + "*";
		}

		if(fragile == true){
			returnDescription = returnDescription + "Fragile\n";
		}
		if(damageMult == 1){
			returnDescription = returnDescription + "Deal " + dmgString + " Damage.\n";
		}
		if(damageMult > 1){
			returnDescription = returnDescription + "Deal " + dmgString + " Damage " + damageMult + " times.\n";
		}
		if(blockMult == 1){
			returnDescription = returnDescription + "Gain " + blkString + " Block.\n";
		}
		if(damageMult > 1){
			returnDescription = returnDescription + "Gain " + blkString + " Block " + blockMult + " times.\n";
		}
		if(empower > 0){
			returnDescription = returnDescription + "Empower " + powString + "\n";
		}
		returnDescription = returnDescription + description;
	}
}
