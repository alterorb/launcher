package net.alterorb.launcher;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.DiscordBuild;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.RichPresence.Builder;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import lombok.extern.slf4j.Slf4j;
import net.alterorb.launcher.alterorb.AlterorbGame;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;

@Slf4j
@Singleton
public class DiscordIntegration {

    private static final long ALTERORB_CLIENT_ID = 564613743636643841L;

    private final IPCClient client = new IPCClient(ALTERORB_CLIENT_ID);

    @Inject
    public DiscordIntegration() {
    }

    public void initialize() {
        LOGGER.info("Initializing the discord integration...");

        try {
            client.connect(DiscordBuild.ANY);
        } catch (NoDiscordClientException e) {
            LOGGER.info("No discord client was detected, the integration will be disabled");
        }
    }

    public void updateRichPresence(AlterorbGame availableGame) {

        if (client.getStatus() != PipeStatus.CONNECTED) {
            return;
        }
        RichPresence richPresence = new Builder()
                .setDetails(availableGame.getName())
                .setStartTimestamp(OffsetDateTime.now())
                .build();

        client.sendRichPresence(richPresence);
    }

    public void clearRichPresence() {
        if (client.getStatus() != PipeStatus.CONNECTED) {
            return;
        }
        client.sendRichPresence(null);
    }

    public void shutdown() {
        if (client.getStatus() == PipeStatus.CONNECTED) {
            client.close();
        }
    }
}
