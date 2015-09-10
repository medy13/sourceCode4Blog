title: 源码浅析-使用ThreadPoolExecutor创建线程池-以Executors提供的几种方式为例
date: 2015-03-26 10:28:31
categories: Java
tags: 
- java
- 多线程

---

ThreadPoolExecutor类是用来创建线程池的类，通常使用Executors里面的静态方法来创建（Executors类封装了一系列的静态方法，类似的还有Arrays，Collections等）。线程池负责管理工作线程，包含一个等待执行的任务队列。线程池的任务队列是一个Runnable集合，工作线程负责从任务队列中取出并执行Runnable对象。

java.util.concurrent.executors 提供了 java.util.concurrent.executor 接口的一个Java实现，可以创建线程池。常见的用法如下：
	
	public class Printer implements Runnable{
		@Override
	    public void run() {
			System.out.println("打印机工作");
			TimeUnit.MILLISECONDS.sleep(1000);
		}
	}
	
	public class Test{
		ExecutorService exec = Executors.newCachedThreadPool();
		for(int i = 0; i < 5; i++) {
	    	exec.execute(new Printer());
		}
		exec.shutdown();
	}
<!--more-->
我们拿到一个 ExecutorService 来管理所有的 Thread 对象，在执行完所有的任务后，只需要调用一个 shutdown()即可关闭所有管理的 Thread 对象。这里面我们用的是Executors.newCachedThreadPool()，除此之外，还有另外几种常用的线程池：

- 固定工作线程数量的线程池：Executors.newFixedThreadPool()
- 一个可缓存的线程池：Executors.newCachedThreadPool()
- 单线程化的Executor：Executors.newSingleThreadExecutor()
- 支持定时的以及周期性的任务执行：Executors.newScheduledThreadPool()

主要就是上面那四种，值得注意的是，**这几种线程池的任务队列各不相同**。关于缓冲队列也就是下面构造函数中的形参workQueue的官方英文解释是：
	
>workQueue: the queue to use for holding tasks before they are executed. This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.

- newFixedThreadPool使用的是LinkedBlockingQueue
- newCachedThreadPool使用的是SynchronousQueue
- newSingleThreadExecutor使用的是LinkedBlockingQueue
- newScheduledThreadPool使用的是DelayedWorkQueue

这些队列有什么区别呢？我现在也不清楚，明天好好整理整理。

下面这些是Executors里面给我们定义好的几种线程池，我们可以根据实际情况进行使用，如果都不满足的话，可以自己创建ThreadPoolExecutor，可以更加灵活的设置各种构造参数。

![](http://xumyselfcn.github.io/imgs/newThreadPool.png)

我们如果看Executors的源码的话，可以发现，上述Executors的四个构造线程池的方法内部都是返回一个新建的ThreadPoolExecutor类，所以我们直接研究ThreadPoolExecutor的构造函数就可以明白Executors创建线程池的原理。

		//ThreadPoolExecutor的构造函数
		public ThreadPoolExecutor(int corePoolSize,
			int maximumPoolSize,
			long keepAliveTime,
			TimeUnit unit,
			BlockingQueue<Runnable> workQueue，
			ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
			this(....);//调用本地的构造函数
		}

先解释在上述的构造函数中出现的几个形参：

- corePoolSize：线程池维护线程的最少数量
- maximumPoolSize：线程池维护线程的最大数量
- keepAliveTime：线程池维护线程所允许的空闲时间
- unit： 线程池维护线程所允许的空闲时间的单位，unit可选的参数为java.util.concurrent.TimeUnit中的几个静态属性：
	- NANOSECONDS
	- MICROSECONDS
	- MILLISECONDS
	- SECONDS
- workQueue：线程池所使用的缓冲队列，常用的是：java.util.concurrent.ArrayBlockingQueue
- threadFactory：创建新线程时，使用的线程工厂
- handler： 线程池对拒绝任务的处理策略，有四个选择如下：
	- ThreadPoolExecutor.AbortPolicy()：抛出java.util.concurrent.RejectedExecutionException异常
	- ThreadPoolExecutor.CallerRunsPolicy()：重试添加当前的任务，他会自动重复调用execute()方法
	- ThreadPoolExecutor.DiscardOldestPolicy()：抛弃旧的任务
	- ThreadPoolExecutor.DiscardPolicy()：抛弃当前的任务

当一个任务通过execute(Runnable)方法想添加线程到线程池时： 

- 如果此时线程池中的数量小于corePoolSize，即使线程池中的线程都处于空闲状态，也要创建新的线程来处理被添加的任务。
- 如果此时线程池中的数量等于 corePoolSize，但是缓冲队列 workQueue未满，那么任务被放入缓冲队列。
- 如果此时线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量小于maximumPoolSize，建新的线程来处理被添加的任务。
- 如果此时线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量等于 maximumPoolSize，那么通过 handler所指定的策略来处理此任务。也就是：处理任务的优先级为：核心线程corePoolSize、任务队列workQueue、最大线程 maximumPoolSize，如果三者都满了，使用handler处理被拒绝的任务。
- 当线程池中的线程数量大于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止。这样，线程池可以动态的调整池中的线程数。
	
下面我们看看ThreadPoolExecutor的execute方法，中间的注释描述的跟上面这段意思差不多。

	public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        /*
         * Proceed in 3 steps:
         *
         * 1. If fewer than corePoolSize threads are running, try to
         * start a new thread with the given command as its first
         * task.  The call to addWorker atomically checks runState and
         * workerCount, and so prevents false alarms that would add
         * threads when it shouldn't, by returning false.
         *
         * 2. If a task can be successfully queued, then we still need
         * to double-check whether we should have added a thread
         * (because existing ones died since last checking) or that
         * the pool shut down since entry into this method. So we
         * recheck state and if necessary roll back the enqueuing if
         * stopped, or start a new thread if there are none.
         *
         * 3. If we cannot queue task, then we try to add a new
         * thread.  If it fails, we know we are shut down or saturated
         * and so reject the task.
         */
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))
            reject(command);
    }


## 参考资料
- Executors、ThreadPoolExecutor源码
- [Java线程池ThreadPoolExecutor](http://my.oschina.net/linuxred/blog/27924)

