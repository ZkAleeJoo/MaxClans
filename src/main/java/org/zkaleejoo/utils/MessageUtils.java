package org.zkaleejoo.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

public class MessageUtils {

    private static final int CENTER_PX = 154;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)&([0-9A-FK-OR])");
    private static final char COLOR_CHAR = '\u00A7';
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(COLOR_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static String getColoredMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(toLegacyHexColor(color)));
        }
        message = matcher.appendTail(buffer).toString();

        return LEGACY_COLOR_PATTERN.matcher(message).replaceAll(COLOR_CHAR + "$1");
    }

    public static void broadcastToPlayersOnly(String message) {
        if (message == null || message.isEmpty())
            return;
        String coloredMessage = getColoredMessage(message);
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (player != null) {
                player.sendMessage(coloredMessage);
            }
        }
    }

    public static String stripColor(String message) {
        if (message == null)
            return null;
        return PlainTextComponentSerializer.plainText()
                .serialize(Objects.requireNonNull(toComponent(message)));
    }

    public static Component legacyToComponentNoItalic(String message) {
        return toComponent(message)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static Component toComponent(String message) {
        return LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(getColoredMessage(message)));
    }

    private static String toLegacyHexColor(String color) {
        StringBuilder builder = new StringBuilder(14);
        builder.append(COLOR_CHAR).append('x');
        for (char character : color.toCharArray()) {
            builder.append(COLOR_CHAR).append(character);
        }
        return builder.toString();
    }

    public static int getPixelWidth(String message) {
        if (message == null || message.isEmpty())
            return 0;
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == COLOR_CHAR || c == '&') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = (c == 'l' || c == 'L');
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        return messagePxSize;
    }

    public static int getComponentPixelWidth(Component component) {
        if (component == null)
            return 0;
        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        boolean isBold = component.hasDecoration(TextDecoration.BOLD);
        int px = 0;
        for (char c : plain.toCharArray()) {
            DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
            px += isBold ? dFI.getBoldLength() : dFI.getLength();
            px++;
        }
        return px;
    }

    public static String getCenteringSpaces(int totalPixelWidth) {
        int halvedMessageSize = totalPixelWidth / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb.toString();
    }

    public static String getCenteredMessage(String message) {
        if (message == null || message.isEmpty())
            return "";
        message = getColoredMessage(message);
        int messagePxSize = getPixelWidth(message);
        return getCenteringSpaces(messagePxSize) + message;
    }

    public static Component buildCenteredButtonRow(Component button1, Component button2, int gapSpaces) {
        int width1 = getComponentPixelWidth(button1);
        int width2 = getComponentPixelWidth(button2);
        int gapWidth = gapSpaces * (DefaultFontInfo.SPACE.getLength() + 1);
        int totalWidth = width1 + gapWidth + width2;
        String leadingSpaces = getCenteringSpaces(totalWidth);
        String gap = " ".repeat(Math.max(1, gapSpaces));
        return Component.text(leadingSpaces)
                .append(button1)
                .append(Component.text(gap))
                .append(button2);
    }

    public static void sendCenteredButtons(Player player, Component button1, Component button2) {
        if (player == null || button1 == null || button2 == null)
            return;
        player.sendMessage(buildCenteredButtonRow(button1, button2, 6));
    }

}
