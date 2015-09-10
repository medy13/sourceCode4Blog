title: Linux环境变量
date: 2015-04-15 18:55:31
categories: linux
tags: 
- linux
- shell
description: 本文主要介绍linux中环境变量的作用，设置环境变量的一些方法，以及这些方法的异同，最后讲述Path这个特殊的环境变量的设置。

---

## 设置环境变量
总体来说有三种：

- 直接在shell下设置变量
- 修改.bashrc文件
- 修改/etc/profile文件

### 直接在shell里面进行修改
<!-- more -->
这种方法仅仅是临时使用，以后要使用的时候又要重新设置，比较麻烦。
 
只需在shell终端执行下列命令：
```
	export JAVA_HOME=/usr/share/jdk1.5.0_05
	export PATH=$JAVA_HOME/bin:$PATH
	export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
```
### 修改bashrc文件
这种方法更为安全，它可以把使用这些环境变量的权限控制到用户级别，如果你需要给某个用户权限使用这些环境变量，你只需要修改其个人用户主目录下的.bashrc文件就可以了。

直接用文本编辑器打开用户目录下的.bashrc文件，在文件末尾加入需要用的环境变量即可，下面是一个示例：
```
	set JAVA_HOME=/usr/share/jdk1.5.0_05
	export JAVA_HOME
	set PATH=$JAVA_HOME/bin:$PATH
	export PATH
	set CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
	export CLASSPATH
```
然后执行一下
```
	source ~/.bashrc
```
就可以生效了，也可以退出，重新登录一下。

### 修改/etc/profile文件
如果你的计算机仅仅作为开发使用时推荐使用这种方法，因为所有用户的shell都有权使用这些环境变量，可能会给系统带来安全性问题。相比较来说还是上一种控制在用户级别的设置方法比较安全。

举个例子，用文本编辑器打开/etc/profile文件，在文件末尾加入
```
	JAVA_HOME=/usr/share/jdk1.5.0_05
	PATH=$JAVA_HOME/bin:$PATH
	CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
	export JAVA_HOME
	export PATH
	export CLASSPATH
```
然后重新登录一下，就生效了。

## Path环境变量的设置
### 临时添加路径到当前的PATH
```
	//这里假设新增的路径是/etc/apache/bin
	PATH=$PATH:/etc/apache/bin
```
这种方法只能临时改变PATH内容，如果想永久的添加进PATH，可以使用下面这个方法。

### 添加全局变量在/etc/profile文件中
在/etc/profile中添加一句代码：
```
	//这里假设新增的路径是/etc/apache/bin
	export PATH="$PATH:/etc/apache/bin"
```
### 为特定用户修改PATH
下面两种方法是针对用户起作用的。
####修改bash_profile
进入~/.bash_profile文件，修改PATH行,把/etc/apache/bin添加进去：
```
	# .bash_profile
	# Get the aliases and functions
	if [ -f ~/.bashrc ]; then
		. ~/.bashrc
	fi
	
	# User specific environment and startup programs
	export HADOOP_HOME=/home/tseg/hadoop-0.20.2
	
	export PATH=$PATH:$HOME/bin:$HADOOP_HOME/bin:/etc/apache/bin
```
这种方法针对所有的用户都起作用~可能会带来安全性问题。

#### 修改bashrc

进入~/.bash_profile文件，添加两行：
```
	# .bashrc

	# Source global definitions
	if [ -f /etc/bashrc ]; then
		. /etc/bashrc
	fi

	PATH=$PATH:/etc/apache/bin
	export PATH

	# User specific aliases and functions
```
这个只对当前用户起作用，如果是在root权限操作，则root用户有效。

## 参考资料

- [linux下添加PATH的方法](http://kekuk.blog.51cto.com/326101/743352)
- [Linux操作系统下三种配置环境变量的方法](http://www.linuxeden.com/html/sysadmin/20080424/56879.html)