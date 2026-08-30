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
}
