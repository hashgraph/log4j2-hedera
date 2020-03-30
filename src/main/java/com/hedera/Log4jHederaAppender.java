package com.hedera;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.JsonLayout;

import java.io.Serializable;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.mirror.MirrorClient;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicQuery;

@Plugin(name = "Log4jHedera", category = "Core", elementType = "appender", printObject = true)
public class Log4jHederaAppender extends AbstractAppender {
    public static final String APPENDER_NAME = "log4j-hedera";
    public static final String LOG4J_NDC = "log4j-NDC";
    public static final String LOG4J_MARKER = "log4j-Marker";
    public static final String THREAD_NAME = "log4j-Threadname";

    // TODO: Move these to .env before commit
    private static final AccountId OPERATOR_ID = AccountId.fromString("0.0.168454");
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(
            "302e020100300506032b657004220420d6177aa879b4b5d6568c030160156849aca8bf9ed12131959c2c16714482c062");
    private static final String MIRROR_NODE_ADDRESS = "api.testnet.kabuto.sh:50211";
    private ConsensusTopicId topicId = null;
    private MirrorClient mirrorClient = null;
    private Client client = null;

    private static Layout<String> layout = JsonLayout.createDefaultLayout();

    public Log4jHederaAppender() {
        this(APPENDER_NAME);
    }

    protected Log4jHederaAppender(String name) {
        super(name, null, layout, true, null);
    }

    @PluginFactory
    public static Log4jHederaAppender createAppender(@PluginAttribute("name") final String name) {
        if (name == null) {
            LOGGER.error("No name provided");
            return null;
        }

        return new Log4jHederaAppender(name);
    }

    // TODO: allow for mainnet

    private final MirrorClient createMirrorClient() {
        MirrorClient cl = new MirrorClient(MIRROR_NODE_ADDRESS);

        return cl;
    }

    private final Client createClient() {
        Client cl = Client.forTestnet();

        cl.setOperator(OPERATOR_ID, OPERATOR_KEY);

        return cl;
    }

    // Create new Topic
    private final TransactionId createTopic(Client client) throws HederaNetworkException, HederaStatusException {
        TransactionId txid = new ConsensusTopicCreateTransaction().execute(client);

        return txid;
    }

    // Get TopicId
    private final ConsensusTopicId getTopicId(TransactionId txid, Client client) throws HederaStatusException {
        ConsensusTopicId ctid = txid.getReceipt(client).getConsensusTopicId();

        return ctid;
    }

    private final void logIt(String content) throws HederaNetworkException, HederaStatusException {
        if (topicId == null)
        {
            mirrorClient = createMirrorClient();
            client = createClient();
            TransactionId txid = createTopic(client);
            topicId = getTopicId(txid, client);
            System.out.println("Created New Topic Id for log: " + topicId);
        }


        new ConsensusMessageSubmitTransaction()
            .setTopicId(topicId)
            .setMessage(content)
            .execute(client)
            .getReceipt(client);
    }

    //Receive LogEvent and send to HCS
    @Override
    public void append(LogEvent logEvent) {
        System.out.println(this.getLayout());
        try {
            logIt(layout.toSerializable(logEvent));
        } catch (HederaNetworkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (HederaStatusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
