title: Scala中参数化类型的可变性
categories: Scala
date: 2014-03-16 10:17:16
toc: true
---
首先，举个栗子：
```scala
	var arr1 = new ArrayList[Int](3)
	var arr2:Array[Any] = null
	
	arr2 = arr1 //Compilation ERROR
```
赋值失败的原因在另一篇文章中已经讲述。
<!-- more -->
将子类实例的容器赋给基类容器的能力成为``协变``。将超类实例的容器赋给子类容器的能力称为``逆变``。**默认情况下，Scala对二者均不支持。**
	
但是，在某些实际情况下，我们希望能够慎重的把派生类的容器当做他的基类型的容器。

再举个栗子：
```scala
	class Pet (val name: String){
		override def toString() = name
	}
	class Dog(override val name: String) extends Pet(name)
	def workWithDogs(pets:Array[Pet]){}
	val dogs = Array(new Dog("Rover"), new Dog("Comet"))
```
如果把最后定义的dogs传递给workWithDogs，则会报出编译错误。Scala不允许这么做，但是，对这个方法而言，这么做是可行的。嗯，下面这个栗子描述了怎么让Scala放行。
```scala
	def playWithPets[T <: Pet](pets: Array[T]) = 
		println("Playing with pets: "+pets.mkString(", "))
```
这里用了特殊的语法定义了playWithPets。T<:Pet表示T所代表的的类派生自Pet。通过使用这种有**上界**的语法，告诉Scala，具有参数化类型T的参数数组必须至少是Pet的数组，也可以是Pet派生类的数组。通过这种方法，就可以进行调用：

>playWithPets(dogs)

输出是：

>Playing with pets: Rover, Comet

-------

我们先设想一下：想把其他类型的数组赋给Pet数组，比如把Dog数组赋给Pet数组，显然，Scala不允许这样做。跟上面类似，只不过这里需要的是一个下界：

举个栗子：
```scala
	def copyPets[S, D >: S](fromPets: Array[S], toPets: Array[D]) = {// ...
	}
	val pets = new Array[Pet](10)
	copyPets(dogs,pets)
```
>将目的数组的参数化类型(D)限制为源数组的参数化类型(S)的超类型。也就是：S(源类型，如Dog)设置了类型D(目的类型，如Dog或Pet)的下界——他可以是类型S或其超类的任意类型。

-----

最后的那两个例子，在方法定义里控制了方法的参数。也可以在容器里进行类似的控制。可以将参数化类型标记为+T，而不是T。

最后一个栗子：
```scala
	class MyList[+T] //...

	var list1 = new MyList[Int]
	var list2 = : MyList[Any] = null
	
	list2 = list1 //ok
```
这里+T告诉Scala允许协变。

>对于Array[Int]，这个是不可以的。不过对于List——Scala程序库中实现的函数式list——这个是可以的。

类似的，参数化类型用-T替换T，就可以让Scala支持类型逆变。

