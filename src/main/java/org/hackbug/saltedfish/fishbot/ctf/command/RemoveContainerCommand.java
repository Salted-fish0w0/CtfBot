package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.UUID;
import java.util.function.Consumer;

public class RemoveContainerCommand implements CommandBase {
	@Override
	public String getName() {
		return "remove_container";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		try {
			CtfUser cu = BotHolder.getMysql().getUser(user.getId());
			if (cu == null) {
				BotHolder.getMysql().registerUser(new CtfUser(0, user.getId(), false, null, 0, UUID.randomUUID()));
				cu = BotHolder.getMysql().getUser(user.getId());
			}

			String containerId = BotHolder.getMysql().getContainer(cu.getId());
			if (containerId == null) {
				messageCallback.accept(MessageChainHelper.asMessageChain("然而你没有容器需要删除"));
			} else {
				BotHolder.getMysql().dropContainer(cu.getId());
				BotHolder.getDocker().stopContainer(containerId);
				BotHolder.getDocker().destroyContainer(containerId);
				messageCallback.accept(MessageChainHelper.asMessageChain("容器删除完成！"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
		}
	}
}
