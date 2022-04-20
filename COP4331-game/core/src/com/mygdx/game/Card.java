package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;


public class Card {
	private String name, description, imageName;
	private int id, cost;
	private int damage, damageMult, block, blockMult; // damage and block
	private int draw, empower, uniqueEffect; // special
	private final boolean baseFragile; // determines if card was originally fragile or not
	private boolean fragile;
	private int[] status = new int[2]; // effect {id, value}
	private String returnDescription = "";
	private Texture image;
	private final int cardWidth = 128;
	private final int cardHeight = 192;
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
		
		image = new Texture(Gdx.files.internal(imageName));
		
		
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
		baseFragile = cardData.getBoolean("fragile");
		status[0] = cardData.getInt("statusId");
		status[1] = cardData.getInt("statusValue");
	}

	public int getId(){
		return id;
	}

	public int getCost() {
		return cost;
	}

	public void setFragile(boolean input){
		// If the card is fragile by default, this will do nothing
		if(!baseFragile){
			fragile = input;
		}
	}

	public boolean getFragile(){
		return fragile;
	}

	public String getName(){return name;}

	public int getEmpower(Combat combat) {
		if(id < 5) return empower + combat.getPlayer().getStatus(11); // empower + overcharge
		return 0; // not a cell card
	}

	public String getImageName(){return imageName;}

	// order of card effects might need to be rearranged
	public void play(Combat combat) {
		Player player = combat.getPlayer();
		Enemy enemy = combat.getEnemy();
		int power = combat.getEmpower();
		int critMultiplier = 1;
		boolean reduceToScrapFlag = false;
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
				break;
			case 5:
				combat.applyRefreshEffect();
				break;
			case 6:
				reduceToScrapFlag = true;
				break;
			case 7:
				if(combat.drawPileEmpty()){
					critMultiplier = 3;
				}
		}
		// Quick Reflexes Handler
		if(player.getStatus(10) > 0){
			player.gainBlock(player.getStatus(10), combat);
		}

		// Deal Damage
		if(damageMult > 0) {
			int dmg = calcValue((damage + power + player.getAccuracy()) * critMultiplier, player.getStatus(2));
			for(int i=0; i<damageMult; i++) {
				player.damage(enemy.damage(dmg)); // damage enemy and take spikey damage
				if(combat.combatantDied()){
					if(reduceToScrapFlag){player.heal(5);}
					return;
				}
				// check if a combatant died from the damage
			}
		}
		if(blockMult > 0) player.gainBlock(blockMult * calcValue(block + power, player.getStatus(1)), combat);
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

	public void updateDescription(Combat combat){
		String dmgString = null, blkString = null, powString = null;
		returnDescription = "";
		if(combat != null){
			Player player = combat.getPlayer();
			Enemy enemy = combat.getEnemy();
			int power = combat.getEmpower();

			// Calculate dmgString
			int displayDamage;
			if(enemy.getStatus(6) > 0) displayDamage = 0; // burrow
			else {
				displayDamage = calcValue(damage + power + player.getAccuracy(), player.getStatus(2)); // empower, accuracy, and disoriented
				if (enemy.getStatus(0) > 0) displayDamage = (int)(displayDamage * 1.5); // vulnerable
			}
			if(displayDamage == damage) dmgString = String.valueOf(damage); // damage is unmodified
			else dmgString = displayDamage + "*"; // damage is modified

			// calculate blkString
			int displayBlock = calcValue(block + power, player.getStatus(1)); // empower and corroded
			if(displayBlock == block) blkString = String.valueOf(block); // block is unmodified
			else blkString = displayBlock + "*"; // block is modified

			// calculate powString
			powString = String.valueOf(getEmpower(combat));
			if(!powString.equals(String.valueOf(empower))) powString += "*";
		}
		if (combat == null){
			dmgString = String.valueOf(damage);
			blkString = String.valueOf(block);
			powString = String.valueOf(empower);
		}

		if(fragile == true) returnDescription += "Fragile\n";
		if(damageMult == 1) returnDescription += "Deal " + dmgString + " Damage.\n";
		else if(damageMult > 1) returnDescription += "Deal " + dmgString + " Damage " + damageMult + " times.\n";
		if(blockMult == 1) returnDescription += "Gain " + blkString + " Block.\n";
		else if(blockMult > 1) returnDescription += "Gain " + blkString + " Block " + blockMult + " times.\n";
		if(empower > 0) returnDescription += "Empower " + powString + "\n";
		returnDescription += description;
	}
	
	public void render(int x, int y, final MyGdxGame game, double size) {
		game.batch.draw(image, x, y, (int)(cardWidth*size), (int)(cardHeight*size));
		if(size == 1){
			game.fontMedium.draw(game.batch, returnDescription, x+(int)(cardWidth*size*0.05), y+(int)(size*80), (int)(cardWidth*size*0.9), 1, true);
		}
		if(size > 1 && size < 2){
			game.fontLarge.draw(game.batch, returnDescription, x+(int)(cardWidth*size*0.05), y+(int)(size*80), (int)(cardWidth*size*0.9), 1, true);
		}
		if(size >= 2){
			game.fontHuge.draw(game.batch, returnDescription, x+(int)(cardWidth*size*0.05), y+(int)(size*80), (int)(cardWidth*size*0.9), 1, true);
		}
	}
}
