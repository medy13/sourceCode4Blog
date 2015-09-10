title: Java 中的重要知识点
date: 2015-02-26 10:28:31
categories: Java
tags: java
---
## Java的容器
顺序表、集合、队列相关类图
<!-- more -->
![](http://xumyselfcn.github.io/imgs/Java_collection_implementation1.jpg)
Map相关类图
![](http://xumyselfcn.github.io/imgs/Java_map_implementation1.jpg)
（图片来源：[Prepare Java Interview](http://bighai.com/ppjava/?p=158)）
上面第二幅图有错，LinkHashMap=>LinkedHashMap

### 非并发容器

- [ArrayList](http://blog.xumingyang.cn/2015/03/27/ArrayList%E8%AF%A6%E8%A7%A3/)和[LinkedList](http://blog.xumingyang.cn/2015/03/27/LinkedList%E8%AF%A6%E8%A7%A3/)（各自特点、区别、联系）
- [Map（Hash冲突的解决）](http://www.importnew.com/10620.html)
- Set
- Stack、Queue

[这位](http://www.cnblogs.com/skywang12345/p/3323085.html#3157660)总结的很不错，推荐给大家。

### 并发容器

- 阻塞类的代表[LinkedBlockingDeque](http://blog.xumingyang.cn/2015/03/12/java%20%E5%B9%B6%E5%8F%91%E6%95%B0%E6%8D%AE%E9%9B%86%E5%90%88/#ConcurrentLinkedDeque)、Vector
- 非阻塞类的代表[ConcurrentLinkedDeque](http://blog.xumingyang.cn/2015/03/12/java%20%E5%B9%B6%E5%8F%91%E6%95%B0%E6%8D%AE%E9%9B%86%E5%90%88/#LinkedBlockingDeque)<!--more-->
- 高效的[ConcurrentHashMap](http://www.ibm.com/developerworks/cn/java/java-lo-concurrenthashmap/index.html?ca=drs-)

## JVM相关
[JVM](http://en.wikipedia.org/wiki/Java_virtual_machine)，也就是Java 虚拟机，由各大厂商根据[JVM规范](https://docs.oracle.com/javase/specs/jvms/se7/html/)实现的。最有名的是甲骨文公司的HotSpot。JVM主要由：**类加载子系统、运行时数据区（内存空间）、执行引擎和本地方法接口**等组成。其中运行时数据区又由：**方法区、堆、Java栈、PC寄存器、本地方法栈**组成。

![JVM主要组成部分](http://xumyselfcn.github.io/imgs/jvm2.jpg)

这个系列是[飘过的小牛](http://github.thinkingbar.com/)关于周志明的[《深入理解Java虚拟机》](https://book.douban.com/subject/24722612/)的读书笔记，我自己也看了一遍这本书（我看的是第一版的，现在已经有第二版了），再看看别人是怎么想的，每个人的关注点可能也不一样，有可能他注意到了我忽略的地方，顺带着复习一下，这是一种很好的学习方式。

- [Java内存区域与内存溢出异常](http://github.thinkingbar.com/jvm-ii/)
- [JVM性能监控与故障处理工具](http://github.thinkingbar.com/jvm-iv/)
- [类文件结构](http://github.thinkingbar.com/jvm-vi/)
- [虚拟机类加载机制](http://github.thinkingbar.com/jvm-vii/)
- [深入分析Java ClassLoader原理](http://www.importnew.com/15362.html)
- [虚拟机字节码执行引擎](http://github.thinkingbar.com/jvm-viii/)
- [类加载及执行子系统的案例与实战](http://github.thinkingbar.com/jvm-ix/)
- [Java内存模型与线程](http://github.thinkingbar.com/jvm-xii/)

## Java I/O

待补充

- [Java IO "装饰模式(Decorator)”总结](http://fangguanhong.iteye.com/blog/1976393)

## Java多线程
以前用到多线程的时候，在网上搜一下资料，实现功能就行；后来看了[《Java 7并发编程实战手册》](http://book.douban.com/subject/25844475/)算是真正的入门，这本书的特点是例子很多，跟着例子走上手比较容易，缺点就是例子太多，容易只见树木不见森林，局限于某个知识点。

这里是[《Java 7 Concurrency Cookbook》](http://ifeve.com/java-7-concurrency-cookbook/)的中文翻译在线版，已经出版的《Java 7并发编程实战手册》是其中文译本。

其他的Java并发编程资料：

- 排名第一的自然是[《Java并发编程实战》](http://book.douban.com/subject/10484692/)
- [Java并发性和多线程](http://ifeve.com/java-concurrency-thread-directory/)


怎么才能构建一个Java并发编程的体系呢，下面的这个系统对Java多线程整体过了一遍，这个系列依旧是[飘过的小牛](http://github.thinkingbar.com/)的读书笔记。


- [Java编程思想-并发系列之一](http://github.thinkingbar.com/thinking_in_java_chapter21-part01/)
- [Java编程思想-并发系列之二](http://github.thinkingbar.com/thinking_in_java_chapter21-part02/)
- [Java编程思想-并发系列之三](http://github.thinkingbar.com/thinking_in_java_chapter21-part03/)
- [Java编程思想-并发系列之四](http://github.thinkingbar.com/thinking_in_java_chapter21-part04/)
- [Java编程思想-并发系列之五](http://github.thinkingbar.com/thinking_in_java_chapter21-part05/)
- [Java编程思想-并发系列之六](http://github.thinkingbar.com/thinking_in_java_chapter21-part06/)


## Java其他关键知识点
- [深入理解Java String#intern() 内存模型](http://www.importnew.com/15397.html)

### Java序列化

下面就是序列化相关的知识点：

- 为什么要序列化、序列化的应用场景？
- 序列化的实现方法？
- 已有的序列化框架？
