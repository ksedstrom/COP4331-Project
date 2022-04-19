package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Player extends Combatant{
	public Player(int hp, int maxHp) {
		super();
		health = hp;
		maxHealth = maxHp;
		healthBar = new Texture(Gdx.files.internal("playerHP.png"));
	}
}
