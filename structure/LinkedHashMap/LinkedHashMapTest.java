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
		String[] names = {"����","����","����","�����","��ë"};
		
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
		//ע��mapLinkedL��201500004ǰ��������ڵ�λ��
		//LinkedHashMap��get()�������˷���Ԫ��֮�⻹���԰ѱ����ʵ�Ԫ�طŵ�����ĵ׶ˣ�����һ��ÿ�ζ��˵�Ԫ�ؾ���remove��Ԫ�ء�
		//������Ҫ���LinkedHashMap�Ĺ��캯��LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) ʹ�ã���accessOrder����Ϊtrue��Ĭ����false��
		traverse();
	}
}
