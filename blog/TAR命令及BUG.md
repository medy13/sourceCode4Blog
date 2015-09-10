title: TAR命令及BUG
date: 2015-05-06 8:55:31
categories: linux
tags: 
- linux
- tar
description: 本文主要介绍tar的用法，以及高版本tar命令的一个bug。该bug的报错信息如下tar file changed as we read it
---
## TAR命令
tar是linux下最常用的**打包**程序，注意啊，是打包，不是压缩，tar本身不具有压缩功能，他是调用压缩功能实现的~如下是个小例子
<!-- more -->
	#解包
	tar xvf FileName.tar
	#打包
	tar cvf FileName.tar DirName

如下是涉及到压缩，解压的例子
	
	#解压
	tar zxvf FileName.tar.gz
	#压缩
	tar zcvf FileName.tar.gz DirName

	#解压
	tar jxvf FileName.tar.bz2
	#压缩
	tar jcvf FileName.tar.bz2 DirName

只将压缩文件中部分文件解压出来：

	[root@localhost test]# tar -zcvf log30.tar.gz log2012.log log2013.log 
	log2012.log
	log2013.log
	[root@localhost test]# ls -al log30.tar.gz 
	-rw-r--r-- 1 root root 1512 11-30 08:19 log30.tar.gz
	[root@localhost test]# tar -zxvf log30.tar.gz log2013.log
	log2013.log
	[root@localhost test]# ll
	-rw-r--r-- 1 root root   1512 11-30 08:19 log30.tar.gz
	[root@localhost test]# cd test3
	[root@localhost test3]# tar -zxvf /opt/soft/test/log30.tar.gz log2013.log
	log2013.log
	[root@localhost test3]# ll
	总计 4
	-rw-r--r-- 1 root root 61 11-13 06:03 log2013.log
	[root@localhost test3]#

## 我遇到的一个tar的bug
背景：同一个工程，执行程序通过shell脚本进行调用。这个工程在两台机器上一个执行成功，一个执行失败，该工程所必须的环境变量都已经正确设置。

根据log日志显示，出错部分是shell脚本的tar命令附近，一开始根本没有往tar命令身上想，老是以为环境变量哪里出了问题，结果折腾了很久也没发现问题所在。然后仔细排查log信息时候发现有如下的报错信息：

	tar: .: file changed as we read it

google了一下，发现有人也遇到了同样的问题。出现问题的场景也一样：压缩当前目录下的所有文件时，出错。然后查看了[stackoverflow上面的一个问题](http://stackoverflow.com/questions/20318852/tar-file-changed-as-we-read-it)，里面说有可能是tar命令的问题，高版本的tar命令存在bug，在压缩当前文件夹时会报错。

随后查看两台机器的tar版本分别是1.14和1.26。然后我在出问题的机器上使用tar 1.14版本就可以正确执行。

## tar命令的参数

必要参数有如下：

	-A 新增压缩文件到已存在的压缩
	-B 设置区块大小
	-c 建立新的压缩文件
	-d 记录文件的差别
	-r 添加文件到已经压缩的文件
	-u 添加改变了和现有的文件到已经存在的压缩文件
	-x 从压缩的文件中提取文件
	-t 显示压缩文件的内容
	-z 支持gzip解压文件
	-j 支持bzip2解压文件
	-Z 支持compress解压文件
	-v 显示操作过程
	-l 文件系统边界设置
	-k 保留原有文件不覆盖
	-m 保留文件不被覆盖
	-W 确认压缩文件的正确性

可选参数如下：

	-b 设置区块数目
	-C 切换到指定目录
	-f 指定压缩文件
	--help 显示帮助信息
	--version 显示版本信息

## 参考资料
- [每天一个linux命令（28）：tar命令](http://www.cnblogs.com/peida/archive/2012/11/30/2795656.html)