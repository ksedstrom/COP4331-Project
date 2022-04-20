package com.mygdx.game;

import java.util.ArrayList;
import java.util.Random;

public class CardStack {
	private ArrayList<Card> stack;
	
	public CardStack() {
		stack = new ArrayList<Card>();
	}
	
	public int getSize() {
		return stack.size();
	}
	
	public void insert(Card card) {
		stack.add(card);
	}
	
	public Card remove(int i) {
		Card removed = stack.get(i);
		stack.remove(i);
		return removed;
	}
	
	public void shuffle() {
		Random rng = new Random(); // this can be seeded
		int i, random;
		Card temp;
		// Fisher-Yates shuffle
		for(i=stack.size()-1; i>1; i--) {
			random = rng.nextInt(i);
			if(random == i-1) continue;
			temp = stack.get(i);
			stack.set(i, stack.get(random));
			stack.set(random, temp);
		}
	}
	
	public Card getCard(int i) {
		return stack.get(i);
	}
	
	public void render(int x, int y, final MyGdxGame game, int offSet) {

		int gridWidth = 9;
		int widthOffset = 15;
		int heightOffset = 15;
		for(int i = 0; i < getSize(); i++) {
			x = (i % gridWidth) * 136 + widthOffset;
			y = ((i / gridWidth) * 200 + heightOffset) - offSet;
			getCard(i).render(x, y, game, 1);

		}
	}
}
