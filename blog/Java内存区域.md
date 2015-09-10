title: Java内存区域
date: 2014-05-16 10:17:16
categories: Java
toc: true
---
## 一种JVM运行时数据区划分
![](http://xumyselfcn.github.io/imgs/JVM-runtime-data-area.jpg)

（图片来源：[JVM运行时是什么样子](http://www.programcreek.com/2013/04/jvm%E8%BF%90%E8%A1%8C%E6%97%B6%E6%95%B0%E6%8D%AE%E5%8C%BA/)）
从上图可以看出，JVM的数据区可以分成两部分

<!-- more -->

#### 每个线程的数据区
每个线程有program counter register(程序计数器)，JVM Stack(JVM栈),和Native Method Stack(本地方法栈)。　这三个区域都是基于每一个线程的，当一个线程创建的时候生成。

程序计数器:　每一条JVM线程都有自己的PC寄存器，用来控制线程的执行。
　
JVM Stack(JVM栈):　Stack里存放的是Frame(帧)（如下图所示）。　

Native Method Stack(本地方法栈):　用来支持native methods (非Java语言method)。　
####所有的线程共享数据区
所有的线程共享数据区有Heap和Method Area.

Heap(堆)是与我们平时编程最直接打交道的区域。它存放所有的对象和数组。在JVM启动时划分生成。常说的Garbage Collector垃圾回收器就是对这个区域工作的。　

Method Area(方法区)存储类的结构信息，包括 run-time constant pool, field and method data, 和methods and constructors代码。　

Runtime Constant Pool（运行时常量池）存放编译时可知的数值字面量和运行期解析后才能获得的method或field的引用。

![](http://xumyselfcn.github.io/imgs/JVM-Stack.png)

Stack中的包含一些Frame, 这些Frame在method调动的时候生成。每一个Frame包括：local variable array, Operand Stack, Reference to Constant Pool.

## 另一种JVM运行时数据区划分
* 程序计数器
* 虚拟机栈
* 本地方法栈
* 堆
* 方法区

### 程序计数器
小内存空间，是当前线程所执行的字节码的行号指示器。

在虚拟机的概念模型里，字节码解释器工作时就是通过改变这个计数器的值来选取下一条需要执行的字节码。分支、循环、跳转、异常处理、线程恢复等基础功能都需要依赖计数器。

Java虚拟机的多线程是通过线程轮流切换并分配处理器执行时间的方式来实现的。每条线程都需要有一个独立的程序计数器，各条线程之间的计数器互不影响，独立存储。这类内存区域是``线程私有``的。

### Java虚拟机栈
同程序计数器一样，Java虚拟机栈一手线程私有的，其生命周期与线程相同。

每个方法被执行的时候，都会同时创建一个栈帧，用于存储局部变量表、操作栈、动态链接、方法出口等信息。每个方法被调用直至完成的过程，就对应着有一个栈帧在虚拟机中入栈到出栈的过程。

关于局部变量表：存放各种基本数据类型（boolean、byte、char、short、int、float、long、double）、对象引用（从c指针的角度来说就是一个指针，指向存放该对象的内存区域）和returnAddress类型（指向一条字节码指令的地址）。

局部变量表所需的内存空间在编译期间完成分配。运行期间也不会改变。

本区域的异常：

* 如果线程请求的栈深度大于虚拟机所允许的深度，抛出StackOverflowError异常
* 如果虚拟机栈可以动态扩展，扩展时无法申请到足够的内存时会抛出OutOfMemoryError异常。

### 本地方法栈
与虚拟机栈发挥的作用类似，虚拟机栈为虚拟机执行Java方法服务，本地方法栈为虚拟机执行Native方法服务。

异常方面：也会抛出OutOfMemoryError、OutOfMemoryError异常。

### Java堆
所有线程共享，唯一目的是存放对象实例。

* Java堆是垃圾收集器管理的主要区域。
* Java堆可以处于物理上不连续的内存空间，但逻辑上是连续的。
* 堆的大小扩展，如果在堆中没有内存空间进行实例分配，且堆已经扩展到最大时，会抛出OutOfMemoryError异常。

### 方法区
同样是所有线程共享，存放已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据。

当方法区无法满足内存分配需求时，抛出OutOfMemoryError异常。

## 参考资料
- 周志明 《深入理解Java虚拟机》第二章
- [JVM运行时是什么样子](http://www.programcreek.com/2013/04/jvm%E8%BF%90%E8%A1%8C%E6%97%B6%E6%95%B0%E6%8D%AE%E5%8C%BA/)
