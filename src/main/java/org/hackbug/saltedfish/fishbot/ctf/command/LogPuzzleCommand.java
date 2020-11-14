package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.puzzle.PuzzleLog;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class LogPuzzleCommand implements CommandBase {
	private static final OptionParser parser = new OptionParser() {
		{
			acceptsAll(asList("title", "t"))
					.withRequiredArg()
					.required()
					.ofType(String.class);
		}
	};

	@Override
	public String getName() {
		return "log_puzzle";
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
			messageCallback.accept(MessageChainHelper.asMessageChain("命令用法错误，用法：/puzzle_log -title <题目>"));
			return;
		}

		try {
			List<PuzzleLog> logs = BotHolder.getMysql().getPuzzleLogByTitle(options.valueOf("title").toString());
			messageCallback.accept(MessageChainHelper.asMessageChain("刷屏预警！"));
			new Thread(() -> {
				try {
					int countdown = 3;
					while (countdown-- > 0) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
						messageCallback.accept(MessageChainHelper.asMessageChain("" + countdown));
					}

					messageCallback.accept(MessageChainHelper.asMessageChain("用户QQ - 题目 - 耗时 - 完成时间"));

					StringBuilder sb = new StringBuilder();
					for (PuzzleLog puzzleLog : logs) {
						if (sb.length() > 50) {
							messageCallback.accept(MessageChainHelper.asMessageChain(sb.toString()));
							sb = new StringBuilder();
						}
						sb.append(puzzleLog.toString()).append("\n");
					}

					if (sb.toString().isEmpty()) {
						messageCallback.accept(MessageChainHelper.asMessageChain("什么都没有"));
					} else {
						messageCallback.accept(MessageChainHelper.asMessageChain(sb.toString()));
					}
				} catch (Exception e) {
					e.printStackTrace();
					messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
		}
	}
}
