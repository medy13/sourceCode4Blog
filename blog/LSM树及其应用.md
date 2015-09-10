title: LSM树及其应用
date: 2015-04-15 18:55:31
categories: 大数据
tags: 
- LSM
- 大数据
description: 本文主要讲述了LSM树，及基于LSM树实现的LevelDB。
---
## 概述
LSM树的全称是Log Structured Merge Tree。从英文名中可以猜出来这个必然跟Log和Merge相关。具体来说：LSM树是将大量的随机写操作转换成批量的序列写，这样可以极大的提升磁盘数据的写入速度，不过会牺牲读取的效率。
<!-- more -->
>有种说法，基于LSM树实现的HBase的写性能比Mysql高了一个数量级，读性能低了一个数量级。

### 所谓LOG
对数据的修改增量保持在内存中，达到指定的大小限制后，将这些修改操作批量写入磁盘。因为这部分是在内存中操作，所以在操作前会将操作写入log文件中进行持久化，然后再执行相应的操作。
### 所谓MERGE
上面说到，内存中的数据达到指定的大小限制，会将其写入磁盘，就这样每次写入一部分文件进入磁盘，在读取的时候就需要执行``merge``操作，这里，在后面的介绍LevelDB中有详细的描述。

## 详解，以LevelDB为例
LSM树在大数据存储系统中获得了极为广泛的应用，比如BigTable中的单机数据存储引擎本质上就是LSM树，基于Flash的海量存储系统SILT也采用了LSM树，内存数据库RAMCloud同样采用了这个数据结构。除此之外，LevelDB也使用了LSM树，这里以LevelDB的LSM树结构来大致介绍其 一般实现原理，其他系统使用LSM树的方式与此类似。

