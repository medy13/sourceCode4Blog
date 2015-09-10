title: 源码浅析-Callable和Future
date: 2015-03-28 10:28:31
categories: Java
tags: 
- java
- Callable

---
## 用Callable接口可以干啥
简单来说，以前多线程的run方法返回值是void，那现在我想让各个线程有返回值咋办：使用Callable接口！

话不多说，先上例子，这个例子引自“飘过的小牛”的[这篇文章中](http://github.thinkingbar.com/thinking_in_java_chapter21-part01/)。代码如下：

<!-- more -->

	class TaskWithResult implements Callable<String> {
		private int id;
		public TaskWithResult(int id) {
			this.id = id;
		}
		//必须重写call函数
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
<!--more-->
这个例子的完整源码可以在[这里](https://github.com/xumyselfcn/sourceCode4Blog/blob/master/callable/Test.java)看到。代码中有两个类，一个TaskWithResult，实现了Callable接口，类中重写call函数；另一个Test类使用Executors.newCachedThreadPool()创建线程池。
	
	ExecutorService exec = Executors.newCachedThreadPool();
	ArrayList<Future<String>> result = new ArrayList<Future<String>>();
	for (int i = 0; i < 10; i++) {
		result.add(exec.submit(new TaskWithResult(i)));
	}

	Future的get()方法用于取出返回的值

这几行比较关键，第一行创建线程池，第二行创建了result——其类型是Future<\String\>，然后exec调用了submit方法创建线程，并将submit方法的返回值装入result中。这表明：

- 使用exec.submit(new TaskWithResult(i))可以执行线程
- submit()方法有返回值，是Future<String/>类型

那这个exec.submit()方法和exec.execute()方法有什么区别呢？进一步，实现Callable接口和Runnable接口有什么不同呢？

- 实现Callable接口，必须重写call方法；而Runnable则对应的是run方法
- call方法有返回值；run方法没有返回值
- call方法可以抛出异常；run方法不能抛出异常
- 使用run方法实现的多线程，使用call方法同样可以实现

**Callable是类似于Runnable的接口，实现Callable接口的类和实现Runnable的类都是可被其他线程执行的任务。**可以看看[这里面](http://www.oschina.net/question/54100_83333)的三个实例，可以帮助理解、加深一下印象。

##究竟如何获取不同线程的返回值-Future登场
回到上面的那个例子

	result.add(exec.submit(new TaskWithResult(i)));

我们可以看到，在for循环里面，将每个exec.submit()方法的返回值放进了result里面。然后在后面，通过增强for循环获取每个Future，然后执行Future.get()方法获取线程返回值。

现在我们先来看看这个submit方法里面究竟做了什么：

	//根据ExecutorService exec = Executors.newCachedThreadPool();
	//找到submit实现的位置在AbstractExecutorService类中。
	//该类通过重载实现了三个不同的submit方法。
	public Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }
	public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }
	public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

这里，我们的样例应该调用的是第三个submit函数。函数执行的过程就是：

1. 先判断task是否为null
2. 创建一个ftask对象
3. 执行ftask
4. 返回ftask

好嘛，**里面其实执行线程的还是execute函数**，可以印证上面的一个结论：**Callable是类似于Runnable的接口，实现Callable接口的类和实现Runnable的类都是可被其他线程执行的任务，**因为Callable里面其实就是用Runnable来实现创建多线程的。

仔细看看这三个submit函数，主要区别就在于每个submit函数的第三行，我把他们放在一起对比下：

	RunnableFuture<Void> ftask = newTaskFor(task, null);
	RunnableFuture<T> ftask = newTaskFor(task, result);
	RunnableFuture<T> ftask = newTaskFor(task);

ftask的类型是RunnableFuture的，那这个RunnableFuture是什么呢？先别着急看答案，我们来猜一猜：

- RunnableFuture必然实现了Runnable接口（因为ftask可以作为execute()方法的参数）
- RunnableFuture必然实现了Future接口（因为ftask可以作为submit()方法的返回值）

欧凯，我们来看看源码：

	public interface RunnableFuture<V> extends Runnable, Future<V> {
	    /**
	     * Sets this Future to the result of its computation
	     * unless it has been cancelled.
	     */
	    void run();
	}

果然。实现Runnable接口不必多说，那这个Future接口是做什么的？

现在有必要介绍一下Future接口了。
	
	/**
	 * A <tt>Future</tt> represents the result of an asynchronous
	 * computation.  Methods are provided to check if the computation is
	 * complete, to wait for its completion, and to retrieve the result of
	 * the computation.  The result can only be retrieved using method
	 * <tt>get</tt> when the computation has completed, blocking if
	 * necessary until it is ready.  Cancellation is performed by the
	 * <tt>cancel</tt> method.  Additional methods are provided to
	 * determine if the task completed normally or was cancelled. Once a
	 * computation has completed, the computation cannot be cancelled.
	 * If you would like to use a <tt>Future</tt> for the sake
	 * of cancellability but not provide a usable result, you can
	 * declare types of the form {@code Future<?>} and
	 * return <tt>null</tt> as a result of the underlying task.
	 * /
	public interface Future<V> {
	    boolean cancel(boolean mayInterruptIfRunning);
	    boolean isCancelled();
	    boolean isDone();
	    V get() throws InterruptedException, ExecutionException;
	    V get(long timeout, TimeUnit unit)
	        throws InterruptedException, ExecutionException, TimeoutException;
	}

Future表示异步的计算结果，还提供了与之相关的一系列的函数。这里重点注意函数名为get()的两个函数。**具体的Future模式在[这篇文章](http://zha-zi.iteye.com/blog/1408189)中有详细描述，一定不能错过！**。

以下是从Future模式这个文章中摘录的：

Future对象本身可以看作是一个显式的引用，一个对异步处理结果的引用。由于其异步性质，在创建之初，它所引用的对象可能还并不可用（比如尚在运算中，网络传输中或等待中）。这时，得到Future的程序流程如果并不急于使用Future所引用的对象，那么它可以做其它任何想做的事儿，当流程进行到需要Future背后引用的对象时，可能有两种情况：

- 希望能看到这个对象可用，并完成一些相关的后续流程。如果实在不可用，也可以进入其它分支流程。
- “没有你我的人生就会失去意义，所以就算海枯石烂，我也要等到你。”（当然，如果实在没有毅力枯等下去，设一个超时也是可以理解的）


对于前一种情况，可以通过调用Future.isDone()判断引用的对象是否就绪，并采取不同的处理；而后一种情况则只需调用get()或get(long timeout, TimeUnit unit)通过同步阻塞方式等待对象就绪。实际运行期是阻塞还是立即返回就取决于get()的调用时机和对象就绪的先后了。

简单而言，Future模式可以在连续流程中满足数据驱动的并发需求，既获得了并发执行的性能提升，又不失连续流程的简洁优雅。但是Futrue模式有个重大缺陷：当消费者工作得不够快的时候，它会阻塞住生产者线程，从而可能导致系统吞吐量的下降。所以不建议在高性能的服务端使用。

java.util.concurrent.Callable与java.util.concurrent.Future类可以协助您完成Future模式。**Future模式在请求发生时，会先产生一个Future对象给发出请求的客户。它的作用类似于代理(Proxy)对象，而同时所代理的真正目标对象的生成是由一个新的线程持续进行。真正的目标对象生成之后，将之设置到Future之中，而当客户端真正需要目标对象时，目标对象也已经准备好，可以让客户提取使用。**


## 参考资料

- Callable及相关源码
- [Callable与Future的介绍](http://www.cnblogs.com/whgw/archive/2011/09/28/2194760.html)
- [Future 模式详解（并发使用）](http://zha-zi.iteye.com/blog/1408189)







