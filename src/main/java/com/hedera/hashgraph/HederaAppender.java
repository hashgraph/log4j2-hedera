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

@Plugin(name = "HederaAppender", category = "Core", elementType = "appender", printObject = true)
public class HederaAppender extends AbstractAppender {
    public static final String LOG4J_NDC = "log4j-NDC";
    public static final String LOG4J_MARKER = "log4j-Marker";
    public static final String THREAD_NAME = "log4j-Threadname";

    private String topicId;
    private String operatorId;
    private String operatorKey;
    private String networkName;
    private String submitKey;

    private Client client;

    // Instantiate Json layout
    private static Layout<String> layout = JsonLayout.createDefaultLayout();

    private HederaAppender(String name, String topicId, String operatorId, String operatorKey, String networkName, String submitKey) {
        super(name, null, layout, true, null);

        this.topicId = topicId;
        this.operatorId = operatorId;
        this.operatorKey = operatorKey;
        this.networkName = networkName;
        this.submitKey = submitKey;
    }

    // TODO: Probably cleaner to use a builder here instead
    // Plugin constructor
    @PluginFactory
    public static HederaAppender createAppender(
        @PluginAttribute("name") 
        @Required(message = "No name provided for HederaAppender") 
        final String name,

        @PluginAttribute("topicId") 
        @Required(message = "No topicId provided for HederaAppender") 
        final String topicId,

        @PluginAttribute("operatorId") 
        @Required(message = "No operatorId provided for HederaAppender") 
        final String operatorId,

        @PluginAttribute("operatorKey") 
        @Required(message = "No operatorKey provided for HederaAppender") 
        final String operatorKey,

        @PluginAttribute("networkName") 
        @Required(message = "No networkName provided for HederaAppender") 
        final String networkName,

        @PluginAttribute("submitKey") 
        final String submitKey
    ) {
        return new HederaAppender(
            name, 
            topicId, 
            operatorId, 
            operatorKey, 
            networkName, 
            submitKey
        );
    }

    // Create client
    private final Client createClient() {
        String network = networkName;
        Client cl = null;
        if (network.equals("testnet")) {
            cl = Client.forTestnet();
        } else if (network.equals("mainnet")) {
            cl = Client.forMainnet();
        } else {
            System.out.println(
                    "Error in Log4jHedera: NETWORK_NAME is incorrect in .env file, please make sure it is either \"testnet\" or \"mainnet\"");
        }

        cl.setOperator(AccountId.fromString(operatorId), Ed25519PrivateKey.fromString(operatorKey));

        return cl;
    }

    // Main HCS function: creates client on first pass, builds transaction, optionally signs with submitKey, 
    // then sends pre-formatted messages to selected topic
    private final void sendLogs(String content) throws HederaNetworkException, HederaStatusException {
        ConsensusTopicId topicId = ConsensusTopicId.fromString(this.topicId);
        if (client == null) {
            client = createClient();
        }

        Transaction consensusTransaction = new ConsensusMessageSubmitTransaction()
            .setTopicId(topicId)
            .setMessage(content)
            .build(client);

        if (submitKey != null) {
            consensusTransaction.sign(Ed25519PrivateKey.fromString(this.submitKey));
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
