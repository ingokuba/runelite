package net.runelite.client.plugins.qp;

import lombok.extern.java.Log;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ChatboxInput;
import net.runelite.client.events.PrivateMessageInput;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "q p Chat Plugin",
        description = "Display amount of spaces/commas needed for perfect q p",
        enabledByDefault = true
)
@Log
public class qpPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    private qpCounter clanCounter;
    private qpCounter publicCounter;

    private int clanCount = -1;
    private int publicCount = -1;

    private static final int SPACE_WIDTH = 3;

    @Override
    protected void startUp() throws Exception {
        clanCounter = new qpCounter(itemManager.getImage(ItemID.STARFACE), this, 0).setMessageType(ChatMessageType.FRIENDSCHAT);
        publicCounter = new qpCounter(itemManager.getImage(ItemID.WHITE_PARTYHAT), this, 0).setMessageType(ChatMessageType.PUBLICCHAT);
        client.refreshChat();
    }

    @Override
    protected void shutDown() throws Exception {
        infoBoxManager.removeIf(infoBox -> infoBox instanceof qpCounter);
        client.refreshChat();
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        Widget chatInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
        String line = chatInput.getText();
        String sender = line.substring(line.indexOf(">") + 1, line.indexOf(":"));
        String input = line.substring(line.indexOf("<col=0000ff>") + "<col=0000ff>".length(), line.indexOf("</col>"));
        if (clanCount > -1) {
            infoBoxManager.removeIf(infoBox -> infoBox instanceof qpCounter && ((qpCounter) infoBox).getMessageType() == ChatMessageType.FRIENDSCHAT);
            clanCounter.setCount(clanCount - getWidth(chatInput, sender + ": " + input.substring(1)) - 6 - (line.contains("<img") ? 13 : 0));
            infoBoxManager.addInfoBox(clanCounter);
        } else if (publicCount > -1) {
            infoBoxManager.removeIf(infoBox -> infoBox instanceof qpCounter && ((qpCounter) infoBox).getMessageType() == ChatMessageType.PUBLICCHAT);
            publicCounter.setCount(publicCount - getWidth(chatInput, sender + ": " + input) - 6 - (line.contains("<img") ? 13 : 0));
            infoBoxManager.addInfoBox(publicCounter);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        ChatMessageType chatMessageType = event.getType();
        String message = event.getMessage();
        switch (chatMessageType) {
            case FRIENDSCHAT:
                if (message.contains("q p")) {
                    clanCount = getIndex(getUsername(event) + ": " + message) + (event.getName().contains("<img") ? 13 : 0);
                } else {
                    clanCount = -1;
                    infoBoxManager.removeInfoBox(clanCounter);
                }
                break;
            case PUBLICCHAT:
                if (message.contains("q p")) {
                    publicCount = getIndex(getUsername(event) + ": " + message) + (event.getName().contains("<img") ? 13 : 0);
                } else {
                    publicCount = -1;
                    infoBoxManager.removeInfoBox(publicCounter);
                }
                break;
        }
    }

    private String getUsername(ChatMessage event) {
        String name = event.getName();
        return name.substring(name.indexOf(">") + 1);
    }

    /**
     * Get size of message until first appearance of 'q p'.
     *
     * @param message Chat message containing 'q p'
     * @return Amount of pixels until first appearance of 'q p'
     */
    private int getIndex(String message) {
        String substring = message.substring(0, message.indexOf("q p"));
        return getWidth(client.getWidget(WidgetInfo.CHATBOX_INPUT), substring) + 10; // plus width of q
    }

    /**
     * Get size of message.
     *
     * @return Amount of pixels in the message.
     */
    private int getWidth(Widget fontParent, String message) {
        FontTypeFace font = fontParent.getFont();
        return font.getTextWidth(message);
    }
}
