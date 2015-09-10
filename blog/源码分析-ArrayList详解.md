title: 源码分析-ArrayList详解
date: 2015-03-23 10:28:31
categories: Java
tags: 
- java
- 容器

---
## 概述

ArrayList是List、RandomAccess、Cloneable、Serializable接口的可变长数组实现。与Vector的最大区别就是没用使用Synchronized关键字进行同步。不过ArrayList对writeObject和readObject两个方法进行了同步。

<!-- more -->

	//java version "1.7.0_40"
	//Java(TM) SE Runtime Environment (build 1.7.0_40-b43)
	//Java HotSpot(TM) Client VM (build 24.0-b56, mixed mode, sharing)
	public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, 
		java.io.Serializable{
		...
	}

其实ArrayList的各种特性就是AbstractList、RandomAccess、Cloneable、Serializable几种接口特性、当然还有它自己的特性的叠加(这几个都是maker interface，具体见这里)。
<!--more-->
## 特性
- 顺序存储
- 默认初始大小为10，不够则扩容至原来的1.5倍(oldCapacity + (oldCapacity >> 1)),如果在已知大小的情况下，最好使用public ArrayList(int initialCapacity)构造函数进行初始化。
- 使用modCount、expectedModCount机制规避风险（这个是AbstractList接口的特性）
- clone方法用于返回一个当前List的引用，使用时注意避免浅拷贝。Returns a shallow copy of this ArrayList instance.  (The elements themselves are not copied.)
- 实现writeObject和readObject方法用于序列化

## 详解
如下是ArrayList成员对象的定义

	private static final int DEFAULT_CAPACITY = 10;
	private transient Object[] elementData;
	private int size;
### 1、Add方法、自动扩充和modCount
数据存储在Object数组中，默认初始大小是10，size存储当前List的长度。ArrayList的主要方法：
	
	public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

	private void ensureCapacityInternal(int minCapacity) {
        if (elementData == EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	
	private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

其实add方法没啥好讲的，就是先检查数组长度够不够再装一个，不够的话会调用grow方法“自动变长”，够的话就直接在size位置插入数据，然后把size加1。不过这里有个modCount++，这个modCount其实是在父类AbstractList中定义了一个int型的属性。

	protected transient int modCount = 0;

在ArrayList的所有涉及结构变化的方法中都增加modCount的值，包括：add()、remove()、addAll()、removeRange()、clear()方法。这些方法每调用一次，modCount的值就加1。

那么，modCount的作用是什么呢？这里可以把它理解为这个**List的版本号**。	

但是，**List要版本号干嘛呢**？

在对一个集合对象进行迭代操作的同时，并不限制对集合对象的元素进行操作，这些操作包括一些可能引起跌代错误的add()或remove()等危险操作。在AbstractList中，使用了一个简单的机制来规避这些风险。这就是modCount和expectedModCount的作用所在。下面是一个例子

	private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;
		...
		...
		final void checkForComodification() {
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	    }
		...
		...
	}

比如在ArrayList的内部类Itr中的next()、remove()方法中都有调用checkForComodification()方法，目的就是为了防止在执行迭代时，List被修改了，造成数据不一致。除此之外，在writeObject()方法中也使用到了modCount，用法也是类似。
	
	public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work

        return oldValue;
    }

可见，在remove()时，modCount执行了加1操作。
### 2、clone，浅拷贝or深拷贝
	
	/**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return a clone of this <tt>ArrayList</tt> instance
     */
	public Object clone() {
        try {
            @SuppressWarnings("unchecked")
                ArrayList<E> v = (ArrayList<E>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

拷贝之后，存储在原有List和克隆List中的对象会保持一致，并指向Java堆中同一内存地址，造成这一误解的原因是它采用Collections对不可变对象进行了浅拷贝。

### 3、序列化

在大型网站架构中，经常需要把对象进行序列化操作，以期在另外一台机器中恢复该对象并执行操作。常见的RPC（远程过程调用）经常就需要相应的序列化框架与其搭配使用。在ArrayList中，elementData数组对象定义为[transient](http://www.cnblogs.com/lanxuezaipiao/p/3369962.html)，也就是“瞬时的”，不能被序列化；这是因为elementData[]中存放的都是数据的引用，都是堆上的地址，所以如果对地址进行序列化其实是没有意义的，所以在这儿需要手工的对ArrayList的元素进行序列化操作。这就是writeObject()的作用。 

	/**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

	/**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }

至此，ArrayList中关键点都已经介绍完毕。下面是Java Collection Framework框架图，点击可放大。
![Collection FrameWork](http://blog.xumingyang.me/imgs/java-collection.jpg)

## 参考资料
- ArrayList源码（JDK的源码在jdk安装目录下：src.zip）
- [Java源码解读之java.util.ArrayList](http://blog.csdn.net/justin_579/article/details/440003)
- [RandomAccess详解](http://blog.xumingyang.cn/2015/03/19/RandomAccess%E8%AF%A6%E8%A7%A3/)