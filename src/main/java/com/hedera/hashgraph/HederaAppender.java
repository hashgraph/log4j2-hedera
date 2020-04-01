package com.hedera.hashgraph;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.JsonLayout;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.mirror.MirrorClient;

@Plugin(name = "HederaAppender", category = "Core", elementType = "appender", printObject = true)
public class HederaAppender extends AbstractAppender {
    public static final String APPENDER_NAME = "hedera-appender";
    public static final String LOG4J_NDC = "log4j-NDC";
    public static final String LOG4J_MARKER = "log4j-Marker";
    public static final String THREAD_NAME = "log4j-Threadname";

    private static ConsensusTopicId TOPIC_ID;
    private static AccountId OPERATOR_ID;
    private static Ed25519PrivateKey OPERATOR_KEY;
    private static String MIRROR_NODE_ADDRESS;
    private static String NETWORK_NAME;
    private static Ed25519PrivateKey SUBMIT_KEY;

    private MirrorClient mirrorClient;
    private Client client;

    // Instantiate Json layout
    private static Layout<String> layout = JsonLayout.createDefaultLayout();

    public HederaAppender() {
        this(APPENDER_NAME);
    }

    protected HederaAppender(String name) {
        super(name, null, layout, true, null);
    }

    // TODO: Probably cleaner to use a builder here instead
    // Plugin constructor
    @PluginFactory
    public static HederaAppender createAppender(
        @PluginAttribute("name") 
        @Required(message = "No name provided for HederaAppender") 
        final String name,

        @PluginAttribute("topic_id") 
        @Required(message = "No topic_id provided for HederaAppender") 
        final String topic_id,

        @PluginAttribute("operator_id") 
        @Required(message = "No operator_id provided for HederaAppender") 
        final String operator_id,

        @PluginAttribute("operator_key") 
        @Required(message = "No operator_key provided for HederaAppender") 
        final String operator_key,

        @PluginAttribute("network_name") 
        @Required(message = "No network_name provided for HederaAppender") 
        final String network_name,

        @PluginAttribute("mirror_node_address") 
        final String mirror_node_address,

        @PluginAttribute("submit_key") 
        final String submit_key
    ) {
        TOPIC_ID = ConsensusTopicId.fromString(topic_id);
        OPERATOR_ID = AccountId.fromString(operator_id);
        OPERATOR_KEY = Ed25519PrivateKey.fromString(operator_key);
        NETWORK_NAME = network_name;
        if (mirror_node_address != null) {
            MIRROR_NODE_ADDRESS = mirror_node_address;
        }
        if (submit_key != null) {
            SUBMIT_KEY = Ed25519PrivateKey.fromString(submit_key);
        }

        return new HederaAppender(name);
    }

    // Create Mirror Client
    private final MirrorClient createMirrorClient() {
        String mirrorAddress = MIRROR_NODE_ADDRESS;
        if (mirrorAddress == null) {
            mirrorAddress = "api.testnet.kabuto.sh:50211";
        }
        MirrorClient cl = new MirrorClient(mirrorAddress);

        return cl;
    }

    // Create client
    private final Client createClient() {
        String network = NETWORK_NAME;
        Client cl = null;
        if (network.equals("testnet")) {
            cl = Client.forTestnet();
        } else if (network.equals("mainnet")) {
            cl = Client.forMainnet();
        } else {
            System.out.println(
                    "Error in Log4jHedera: NETWORK_NAME is incorrect in .env file, please make sure it is either \"testnet\" or \"mainnet\"");
        }

        cl.setOperator(OPERATOR_ID, OPERATOR_KEY);

        return cl;
    }

    // Main HCS function: creates client on first pass, builds transaction, optionally signs with submit_key, 
    // then sends pre-formatted messages to selected topic
    private final void sendLogs(String content) throws HederaNetworkException, HederaStatusException {
        ConsensusTopicId topicId = TOPIC_ID;
        if (mirrorClient == null && client == null) {
            mirrorClient = createMirrorClient();
            client = createClient();
        }

        Transaction consensusTransaction = new ConsensusMessageSubmitTransaction()
            .setTopicId(topicId)
            .setMessage(content)
            .build(client);

        if (SUBMIT_KEY != null) {
            consensusTransaction.sign(SUBMIT_KEY);
        }

        // TODO: add .executeAsync functionality
        consensusTransaction.execute(client);
    }

    // Receive LogEvent, format to JSON, and send to sendLogs function
    @Override
    public void append(LogEvent logEvent) {
        try {
            sendLogs(layout.toSerializable(logEvent));
        } catch (HederaNetworkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (HederaStatusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
