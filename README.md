# log4j2-hedera
Provides [Log4j 2.x] support for Hedera™ via an Appender that records logged events using the Hedera™ Consensus Service (HCS).

[Log4j 2.x]: https://logging.apache.org/log4j/2.x/

## ⚠️ Disclaimer

This project is actively under development and not recommended for production use. 
Join the [Hedera discord](https://hedera.com/discord) for the latest updates and announcements.

## Setup

Setup with Maven and Gradle coming soon!

#### Instructions for Building from this Repo with Maven

* Build the jar with Maven:
```
$ mvn package
```


* Install the jar to your project by running this in your project's directory:
```
$ mvn install:install-file -Dfile=<path-to-jar-file> -DgroupId=com.hedera.hashgraph \
    -DartifactId=log4j2-hedera -Dversion=0.1 -Dpackaging=jar
```


* Add the following dependencies to your project's pom.xml:
```
...
<dependencies>
    ...
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.13.1</version>
    </dependency>
    <dependency>
        <groupId>com.hedera.hashgraph</groupId>
        <artifactId>log4j2-hedera</artifactId>
        <version>0.1</version>
    </dependency>
    <dependency>
        <groupId>com.hedera.hashgraph</groupId>
        <artifactId>sdk</artifactId>
        <version>1.1.4</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-okhttp</artifactId>
        <version>1.24.0</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.9.10.3</version>
    </dependency>
</dependencies>
...
```


* Create the directory `src/main/resources` in your project


* Add the file `log4j2.xml` to the resources directory you just made in your project.  An example configuration file is in this repo.


* Add HederaAppender under Appenders in the log4j2.xml like this:
```
...
<Appenders>
    ...
    <HederaAppender name="HederaAppender" operator_id="0.0.555555" topic_id="0.0.555556" 
        operator_key="Ed25519PrivateKeyHere" network_name="testnet_or_mainnet" />
</Appenders>
<Loggers>
    <Root level="error">
        <AppenderRef ref="HederaAppender"/>
    </Root>
</Loggers>
...
```


* Import the log4j packages `LogManager` and `Logger` like this:
```
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
```


* Instantiate the logger like this:
```
final static Logger logger = LogManager.getLogger(App.class);
```


* Log things like this:
```
logger.debug("This is a debug log: " + message);
logger.info("This is an info log : " + message);
logger.warn("This is a warning log : " + message);
logger.error("This is an error log : " + message);
logger.fatal("This is a fatal log : " + message);
```

## Configuration

All configuration for the Appender is done in `log4j2.xml`.

To log things under the level of error, make sure to change the line `<Root level="error">` to `<Root level="debug">`,\
`<Root level="info">`, or `<Root level="warn">`.

Make sure you remember to change the values for each of the `<HederaAppender>` attributes in your project!  The ones provided in the example won't work as is!

#### Attributes

Note: All attribute values should be Strings.

##### Required:

```name```\
The name of the appender

```topicId```\
The ID of your topic in the form of `shard.realm.num`

```operatorId```\
Your Hedera Account ID in the form of `shard.realm.num`

```operatorKey```\
Your Hedera Account Ed25519 Private Key

```networkName```\
The name of the network you wish to log to, either `testnet` or `mainnet`

##### Optional:

```submitKey```\
If your HCS topic was created with a submit key, you must provide an Ed25519 Private Key\
(your submit key) to sign each message with

## License

Licensed under Apache License,
Version 2.0 – see [LICENSE](LICENSE) in this repo
or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
