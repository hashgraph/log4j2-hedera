# log4j2-hedera
Provides [Log4j 2.x] support for Hedera™ via an Appender that records logged events using the Hedera™ Consensus Service (HCS).

[Log4j 2.x]: https://logging.apache.org/log4j/2.x/

## Install

##### Gradle

```groovy
implementation 'com.hedera.hashgraph:log4j2:0.1.0'
```

##### Maven

```xml
<dependency>
    <groupId>com.hedera.hashgraph</groupId>
    <artifactId>log4j2</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage


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

## Contributing to this Project

We welcome participation from all developers!
For instructions on how to contribute to this repo, please
review the [Contributing Guide](CONTRIBUTING.md).

## License

Licensed under Apache License,
Version 2.0 – see [LICENSE](LICENSE) in this repo
or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
