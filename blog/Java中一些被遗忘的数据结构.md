title: Java中一些被遗忘的数据结构
date: 2015-04-08 15:28:31
categories: Java
tags: 数据结构
description: 总结了目前为止我在Java中没用过或者用的很少的数据结构，比如LinkedHashMap、WeakHashMap、SynchronousQueue和DelayedWorkQueue

---
## 前言
这篇文章真是折磨人，从最初打算写的4个，一直增加到近10个。覆盖面太广，以后有时间把他们拆出来单独写~
## LinkedHashMap
首先看看LinkedHashMap的继承关系：
<!-- more -->
	public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>{}

它是HashMap的子类，其内部在HashMap的基础上维护了一个双向链表，这个可以从其内部的Entry类看出：

	private static class Entry<K,V> extends HashMap.Entry<K,V> {
        // These fields comprise the doubly linked list used for iteration.
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, HashMap.Entry<K,V> next) {
            super(hash, key, value, next);
        }
        /**
         * Removes this entry from the linked list.
         */
        private void remove() {
            before.after = after;
            after.before = before;
        }
        /**
         * Inserts this entry before the specified existing entry in the list.
         */
        private void addBefore(Entry<K,V> existingEntry) {
            after  = existingEntry;
            before = existingEntry.before;
            before.after = this;
            after.before = this;
        }
        /**
         * This method is invoked by the superclass whenever the value
         * of a pre-existing entry is read by Map.get or modified by Map.set.
         * If the enclosing Map is access-ordered, it moves the entry
         * to the end of the list; otherwise, it does nothing.
         * 
         * LinkedHashMap的迭代顺序是插入顺序还是访问顺序就是基于这个方法实现的。
         * 其原理是如果accessOrder为true，那么每次访问后都会把它放在链表的尾部。
         */
        void recordAccess(HashMap<K,V> m) {
            LinkedHashMap<K,V> lm = (LinkedHashMap<K,V>)m;
            if (lm.accessOrder) {
                lm.modCount++;
                remove();
                addBefore(lm.header);
            }
        }
        void recordRemoval(HashMap<K,V> m) {
            remove();
        }
    }

这个双向链表定义了迭代顺序，迭代的时候就按照这个链表的顺序进行。但是LinkedHashMap还为大家提供了一个功能就是按照访问顺序进行迭代。在LinkedHashMap内部有个``accessOrder``属性：

	/**
     * The iteration ordering method for this linked hash map: <tt>true</tt>
     * for access-order, <tt>false</tt> for insertion-order.
     *
     * @serial
     */
    private final boolean accessOrder;

