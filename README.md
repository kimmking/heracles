# heracles
**Heracles是一款开源的分布式消息队列，提供了高性能、稳定性、线性扩展等特性。采用了计算与存储分离的分层式架构**


1. 通讯层采用GRPC+Vert.x实现，一方面GRPC可以直接使用工具生成不同语言的客户端代码，这样编写多语言SDK的成本
会低很多，另外目前GRPC是基于HTTP2协议的，Nginx也增加了对GRPC协议的支持，因此这款会有很多可以做的事情，比如说
限流、负载均衡等，甚至都可以通过lua脚本放到Nginx去做
2. 存储介质基于DistributedLog，Dlog是在Apache BookKeeper之上构建的一个分布式日志框架，可以简单的理解为
分布式日志。也就是说topic的数据是存储到分布式文件系统中，而不是本地文件系统，这样有几点好处：
    1. 日志的复制、写入等细节不需要你去处理，Bookkeeper自身就自带这些特性，如果你去看RocketMQ、Kafka的代码的话，
    你会发现很多处理文件系统的理解，比如说topic的数据存储到commit log中，并在其之上构建一层索引，RocketMQ中叫做
    consumer queue，你还需要处理文件的切分、刷新、校验等，包括数据的复制，同步复制还是异步复制，基于主从、还是
    kafka的ISR、还是基于Raft协议？这块有很多需要考虑的点，而这些正是分布式文件系统擅长的部分。
    2. 加入说kafka中的一台broker挂掉了，那么我们就需要另外一个节点加入到ISR中，替代它的角色，但这需要复制大量的
    topic日志文件，产生大量的IO，导致系统性能下降。但Heracles的架构中，数据是存储到底层分布式文件系统中的，是计算与
    存储分离的架构，也就是说你可以broker是无状态的，如果挂掉客户端可以连接到另外一台broker，由另外一台broker继续提供
    服务，而如果Bookkeeper的某个节点挂掉了，对于上层来说是透明的，比如我们指定需要写3份数据，2份ack了才返回给客户端，
    而如果有一个节点挂掉了，bk会将数据复制到另外的节点，均衡数据，对于上层是无感知的。
    3. 读写分离。写入我们可以由某一台broker提供服务，并将最近写入到消息缓存到内存中。我们知道consumer分为两种，一种是
    tailing read,也就是读取速度大致跟写入速度是一致的， 那么这类consumer我们就看可以让它直接通过内存读取消息；另外一种
    是catchup read,也就是offset比较落后的，比如说刚启动的consumer，想要从最早的消息开始读取，这个时候我们就可以让他从
    另外的节点读取数据，broker可以根据consumer的消费速度、网络延迟等，预先从bookkeeper中抓取数据，从而是实现IO隔离。
