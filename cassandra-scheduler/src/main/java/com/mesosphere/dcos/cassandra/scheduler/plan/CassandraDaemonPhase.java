package com.mesosphere.dcos.cassandra.scheduler.plan;

import com.google.common.eventbus.EventBus;
import com.mesosphere.dcos.cassandra.scheduler.config.ConfigurationManager;
import com.mesosphere.dcos.cassandra.scheduler.offer.CassandraOfferRequirementProvider;
import com.mesosphere.dcos.cassandra.scheduler.tasks.CassandraTasks;
import org.apache.commons.collections.CollectionUtils;
import org.apache.mesos.scheduler.plan.Block;
import org.apache.mesos.scheduler.plan.Phase;
import org.apache.mesos.scheduler.plan.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CassandraDaemonPhase implements Phase {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CassandraDaemonPhase.class);
    private List<Block> blocks = null;
    private int servers;
    private int seeds;
    private EventBus eventBus;
    private CassandraOfferRequirementProvider offerRequirementProvider;
    private CassandraTasks cassandraTasks;

    public CassandraDaemonPhase(
            ConfigurationManager configurationManager,
            CassandraOfferRequirementProvider offerRequirementProvider,
            EventBus eventBus,
            CassandraTasks cassandraTasks) {
        this.servers = configurationManager.getServers();
        this.seeds = configurationManager.getSeeds();
        this.offerRequirementProvider = offerRequirementProvider;
        this.eventBus = eventBus;
        this.cassandraTasks = cassandraTasks;
        this.blocks = createBlocks();
    }

    private List<Block> createBlocks() {
        final List<Block> blocks = new ArrayList<>(servers);
        final List<String> created =
                new ArrayList<>(cassandraTasks.getDaemons().keySet());
        //here we will add a block for all tasks we have recorded and create a
        //new block with a newly recorded task for a scale out
        try {
            for (int i = 0; i < servers; i++) {
                final CassandraDaemonBlock daemonBlock =
                        CassandraDaemonBlock.create(i,
                                (i < created.size()) ? created.get(i) :
                                        cassandraTasks.createDaemon().getId(),
                                offerRequirementProvider,
                                cassandraTasks);
                eventBus.register(daemonBlock);
                blocks.add(daemonBlock);
            }
        } catch (Throwable throwable) {

            String message = "Failed to create CassandraDaemonPhase this is a" +
                    " fatal exception and the program will now exit. Please " +
                    "verify your scheduler configuration and attempt to " +
                    "relaunch the program.";

            LOGGER.error(message, throwable);

            throw new IllegalStateException(message, throwable);

        }

        return blocks;
    }

    @Override
    public List<? extends Block> getBlocks() {
        return blocks;
    }

    @Override
    public Block getCurrentBlock() {
        Block currentBlock = null;
        if (!CollectionUtils.isEmpty(blocks)) {
            for (Block block : blocks) {
                if (!block.isComplete()) {
                    currentBlock = block;
                    break;
                }
            }
        }

        return currentBlock;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return "Update to default config";
    }

    @Override
    public Status getStatus() {
        return getCurrentBlock().getStatus();
    }

    @Override
    public boolean isComplete() {
        for (Block block : blocks) {
            if (!block.isComplete()) {
                return false;
            }
        }

        return true;
    }
}
