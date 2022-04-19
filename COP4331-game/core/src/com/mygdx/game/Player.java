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
	public void render(int x, int y, final MyGdxGame game) {
		super.render(x, y, game); // health bar and status
		if(block > 0) {
			// TODO: render icon for block
			//game.batch.draw(blkIcon, x-, y+, , ); // block icon
			game.fontLarge.draw(game.batch, blkDisplay, x+1030, y+25); // block value
		}
	}
}
