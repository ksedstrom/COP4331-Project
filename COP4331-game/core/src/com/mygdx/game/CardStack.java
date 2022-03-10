package com.mygdx.game;

import java.util.Random;

public class CardStack {
	private int size;
	private Card[] stack;
	
	public CardStack() {
		size = 30;
		stack = new Card[size];
	}
	
	public CardStack(int stackSize) {
		size = stackSize;
		stack = new Card[size];
	}
	
	public int getSize() {
		return size;
	}
	
	public void insert(Card card) {
		stack[size] = card;
		size++;
	}
	
	public Card remove() {
		size--;
		return stack[size];
	}
	
	public void shuffle() {
		Random random = new Random();
		int i, r;
		Card temp;
		for(i=size; i>1; i--) {
			r = random.nextInt(i);
			if(r == i-1) continue;
			temp = stack[i];
			stack[i] = stack[r];
			stack[r] = temp;
		}
	}
	
	public Card getCard(int i) {
		return stack[i];
	}
}