[LevelDB](http://leveldb.org/)是由谷歌公司研发的k/V数据库，LevelDB的静态结构如下图（[图片源自这里](http://www.cnblogs.com/haippy/archive/2011/12/04/2276064.html)）：

![LevelDB静态图](http://blog.xumingyang.me/imgs/leveldb-static.png)

构成LevelDB静态结构包括6个主要部分：

- （内存）MemTable
- （内存）Immutable MemTable
- （磁盘）Current文件
- （磁盘）manifest文件
- （磁盘）log文件
- （磁盘）SSTable文件

### 插入数据时
当应用写入一条Key：Value记录的时候，LevelDB会先往log文件里写入，成功后将记录插进MemTable文件中，这样基本就完成了写操作，因为一次写操作只涉及一次磁盘顺序写和一次内存写入，而MemTable才用了维护有序记录快速插入查找的``SkipList``数据结构。

>log文件在系统中的作用主要用于系统崩溃恢复而不丢失数据，假如没有log文件，因为写入的记录刚开始是保存在内存中的，此时如果系统崩溃，内存中的数据还没有来得及保存到磁盘，所以会丢失数据。

### MemTable大小达到限定时
当MemTable插入的数据占用内存到了一个界限后，需要将内存的记录导出到外存文件中，LevelDB会生成新的log文件和MemTable，原先的MemTable就成为Immutable MemTable，顾名思义，就是说这个MemTable的内容是不可更改的，只能读不能写入或删除。新来的数据被记录到新的log文件和MemTable，LevelDB后台调度会将Immutable MemTable的数据导出到磁盘，形成一个新的SSTable文件。SSTable就是由内存中的数据不断导出并进行Compaction操作后形成的，而且SSTable的所有文件是一种层级结构，第一层是Level 0 ，第二层是 Level 1 ，依次类推，层级逐渐增高，这也是称之为LevelDB的原因。

小节一下上面的过程：

1. 数据进入内存
2. 记录log，数据插入MemTable
3. MemTable文件达到上限，变身Immutable MemTable，dump到磁盘，形成SSTable
4. 生成新的MemTable，log

### 关于SSTable

通过上面的描述，可以知道Level 0层的SSTable是如何形成的。显然这些SSTable是（主键）有序的。其它Level的SSTable是由上一层Level里面的SSTable文件合并（多个有序文件的合并，这里使用的是多路归并算法）而成，其内部也是（主键）有序的。

有个值得注意的地方，level 0和其他level不一样的是：第0层的不同的SSTable之间主键范围有重复。比如该层的两个文件A和B，A的主键范围是{bar,cat}，B的主键的范围是{blue,same}，那么很有可能两个文件都存在key="blood"的记录。对于其他的level则不会有这种情况。

关于SSTable这部分推荐[这篇文章](https://www.igvita.com/2012/02/06/sstable-and-log-structured-storage-leveldb/)，这里有个国人参考上篇写的[一篇文章](http://www.cnblogs.com/fxjwind/archive/2012/08/14/2638371.html)。

### Manifest文件
manifest里面记录了SSTable各个文件的管理信息，比如属于哪个Level，文件名称，最小key和最大key各自是多少。

![LevelDB静态图](http://blog.xumingyang.me/imgs/leveldb-manifest.png)

### Current文件
记载当前的manifest文件名。因为在LevelDB运行过程中，随着Compaction的进行，SSTable文件会发生变化，会有新的文件产生，老的文件被废弃，manifest也会跟着反映这种变化，此时往往会生成新的manifest文件来记载这种变化，而Current则用来指定哪个manifest文件才是我们关心的那个。

### Compaction操作

- minor Compaction：当内存中的MemTable大小达到一定值时，将内容保存到磁盘中。
- major Compaction：当某个Level下的SSTable文件树木超过一定设置值后，LevelDB会从这个Level的SSTable中选择一个文件(Level>0)，将其和高一层级的Level+1的SSTable文件合并，这就是major Compaction。

#### minor Compaction
当memtable数量到了一定程度会转换为immutable memtable，此时不能往其中写入记录，只能从中读取KV内容。之前介绍过，immutable memtable其实是一个多层级队列SkipList，其中的记录是根据key有序排列的。所以这个minor compaction实现起来也很简单，就是按照immutable memtable中记录由小到大遍历，并依次写入一个level 0 的新建SSTable文件中，写完后建立文件的index 数据，这样就完成了一次minor compaction。从图中也可以看出，对于被删除的记录，在minor compaction过程中并不真正删除这个记录，原因也很简单，这里只知道要删掉key记录，但是这个KV数据在哪里?那需要复杂的查找，所以在minor compaction的时候并不做删除，只是将这个key作为一个记录写入文件中，至于真正的删除操作，在以后更高层级的compaction中会去做。

#### major Compaction
我们知道在大于0的层级中，每个SSTable文件内的Key都是由小到大有序存储的，而且不同文件之间的key范围（文件内最小key和最大key之间）不会有任何重叠。Level 0的SSTable文件有些特殊，尽管每个文件也是根据Key由小到大排列，但是因为level 0的文件是通过minor compaction直接生成的，所以任意两个level 0下的两个sstable文件可能再key范围上有重叠。所以在做major compaction的时候，对于大于level 0的层级，选择其中一个文件就行，但是对于level 0来说，指定某个文件后，本level中很可能有其他SSTable文件的key范围和这个文件有重叠，这种情况下，要找出所有有重叠的文件和level 1的文件进行合并，即level 0在进行文件选择的时候，可能会有多个文件参与major compaction。

levelDb在选定某个level进行compaction后，还要选择是具体哪个文件要进行compaction，levelDb在这里有个小技巧， 就是说轮流来，比如这次是文件A进行compaction，那么下次就是在key range上紧挨着文件A的文件B进行compaction，这样每个文件都会有机会轮流和高层的level 文件进行合并。

如果选好了level L的文件A和level L+1层的文件进行合并，那么问题又来了，应该选择level L+1哪些文件进行合并？levelDb选择L+1层中和文件A在key range上有重叠的所有文件来和文件A进行合并。

也就是说，选定了level L的文件A,之后在level L+1中找到了所有需要合并的文件B,C,D…..等等。剩下的问题就是具体是如何进行major 合并的？就是说给定了一系列文件，每个文件内部是key有序的，如何对这些文件进行合并，使得新生成的文件仍然Key有序，同时抛掉哪些不再有价值的KV 数据。


## 参考资料
- 张俊林《大数据日知录》第二章：大数据常用的算法和数据结构
- [SSTable and Log Structured Storage: LevelDB](https://www.igvita.com/2012/02/06/sstable-and-log-structured-storage-leveldb/)（[这里](http://www.cnblogs.com/fxjwind/archive/2012/08/14/2638371.html)有个中文的）
- [LevelDB 实现原理](http://www.cnblogs.com/haippy/archive/2011/12/04/2276064.html)（这篇文章主要搬自[levelDB日知录](http://www.samecity.com/blog/Index.asp?SortID=12)）

## 扩展资料
- [RocksDB介绍：一个比LevelDB更彪悍的引擎](http://tech.uc.cn/?p=2592)