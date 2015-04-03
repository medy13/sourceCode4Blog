package com.main;

import java.util.LinkedList;

/**
 * 基于LinkedList实现的线程安全存放苹果的“箱子”
 * 
 */
public class Box {
	private volatile LinkedList<Apple> queue = new LinkedList<Apple>();
	private final static int MAXSIZE = 5;

	public void add(Apple apple){
		synchronized (queue) {
			if(queue.size()<= Box.MAXSIZE){
				queue.add(apple);
				System.out.println("fang");
				queue.notify();
			}
			else{
				try {
					queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Apple get(){
		Apple apple = null;
		synchronized (queue) {
			if(queue.size()>0){
				apple = queue.pollFirst();
				System.out.println("na");
				queue.notify();
			}else{
				try {
					queue.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return apple;
	}
}
