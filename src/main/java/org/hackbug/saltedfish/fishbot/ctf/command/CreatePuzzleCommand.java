package org.hackbug.saltedfish.fishbot.ctf.command;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.puzzle.Puzzle;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class CreatePuzzleCommand implements CommandBase {
	private static final OptionParser parser = new OptionParser() {
		{
			acceptsAll(asList("name", "n"))
					.withRequiredArg()
					.required()
					.ofType(String.class);
			acceptsAll(asList("description", "d"))
					.withRequiredArg()
					.ofType(String.class)
					.defaultsTo("");
			acceptsAll(asList("type", "t"))
					.withRequiredArg()
					.required()
					.ofType(String.class);
			acceptsAll(asList("templateFile"))
					.withRequiredArg()
					.ofType(String.class);
			acceptsAll(asList("templateScript"))
					.withRequiredArg()
					.ofType(String.class);
			acceptsAll(asList("userFile"))
					.withRequiredArg()
					.ofType(String.class);
			acceptsAll(asList("userScript"))
					.withRequiredArg()
					.ofType(String.class);
			acceptsAll(asList("docker", "dk"))
					.withRequiredArg()
					.ofType(boolean.class)
					.defaultsTo(false);
			acceptsAll(asList("image", "img"))
					.withRequiredArg()
					.ofType(String.class);
		}
	};

	@Override
	public String getName() {
		return "create_puzzle";
	}

	@Override
	public void execute(User user, String[] args, Consumer<MessageChain> messageCallback) {
		OptionSet options;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("命令用法错误，用法：/create_puzzle -name <名字> -type <类型> [-description <描述>] [-templateFile <服务器的模板文件> [-templateScript <服务器的初始化脚本>]] [-userFile <用户文件> [-userScript <用户文件初始化脚本>]] [-docker <true|false> -image <镜像文件>]"));
			return;
		}

		try {
			if (BotHolder.getMysql().getPuzzle(options.valueOf("name").toString()) != null) {
				messageCallback.accept(MessageChainHelper.asMessageChain("名字为" + options.valueOf("name") + "的题目已经存在了 >_> 考虑换个名字吧"));
			}

			BotHolder.getMysql().putPuzzle(new Puzzle(options.valueOf("name").toString(), options.valueOf("type").toString(), options.valueOf("description").toString(), (String) options.valueOf("templateFile"), (String) options.valueOf("templateScript"), (String) options.valueOf("userFile"), (String) options.valueOf("userScript"), (Boolean) options.valueOf("docker"), (String) options.valueOf("image")));
			messageCallback.accept(MessageChainHelper.asMessageChain("题目" + options.valueOf("name") + "创建成功！"));
		} catch (FileNotFoundException e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("对不起！文件" + e.getMessage() + "不存在！"));
		} catch (Exception e) {
			messageCallback.accept(MessageChainHelper.asMessageChain("我们遇见了一些八阿哥，请通知管理员并修复！"));
			e.printStackTrace();
		}
	}

	@Override
	public boolean requiresOp() {
		return true;
	}
}
