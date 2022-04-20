package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Player extends Combatant{
	public Player(int hp, int maxHp) {
		super();
		health = hp;
		maxHealth = maxHp;
		healthBar = new Texture(Gdx.files.internal("playerHP.png"));
		hpDisplay = "HP: " + health;
	}
	
	public void resetBlock() {
		block = 0;
	}
	
	@Override
	public void render(int x, int y, final MyGdxGame game, Combat combat) {
		super.render(x, y, game, combat); // health bar and status
		if(block > 0) {
			game.batch.draw(blkIcon, x+1000, y); // block icon
			game.fontHuge.draw(game.batch, blkDisplay, x+1000, y+25, 40, 1, false); // block value
		}
		for(int i = 0; i < 7; i++){
			if(statusEffects[i] != 0){
				game.batch.draw(effectTextures[i], i*40, 400);
				game.fontHuge.draw(game.batch, String.valueOf(statusEffects[i]), i*40, 425, 40, 1, false);
			}
		}
		for(int i = 7; i < 14; i++){
			if(statusEffects[i] != 0){
				game.batch.draw(effectTextures[i], (i-7)*40, 360);
				game.fontHuge.draw(game.batch, String.valueOf(statusEffects[i]), (i-7)*40, 385, 40, 1, false);
			}
		}
	}
}
