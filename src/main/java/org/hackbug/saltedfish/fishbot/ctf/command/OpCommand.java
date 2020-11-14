package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class OpCommand implements CommandBase {
	private static final OptionParser parser = new OptionParser() {
		{
			acceptsAll(asList("user", "u"))
					.withRequiredArg()
					.required()
					.ofType(long.class);
			acceptsAll(asList("admin", "a"))
					.withRequiredArg()
					.required()
					.ofType(boolean.class);
		}
	};

	@Override
	public String getName() {
		return "set_admin";
	}

	@Override
	public boolean requiresOp() {
		return true;
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		OptionSet options;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("命令用法错误，用法：/set_admin -user <用户id> -admin <true|false>"));
			return;
		}

		try {
			long targetUser = (long) options.valueOf("user");
			boolean admin = (boolean) options.valueOf("admin");
			BotHolder.getMysql().setOperator(targetUser, admin);
			if (admin) {
				messageCallback.accept(MessageChainHelper.asMessageChain("已将")
						.plus(String.valueOf(targetUser))
						.plus("设为管理员"));
			} else {
				messageCallback.accept(MessageChainHelper.asMessageChain("夺去")
						.plus(String.valueOf(targetUser))
						.plus("的管理员权限"));
			}
		} catch (Exception e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
			e.printStackTrace();
		}
	}
}
