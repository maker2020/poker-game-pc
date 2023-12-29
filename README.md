# poker-game

## 版本

V1:Client和Server单独项目，便于热更新。

## 介绍

基于Java17的Java游戏，架构为C/S，Client的UI框架为JavaFX。
本项目涉及多线程，网络编程，Socket，IO流（BIO、NIO等），几个设计模式（观察者，原型，策略等），类加载器的使用，热更新，几个设计原则如（开闭原则，单一，里氏代换），SPI设计思想。
适合练手

## 软件架构

**C/S架构**
详细说明：该项目暂时使用传统较为常见的[BIO+线程池]的模型，NIO在连接数<1000并发程度不高或局域网环境，它所带来的好处远不及它的复杂繁琐度。后续会通过成熟的NIO框架，Netty、MINA等，将该软件的Client和Server进行优化。至于AIO（async)暂不考虑使用。
</br>

**环境说明**
采用Jdk17（lts版本）。熟练使用新语法新功能，如record记录类、string库、sealed关键字、三引号字符串、switch返回值、var可推测类型、instanceof简化、lambda等变化，并带来针对Socket等方面优化的优势。

## 安装教程

服务器直接运行，客户端运行win64下的exe执行文件。

## 使用说明

该程序默认选择下面列表的第三种，无需更改，直接运行即可。

以下是相关说明：

1.将vscode项目的java-fx-sdk文件夹中的bin目录下的所有文件复制到vscode的bin输出目录。
Run/Debug ServerApp.java和FXClientApp.java

2(任选).Vscode的java project设置添加classpath，将javafx-sdk\bin路径添加进去即可。

3(默认).System.setProperty("java.library.path",sdk/bin)

4(任选).jvm参数-Djava.library.path=dll所在绝对路径。

5(任选).手动加载dll

## 数据传输相关说明

TCP传输粘包/拆包问题的说明：经过自己的摸索，本应用传输连续数据时，一方面使用封装的SuitStream，它以换行作为分隔符；另一方面可以利用呼叫应答（响应）的方式交替等待从而阻断数据一次性过大传输（避免IP分片拆包），因此数据不会出现粘包和拆包。

## 设计原则/模式相关说明

回顾七大原则：开闭，单一职责，组合复用，接口隔离，里氏代换，依赖倒转，迪米特法则。</br>
这七大原则除了开闭原则，其他都围绕封装、多态、继承这三大面向对象原则细化而来的。
</br>

本游戏的相关设计模式或设计原则在注释中有相关联注释。</br>
设计模式：克隆，观察者等。</br>
设计原则：单一职责（各对象分明），组合复用（依赖其他对象），接口隔离（游戏规则验证接口valid()），里氏代换（继承扩展而不修改父类功能），开闭原则（新增不修改，热更新），依赖倒转（类结构的设计面向接口和抽象），迪米特法则（把没有直接关系的对象**封装**到局部）。

建议改进：</br>

1. 设计客户端时，UI组件相关业务服务中，构造参数过多。为了使建造者独立，易扩展，同时更好把控**装配**细节，应使用Builder模式/Factory模式（它们区别在于顺序要求）。
2. 在Poker类中的比较器可以设计为单例模式更加良好。
</br>

### 将来的版本

基于强大的网络编程框架Netty。
它将做到：</br>
解放原生jdk的nio编程，用channel取代stream，数据传输将引入ByteBuf作为缓存区中转。
</br>
该游戏会变得更加的健壮。
