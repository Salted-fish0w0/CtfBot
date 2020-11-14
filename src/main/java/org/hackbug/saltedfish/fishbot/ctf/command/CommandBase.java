package org.hackbug.saltedfish.fishbot.ctf.command;

import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.function.Consumer;

public interface CommandBase {
	String getName();
	default boolean requiresOp() {
		return false;
	}

	void execute(User user, String[] args, Consumer<MessageChain> messageCallback);
	default boolean availableOnGroup() {
		return true;
	}
}
