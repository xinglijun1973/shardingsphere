+++
pre = "<b>7.7. </b>"
title = "数据加密"
weight = 7
+++

## 处理流程详解

Apache ShardingSphere 通过对用户输入的 SQL 进行解析，并依据用户提供的加密规则对 SQL 进行改写，从而实现对原文数据进行加密，并将原文数据（可选）及密文数据同时存储到底层数据库。
在用户查询数据时，它仅从数据库中取出密文数据，并对其解密，最终将解密后的原始数据返回给用户。
Apache ShardingSphere 自动化 & 透明化了数据加密过程，让用户无需关注数据加密的实现细节，像使用普通数据那样使用加密数据。
此外，无论是已在线业务进行加密改造，还是新上线业务使用加密功能，Apache ShardingSphere 都可以提供一套相对完善的解决方案。

### 整体架构

![1](https://shardingsphere.apache.org/document/current/img/encrypt/1.png)

加密模块将用户发起的 SQL 进行拦截，并通过 SQL 语法解析器进行解析、理解 SQL 行为，再依据用户传入的加密规则，找出需要加密的字段和所使用的加解密算法对目标字段进行加解密处理后，再与底层数据库进行交互。
Apache ShardingSphere 会将用户请求的明文进行加密后存储到底层数据库；并在用户查询时，将密文从数据库中取出进行解密后返回给终端用户。
通过屏蔽对数据的加密处理，使用户无需感知解析 SQL、数据加密、数据解密的处理过程，就像在使用普通数据一样使用加密数据。

### 加密规则

在详解整套流程之前，我们需要先了解下加密规则与配置，这是认识整套流程的基础。加密配置主要分为四部分：数据源配置，加密算法配置，加密表配置以及查询属性配置，其详情如下图所示：

![2](https://shardingsphere.apache.org/document/current/img/encrypt/2_v2.png)

**数据源配置**：指数据源配置。

**加密算法配置**：指使用什么加密算法进行加解密。目前 ShardingSphere 内置了五种加解密算法：AES，MD5，RC4，SM3 和 SM4。用户还可以通过实现 ShardingSphere 提供的接口，自行实现一套加解密算法。

**加密表配置**：用于告诉 ShardingSphere 数据表里哪个列用于存储密文数据（cipherColumn）、使用什么算法加解密（encryptorName）、哪个列用于存储辅助查询数据（assistedQueryColumn）、使用什么算法加解密（assistedQueryEncryptorName）、哪个列用于存储明文数据（plainColumn）以及用户想使用哪个列进行 SQL 编写（logicColumn）。

>  如何理解 `用户想使用哪个列进行 SQL 编写（logicColumn）`？
>
> 我们可以从加密模块存在的意义来理解。加密模块最终目的是希望屏蔽底层对数据的加密处理，也就是说我们不希望用户知道数据是如何被加解密的、如何将明文数据存储到 plainColumn，将密文数据存储到 cipherColumn，将辅助查询数据存储到 assistedQueryColumn。
换句话说，我们不希望用户知道 plainColumn、cipherColumn 和 assistedQueryColumn 的存在和使用。
所以，我们需要给用户提供一个概念意义上的列，这个列可以脱离底层数据库的真实列，它可以是数据库表里的一个真实列，也可以不是，从而使得用户可以随意改变底层数据库的 plainColumn、cipherColumn 和 assistedQueryColumn 的列名。
或者删除 plainColumn，选择永远不再存储明文，只存储密文。
只要用户的 SQL 面向这个逻辑列进行编写，并在加密规则里给出 logicColumn 和 plainColumn、cipherColumn、assistedQueryColumn 之间正确的映射关系即可。
>
> 为什么要这么做呢？答案在文章后面，即为了让已上线的业务能无缝、透明、安全地进行数据加密迁移。

**查询属性的配置**：当底层数据库表里同时存储了明文数据、密文数据后，该属性开关用于决定是直接查询数据库表里的明文数据进行返回，还是查询密文数据通过 Apache ShardingSphere 解密后返回。该属性开关支持表级别和整个规则级别配置，表级别优先级最高。

### 加密处理过程

举例说明，假如数据库里有一张表叫做 `t_user`，这张表里实际有两个字段 `pwd_plain`，用于存放明文数据、`pwd_cipher`，用于存放密文数据、`pwd_assisted_query`，用于存放辅助查询数据，同时定义 logicColumn 为 `pwd`。
那么，用户在编写 SQL 时应该面向 logicColumn 进行编写，即 `INSERT INTO t_user SET pwd = '123'`。
Apache ShardingSphere 接收到该 SQL，通过用户提供的加密配置，发现 `pwd` 是 logicColumn，于是便对逻辑列及其对应的明文数据进行加密处理。
**Apache ShardingSphere 将面向用户的逻辑列与面向底层数据库的明文列和密文列进行了列名以及数据的加密映射转换。** 
如下图所示：

![3](https://shardingsphere.apache.org/document/current/img/encrypt/3.png)

即依据用户提供的加密规则，将用户 SQL 与底层数据表结构割裂开来，使得用户的 SQL 编写不再依赖于真实的数据库表结构。
而用户与底层数据库之间的衔接、映射、转换交由 Apache ShardingSphere 进行处理。

下方图片展示了使用加密模块进行增删改查时，其中的处理流程和转换逻辑，如下图所示。

![4](https://shardingsphere.apache.org/document/current/img/encrypt/4.png)

## 解决方案详解

在了解了 Apache ShardingSphere 加密处理流程后，即可将加密配置、加密处理流程与实际场景进行结合。
所有的设计开发都是为了解决业务场景遇到的痛点。那么面对之前提到的业务场景需求，又应该如何使用 Apache ShardingSphere 这把利器来满足业务需求呢？

### 新上线业务

业务场景分析：新上线业务由于一切从零开始，不存在历史数据清洗问题，所以相对简单。

解决方案说明：选择合适的加密算法，如 AES 后，只需配置逻辑列（面向用户编写 SQL ）和密文列（数据表存密文数据）即可，**逻辑列和密文列可以相同也可以不同**。建议配置如下（YAML 格式展示）：

```yaml
-!ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    t_user:
      columns:
        pwd:
          cipherColumn: pwd_cipher
          encryptorName: aes_encryptor
          assistedQueryColumn: pwd_assisted_query
          assistedQueryEncryptorName: pwd_assisted_query_cipher
          queryWithCipherColumn: true
```

使用这套配置， Apache ShardingSphere 只需将 logicColumn 和 cipherColumn，assistedQueryColumn 进行转换，底层数据表不存储明文，只存储了密文，这也是安全审计部分的要求所在。
如果用户希望将明文、密文一同存储到数据库，只需添加 plainColumn 配置即可。整体处理流程如下图所示：

![5](https://shardingsphere.apache.org/document/current/img/encrypt/5.png)

### 已上线业务改造

业务场景分析：由于业务已经在线上运行，数据库里必然存有大量明文历史数据。现在的问题是如何让历史数据得以加密清洗、如何让增量数据得以加密处理、如何让业务在新旧两套数据系统之间进行无缝、透明化迁移。

解决方案说明：在提供解决方案之前，我们先来头脑风暴一下：首先，既然是旧业务需要进行加密改造，那一定存储了非常重要且敏感的信息。这些信息含金量高且业务相对基础重要。
不应该采用停止业务禁止新数据写入，再找个加密算法把历史数据全部加密清洗，再把之前重构的代码部署上线，使其能把存量和增量数据进行在线加密解密。

那么另一种相对安全的做法是：重新搭建一套和生产环境一模一样的预发环境，然后通过相关迁移洗数工具把生产环境的**存量原文数据**加密后存储到预发环境，
而**新增数据**则通过例如 MySQL 主从复制及业务方自行开发的工具加密后存储到预发环境的数据库里，再把重构后可以进行加解密的代码部署到预发环境。
这样生产环境是一套**以明文为核心的查询修改**的环境；预发环境是一套**以密文为核心加解密查询修改**的环境。
在对比一段时间无误后，可以夜间操作将生产流量切到预发环境中。
此方案相对安全可靠，只是时间、人力、资金、成本较高，主要包括：预发环境搭建、生产代码整改、相关辅助工具开发等。

业务开发人员最希望的做法是：减少资金费用的承担、最好不要修改业务代码、能够安全平滑迁移系统。于是，ShardingSphere 的加密功能模块便应运而生。可分为 3 步进行：

1. 系统迁移前

假设系统需要对 `t_user` 的 `pwd` 字段进行加密处理，业务方使用 Apache ShardingSphere 来代替标准化的 JDBC 接口，此举基本不需要额外改造（我们还提供了 YAML 接入方式，满足不同业务方需求）。
另外，提供一套加密配置规则，如下所示：

```yaml
-!ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    t_user:
      columns:
        pwd:
          plainColumn: pwd
          cipherColumn: pwd_cipher
          encryptorName: aes_encryptor
          assistedQueryColumn: pwd_assisted_query
          assistedQueryEncryptorName: pwd_assisted_query_cipher
          queryWithCipherColumn: false
```

依据上述加密规则可知，首先需要在数据库表 `t_user` 里新增一个字段叫做 `pwd_cipher`，即 cipherColumn，用于存放密文数据，同时我们把 plainColumn 设置为 `pwd`，用于存放明文数据，而把 logicColumn 也设置为 `pwd`。
由于之前的代码 SQL 就是使用 `pwd` 进行编写，即面向逻辑列进行 SQL 编写，所以业务代码无需改动。
通过 Apache ShardingSphere，针对新增的数据，会把明文写到 pwd 列，并同时把明文进行加密存储到 `pwd_cipher` 列。
此时，由于 `queryWithCipherColumn` 设置为 false，对业务应用来说，依旧使用 `pwd` 这一明文列进行查询存储，却在底层数据库表 `pwd_cipher` 上额外存储了新增数据的密文数据，其处理流程如下图所示：

![6](https://shardingsphere.apache.org/document/current/img/encrypt/6.png)

新增数据在插入时，就通过 Apache ShardingSphere 加密为密文数据，并被存储到了 cipherColumn。而现在就需要处理历史明文存量数据。
**由于 Apache ShardingSphere 目前并未提供相关迁移洗数工具，此时需要业务方自行将 `pwd` 中的明文数据进行加密处理存储到 `pwd_cipher`。**

2. 系统迁移中

新增的数据已被 Apache ShardingSphere 将密文存储到密文列，明文存储到明文列；历史数据被业务方自行加密清洗后，将密文也存储到密文列。
也就是说现在的数据库里即存放着明文也存放着密文，只是由于配置项中的 `queryWithCipherColumn = false`，所以密文一直没有被使用过。
现在我们为了让系统能切到密文数据进行查询，需要将加密配置中的 queryWithCipherColumn 设置为 true。
在重启系统后，我们发现系统业务一切正常，但是 Apache ShardingSphere 已经开始从数据库里取出密文列的数据，解密后返回给用户；
而对于用户的增删改需求，则依旧会把原文数据存储到明文列，加密后密文数据存储到密文列。

虽然现在业务系统通过将密文列的数据取出，解密后返回；但是，在存储的时候仍旧会存一份原文数据到明文列，这是为什么呢？
答案是：为了能够进行系统回滚。
**因为只要密文和明文永远同时存在，我们就可以通过开关项配置自由将业务查询切换到 cipherColumn 或 plainColumn。**
也就是说，如果将系统切到密文列进行查询时，发现系统报错，需要回滚。那么只需将 `queryWithCipherColumn = false`，Apache ShardingSphere 将会还原，即又重新开始使用 plainColumn 进行查询。
处理流程如下图所示：

![7](https://shardingsphere.apache.org/document/current/img/encrypt/7.png)

3. 系统迁移后

由于安全审计部门要求，业务系统一般不可能让数据库的明文列和密文列永久同步保留，我们需要在系统稳定后将明文列数据删除。
即我们需要在系统迁移后将 plainColumn，即 pwd 进行删除。那问题来了，现在业务代码都是面向pwd进行编写 SQL 的，把底层数据表中的存放明文的 pwd 删除了，
换用 pwd_cipher 进行解密得到原文数据，那岂不是意味着业务方需要整改所有 SQL，从而不使用即将要被删除的 pwd 列？还记得我们 Apache ShardingSphere 的核心意义所在吗？

> 这也正是 Apache ShardingSphere 核心意义所在，即依据用户提供的加密规则，将用户 SQL 与底层数据库表结构割裂开来，使得用户的 SQL 编写不再依赖于真实的数据库表结构。
而用户与底层数据库之间的衔接、映射、转换交由 Apache ShardingSphere 进行处理。

是的，因为有 logicColumn 存在，用户的编写 SQL 都面向这个虚拟列，Apache ShardingSphere 就可以把这个逻辑列和底层数据表中的密文列进行映射转换。于是迁移后的加密配置即为：

```yaml
-!ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    t_user:
      columns:
        pwd: # pwd 与 pwd_cipher 的转换映射
          cipherColumn: pwd_cipher
          encryptorName: aes_encryptor
          assistedQueryColumn: pwd_assisted_query
          assistedQueryEncryptorName: pwd_assisted_query_cipher
          queryWithCipherColumn: true
```

其处理流程如下：

![8](https://shardingsphere.apache.org/document/current/img/encrypt/8.png)

4. 系统迁移完成 

安全审计部门再要求，业务系统需要定期或某些紧急安全事件触发修改密钥，我们需要再次进行迁移洗数，即使用旧密钥解密后再使用新密钥加密。既要又要还要的问题来了，明文列数据已删除，数据库表中数据量千万级，迁移洗数需要一定时间，迁移洗数过程中密文列在变化，系统还需正确提供服务。怎么办？
答案是：辅助查询列
**因为辅助查询列一般使用不可逆的 MD5 和 SM3 等算法，基于辅助列进行查询，即使在迁移洗数过程中，系统也是可以提供正确服务。**

至此，已在线业务加密整改解决方案全部叙述完毕。我们提供了 Java、YAML 两方式供用户选择接入，力求满足业务不同的接入需求。
该解决方案目前已在京东数科不断落地上线，提供对内基础服务支撑。

## 中间件加密服务优势

1. 自动化 & 透明化数据加密过程，用户无需关注加密中间实现细节。
2. 提供多种内置、第三方（AKS）的加密算法，用户仅需简单配置即可使用。
3. 提供加密算法 API 接口，用户可实现接口，从而使用自定义加密算法进行数据加密。
4. 支持切换不同的加密算法。
5. 针对已上线业务，可实现明文数据与密文数据同步存储，并通过配置决定使用明文列还是密文列进行查询。可实现在不改变业务查询 SQL 前提下，已上线系统对加密前后数据进行安全、透明化迁移。

## 加密算法解析

Apache ShardingSphere 提供了加密算法用于数据加密，即 `EncryptAlgorithm`。

一方面，Apache ShardingSphere 为用户提供了内置的加解密实现类，用户只需进行配置即可使用；
另一方面，为了满足用户不同场景的需求，我们还开放了相关加解密接口，用户可依据这两种类型的接口提供具体实现类。
再进行简单配置，即可让 Apache ShardingSphere 调用用户自定义的加解密方案进行数据加密。

### EncryptAlgorithm

该解决方案通过提供 `encrypt()`，`decrypt()` 两种方法对需要加密的数据进行加解密。
在用户进行 `INSERT`，`DELETE`，`UPDATE` 时，ShardingSphere会按照用户配置，对SQL进行解析、改写、路由，并调用 `encrypt()` 将数据加密后存储到数据库， 
而在 `SELECT` 时，则调用 `decrypt()` 方法将从数据库中取出的加密数据进行逆向解密，最终将原始数据返回给用户。

当前，Apache ShardingSphere 针对这种类型的加密解决方案提供了五种具体实现类，分别是 MD5（不可逆），AES（可逆），RC4（可逆），SM3（不可逆），SM4（可逆），用户只需配置即可使用这五种内置的方案。
