package org.zkaleejoo.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    private static final int CENTER_PX = 154;
    private static final char COLOR_CHAR = '\u00A7';

    private static final Pattern SPIGOT_HEX_PATTERN = Pattern.compile("(?i)[&§]x([&§][0-9a-fA-F]){6}");
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)[&§]#([0-9a-fA-F]{6})");

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(COLOR_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final String RESET_DECORATIONS = "<!b><!i><!u><!st><!obf>";

    public static String legacyToMiniMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        Matcher spigotMatcher = SPIGOT_HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (spigotMatcher.find()) {
            String match = spigotMatcher.group();
            String hex = match.replaceAll("(?i)[&§x]", "");
            spigotMatcher.appendReplacement(sb, Matcher.quoteReplacement(RESET_DECORATIONS + "<#" + hex + ">"));
        }
        spigotMatcher.appendTail(sb);
        message = sb.toString();

        Matcher hexMatcher = HEX_PATTERN.matcher(message);
        sb = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            hexMatcher.appendReplacement(sb, Matcher.quoteReplacement(RESET_DECORATIONS + "<#" + hex + ">"));
        }
        hexMatcher.appendTail(sb);
        message = sb.toString();

        StringBuilder result = new StringBuilder();
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if ((c == '&' || c == '§') && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);
                String replacement = switch (code) {
                    case '0' -> RESET_DECORATIONS + "<black>";
                    case '1' -> RESET_DECORATIONS + "<dark_blue>";
                    case '2' -> RESET_DECORATIONS + "<dark_green>";
                    case '3' -> RESET_DECORATIONS + "<dark_aqua>";
                    case '4' -> RESET_DECORATIONS + "<dark_red>";
                    case '5' -> RESET_DECORATIONS + "<dark_purple>";
                    case '6' -> RESET_DECORATIONS + "<gold>";
                    case '7' -> RESET_DECORATIONS + "<gray>";
                    case '8' -> RESET_DECORATIONS + "<dark_gray>";
                    case '9' -> RESET_DECORATIONS + "<blue>";
                    case 'a' -> RESET_DECORATIONS + "<green>";
                    case 'b' -> RESET_DECORATIONS + "<aqua>";
                    case 'c' -> RESET_DECORATIONS + "<red>";
                    case 'd' -> RESET_DECORATIONS + "<light_purple>";
                    case 'e' -> RESET_DECORATIONS + "<yellow>";
                    case 'f' -> RESET_DECORATIONS + "<white>";
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
                    default -> null;
                };
                if (replacement != null) {
                    result.append(replacement);
                    i++;
                    continue;
                }
            }
            if (c != '§') {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static Component toComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        try {
            String converted = legacyToMiniMessage(message);
            return MINI_MESSAGE.deserialize(converted);
        } catch (Exception e) {
            try {
                return LEGACY_SERIALIZER.deserialize(message);
            } catch (Exception ex) {
                return Component.text(message);
            }
        }
    }

    public static Component toComponentNoItalic(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty().decoration(TextDecoration.ITALIC, false);
        }
        Component comp = toComponent(message);
        return Component.empty().decoration(TextDecoration.ITALIC, false).append(comp);
    }

    public static Component legacyToComponentNoItalic(String message) {
        return toComponentNoItalic(message);
    }

    public static String getColoredMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(toComponent(message));
    }

    public static String toLegacy(String message) {
        return getColoredMessage(message);
    }

    public static String toMiniMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return MINI_MESSAGE.serialize(toComponent(message));
    }

    public static String toMiniMessage(Component component) {
        if (component == null) {
            return "";
        }
        return MINI_MESSAGE.serialize(component);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        sender.sendMessage(toComponent(message));
    }

    public static void sendMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        player.sendMessage(toComponent(message));
    }

    public static void broadcast(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        Bukkit.broadcast(toComponent(message));
    }

    public static void broadcastToPlayersOnly(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        Component component = toComponent(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null) {
                player.sendMessage(component);
            }
        }
    }

    public static String stripColor(String message) {
        if (message == null) {
            return null;
        }
        return PlainTextComponentSerializer.plainText().serialize(toComponent(message));
    }

    public static int getPixelWidth(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        return getComponentPixelWidth(toComponent(message));
    }

    public static int getComponentPixelWidth(Component component) {
        if (component == null) {
            return 0;
        }
        return calculatePixelWidth(component, false);
    }

    private static int calculatePixelWidth(Component component, boolean parentBold) {
        boolean isBold = component.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE
                || (parentBold && component.decoration(TextDecoration.BOLD) != TextDecoration.State.FALSE);

        int px = 0;
        if (component instanceof TextComponent textComponent) {
            String text = textComponent.content();
            for (char c : text.toCharArray()) {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                px += isBold ? dFI.getBoldLength() : dFI.getLength();
                px++;
            }
        }

        for (Component child : component.children()) {
            px += calculatePixelWidth(child, isBold);
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
        if (message == null || message.isEmpty()) {
            return "";
        }
        int messagePxSize = getPixelWidth(message);
        return getCenteringSpaces(messagePxSize) + getColoredMessage(message);
    }

    public static Component getCenteredComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        Component comp = toComponent(message);
        int messagePxSize = getComponentPixelWidth(comp);
        return Component.text(getCenteringSpaces(messagePxSize)).append(comp);
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
        if (player == null || button1 == null || button2 == null) {
            return;
        }
        player.sendMessage(buildCenteredButtonRow(button1, button2, 6));
    }
}
