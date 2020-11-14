package org.hackbug.saltedfish.fishbot.ctf.util;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;

public class SecurityHelper {
	private static final long[] availableGroup = new long[] {
			763416094L, 902722826L
	};

	public static boolean isInAvailableGroup(User user) {
		Bot bot = BotHolder.getBot();
		for (long id : availableGroup) {
			Group g = bot.getGroup(id);
			if (!g.contains(user.getId())) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLegalGroup(long qq) {
		for (long id : availableGroup) {
			if (id == qq) {
				return true;
			}
		}
		return false;
	}
}
