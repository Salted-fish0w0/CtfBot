package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.puzzle.Puzzle;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class ViewPuzzleCommand implements CommandBase {
	private static final OptionParser parser = new OptionParser() {
		{
			acceptsAll(asList("title"))
					.withRequiredArg()
					.required()
					.ofType(String.class);
		}
	};

	@Override
	public String getName() {
		return "view_puzzle";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		OptionSet options;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("命令用法错误，用法：/view_puzzle -title <题目标题>"));
			return;
		}

		try {
			String title = options.valueOf("title").toString();
			Puzzle puzzle = BotHolder.getMysql().getPuzzle(title);

			if (puzzle == null) {
				messageCallback.accept(MessageChainHelper.asMessageChain("该题目不存在！"));
				return;
			}

			MessageChain chain = MessageChainHelper.asMessageChain("题目：").plus(puzzle.getTitle()).plus("\n");
			chain = chain.plus("类型：").plus(puzzle.getType()).plus("\n");
			if (puzzle.doUseDocker()) {
				chain = chain.plus("可能需要通过网络链接来解答").plus("\n");
			}
			if (puzzle.getUserRequiredFileName() != null) {
				chain = chain.plus("可能需要通过下载文件来解答").plus("\n");
			}
			messageCallback.accept(chain);
		} catch (Exception e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
			e.printStackTrace();
		}
	}
}
