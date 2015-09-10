title: 从Oracle数据库中大量导出文本数据的实践
date: 2014-12-08 15:32:25
categories: Oracle
tags: 
- oracle
- spool
- expdp
- sqluldr2

---

## 背景
在网页上选择待导出的数据表和字段，从Oracle数据库中导出，以文本方式保存。比较大的一张表估计得有几千万行了。目标导出速度得达到15w行/s左右。

## 使用JDBC
通过JDBC连接数据库的方式导出数据，最开始的想法是这样：

1. 读取该表的记录数M
2. 设置每次读取的记录数N，计算出读取的次数X=M/N+1
3. 循环X次，每次从数据库中读取N（用的是项目中现有的分页接口）
4. 把结果写到文件中

<!-- more -->

大概下图的样子：

![](http://img-niren.qiniudn.com/1-1.png)

测试了下，结果很不好。

于是有了下面的想法：

1. 在上面的基础上开三个子线程，连接数据库，读取数据
2. 将每次读取的数据存放在一个队列中
3. 再开一个写文件的子线程，检查队列是否为空，否则把队列中的数据写入到文件中

大概是下图的这个样子。

![](http://img-niren.qiniudn.com/1-2.png)

测试了下，结果依旧不理想。仔细分析了下各个过程的耗时，发现瓶颈均是在从数据库读取数据这个地方。最开始读取一次大概1s的样子，到最后甚至得需要10s中才能返回一条记录，而且多线程并不能起到加快读取数据的目的。

好吧，瓶颈在数据库读取部分。其实也就表明，这种方法不行啊~

## 使用exp/expdp
这里使用的是oracle提供的expdp工具，使用这个工具的大概步骤如下：

	//这是在SQL*PLUS中进行操作
	//创建逻辑目录
	create directory dpdata3 as '/home/oracle/expdp';
	//查看管理员目录下面是否创建成功
	select * from dba_directories;
	//给普通的数据库用户赋予在指定目录的操作权限
	grant read,write on directory dpdata3 to User;

在shell命令行中执行：

	expdp User/Pwd TABLES=tableName dumpfile=expdp.dmp DIRECTORY=dpdata3;

这样在```/home/oracle/expdp```目录中就生成了expdp.dmp文件，这个文件就是从oracle数据库中导出的二进制数据文件。

但是这个方案有个问题，导出的二进制数据如何转换成文本数据？

找了一下，没有理想的方案。

关于expdp命令的详细信息可以参考[expdp impdp 数据库导入导出命令详解](http://shitou118.blog.51cto.com/715507/310033)

## 使用spool
使用spool。这里是从某个表中导出数据的spool脚本：

	set echo on;
	set feedback on;
	set trimspool off;
	set linesize 120;
	set pagesize 2000;
	set newpage 1;
	set heading on;
	set term off;
	set termout on;
	set timing off;
	spool /home/oracle/xmy/oracle/spool/data.txt;
	select '"'||id||'"'||','||lasttime||','||taskname||','||algtype||','||to_number(to_char(starttime,'YYYYMMDD')) from common;
	spool off;

这里就是从common表中导出id、lasttime等字段。具体的执行方法是登陆sqlplus，然后执行

	#登陆
	sqlplus / as sysdba
	#@后面跟的是脚本的路径
	@spool.sh

这种方法效率也是比较低。

## 使用Pro*C

仿照网上的Pro*C程序一直编译出错，据说这个的效率非常高。

## 使用sqluldr2工具

这个方案也是最后采用的。

sqluldr2工具是由[平民架构](http://weibo.com/dbatools)开发的，有免费版和商业版之分，不过我这里免费版就已经够用了。这个工具可以在[anysql](http://www.anysql.net/download)下载到（第5个，Oracle文本导出工具），里面有windows/linux的32/64位四个版本的执行文件。

我是把这个工具放在了/usr/local/bin中，因为这个路径已经在环境变量中设置了，所以现在可以全局使用这个命令。

直接使用命令：

	sqluldr2_linux64_10204.bin user=user/pwd query="select * from common" file=data.csv head=no charset=UTF8

就可以导出common表中的数据了，在当前路径中生成文件data.csv。

这里有关于expdp和sqluldr2的性能测试截图：

这个是expdp工具的测试截图
![](http://img-niren.qiniudn.com/expdp-modify.png)
这个是sqluldr2工具的测试截图
![](http://img-niren.qiniudn.com/sqluldr2-modify.png)

两者导出的数据都是一样的，一共26405520行记录，大约1.4GG。两者耗时相差不大，都在两分钟以内，不过，可以发现sqluldr2甚至比oracle官方提供的expdp工具还要快一些。

在实际项目中遇到了一个问题：这个工具（不止这个工具，除了第一种方案，其他的都需要）必须得在有oracle环境下才能运行，项目部署的用户和数据库的用户并不是同一个。

一个方法就是在当前用户下，切换到另外一个用户去执行命令，但是这就需要输入数据库用户的用户名和密码，查资料可以通过安装expect工具，写成shell脚本，然后程序直接调用执行这个脚本。

另一个方法是在数据库用户下部署一个server，当需要导出数据的时候，给该server发送相应的信息（包括需要导出的表名，字段名）。server接受信息之后开始执行数据导出工作。

现在实现了第二种方法，其实这两个方法都挺麻烦的，不知道有没有更简便的方法。

## 参考资料
1. [expdp impdp 数据库导入导出命令详解](http://shitou118.blog.51cto.com/715507/310033)
2. [SQLPLUS SPOOL命令使用详解](http://blog.csdn.net/cmingjun/article/details/5343019)
3. [How to pump data to txt file using Oracle datapump?](http://stackoverflow.com/questions/11555847/how-to-pump-data-to-txt-file-using-oracle-datapump)
4. [关于Pro*C的一篇国外的文章](https://asktom.oracle.com/pls/apex/f?p=100:11:0::::P11_QUESTION_ID:459020243348)
5. [Shell脚本学习之expect命令](http://blog.csdn.net/leexide/article/details/17485451)
