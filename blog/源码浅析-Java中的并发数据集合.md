title: 源码浅析-Java中的并发数据集合
date: 2015-03-12 20:37:11
categories: Java
tags: 
- java
- LinkedBlockingDeque
- ConcurrentLinkedDeque
- 并发
- CAS
---
## 概述
并发集合是在并发环境下使用的一类集合，不同于普通的集合：ArrayList、HashSet等，并发集合在并发环境下不需要考虑数据不一致的情况，也就是说使用并发集合是[线程安全](http://zh.wikipedia.org/wiki/%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8)的。

一般来说，并发集合有如下两大类：

> * 阻塞集合：这种集合包括添加和删除数据的操作。如果操作不能立即进行，是因为集合已满或者为空，该程序将被阻塞，直到操作可以进行。
* 非阻塞集合：这种集合也包括添加和删除数据的操作。如果操作不能立即进行，这个操作将返回null值或抛出异常，但该线程将不会阻塞。

<!--more-->

具体的集合如下：

- 非阻塞列表，使用ConcurrentLinkedDeque类。
- 非阻塞可导航的map，使用[ConcurrentSkipListMap](http://ifeve.com/concurrent-collections-6/)类。
- 阻塞列表，使用LinkedBlockingDeque类。
- 用在生产者与消费者数据的阻塞列表，使用LinkedTransferQueue类。
- 使用优先级排序元素的阻塞列表，使用[PriorityBlockingQueue](http://ifeve.com/concurrent-collections-4/)类。
- 存储延迟元素的阻塞列表，使用[DelayQueue](http://ifeve.com/concurrent-collections-5/)类。

**下面详细从ConcurrentLinkedDeque和LinkedBlockingDeque源码入手分析。**

## ConcurrentLinkedDeque

这是一个非阻塞集合，其主要成员对象如下

	//主要成员对象
	private transient volatile Node<E> head;
	private transient volatile Node<E> tail;

常用的方法有：
	
	public void addFirst(E e)
	public void addLast(E e)
	public boolean add(E e) //内部调用 addLast(E e)

	public boolean offerFirst(E e) //调用addFirst(E e)
	public boolean offerLast(E e) //调用addLast(E e)
	public boolean offer(E e) //内部调用offerLast(E e)
	
	public E pollFirst() //弹出第一个，并返回
	public E pollLast() //弹出最后一个 并返回

	public E removeFirst() //调用pollFirst()
	public E removeLast() //调用pollLast()

	public E poll()           { return pollFirst(); }
    public E remove()         { return removeFirst(); }
    public E peek()           { return peekFirst(); }
    public E element()        { return getFirst(); }
    public void push(E e)     { addFirst(e); }
    public E pop()            { return removeFirst(); }
	
以offerFirst()代码为例，查看ConcurrentLinkedDeque内部方法的实现：
	
	public boolean offerFirst(E e) {
        linkFirst(e);
        return true;
    }
	
	private void linkFirst(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);

        restartFromHead:
        for (;;)
            for (Node<E> h = head, p = h, q;;) {
                if ((q = p.prev) != null &&
                    (q = (p = q).prev) != null)
                    // Check for head updates every other hop.
                    // If p == q, we are sure to follow head instead.
                    p = (h != (h = head)) ? h : q;
                else if (p.next == p) // PREV_TERMINATOR
                    continue restartFromHead;
                else {
                    // p is first node
                    newNode.lazySetNext(p); // CAS piggyback
                    if (p.casPrev(null, newNode)) {
                        // Successful CAS is the linearization point
                        // for e to become an element of this deque,
                        // and for newNode to become "live".
                        if (p != h) // hop two nodes at a time
                            casHead(h, newNode);  // Failure is OK.
                        return;
                    }
                    // Lost CAS race to another thread; re-read prev
                }
            }
    }

ConcurrentLinkedDeque类中的head和tail是有volatile关键字修饰的，上面插入头结点的关键部分使用的是Node内部类中的方法，具体代码最后有附上。其方法并没有使用任何同步措施，比如：synchronizd关键字或ReentrantLock类，也正是因为这个，所以可以做到非阻塞，读者注意比较下面LinkedBlockingDeque。

## LinkedBlockingDeque

这是一个阻塞集合，其主要成员如下

	//主要成员对象
	transient Node<E> first;
	transient Node<E> last;
	private transient int count;
	
	/** Main lock guarding all access */
    final ReentrantLock lock = new ReentrantLock();

    /** Condition for waiting takes */
    private final Condition notEmpty = lock.newCondition();

    /** Condition for waiting puts */
    private final Condition notFull = lock.newCondition();


常用方法同上面的ConcurrentLinkedDeque类中提供的方法比较类似，addFirst/addLast/offerFirst/offerLast/pollFirst/pollLast等均有提供，不同的是，这些方法中都使用了lock进行同步，以offerFirst()方法为例

	public boolean offerFirst(E e) {
        if (e == null) throw new NullPointerException();
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return linkFirst(node);
        } finally {
            lock.unlock();
        }
    }

	private boolean linkFirst(Node<E> node) {
        // assert lock.isHeldByCurrentThread();
        if (count >= capacity)
            return false;
        Node<E> f = first;
        node.next = f;
        first = node;
        if (last == null)
            last = node;
        else
            f.prev = node;
        ++count;
        notEmpty.signal();
        return true;
    }

	

在向当前deque中插入新数据时，会先使用lock()方法上锁，然后插入新节点作为第一个元素。

**ConcurrentLinkedDeque和LinkedBlockingDeque的区别**

* 第一个区别：两者的方法一个使用Lock加锁，另一个则不是。
* 第二个区别：两者的内部Node类也有很大不同（具体看下面附上的Node类的代码）

ConcurrentLinkedDeque类的Node类

	static final class Node<E> {
	    volatile Node<E> prev;
	    volatile E item;
	    volatile Node<E> next;
	
	    Node() {  // default constructor for NEXT_TERMINATOR, PREV_TERMINATOR
	    }
	
	    /**
	     * Constructs a new node.  Uses relaxed write because item can
	     * only be seen after publication via casNext or casPrev.
	     */
	    Node(E item) {
	        UNSAFE.putObject(this, itemOffset, item);
	    }
	
	    boolean casItem(E cmp, E val) {
	        return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
	    }
	
	    void lazySetNext(Node<E> val) {
	        UNSAFE.putOrderedObject(this, nextOffset, val);
	    }
	
	    boolean casNext(Node<E> cmp, Node<E> val) {
	        return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
	    }
	
	    void lazySetPrev(Node<E> val) {
	        UNSAFE.putOrderedObject(this, prevOffset, val);
	    }
	
	    boolean casPrev(Node<E> cmp, Node<E> val) {
	        return UNSAFE.compareAndSwapObject(this, prevOffset, cmp, val);
	    }
	
	    // Unsafe mechanics
	
	    private static final sun.misc.Unsafe UNSAFE;
	    private static final long prevOffset;
	    private static final long itemOffset;
	    private static final long nextOffset;
	
	    static {
	        try {
	            UNSAFE = sun.misc.Unsafe.getUnsafe();
	            Class k = Node.class;
	            prevOffset = UNSAFE.objectFieldOffset
	                (k.getDeclaredField("prev"));
	            itemOffset = UNSAFE.objectFieldOffset
	                (k.getDeclaredField("item"));
	            nextOffset = UNSAFE.objectFieldOffset
	                (k.getDeclaredField("next"));
	        } catch (Exception e) {
	            throw new Error(e);
	        }
	    }
	}

LinkedBlockingDeque类的Node类

	static final class Node<E> {
        /**
         * The item, or null if this node has been removed.
         */
        E item;

        /**
         * One of:
         * - the real predecessor Node
         * - this Node, meaning the predecessor is tail
         * - null, meaning there is no predecessor
         */
        Node<E> prev;

        /**
         * One of:
         * - the real successor Node
         * - this Node, meaning the successor is head
         * - null, meaning there is no successor
         */
        Node<E> next;

        Node(E x) {
            item = x;
        }
    }

有注意到ConcurrentLinkedDeque类的Node类的cas开头的几个函数吗?比如：casItem(),casNext()和casPrev(),这些都是CAS操作，CAS(Compare & Set 或者 Compare & Swap)为并发操作对象的提供更好的性能，CAS操作通过以下3个步骤来实现对变量值得修改：

- 获取当前内存中的变量的值
- 用一个新的临时变量(temporal variable)保存改变后的新值
- 如果当前内存中的值等于变量的旧值，则将新值赋值到当前变量；否则不进行任何操作

这里先介绍一下``乐观锁``和``悲观锁``：
>- 悲观锁：假定会发生并发冲突，屏蔽一切可能违反数据完整性的操作。悲观锁假定其他用户企图访问或者改变你正在访问、更改的对象的概率是很高的，因此在悲观锁的环境中，在你开始改变此对象之前就将该对象锁住，并且直到你提交了所作的更改之后才释放锁。
- 乐观锁:假设不会发生并发冲突，只在提交操作时检查是否违反数据完整性。观锁不能解决脏读的问题。乐观锁则认为其他用户企图改变你正在更改的对象的概率是很小的，因此乐观锁直到你准备提交所作的更改时才将对象锁住，当你读取以及改变该对象时并不加锁。可见乐观锁加锁的时间要比悲观锁短，乐观锁可以用较大的锁粒度获得较好的并发访问性能。乐观锁中如果因为冲突失败就重试，直到成功为止。

对于这个机制，你不需要使用任何同步机制，这样你就避免了deadlocks，也获得了更好的性能。这种机制能保证多个并发线程对一个共享变量操作做到最终一致。Java 在原子类中实现了CAS机制。这些类提供了compareAndSet() 方法；这个方法是CAS操作的实现和其他方法的基础。

显然，阻塞队列使用悲观锁，基于Lock实现；非阻塞使用乐观锁，基于volatile和CAS实现。

## 总结

- 阻塞集合：如果操作不能立即进行，是因为集合已满或者为空，该程序将被阻塞，直到操作可以进行。
- 非阻塞集合：如果操作不能立即进行，这个操作将返回null值或抛出异常，但该线程将不会阻塞。


问题1：CAS机制和传统的使用锁或者关键字相比，各自的优缺点，各自的适用情景(扩展阅读中有答案)？

这里先说一下[CAS的优缺点](http://www.cnblogs.com/pingh/p/3505486.html)：
>- 优点：
非阻塞算法（通常叫作乐观算法）相对于基于锁的版本有几个性能优势。首先，它用硬件的原生形态代替 JVM 的锁定代码路径，从而在更细的粒度层次上（独立的内存位置）进行同步，失败的线程也可以立即重试，而不会被挂起后重新调度。更细的粒度降低了争用的机会，不用重新调度就能重试的能力也降低了争用的成本。即使有少量失败的 CAS 操作，这种方法仍然会比由于锁争用造成的重新调度快得多。

>- 缺点：
	- 1、ABA问题
CAS操作容易导致ABA问题,也就是在做a++之间，a可能被多个线程修改过了，只不过回到了最初的值，这时CAS会认为a的值没有变。a在外面逛了一圈回来，你能保证它没有做任何坏事，不能！！也许它讨闲，把b的值减了一下，把c的值加了一下等等，更有甚者如果a是一个对象，这个对象有可能是新创建出来的，a是一个引用呢情况又如何，所以这里面还是存在着很多问题的，解决ABA问题的方法有很多，可以考虑增加一个修改计数，只有修改计数不变的且a值不变的情况下才做a++，也可以考虑引入版本号，当版本号相同时才做a++操作等，这和事务原子性处理有点类似！
	- 2、比较花费CPU资源，即使没有任何争用也会做一些无用功。
	- 3、会增加程序测试的复杂度，稍不注意就会出现问题。

>可以用CAS在无锁的情况下实现原子操作，但要明确应用场合，非常简单的操作且又不想引入锁可以考虑使用CAS操作(比如get/set)，当想要非阻塞地完成某一操作也可以考虑CAS。不推荐在复杂操作中引入CAS，会使程序可读性变差，且难以测试，同时会出现ABA问题。嘿嘿嘿，CAS操作适用于那种读多写少的情况，因为这时CAS在更新时候冲突的概率会大大降低。

## 补充知识点

1. java中CAS的实现被封装在[sun.misc.Unsafe](http://www.docjar.com/html/api/sun/misc/Unsafe.java.html)类中。
2. java.util.concurrent.atomic中的AtomicInteger、AtomicIntegerArray、AtomicLong等都是基于CAS实现的。
3. volilate和cas只能乐观锁保证的状态控制的正确，而在设置状态失败的时候，仍然需要阻塞线程。juc里提供了LockSupport的park和unpark方法用于阻塞线程。而不同的场景下需要不同的等待策略和锁共享策略，juc提供了AbstractQueuedSynchronizer（AQS）为基类的一序列不同的锁，底层都是基于CAS、LocakSupport和Queue来管理。


## 参考资料
- [并发集合](http://ifeve.com/concurrent-collections-1/)
- [使用原子 arrays](http://ifeve.com/concurrent-collections-9/)
- [乐观锁和悲观锁的区别](http://www.cnblogs.com/Bob-FD/p/3352216.html)
- [乐观锁与悲观锁及其实现](http://www.cnblogs.com/pingh/p/3505486.html)
- Java源码（JDK的源码在jdk安装目录下：src.zip）

## 本文扩展阅读
- [原子操作的实现原理](http://www.infoq.com/cn/articles/atomic-operation)
- [非阻塞同步算法与CAS算法](http://www.cnblogs.com/Mainz/p/3546347.html)
- [无锁队列的实现](http://coolshell.cn/articles/8239.html)
- [ReentrantLock(重入锁)以及公平性](http://ifeve.com/reentrantlock-and-fairness/)

## Java并发资料
- [Java并发性和多线程](http://ifeve.com/java-concurrency-thread-directory/)
- [Java 7 并发编程指南](http://ifeve.com/java-7-concurrency-cookbook/)
- [七周七并发模型](http://ifeve.com/concurrency-modle-seven-week-1/)

