package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.UUID;
import java.util.function.Consumer;

public class ExitPuzzleCommand implements CommandBase {
	static void cleanupPuzzles(CtfUser cu) throws Exception {
		String containerId = BotHolder.getMysql().getContainer(cu.getId());
		BotHolder.getMysql().dropFlag(cu.getId());
		if (containerId != null) {
			BotHolder.getMysql().dropContainer(cu.getId());
			BotHolder.getDocker().stopContainer(containerId);
			BotHolder.getDocker().destroyContainer(containerId);
		}
	}

	@Override
	public String getName() {
		return "exit_puzzle";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		try {
			CtfUser cu = BotHolder.getMysql().getUser(user.getId());
			if (cu == null) {
				BotHolder.getMysql().registerUser(new CtfUser(0, user.getId(), false, null, 0, UUID.randomUUID()));
				cu = BotHolder.getMysql().getUser(user.getId());
			}

			if (cu.getSolvingPuzzle() == null) {
				messageCallback.accept(MessageChainHelper.asMessageChain("你并没有在答题"));
			} else {
				BotHolder.getMysql().endSolving(cu);
				cleanupPuzzles(cu);
				messageCallback.accept(MessageChainHelper.asMessageChain("题目清理完成，你在这道题目上使用了" + ((System.currentTimeMillis() - cu.getStartTime()) / 1000) + "秒"));
			}
		} catch (Exception e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
			e.printStackTrace();
		}
	}
}
