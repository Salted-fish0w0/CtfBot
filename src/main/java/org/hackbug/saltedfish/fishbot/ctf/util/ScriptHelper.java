package org.hackbug.saltedfish.fishbot.ctf.util;

import java.io.*;

public class ScriptHelper {
	public static void convertScript(File in, File out, int port, String flag) throws IOException {
		String content;
		try (BufferedReader br = new BufferedReader(new FileReader(in))) {
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s).append(System.getProperty("line.separator"));
			}
			content = sb.toString();
		}
		content = content.replaceAll("%FAKE_FLAG%", FlagHelper.generateFakeFlag());
		content = content.replaceAll("%FLAG%", flag);
		content = content.replaceAll("%PORT%", String.valueOf(port));
		try (FileWriter fw = new FileWriter(out)) {
			fw.write(content);
		}
	}
	public static void executeScript(File script, File directory) throws IOException {
		String targetToExecute = "";
		try (BufferedReader br = new BufferedReader(new FileReader(script))) {
			String s;
			while ((s = br.readLine()) != null) {
				if (s.isEmpty()) {
					continue;
				}
				targetToExecute = s;
				break;
			}
		}
		// remove #!
		if (!targetToExecute.startsWith("#!")) {
			return;
		}
		String process = targetToExecute.substring(2);
		ProcessBuilder pb = new ProcessBuilder(process, script.getCanonicalPath()).directory(directory);
		Process p = pb.start();
		while (true) {
			try {
				p.waitFor();
			} catch (Exception e) {
				continue;
			}
			return;
		}
	}
}
