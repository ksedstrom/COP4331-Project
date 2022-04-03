package com.mygdx.game;

import java.util.ArrayList;
import java.util.Random;

public class CardStack {
	private ArrayList<Card> stack;
	
	public CardStack() {
		// I think this can remain empty
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
		for(i=stack.size(); i>1; i--) {
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
	
	public void render(int x, int y) {
		
	}
}
