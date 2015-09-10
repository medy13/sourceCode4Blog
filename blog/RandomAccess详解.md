title: RandomAccess详解
date: 2015-03-23 10:28:31
categories: Java
tags: 
- java
- maker
- interface
---

RandomAccess、Cloneable、Serializable都是标记接口（maker interface），所谓标记接口不会在其内部定义方法，实现标记接口的类表明该类拥有一种特殊的能力。

比如实现RandomAccess接口的ArrayList有快速随机访问的能力，Cloneable、Serializable与之类似。其实ArrayList的各种特性就是AbstractList、RandomAccess、Cloneable、Serializable几种接口特性的叠加。

<!--more-->

>RandomAccess is a marker interface, like the Serializable and Cloneable interfaces. All of these marker interfaces do not define methods; instead, they identify a class as having a particular capability.

实现了maker interface的类有什么神奇的作用呢？

实现了Serializable接口的类的对象，在序列化的时候不会抛出NotSerializableException异常了（除非这个对象还包含其他未实现Serializable接口的内部类）；实现了Cloneable接口的类的对象，在使用clone()方法时，不会抛出CloneNotSupportedException异常了。

>In the case of Serializable, the interface specifies that if the class is serialized using the serialization I/O classes, then a NotSerializableException will not be thrown (unless the object contains some other class that cannot be serialized). Cloneable similarly indicates that the use of the Object.clone() method for a Cloneable class will not throw a CloneNotSupportedException.

其实，RandomAccess跟上述两个接口有点区别的。实现RandomAccess接口的集合类意味着：List.get()方法的执行速度会比Iterator.next()方法速度快。 

>The RandomAccess interface identifies that a particular java.util.List implementation has fast random access. A more accurate name for the interface would have been FastRandomAccess. This interface tries to define an imprecise concept: how fast is fast? The documentation provides a simple guide: if repeated access using the List.get() method is faster than repeated access using the Iterator.next() method, then the List has fast random access. 

关于RandomAccess的一个用法：
	
	if (listObject instanceof RandomAccess)
	{
	  for (int i=0, n=list.size(); i < n; i++)
	  {
	    o = list.get(i);
	    //do something with object o
	  }
	
	}
	else
	{
	  Iterator itr = list.iterator();
	  for (int i=0, n=list.size(); i < n; i++)
	  {
	    o = itr.next();
	    //do something with object o
	
	  }
	}

先判断这个对象是不是实现了RandomAccess的接口，如果是，那么就采用get()方法进行遍历；否则就使用迭代器进行遍历。

### 参考资料
- [Interface RandomAccess](http://www.onjava.com/pub/a/onjava/2001/10/23/optimization.html)
