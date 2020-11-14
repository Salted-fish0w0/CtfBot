package org.hackbug.saltedfish.fishbot.ctf.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonConfigurationTest {
	private File config;
	private JsonConfiguration jc;
	@org.junit.jupiter.api.BeforeEach
	void setUp() throws IOException {
		config = new File("config.json");
		if (!config.exists()) {
			config.createNewFile();
		}
		jc = new JsonConfiguration(config);
	}

	@org.junit.jupiter.api.Test
	void get() {
		System.out.println(jc.getString("a"));
		System.out.println(jc.getBoolean("c"));
		System.out.println(jc.getInt("d"));
		JsonConfiguration jc2 = jc.getSection("owo");
		System.out.println(jc2.getDouble("e"));
		System.out.println(jc2.getList("f"));
	}

	@org.junit.jupiter.api.Test
	void saveTo() throws IOException {
		jc.set("a", "b");
		jc.set("c", true);
		jc.set("d", 114514);
		JsonConfiguration jc2 = jc.createSection("owo");
		jc2.set("e", 123d);
		List<String> w = new ArrayList<>();
		w.add("q");
		w.add("w");
		w.add("q");
		jc2.set("f", w);
		jc.saveTo(config);
	}
}