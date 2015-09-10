title: Scala的三个特殊类型Nothing、Any、Option
categories: Scala
date: 2014-03-19 10:17:16
toc: true
---
## Scala的三个特殊类型Nothing、Any、Option
```scala
	import java.util._

	var list1 : List[Int] = new ArrayList[Int]
	var list2 = new ArrayList[Int]
	var list3 = new ArrayList[Any]	
	
	var ref1 : Int = 1
	var ref2 : Any = null

	list2 add 1
	list2 add 2
	
	var total = 0

	for(val index <- 0 until list2.size())
	{
		total += list2.get(index)
	}

	println("total is: "+total)
```
<!-- more -->
解释一下上面的代码：

* import里面的下划线，等价于java里的 ( * ),该语句导入了java.util包中的所有的类。

* 创建list1引用，只想ArrayList[Int]的实例。然后创建另一个引用list2，指向为指导参数类型的ArrayList实例。``在幕后，Scala实际上穿件了一个ArrayList[Nothing]``的实例。如果将list1赋给list2，则会编译报错。
* 默认情况下，Scala是不允许把一个持有任意类型实例的容器赋值给持有Any实例的容器。也就是若将list2赋值给list3，会发生编译错误。
* ref1赋值给ref2可以通过编译，这个等价于将Integer引用赋给一个Object类型的引用。



### Nothing类型
在Scala中Nothing是所有类的子类。Scala把new ArrayList 当做ArrayList[Nothing]这样的实例。基类实例是不能当做派生类实例，Nothing是最底层的子类。

Scala使用Nothing——所有类型的子类——帮助类型推演更平滑的进行。既然他是任何类的子类，他就可以替换任何东西。Nothing是抽象的，在运行时，他的实例并不会真实存在，纯粹就是类型推演的帮手。

### Any类型
Any是所有Scala类型的超类。Any可以持有任何类型对象的引用。Any是抽象类，有如下的方法： !=() 、 ==() 、 asInstanceOf() 、 equals() 、 hashCode() 、 isInstanceOf() 和 toString()。

Any的直接后代是**AnyVal**和**AnyRef**。

* AnyVal是所有可以映射为Java基本类型（比如Int，Double等）的Scala类型的基类。
* AnyRef是所有引用类型的基类。

**AnyRef**直接映射为Java的Object，在Scala中使用它，就如同在Java里使用Object一样。

**Any**或**AnyVal**在编译成字节码时，Scala也会在内部把他们当做Object的引用处理。Object的某些方法也不能通过Any或AnyVal的引用来调用。

### Option类型
```scala
	def commentOnPractice(input: String) = {
		//rather than returning null
		if(input == "test") Some ("good") else None
	}
	
	for(input <- Set("test","hack")){
		val comment = commentOnPractice(input)
		println("input " + input +" comment "+comment.getOrElse("Found no comments"))

	}
```
上面的代码输出如下：
>input test comment good
>
>input hack comment Found no comments

* 这里，commentOnPractice()也许会返回一个注释（String），也许压根没有注释，这两点分别用Some[T]和None的实例表示。这两个类都集成Option[T]。
* 调用返回Option[T]的getOrElse()方法，可以主动的应对结果不存在(None)的情形。