插入顺序、访问顺序这样说可能会有些不直观，具体大家可以看我写的一个[例子](https://github.com/xumyselfcn/sourceCode4Blog/blob/master/structure/LinkedHashMap)


## TreeMap
看看TreeMap的继承关系：

	public class TreeMap<K,V>
	    extends AbstractMap<K,V>
	    implements NavigableMap<K,V>, Cloneable, java.io.Serializable{}

- TreeMap 是一个有序的key-value集合，它是通过红黑树实现的。
- TreeMap 继承于AbstractMap，所以它是一个Map，即一个key-value集合。
- TreeMap 实现了NavigableMap接口，意味着它支持一系列的导航方法。比如返回有序的key集合。
- TreeMap 实现了Cloneable接口，意味着它能被克隆。
- TreeMap 实现了java.io.Serializable接口，意味着它支持序列化。

TreeMap基于红黑树（Red-Black tree）实现。该映射根据其键的自然顺序进行排序，或者根据创建映射时提供的 Comparator 进行排序，具体取决于使用的构造方法。

TreeMap的基本操作 containsKey、get、put 和 remove 的时间复杂度是 log(n) 。
另外，TreeMap是非同步的。 它的iterator方法返回的迭代器是fail-fast的。

>fail-fast机制是java集合(Collection)中的一种错误机制。在文章底部有关于该机制的扩展链接

具体的请移步[这里](http://www.cnblogs.com/skywang12345/p/3310928.html)和[这里](http://www.cnblogs.com/chenssy/p/3746600.html)，我自认为不会比他们讲的更好了(^ ^)~

## WeakHashMap
先看看WeakHashMap的继承关系：

	public class WeakHashMap<K,V>
	    extends AbstractMap<K,V>
	    implements Map<K,V> {}

WeakHashMap 继承于AbstractMap，实现了Map接口。

和HashMap一样，WeakHashMap 也是一个**散列表**，它存储的内容也是**键值对(key-value)映射**，而且键和值都可以是**null**。

不过WeakHashMap的键是**“弱键”**。在 WeakHashMap 中，当某个键不再正常使用时，会被从WeakHashMap中被自动移除。更精确地说，对于一个给定的键，其映射的存在并不阻止垃圾回收器对该键的丢弃，这就使该键成为可终止的，被终止，然后被回收。某个键被终止时，它对应的键值对也就从映射中有效地移除了。

这个“弱键”的原理呢？大致上就是，**通过WeakReference和ReferenceQueue实现的**。 WeakHashMap的key是“弱键”，即是WeakReference类型的；ReferenceQueue是一个队列，它会保存被GC回收的“弱键”。实现步骤是：

- 新建WeakHashMap，将**“键值对”**添加到WeakHashMap中。
实际上，WeakHashMap是通过数组table保存Entry(键值对)；每一个Entry实际上是一个单向链表，即Entry是键值对链表。
- **当某“弱键”不再被其它对象引用**，并被**GC回收**时。在GC回收该“弱键”时，这个“弱键”也同时会被添加到ReferenceQueue(queue)队列中。
- 当下一次我们需要操作WeakHashMap时，会先同步table和queue。table中保存了全部的键值对，而queue中保存被GC回收的键值对；同步它们，就是**删除table中被GC回收的键值对**。

这就是“弱键”如何被自动从WeakHashMap中删除的步骤了。

WeakReference是“弱键”实现的哈希表。它这个“弱键”的目的就是：实现对“键值对”的动态回收。当“弱键”不再被使用到时，GC会回收它，WeakReference也会将“弱键”对应的键值对删除。

“弱键”是一个“弱引用(WeakReference)”，在Java中，WeakReference和ReferenceQueue 是联合使用的。在WeakHashMap中亦是如此：如果弱引用所引用的对象被垃圾回收，Java虚拟机就会把这个弱引用加入到与之关联的引用队列中。 接着，WeakHashMap会根据“引用队列”，来删除“WeakHashMap中已被GC回收的‘弱键’对应的键值对”。

### WeakReference/ReferenceQueue
详细请移步[这里](http://www.cnblogs.com/skywang12345/p/3311092.html)


## SynchronousQueue

- 没有数据缓冲（size/peek/remove都是直返回0/null/false）
- SynchronousQueue的同步使用无锁算法
- 竞争机制支持公平和非公平两种
- 其内部也有用到ReentrantLock，不过是在writeObject方法中实现序列化过程中使用的
- 重点在TransferQueue内部类
- 其内部若干个方法都有中断

具体请看文章底部推荐的两篇文章及SynchronousQueue源码

## DelayQueue/DelayedWorkQueue
关于DelayQueue请移步[这里](http://www.cnblogs.com/jobs/archive/2007/04/27/730255.html)

这里把DelayedWorkQueue拿出来讲是不太恰当的，因为DelayedWorkQueue是ScheduledThreadPoolExecutor的内部类。作为线程安全集合，其内部的同步方法使用的是ReentrantLock锁。下面是它的继承关系：

	static class DelayedWorkQueue extends AbstractQueue<Runnable>
        implements BlockingQueue<Runnable> {}

DelayedWorkQueue内部使用的是数组进行存储
	
	private RunnableScheduledFuture[] queue =
            new RunnableScheduledFuture[INITIAL_CAPACITY];

而DelayQueue是用的PriorityQueue。

>这个 DelayedWorkQueue 和另一个 BlockingQueue 的实现 DelayQueue 很像。都是通过二叉堆算法实现排序，同样是在取操作的时候会 block 住知道 delay 到期。不同的是 DelayedWorkQueue 并没有采用 PriorityQueue，而是自己实现的二叉堆算法，不知道这是为什么（我猜是因为同是 1.5 新类，所以没有重用）。

>根据二叉堆的定义，DelayedWorkQueue 中的元素第一个元素永远是 delay 时间最小的那个元素，如果 delay 没有到期，take 的时候便会 block 住。

>了解了 DelayedWorkQueue，理解 ScheduledThreadPoolExecutor 就容易了。当执行 schedule 方法是。如果不是重复的任务，那任务从 DelayedWorkQueue 取出之后执行完了就结束了。如果是重复的任务，那在执行结束前会重置执行时间并将自己重新加入到 DelayedWorkQueue 中。

## PriorityQueue/PriorityBlockingQueue





## 参考资料
- [深入Java集合学习系列：LinkedHashMap的实现原理](http://zhangshixi.iteye.com/blog/673789)
- LinkedHashMap的应用场景：[如何用LinkedHashMap实现LRU缓存算法](http://blog.csdn.net/exceptional_derek/article/details/11713255)
- [Java 集合系列12之 TreeMap详细介绍(源码解析)和使用示例](http://www.cnblogs.com/skywang12345/p/3310928.html)
- [Java提高篇（二七）-----TreeMap](http://www.cnblogs.com/chenssy/p/3746600.html)
- 关于fail-fast的几篇文章
	- [what is fail-safe & fail-fast Iterators in java & how they are implemented](http://stackoverflow.com/questions/17377407/what-is-fail-safe-fail-fast-iterators-in-java-how-they-are-implemented)
	- [Fail Fast vs Fail Safe Iterator in java : Java Developer Interview Questions](http://javahungry.blogspot.com/2014/04/fail-fast-iterator-vs-fail-safe-iterator-difference-with-example-in-java.html)
- [Java 集合系列13之 WeakHashMap详细介绍(源码解析)和使用示例](http://www.cnblogs.com/skywang12345/p/3311092.html)
- [Java并发包中的同步队列SynchronousQueue实现原理](http://ifeve.com/java-synchronousqueue/)
- [SynchronousQueue(同步队列)](http://shift-alt-ctrl.iteye.com/blog/1840385)
- [精巧好用的DelayQueue](http://www.cnblogs.com/jobs/archive/2007/04/27/730255.html)
- [基于堆实现的优先级队列：PriorityQueue 解决 Top K 问题](http://my.oschina.net/leejun2005/blog/135085)