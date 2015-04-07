import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class LinkedHashMapTest {
	static LinkedHashMap<Integer,String> mapLinked = 
			new LinkedHashMap<Integer,String>(8, (float)0.75, true);
	static HashMap<Integer,String> mapHash = new HashMap<Integer,String>();
	
	/**
	 * init the two maps
	 */
	private static void init(){
		int[] ids = {201500002,201500001,201500009,201500004,201500006};
		String[] names = {"张三","李四","王二","葛二蛋","三毛"};
		
		for(int i=0;i<ids.length&&i<names.length;i++){
			mapLinked.put(ids[i], names[i]);
			mapHash.put(ids[i], names[i]);
		}
	}
	
	/**
	 * traverse the two maps
	 */
	private static void traverse(){
		Iterator<Integer> iterLinked = mapLinked.keySet().iterator();
		Iterator<Integer> iterHash = mapHash.keySet().iterator();
		
		while(iterLinked.hasNext() && iterHash.hasNext()){
			System.out.println(iterLinked.next()+" || "+iterHash.next());
		}
		System.out.println("================================");
	}
	
	public static void main(String[] args) {
		init();
		traverse();
		mapLinked.get(201500004);
		//注意mapLinkedL的201500004前后遍历所在的位置
		//LinkedHashMap的get()方法除了返回元素之外还可以把被访问的元素放到链表的底端，这样一来每次顶端的元素就是remove的元素。
		//但是需要结合LinkedHashMap的构造函数LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) 使用，将accessOrder设置为true（默认是false）
		traverse();
	}
}
