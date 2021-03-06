import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class TaskWithResult implements Callable<String> {
	  
	private int id;

	public TaskWithResult(int id) {
		this.id = id;
	}

	@Override
	public String call() {
		  return "result of TaskWithResult: " + id;
	}
}

public class Test {
	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		ArrayList<Future<String>> result = new ArrayList<Future<String>>();
		for (int i = 0; i < 10; i++) {
			result.add(exec.submit(new TaskWithResult(i)));
		}
		    
		for(Future<String> fs : result) {
			try {
				System.out.println(fs.get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				exec.shutdown();
			}
		}
	}
}
