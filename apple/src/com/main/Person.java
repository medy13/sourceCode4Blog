package com.main;


/**
 * 人，有放苹果和拿苹果两种人
 *
 */
public class Person implements Runnable{
	enum Action{
		getApple,putApple;
	}
	
	private String name;
	private Action action;
	private Box box = new Box();
	private boolean flag = true;
	
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	Person(String _name, Action _action, Box _box){
		this.name = _name;
		action = _action;
		box = _box;
	}
	
	@Override
	public void run() {
		if(action == Action.getApple){
			while(flag){
				getApple(box);
			}
		}else if(action == Action.putApple){
			while(flag){
				putApple(box);
			}
		}
		
	}
	
	public void putApple(Box box){
		Apple apple = new Apple();
		box.add(apple);
	}
	
	public Apple getApple(Box box){
		return box.get();
	}
}
