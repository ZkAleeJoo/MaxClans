package org.zkaleejoo;

import org.junit.jupiter.api.Test;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.models.MemberSortType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MemberStatsAndSortTest {

    @Test
    public void testMemberSortTypeCycling() {
        assertEquals(MemberSortType.KDR, MemberSortType.ROLE.next());
        assertEquals(MemberSortType.PLAYTIME, MemberSortType.KDR.next());
        assertEquals(MemberSortType.JOIN_RECENT, MemberSortType.PLAYTIME.next());
        assertEquals(MemberSortType.JOIN_OLDEST, MemberSortType.JOIN_RECENT.next());
        assertEquals(MemberSortType.ROLE, MemberSortType.JOIN_OLDEST.next());

        assertEquals(MemberSortType.JOIN_OLDEST, MemberSortType.ROLE.previous());
        assertEquals(MemberSortType.JOIN_RECENT, MemberSortType.JOIN_OLDEST.previous());
    }

    @Test
    public void testKDRCalculation() {
        assertEquals("0.00", formatKDR(0, 0));
        assertEquals("5.00", formatKDR(5, 0));
        assertEquals("2.50", formatKDR(5, 2));
        assertEquals("0.33", formatKDR(1, 3));
    }

    private String formatKDR(int kills, int deaths) {
        double kdr = (deaths <= 0) ? (double) kills : (double) kills / deaths;
        return String.format(Locale.US, "%.2f", kdr);
    }

    @Test
    public void testPlaytimeFormatting() {
        assertEquals("0m", formatPlaytime(0));
        assertEquals("1m", formatPlaytime(20 * 60)); // 60s = 1m
        assertEquals("1h 30m", formatPlaytime(20 * 90 * 60)); // 90 min = 1h 30m
        assertEquals("2d 4h 15m", formatPlaytime(20L * (2 * 86400 + 4 * 3600 + 15 * 60)));
    }

    private String formatPlaytime(long ticks) {
        long seconds = ticks / 20L;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return Math.max(0, minutes) + "m";
    }

    @Test
    public void testClanPlayerJoinedAt() {
        UUID uuid = UUID.randomUUID();
        long customTime = 1700000000000L;
        ClanPlayer cp = new ClanPlayer(uuid, "TestClan", ClanRole.MEMBER, customTime);

        assertEquals(uuid, cp.getUuid());
        assertEquals("TestClan", cp.getClanName());
        assertEquals(ClanRole.MEMBER, cp.getRole());
        assertEquals(customTime, cp.getJoinedAt());
    }

    @Test
    public void testRoleSorting() {
        List<ClanPlayer> members = new ArrayList<>();
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        UUID u3 = UUID.randomUUID();

        ClanPlayer m1 = new ClanPlayer(u1, "Clan", ClanRole.MEMBER, 100);
        ClanPlayer m2 = new ClanPlayer(u2, "Clan", ClanRole.LEADER, 100);
        ClanPlayer m3 = new ClanPlayer(u3, "Clan", ClanRole.MODERATOR, 100);

        members.add(m1);
        members.add(m2);
        members.add(m3);

        members.sort((a, b) -> Integer.compare(b.getRole().getWeight(), a.getRole().getWeight()));

        assertEquals(ClanRole.LEADER, members.get(0).getRole());
        assertEquals(ClanRole.MODERATOR, members.get(1).getRole());
        assertEquals(ClanRole.MEMBER, members.get(2).getRole());
    }

    @Test
    public void testJoinedDateSorting() {
        List<ClanPlayer> members = new ArrayList<>();
        ClanPlayer oldMember = new ClanPlayer(UUID.randomUUID(), "Clan", ClanRole.MEMBER, 1000L);
        ClanPlayer newMember = new ClanPlayer(UUID.randomUUID(), "Clan", ClanRole.MEMBER, 5000L);

        members.add(oldMember);
        members.add(newMember);

        // Sort Recent first
        members.sort((a, b) -> Long.compare(b.getJoinedAt(), a.getJoinedAt()));
        assertEquals(5000L, members.get(0).getJoinedAt());
        assertEquals(1000L, members.get(1).getJoinedAt());

        // Sort Oldest first
        members.sort((a, b) -> Long.compare(a.getJoinedAt(), b.getJoinedAt()));
        assertEquals(1000L, members.get(0).getJoinedAt());
        assertEquals(5000L, members.get(1).getJoinedAt());
    }
}
