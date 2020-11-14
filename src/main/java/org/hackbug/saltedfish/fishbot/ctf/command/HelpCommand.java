package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.UUID;
import java.util.function.Consumer;

public class HelpCommand implements CommandBase {
	@Override
	public String getName() {
		return "help";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		StringBuilder sb = new StringBuilder();
		try {
			CtfUser cu = BotHolder.getMysql().getUser(user.getId());
			if (cu == null) {
				BotHolder.getMysql().registerUser(new CtfUser(0, user.getId(), false, null, 0, UUID.randomUUID()));
				cu = BotHolder.getMysql().getUser(user.getId());
			}

			if (cu.isOperator()) {
				sb.append("/create_puzzle 创建题目\n");
				sb.append("/delete_puzzle 删除题目\n");
				sb.append("/log_puzzle 获取答题记录\n");
				sb.append("/set_admin 设置管理员权限\n");
			}
		} catch (Exception e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
			e.printStackTrace();
		}

		sb.append("/enter_puzzle 开始答题\n");
		sb.append("/exit_puzzle 放弃答题，并不会产生答题记录\n");
		sb.append("/view_puzzle 查看题目信息\n");
		sb.append("/check_flag 提交flag并结束答题\n");
		sb.append("/submit_writeup 上传writeup\n");
		sb.append("/remove_container 移除你答题时创建的容器\n");
		sb.append("/help 查阅帮助手册，对，就是你盯着的这个");

		messageCallback.accept(MessageChainHelper.asMessageChain(sb.toString()));

		messageCallback.accept(MessageChainHelper.asMessageChain("注：你需要通过执行它们以获取详细的用法，方括号内的参数为可选参数，尖括号内的参数或没有被括号所包含的参数为必选参数，方括号中间套娃尖括号或没有括号代表这几个参数必须同时存在"));
		messageCallback.accept(MessageChainHelper.asMessageChain("样例：[-a <b>] -c <d> 代表参数a可选，但a存在时必须填写b，c必须存在且必须填写d"));
	}
}
