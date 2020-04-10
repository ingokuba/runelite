package net.runelite.client.plugins.qp;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runelite.api.ChatMessageType;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Counter;

import java.awt.image.BufferedImage;

@Accessors(chain = true)
public class qpCounter extends Counter {

    @Getter
    @Setter
    private ChatMessageType messageType;

    public qpCounter(BufferedImage image, Plugin plugin, int count) {
        super(image, plugin, count);
    }
}
