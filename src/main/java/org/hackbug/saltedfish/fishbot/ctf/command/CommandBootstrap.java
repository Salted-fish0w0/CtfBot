package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class CommandBootstrap {
	private static final Map<String, CommandBase> commandMap = new HashMap<>();

	public static void registerCommand(CommandBase command) {
		commandMap.put(command.getName(), command);
	}

	public static void executeCommand(User user, String command, Consumer<MessageChain> messageCallback, boolean fromGroup) {
		try {
			CtfUser cu = BotHolder.getMysql().getUser(user.getId());
			if (cu == null) {
				BotHolder.getMysql().registerUser(new CtfUser(0, user.getId(), false, null, 0, UUID.randomUUID()));
				cu = BotHolder.getMysql().getUser(user.getId());
			}

			String[] slices = command.split(" ");
			String[] args = new String[slices.length - 1];
			System.arraycopy(slices, 1, args, 0, args.length);
			if (commandMap.containsKey(slices[0])) {
				CommandBase cb = commandMap.get(slices[0]);
				if ((!cb.availableOnGroup()) && fromGroup) {
					messageCallback.accept(MessageChainHelper.asMessageChain("这条指令只能在私聊执行！"));
					return;
				}

				if (cb.requiresOp() && (!cu.isOperator())) {
					messageCallback.accept(MessageChainHelper.asMessageChain("你没有执行这条命令的权力！"));
					return;
				}
				cb.execute(user, args, messageCallback);
			}
		} catch (Exception e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("命令出错了，请通知管理员"));
			e.printStackTrace();
		}
	}
}
