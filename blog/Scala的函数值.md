title: Scala的函数值
date: 2014-03-12 10:17:16
categories: Scala
toc: true
---
## 高阶函数
高阶函数：以其他函数为参数的函数。

情景引入：

* 1-n求和
* 1-n的偶数求和
* 1-n的奇数求和

如果用Java应该就是写三个差不多的函数，修改下需要求和部分的代码。产生重复代码，降低可重用性。
<!-- more -->
## 函数值

在Scala中，可以在函数里创建函数，将函数赋给引用，或者把它们当做参数传给其它函数。首先：提取公共代码：

	def countAll(number: Int, codeBlock: Int => Int) = {
		var result = 0
		for(i <- 1 to number){
			result += codeBlock(i)
		}
		result
	}

countAll定义了两个参数：第一个是int型的number，也就是求和的上限。另一个是**函数值**，参数名称是codeBlock，类型是一个函数，接受一个int，返回一个int。

	println("求和： "+countAll(5,i=>i ))
	println("计算偶数和： "+countAll(5,i=>if(i%2 == 0) i else 0 ))
	println("计算奇数和： "+countAll(5,i=>if(i%2 == 1) i else 0 ))

第一个print传了两个参数：第一个（5）是循环范围的上限，第二个（i=>i）实际上是一个匿名函数（Just In Time），也就是一个没有名字只有实现的函数。这个函数实现知识简单的把给定的参数返回。=>将左边的参数列表同右边的实现分开。Scala能够从countAll参数列表中推断出参数（i）的类型是Int，如果参数类型或者结果类型与预期不符，Scala就会报错。

第二个和第三个也是类似，只是加了判断语句。
