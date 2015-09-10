title: Java序列化
date: 2015-03-27 10:28:31
categories: Java
tags: 
- java
- 序列化

---

## 序列化的相关知识点
先看这三个知识点

- 将对象转换为字节流保存起来，并在以后还原这个对象，这种机制叫做``(对象)序列化``。
- 将一个对象保存到永久存储设备上称为``持久化``。
- 一个对象要想能够实现序列化，必须实现``Serializable``接口或``Externalizable``接口。
<!-- more -->
>比如内存里面有Person这样一个对象，这个对象已经new出来了，接下来我把这个对象保存到文件里面，因为内存里面的东西一旦java虚拟机关闭了就都没有了，所以保存到文件里面，保存到文件之后，等到下一次java虚拟机再次起来之后，我再把这个Person对象从文件里面读取回来，再加载到内存中，这就是序列化。**必须注意地是，对象序列化保存的是对象的"状态"，即它的成员变量。由此可知，对象序列化不会关注类中的静态变量。**

好了，如果您是初次接触序列化，建议您先看一下[这个入门的小例子](http://www.w3cschool.cc/java/java-serialization.html)~

Serializable是一个``标记接口``(maker interface)，关于标记接口可以参考[这篇文章](http://blog.xumingyang.cn/2015/03/23/RandomAccess%E8%AF%A6%E8%A7%A3/)。若某个类实现了Serializable接口，编译器就知道这个类是可以序列化的；否则，序列化一个没有实现该接口的对象，会抛出NotSerializableException异常。

这里需要厘清``序列化``和``持久化``的概念，下面是一些关于序列化/持久化的说法：

[徐辰](http://www.zhihu.com/people/xu-chen-5)
>对象持久化就是让对象的生存期超越使用对象的程序的运行期，简单说就是save&load

[孙立伟](http://www.zhihu.com/people/lsun)
>- 对象持久化就是将对象存储在可持久保存的存储介质上，例如主流的关系数据库中。在实际应用中，需要将业务数据以对象的方式保存在数据库中，就需要应用到相应的对象持久化框架，如现在广为认知的Hibernate。而如果查阅对象持久化的历史，你会发现早在1970年就已经开始有称之为面向对象数据库OODBMS。通常这些面向对象的数据库和特定的一种语言绑定。对象持久化的重点在于如何将业务数据对象存储在持久化介质上，并同时提供查询修改的手段。
>- 数据序列化就是将对象或者数据结构转化成特定的格式，使其可在网络中传输，或者可存储在内存或者文件中。反序列化则是相反的操作，将对象从序列化数据中还原出来。而对象序列化后的数据格式可以是二进制，可以是XML，也可以是JSON等任何格式。对象/数据序列化的重点在于数据的交换和传输，例如在远程调用技术(如EJB,XML-RPC, Web Service)，或者在GUI控件开发(JavaBean)等等。
>
>总结一下：对象持久化和对象序列化是两个完全不同的应用场景，尽管你也可以说将一个对象序列化后存储在数据库中，但是你也不能说是对象持久化。

[梁川](http://www.zhihu.com/people/liang-chuan)
>一个轻量级，一个重量级。
>
- 持久化往往依赖于数据库，是为了长期存储的。
- 序列化是为了散集和列集做短期存储和数据传递的。

我觉得最开始的知识点里面描述的很到位：

- 序列化就是把对象转化成字节流，以备后面再使用反序列化还原对象。
- 持久化则是把对象保存起来。

## 序列化的实现
### 使用Java内置的序列化方式：

- 如果需要让某个对象可以支持序列化机制，必须让它的类是可序列化（serializable），为了让某个类可序列化的，必须实现如下两个接口之一： Serializable，Externalizable。
- 所有在网络上传输的对象都应该是可序列化的，否则将会出现异常；所有需要保存到磁盘里的对象的类都必须可序列化。（通常建议：程序创建的每个JavaBean类都实现Serializable)

### 其他序列化方式
有时候Java内置的序列化方式并不能满足我们的需求，因此又有了其他的序列化方式，具体请看``序列化协议&框架``部分。


## 影响序列化的因素
### transient关键字

当某个字段被声明为transient后，默认序列化机制就会忽略该字段。

### writeObject()方法与readObject()方法

如果实现了Serializable接口的对象重写了writeObject方法，则该对象的序列化操作就交给了该函数。一般还要相应的实现readObject方法。

必须注意地是，writeObject()与readObject()都是private方法，那么它们是如何被调用的呢？毫无疑问，是使用反射。详情可见ObjectOutputStream中的writeSerialData方法，以及ObjectInputStream中的readSerialData方法。

>一般在集合类中，都会把存储数据的数组（或Node节点）设置为transient，然后重写writeObject和readObject方法。这是因为那些数组（或Node节点）里面存储的可能并不是原生类型，而是堆上对象的引用地址，那么我们如果直接把这些地址进行序列化操作是毫无意义的，所以需要重写writeObject和readObject方法。

### Externalizable接口

无论是使用transient关键字，还是使用writeObject()和readObject()方法，其实都是基于Serializable接口的序列化。JDK中提供了另一个序列化接口--Externalizable，使用该接口之后，之前基于Serializable接口的序列化机制就将失效。

Externalizable继承于Serializable，当使用该接口时，序列化的细节需要由程序员去完成。如果writeExternal()与readExternal()方法未作任何处理，那么该序列化行为将不会保存/读取任何一个字段，输出结果中所有字段的值均为空。

另外，若使用Externalizable进行序列化，当读取对象时，会调用被序列化类的无参构造器去创建一个新的对象，然后再将被保存对象的字段的值分别填充到新对象中。实现Externalizable接口的类必须要提供一个无参的构造器，且它的访问权限为public。

### readResolve()方法
当我们使用Singleton模式时，应该是期望某个类的实例应该是唯一的，但如果该类是可序列化的，那么情况可能会略有不同，因为我们不希望在反序列化时生成多个对象。

无论是实现Serializable接口，或是Externalizable接口，当从I/O流中读取对象时，readResolve()方法都会被调用到。实际上就是用readResolve()中返回的对象直接替换在反序列化过程中创建的对象，而被创建的对象则会被垃圾回收掉。

### serialVersionUID
**SerialVersionUid**，简言之，其目的是序列化对象版本控制，有关各版本反序列化时是否兼容。如果在新版本中这个值修改了，新版本就不兼容旧版本，反序列化时会抛出**InvalidClassException**异常。如果修改较小，比如仅仅是增加了一个属性，我们希望向下兼容，老版本的数据都能保留，那就不用修改；如果我们删除了一个属性，或者更改了类的继承关系，必然不兼容旧数据，这时就应该手动更新版本号，即**SerialVersionUid**。

更加详细的关于serialVersionUID的讲解可以在[这篇文章](http://blog.csdn.net/fbysss/article/details/5844478)里面见到。

## 序列化协议&框架
### 序列化协议
- XML
- JSON
- Protobuf
- Thrift
- Avro

更多的关于序列化协议的介绍可以看[序列化和反序列化](http://tech.meituan.com/serialization_vs_deserialization.html)
### 序列化框架:
- Kryo
- Hessian
- Protostuff
- Protostuff-Runtime
- Java

更多的关于序列化框架的介绍可以看[序列化框架 kryo VS hessian VS Protostuff VS java](http://x-rip.iteye.com/blog/1555293)。

## 序列化中需要注意的地方

- 当一个对象被序列化时，只保存对象的非静态成员变量，不能保存任何的成员方法和静态的成员变量。 
- 如果一个对象的成员变量是一个对象，那么这个对象的数据成员也会被保存。
- 如果一个可序列化的对象包含对某个不可序列化的对象的引用，那么整个序列化操作将会失败，我们可以将这个对象引用标记为transient，那么对象仍然可以序列化。
- 如果我们向文件中使用序列化机制写入了多个Java对象，使用反序列化机制恢复对象必须按照实际写入的顺序读取。 
- 当一个可序列化类有多个父类时（包括直接父类和间接父类），这些父类要么有无参的构造器，要么也是可序列化的—否则反序列化将抛出InvalidClassException异常。如果父类是不可序列化的，只是带有无参数的构造器，则该父类定义的Field值不会被序列化到二进制流中。
- 序列化和反序列化时所用的类必须一致。比较两个类是否“相等”，只有在这两个类是由``同一个类加载器``加载的前提下才有意义，否则，即使这两类是来源于同一个Class文件，只要加载他们的类加载器不用，那这两个类就必定不相等。
>这里所指的"相等"，包括代表类的 Class 对象的 equals()方法、 isAssignableFrom()方法、 islnstance()方法的返回结果，也包括了使用 instanceof 关键字做对象所属关系判定等情况 。

## 参考资料
- [Java序列化(这里提供了一个最基础的示例)](http://www.w3cschool.cc/java/java-serialization.html)
- [什么是对象持久化，与数据序列化有何联系？(来自知乎)](http://www.zhihu.com/question/20706270)
- ["java对象序列化与对象反序列化"深入详解](http://fangguanhong.iteye.com/blog/1976911)
- [理解Java对象序列化](http://www.blogjava.net/jiangshachina/archive/2012/02/13/369898.html)
- [Java序列化的机制和原理](http://developer.51cto.com/art/200908/147650.htm)
- [深入理解Java序列化中的SerialVersionUid](http://blog.csdn.net/fbysss/article/details/5844478)


## 扩展阅读
- [序列化和反序列化(来自美团技术博客)](http://tech.meituan.com/serialization_vs_deserialization.html)
- [Java序列化的高级认识](http://www.ibm.com/developerworks/cn/java/j-lo-serial/)
- [如何生成一个SerialVersionUID](http://tangyongjunbk.blog.163.com/blog/static/128389013201082481625255/)
- [对象序列化为何要定义serialVersionUID的来龙去脉](http://lenjey.iteye.com/blog/513736)



