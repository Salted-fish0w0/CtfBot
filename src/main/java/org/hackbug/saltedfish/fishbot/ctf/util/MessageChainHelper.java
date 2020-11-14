package org.hackbug.saltedfish.fishbot.ctf.util;

import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class MessageChainHelper {
	public static MessageChain asMessageChain(String s) {
		MessageChainBuilder mcb = new MessageChainBuilder();
		mcb.add(s);
		return mcb.build();
	}
}
