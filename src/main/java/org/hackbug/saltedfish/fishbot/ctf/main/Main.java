package org.hackbug.saltedfish.fishbot.ctf.main;

import org.hackbug.saltedfish.fishbot.ctf.command.*;
import org.hackbug.saltedfish.fishbot.ctf.http.HttpServer;
import org.hackbug.saltedfish.fishbot.ctf.listener.MessageHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Events;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		Bot bot = BotHolder.getBot();

		bot.login();
		Events.registerEvents(bot, new MessageHandler());

		new HttpServer(BotHolder.getConfig().getInt("file-port")).start();

		CommandBootstrap.registerCommand(new CreatePuzzleCommand());
		CommandBootstrap.registerCommand(new EnterPuzzleCommand());
		CommandBootstrap.registerCommand(new RemoveContainerCommand());
		CommandBootstrap.registerCommand(new ExitPuzzleCommand());
		CommandBootstrap.registerCommand(new DeletePuzzleCommand());
		CommandBootstrap.registerCommand(new FlagSubmissionCommand());
		CommandBootstrap.registerCommand(new OpCommand());
		CommandBootstrap.registerCommand(new WriteupCommand());
		CommandBootstrap.registerCommand(new LogPuzzleCommand());
		CommandBootstrap.registerCommand(new ViewPuzzleCommand());
		CommandBootstrap.registerCommand(new HelpCommand());
	}
}
