package org.hackbug.saltedfish.fishbot.ctf.main;

import org.hackbug.saltedfish.fishbot.ctf.configuration.JsonConfiguration;
import org.hackbug.saltedfish.fishbot.ctf.data.MySQL;
import org.hackbug.saltedfish.fishbot.ctf.docker.DockerManager;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactoryJvm;
import net.mamoe.mirai.utils.BotConfiguration;

import java.io.File;
import java.io.IOException;

public class BotHolder {
	private static final Bot bot = BotFactoryJvm.newBot(1994780494, new byte[] {
			0x57, (byte) 0xe0, 0x06, 0x46, (byte) 0xdc, 0x14, 0x28, (byte) 0xe0, 0x68, 0x3c, (byte) 0xee, 0x7c, (byte) 0xbd, (byte) 0x8a, (byte) 0x98, (byte) 0xdc
	}, new BotConfiguration() {
		{
			fileBasedDeviceInfo("deviceinfo.json");
		}
	});
	private static final JsonConfiguration config;
	private static final MySQL mysql;
	private static final DockerManager docker = new DockerManager();

	public static Bot getBot() {
		return bot;
	}

	public static MySQL getMysql() {
		return mysql;
	}

	public static DockerManager getDocker() {
		return docker;
	}

	public static JsonConfiguration getConfig() {
		return config;
	}

	static {
		File configFile = new File("config.json");
		boolean first = false;
		if (!configFile.exists()) {
			// it seems that it is the first time to start.
			first = true;
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			config = new JsonConfiguration(configFile);
			if (first) {
				MySQL.initConfig(config.createSection("mysql"));
				config.set("port-min", 25565);
				config.set("port-max", 65535);
				config.set("host", "127.0.0.1");
				config.set("file-port", 12345);
				config.set("file-upload-folder", "/home/lucky-fish/upload");
				config.saveTo(configFile);
			}

			File uploadFolder = new File(config.getString("file-upload-folder"));
			if (!uploadFolder.exists()) {
				uploadFolder.mkdirs();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			mysql = new MySQL(config.getSection("mysql"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
