package org.hackbug.saltedfish.fishbot.ctf.puzzle;

import org.hackbug.saltedfish.fishbot.ctf.docker.DockerManager;
import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.ArchiveHelper;
import org.hackbug.saltedfish.fishbot.ctf.util.FlagHelper;
import org.hackbug.saltedfish.fishbot.ctf.util.ScriptHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;
import java.util.function.Consumer;

public class Puzzle {
	private final String title;
	private final String type;
	private final String description;
	private final File fileToArchive;
	private final File initializeScriptFile;
	private final String userRequiredFileName;
	private final File userFileModificationScriptFile;
	private final boolean useDocker;
	private final String dockerImage;

	public Puzzle(String title, String type, String description, String fileToArchive, String initializeScriptFile, String userRequiredFileName, String userFileModificationScriptFile, boolean useDocker, String dockerImage) throws FileNotFoundException {
		this.title = title;
		this.type = type;
		this.description = description;
		this.userRequiredFileName = userRequiredFileName;
		if (userFileModificationScriptFile != null) {
			this.userFileModificationScriptFile = new File(userFileModificationScriptFile);
			if (!this.userFileModificationScriptFile.exists()) {
				throw new FileNotFoundException(userFileModificationScriptFile);
			}
		} else {
			this.userFileModificationScriptFile = null;
		}
		if (fileToArchive != null) {
			this.fileToArchive = new File(fileToArchive);
			if (!this.fileToArchive.exists()) {
				throw new FileNotFoundException(fileToArchive);
			}
		} else {
			this.fileToArchive = null;
		}
		if (initializeScriptFile != null) {
			this.initializeScriptFile = new File(initializeScriptFile);

			if (!this.initializeScriptFile.exists()) {
				throw new FileNotFoundException(initializeScriptFile);
			}
		} else {
			this.initializeScriptFile = null;
		}

		if (useDocker) {
			this.useDocker = true;
			this.dockerImage = dockerImage;
		} else {
			this.useDocker = false;
			this.dockerImage = null;
		}
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public File getFileToArchive() {
		return fileToArchive;
	}

	public File getInitializeScriptFile() {
		return initializeScriptFile;
	}

	public String getUserRequiredFileName() {
		return userRequiredFileName;
	}

	public File getUserFileModificationScriptFile() {
		return userFileModificationScriptFile;
	}

	public boolean doUseDocker() {
		return useDocker;
	}

	public String getDockerImage() {
		return dockerImage;
	}

	public void buildEnvironment(CtfUser who, int port, Consumer<UUID> fileDownload) throws Exception {
		String flag = FlagHelper.generateRandomFlag();
		BotHolder.getMysql().putFlag(who, flag);

		if (fileToArchive != null) {
			File archive = new File(who.getQq() + "_" + getTitle() + ".tar");
			ArchiveHelper.buildTar(fileToArchive, archive);

			DockerManager dm = BotHolder.getDocker();
			String dockerName = null;
			if (useDocker) {
				dockerName = dm.buildContainer(dockerImage, port);
				dm.startContainer(dockerName);
				BotHolder.getMysql().putContainer(dockerName, this.title, who.getId());
				dm.uploadArchive(dockerName, archive);
			}

			if (initializeScriptFile != null) {
				String name = who.getQq() + initializeScriptFile.getName();
				// assuming all of the script file is modifiable.
				File scriptArchive = new File(who.getQq() + "_initScript.tar");
				File tempScriptFile = new File(name);
				if (!tempScriptFile.exists()) {
					tempScriptFile.createNewFile();
				}
				ScriptHelper.convertScript(initializeScriptFile, tempScriptFile, port, flag);
				ArchiveHelper.buildTar(tempScriptFile, scriptArchive);
				if (useDocker) {
					dm.uploadArchive(dockerName, scriptArchive);
					dm.executeCommand(dockerName, "/" + name + "/" + name);
				}
				tempScriptFile.delete();
				scriptArchive.delete();
			}
			archive.delete();
		}

		if (userFileModificationScriptFile != null) {
			File tempScriptFile = new File(who.getQq() + "_" + getTitle() + "_user_tempScript");
			if (!tempScriptFile.exists()) {
				tempScriptFile.createNewFile();
			}
			ScriptHelper.convertScript(userFileModificationScriptFile, tempScriptFile, port, flag);
			ScriptHelper.executeScript(tempScriptFile, userFileModificationScriptFile.getAbsoluteFile().getParentFile());
			tempScriptFile.delete();
		}
		if (userRequiredFileName != null) {
			// might be impossible to convert, but it would be modified by the script file.
			File userRequiredFile = new File(userRequiredFileName);
			if (userRequiredFile.exists()) {
				// just send it to user.
				UUID fileUid = UUID.randomUUID();
				BotHolder.getMysql().putFile(fileUid, userRequiredFile.getAbsolutePath());
				fileDownload.accept(fileUid);
			}
		}
	}

	public String getType() {
		return type;
	}
}
