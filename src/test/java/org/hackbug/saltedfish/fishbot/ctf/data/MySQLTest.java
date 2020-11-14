package org.hackbug.saltedfish.fishbot.ctf.data;

import org.hackbug.saltedfish.fishbot.ctf.configuration.JsonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySQLTest {
	private MySQL mySQL;
	@BeforeEach
	void setUp() {
		JsonConfiguration config = new JsonConfiguration();
		MySQL.initConfig(config);
		try {
			mySQL = new MySQL(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	void startTest() {

	}
}