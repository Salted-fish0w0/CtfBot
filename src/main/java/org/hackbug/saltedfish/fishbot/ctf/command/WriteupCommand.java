package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.UUID;
import java.util.function.Consumer;

public class WriteupCommand implements CommandBase {
	@Override
	public String getName() {
		return "submit_writeup";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		try {
			CtfUser cu = BotHolder.getMysql().getUser(user.getId());
			if (cu == null) {
				BotHolder.getMysql().registerUser(new CtfUser(0, user.getId(), false, null, 0, UUID.randomUUID()));
				cu = BotHolder.getMysql().getUser(user.getId());
			}

			String host = BotHolder.getConfig().getString("host");
			int httpPort = BotHolder.getConfig().getInt("file-port");
			messageCallback.accept(MessageChainHelper.asMessageChain("请到")
					.plus("http://" + host + ":" + httpPort + "/?id=" + cu.getFileUuid())
					.plus("上传你的Writeup"));
		} catch (Exception e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
			e.printStackTrace();
		}
	}

	@Override
	public boolean availableOnGroup() {
		return false;
	}
}
