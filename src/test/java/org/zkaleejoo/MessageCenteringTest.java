package org.zkaleejoo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;
import org.zkaleejoo.utils.DefaultFontInfo;
import org.zkaleejoo.utils.MessageUtils;

import static org.junit.jupiter.api.Assertions.*;

public class MessageCenteringTest {

    @Test
    public void testFontInfoWidths() {
        assertEquals(5, DefaultFontInfo.getDefaultFontInfo('A').getLength());
        assertEquals(1, DefaultFontInfo.getDefaultFontInfo('i').getLength());
        assertEquals(3, DefaultFontInfo.getDefaultFontInfo(' ').getLength());
        assertEquals(3, DefaultFontInfo.getDefaultFontInfo(' ').getBoldLength());
        assertEquals(6, DefaultFontInfo.getDefaultFontInfo('A').getBoldLength());
    }

    @Test
    public void testComponentPixelWidth() {
        Component accept = Component.text("[ACCEPT]").decorate(TextDecoration.BOLD);
        int px = MessageUtils.getComponentPixelWidth(accept);
        assertTrue(px > 0, "Pixel width should be positive");
    }

    @Test
    public void testCenteringSpaces() {
        String spaces = MessageUtils.getCenteringSpaces(100);
        assertNotNull(spaces);
        assertTrue(spaces.length() > 0);
    }

    @Test
    public void testBuildCenteredButtonRow() {
        Component accept = Component.text("[ACEPTAR]").decorate(TextDecoration.BOLD);
        Component deny = Component.text("[RECHAZAR]").decorate(TextDecoration.BOLD);
        Component row = MessageUtils.buildCenteredButtonRow(accept, deny, 6);
        assertNotNull(row);
    }

    @Test
    public void testMiniMessageAndGradients() {
        Component comp = MessageUtils.toComponent("<gradient:#ff0000:#0000ff>Gradient Text</gradient>");
        assertNotNull(comp);

        String colored = MessageUtils.getColoredMessage("<gradient:#ff0000:#0000ff>Gradient Text</gradient>");
        assertTrue(colored.contains("§x"));

        Component rainbow = MessageUtils.toComponent("<rainbow>Rainbow Clan</rainbow>");
        assertNotNull(rainbow);

        String mixed = "&8[&9OnlyClans&8] &aHola <gradient:#FFD700:#FFA500>Usuario</gradient> &#00E5FF[10] &7!";
        Component mixedComp = MessageUtils.toComponent(mixed);
        assertNotNull(mixedComp);
        assertEquals("[OnlyClans] Hola Usuario [10] !", MessageUtils.stripColor(mixed));
    }

    @Test
    public void testMiniMessageCentering() {
        String tagMessage = "<gradient:#2F6AFA:#00E5FF><bold>CLAN LIST</bold></gradient>";
        int px = MessageUtils.getPixelWidth(tagMessage);
        assertTrue(px > 0 && px < 150);

        String centered = MessageUtils.getCenteredMessage(tagMessage);
        assertNotNull(centered);
        assertTrue(centered.startsWith(" "));
    }

    @Test
    public void testToComponentNoItalic() {
        Component normal = MessageUtils.toComponentNoItalic("<gradient:#00E5FF:#00FF88>Clan Menu</gradient>");
        assertEquals(TextDecoration.State.FALSE, normal.decoration(TextDecoration.ITALIC));
    }
}
