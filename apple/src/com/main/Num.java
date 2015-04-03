package com.main;

import java.util.Random;


public class Num {
	
	
	public void printNums(){
		int[] nums = new int[1000];
 		for(int i=0;i<1000;i++)
 			nums[i] = i+1;
 		
 		int times = 900;
 		int scope = 1000;
 		while(times >= 0){
 			Random r = new Random();
 	 		int index = r.nextInt(scope);
 	 		System.out.println(nums[index]);
 	 		
 	 		nums[index] = nums[scope-1];
 	 		
 	 		scope--;
 	 		times--;
 		}
 		
	}
	
	
	public static void main(String[] args) {
		Num n = new Num();
		n.printNums();
	}
}
