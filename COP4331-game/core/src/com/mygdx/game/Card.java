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
		System.out.println(name);
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
