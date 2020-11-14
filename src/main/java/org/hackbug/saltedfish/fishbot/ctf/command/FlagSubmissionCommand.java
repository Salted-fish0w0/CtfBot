package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class FlagSubmissionCommand implements CommandBase {
	private static final OptionParser parser = new OptionParser() {
		{
			acceptsAll(asList("flag"))
					.withRequiredArg()
					.required()
					.ofType(String.class);
		}
	};

	@Override
	public String getName() {
		return "check_flag";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		OptionSet options;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("命令用法错误，用法：/check_flag -flag <获得的flag>"));
			return;
		}

		try {
			String flag = options.valueOf("flag").toString();

			CtfUser cu = BotHolder.getMysql().getUser(user.getId());
			if (cu == null) {
				BotHolder.getMysql().registerUser(new CtfUser(0, user.getId(), false, null, 0, UUID.randomUUID()));
				cu = BotHolder.getMysql().getUser(user.getId());
			}

			if (BotHolder.getMysql().checkFlag(cu.getId(), flag)) {
				messageCallback.accept(MessageChainHelper.asMessageChain("你做出了这道题！用时：")
						.plus(String.valueOf((System.currentTimeMillis() - cu.getStartTime()) / 1000))
						.plus("秒"));
				messageCallback.accept(MessageChainHelper.asMessageChain("请考虑使用/submit_writeup命令以提交writeup"));
				BotHolder.getMysql().finishSolving(cu);
				ExitPuzzleCommand.cleanupPuzzles(cu);
			} else {
				messageCallback.accept(MessageChainHelper.asMessageChain("Flag无效"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
		}
	}
}
