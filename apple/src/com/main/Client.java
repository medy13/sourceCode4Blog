package com.main;

import java.util.concurrent.TimeUnit;

import com.main.Person.Action;

public class Client {
	
	public static void main(String[] args) {
		Box box = new Box();
		Person putMan = new Person("³Â·Å",Action.putApple,box);
		Person getMan = new Person("ÇúÄÃ",Action.getApple,box);
		
		Thread put = new Thread(putMan);
		Thread get = new Thread(getMan);
		
		put.start();
		get.start();
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		putMan.setFlag(false);
		getMan.setFlag(false);
	}
}
