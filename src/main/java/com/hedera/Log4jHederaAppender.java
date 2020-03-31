package com.hedera;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.JsonLayout;
import io.github.cdimascio.dotenv.Dotenv;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.mirror.MirrorClient;

@Plugin(name = "Log4jHedera", category = "Core", elementType = "appender", printObject = true)
public class Log4jHederaAppender extends AbstractAppender {
    public static final String APPENDER_NAME = "log4j-hedera";
    public static final String LOG4J_NDC = "log4j-NDC";
    public static final String LOG4J_MARKER = "log4j-Marker";
    public static final String THREAD_NAME = "log4j-Threadname";

    private static final Dotenv dotenv = Dotenv.load();

    private static final AccountId OPERATOR_ID = AccountId.fromString(dotenv.get("OPERATOR_ID"));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(dotenv.get("OPERATOR_KEY"));
    private static final String MIRROR_NODE_ADDRESS = dotenv.get("MIRROR_NODE_ADDRESS");
    private static final ConsensusTopicId TOPIC_ID = ConsensusTopicId.fromString(dotenv.get("TOPIC_ID"));

    private MirrorClient mirrorClient = null;
    private Client client = null;

    // Instantiate Json layout
    private static Layout<String> layout = JsonLayout.createDefaultLayout();

    public Log4jHederaAppender() {
        this(APPENDER_NAME);
    }

    protected Log4jHederaAppender(String name) {
        super(name, null, layout, true, null);
    }

    // Plugin constructor
    @PluginFactory
    public static Log4jHederaAppender createAppender(@PluginAttribute("name") final String name) {
        if (name == null) {
            LOGGER.error("No name provided");
            return null;
        }

        return new Log4jHederaAppender(name);
    }

    // Create Mirror Client
    private final MirrorClient createMirrorClient() {
        String mirrorAddress = MIRROR_NODE_ADDRESS;
        if(mirrorAddress == null) {
            mirrorAddress = "api.testnet.kabuto.sh:50211";
        }
        MirrorClient cl = new MirrorClient(mirrorAddress);

        return cl;
    }

    // Create client
    private final Client createClient() {
        String network = dotenv.get("NETWORK_NAME");
        Client cl = null;
        if (network.equals("testnet")) {
            cl = Client.forTestnet();
        } else if (network.equals("mainnet")) {
            cl = Client.forMainnet();
        } else {
            System.out.println("Error in Log4jHedera: NETWORK_NAME is incorrect in .env file, please make sure it is either \"testnet\" or \"mainnet\"");
        }

        cl.setOperator(OPERATOR_ID, OPERATOR_KEY);

        return cl;
    }

    // Main Logging function, creates a topic if needed, then uses json formatter to send messages to the topic
    private final void logIt(String content) throws HederaNetworkException, HederaStatusException {
        ConsensusTopicId topicId = TOPIC_ID;
        if (mirrorClient == null && client == null)
        {
            mirrorClient = createMirrorClient();
            client = createClient();
        }

        new ConsensusMessageSubmitTransaction()
            .setTopicId(topicId)
            .setMessage(content)
            .execute(client);
    }

    //Receive LogEvent and send to HCS
    @Override
    public void append(LogEvent logEvent) {
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
