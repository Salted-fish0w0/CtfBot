package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.puzzle.Puzzle;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class EnterPuzzleCommand implements CommandBase {
	private static final OptionParser parser = new OptionParser() {
		{
			acceptsAll(asList("title"))
					.withRequiredArg()
					.required()
					.ofType(String.class);
		}
	};
	private static final Random portSelector = new Random();

	@Override
	public String getName() {
		return "enter_puzzle";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		OptionSet options;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("命令用法错误，用法：/enter_puzzle -title <题目标题>"));
			return;
		}

		try {
			String title = options.valueOf("title").toString();
			Puzzle puzzle = BotHolder.getMysql().getPuzzle(title);

			if (puzzle == null) {
				messageCallback.accept(MessageChainHelper.asMessageChain("该题目不存在！"));
				return;
			}

			int port = Math.abs(portSelector.nextInt(BotHolder.getConfig().getInt("port-max") - BotHolder.getConfig().getInt("port-min"))) + BotHolder.getConfig().getInt("port-min");
			CtfUser cu = BotHolder.getMysql().getUser(user.getId());
			if (cu == null) {
				BotHolder.getMysql().registerUser(new CtfUser(0, user.getId(), false, null, 0, UUID.randomUUID()));
				cu = BotHolder.getMysql().getUser(user.getId());
			}

			if (cu.getSolvingPuzzle() != null) {
				messageCallback.accept(MessageChainHelper.asMessageChain("你已经在答另外一道题了，如果想要放弃请使用/exit_puzzle命令"));
				return;
			}

			messageCallback.accept(MessageChainHelper.asMessageChain("你选择了题目：")
					.plus(puzzle.getTitle())
					.plus("描述：").plus(puzzle.getDescription()));

			if (puzzle.doUseDocker() && BotHolder.getMysql().getContainer(cu.getId()) != null) {
				messageCallback.accept(MessageChainHelper.asMessageChain("你需要创建一个新的容器以答题，但你已经有一个容器了，建议使用/remove_container命令以删除原来的容器，当然这也可能是一个bug，请及时联系管理员"));
				return;
			}
			String host = BotHolder.getConfig().getString("host");
			BotHolder.getMysql().startSolving(cu, puzzle.getTitle());

			messageCallback.accept(MessageChainHelper.asMessageChain("正在噜一个答题环境，请稍后..."));
			puzzle.buildEnvironment(cu, port, uuid -> messageCallback.accept(MessageChainHelper.asMessageChain("请从 http://" + host + ":" + BotHolder.getConfig().getInt("file-port") + "/download?id=" + uuid + " 处获取答题文件")
					.plus("\n注：文件下载是一次性的")));
			if (puzzle.doUseDocker()) {
				messageCallback.accept(MessageChainHelper.asMessageChain("答题地址：" + host + ":" + port));
			}
			messageCallback.accept(MessageChainHelper.asMessageChain("答题环境噜完了，可以开始答题了"));
		} catch (Exception e) {
			e.printStackTrace();
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
		}
	}
}
