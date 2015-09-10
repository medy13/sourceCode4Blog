title: 源码浅析-LinkedList详解
date: 2015-03-25 21:28:31
categories: Java
tags: 
- java
- 容器
---
## 概述
LinkedList是一个双向链表，继承自AbstractSequentialList类，实现了List、Deque、Cloneable和Serializable接口。
	
	//java version "1.7.0_40"
	//Java(TM) SE Runtime Environment (build 1.7.0_40-b43)
	//Java HotSpot(TM) Client VM (build 24.0-b56, mixed mode, sharing)
	public class LinkedList<E>
		extends AbstractSequentialList<E>
		implements List<E>, Deque<E>, Cloneable, java.io.Serializable{
		...
	}

对比上一篇[ArrayList详解](http://blog.xumingyang.cn/2015/03/27/ArrayList%E8%AF%A6%E8%A7%A3/) ，我们已经可以大概得知LinkedList有哪些特性了：是线性存储、可以当队列使用、可以调用clone方法、可以序列化。
<!--more-->

## 特性
- LinkedList是双向链表
- 实现 Queue 接口，提供先进先出队列操作
- 在列表的开头及结尾 get、remove和insert元素提供了统一的命名方法，这些操作允许将链接列表用作堆栈、队列或双端队列 (deque)
- clone、modCount和序列化的情况同ArrayList一致


## 详解
### 1、双向链表
	
	transient int size = 0;
	transient Node<E> first;
	transient Node<E> last;
	
从其属性定义看，一个指向头结点，一个指向尾节点，这里的Node是LinkedList的一个内部类：

	private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

### 实现Queue接口
其实LinkedList不仅可以拿来当Queue使用，也可以当Stack使用，因为它是双向链表！其内部提供的方法有：

	public boolean add(E e) 
	public void addFirst(E e) 
	public void addLast(E e) 
	public E get(int index) 
	public E getFirst() 
	public E getLast() 
	public boolean offer(E e) 
	public boolean offerFirst(E e) 
	public boolean offerLast(E e) 
	public E peek() 
	public E peekFirst() 
	public E peekLast() 
	public E poll() 
	public E pollFirst() 
	public E pollLast() 
	public E pop() 
	public void push(E e) 
	public E remove() 
	public E removeFirst() 
	public E removeLast() 
	public int size() 

队列和栈需要的方法全部都提供了

### 其他
1. LinkedList有两个内部类：ListItr和DescendingIterator，分别用来提供正向（从first到last）迭代器和反向（从last到first）迭代器。
2. remove(int)和remove(Object)的时间复杂度都是O(n)，因为需要先找到那个int 值/Object对象。

## LinkedList和ArrayList的区别、联系
相同点是两者都实现了List, Cloneable, Serializable接口，两者都是线性集合，都可以克隆，序列化。

ArrayList 采用的是数组形式来保存对象的，这种方式将对象放在连续的位置中，所以最大的缺点就是插入删除时非常麻烦，优点是查询速度快

LinkedList 采用的将对象存放在独立的空间中，而且在每个空间中还保存下一个链接的索引，但是缺点就是查找非常麻烦，要丛第一个索引开始，优点是插入、删除快。

更详细的可以参考[这篇](http://www.importnew.com/6629.html)

下面是Java Collection Framework框架图，点击可放大。
![Collection FrameWork](http://blog.xumingyang.me/imgs/java-collection.jpg)

## 参考资料
- LinkedList源码
- [LinkedList和ArrayList的区别](http://www.importnew.com/6629.html)