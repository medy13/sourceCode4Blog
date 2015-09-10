title: docker 入门操作
date: 2014-10-15 20:38
tags: docker
categories: Docker
toc: true
---
>[Docker](https://www.docker.com/) is a platform for developers and sysadmins to **develop, ship, and run applications**. Docker lets you quickly assemble applications from components and eliminates the friction that can come when shipping code. Docker lets you get your code tested and deployed into production as fast as possible.

检查docker是否安装成功：
	
	root@do:~# docker version
	//如下显示则表明docker安装成功。

	Client version: 1.0.1
	Client API version: 1.12
	Go version (client): go1.2.1
	Git commit (client): 990021a
	Server version: 1.0.1
	Server API version: 1.12
	Go version (server): go1.2.1
	Git commit (server): 990021a

<!-- more -->

### 使用Docker输出hello world
	docker run ubuntu:14.04 /bin/echo 'Hello world'

`docker run`：运行容器

`ubuntu:14.04`：指定image。
>This is the source of the container we ran. Docker calls this an image. 

这里我们运行的是ubuntu 14.04 操作系统 image。

`/bin/echo 'Hello world'`：当我们的容器启动之后，Docker创建了一个新的Ubuntu14.04环境，然后在这个ubuntu系统中执行`/bin/echo 'Hello world'`。

### 创建一个可交互的容器
	root@do:~# docker run -t -i ubuntu:14.04 /bin/bash
	root@dc270626bc6e:/# 

这里多了两个参数`t`和`i`。

>-t：assigns a pseudo-tty or terminal inside our new container

>-i：allows us to make an interactive connection by grabbing the standard in (STDIN) of the container

这个时候，我们可以在新创建的ubuntu终端里面进行bash操作。


	root@do:~# docker run -t -i ubuntu:14.04 /bin/bash
	root@dc270626bc6e:/# pwd
	/
	root@dc270626bc6e:/# ls
	bin  boot  dev	etc  home  lib	lib64  media  mnt  opt	proc  root  run  sbin  srv  sys  tmp  usr  var
	root@dc270626bc6e:/# 

最后输入`exit`或者按Ctrl+d来关闭这个容器。

### 创建一个在后台运行的容器
	root@do:~# docker run -d ubuntu:14.04 /bin/sh -c "while true; do echo hello world; sleep 1; done"
	0c981b692eb2f2b3aee085cb873231e9f77ddf9516352bbf080176d8c8567df7

>The -d flag tells Docker to run the container and put it in the background, to daemonize it.表示在后台运行该容器。

提交刚才的命令之后，终端会打印
	0c981b692eb2f2b3aee085cb873231e9f77ddf9516352bbf080176d8c8567df7

这个是创建的容器的ID，用于唯一标志容器。
>The container ID is a bit long and unwieldy and a bit later on we'll see a shorter ID and some ways to name our containers to make working with them easier.

该容器中执行`/bin/sh -c "while true; do echo hello world; sleep 1; done"`。这个时候在前台看不见任何输出。

执行`docker ps`查看后台运行的容器。

	root@do:~# docker ps
	CONTAINER ID        IMAGE               COMMAND                CREATED             STATUS              PORTS               NAMES
	0c981b692eb2        ubuntu:14.04        /bin/sh -c 'while tr   4 seconds ago       Up 4 seconds                            backstabbing_brown

最后一列Names是docker自动给容器分配的名称，自己也可以对其进行修改。

执行`docker logs [name]`查看后台容器的输出。

	root@do:~# docker logs backstabbing_brown
	hello world
	hello world
	hello world
	```

执行`docker stop [name]`，关闭该后台容器。

	root@do:~# docker stop backstabbing_brown
	backstabbing_brown

### 本篇命令总结

	docker run ubuntu:14.04 /bin/echo 'Hello world'

	docker run -t -i ubuntu:14.04 /bin/bash (-t带终端，-i可交互)

	docker run -d ubuntu:14.04 /bin/sh -c "while true; do echo hello world; sleep 1; done"
	(返回 container ID，用于唯一标示container。)

	docker ps
	查看docker的后台进程

	docker logs [NAMES]
	这里的[NAMES]是使用 docker ps 查看后台进程的时候，最后一列信息。
	docker logs 会返回该容器的标准输出

	docker stop [NAMES]
	停止正在运行的容器

>参考自docker文档：[https://docs.docker.com/userguide/dockerizing/](https://docs.docker.com/userguide/dockerizing/)
>
>[这里](https://docs.docker.com/reference/commandline/cli/)有更多的关于Docker的命令介绍。
