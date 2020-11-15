package org.hackbug.saltedfish.fishbot.ctf.puzzle;

import org.hackbug.saltedfish.fishbot.ctf.docker.DockerManager;
import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.hackbug.saltedfish.fishbot.ctf.util.ArchiveHelper;
import org.hackbug.saltedfish.fishbot.ctf.util.FlagHelper;
import org.hackbug.saltedfish.fishbot.ctf.util.ScriptHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

public class Puzzle {
	private final String title;
	private final String type;
	private final String description;
	private final File fileToArchive;
	private final File initializeScriptFile;
	private final File postInitializeScriptFile;
	private final String userRequiredFileName;
	private final File userFileModificationScriptFile;
	private final boolean useDocker;
	private final String dockerImage;

	public Puzzle(String title, String type, String description, String fileToArchive, String initializeScriptFile, String postInitializeScriptFile, String userRequiredFileName, String userFileModificationScriptFile, boolean useDocker, String dockerImage) throws FileNotFoundException {
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
		if (postInitializeScriptFile != null) {
			this.postInitializeScriptFile = new File(postInitializeScriptFile);
			if (!this.postInitializeScriptFile.exists()) {
				throw new FileNotFoundException(postInitializeScriptFile);
			}
		} else {
			this.postInitializeScriptFile = null;
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

		if (useDocker) {
			DockerManager dm = BotHolder.getDocker();
			String dockerName;
			dockerName = dm.buildContainer(dockerImage, port);
			dm.startContainer(dockerName);
			BotHolder.getMysql().putContainer(dockerName, this.title, who.getId());
			if (fileToArchive != null) {
				File archive = new File(who.getQq() + "_" + getTitle() + ".tar");
				ArchiveHelper.buildTar(fileToArchive, archive);
				dm.uploadArchive(dockerName, archive);
				archive.delete();
			}

			if (initializeScriptFile != null) {
				String name = who.getQq() + initializeScriptFile.getName();
				// assuming all of the script file is modifiable.
				File scriptArchive = new File(who.getQq() + "_initScript.tar");
				executeScriptOnDocker(port, flag, dm, dockerName, name, scriptArchive, initializeScriptFile);
			}

			// Ok I think we need to restart the docker to apply our configuration.
			dm.stopContainer(dockerName);
			dm.startContainer(dockerName);

			if (postInitializeScriptFile != null) {
				String name = who.getQq() + postInitializeScriptFile.getName();
				// assuming all of the script file is modifiable.
				File scriptArchive = new File(who.getQq() + "_postInitScript.tar");
				executeScriptOnDocker(port, flag, dm, dockerName, name, scriptArchive, postInitializeScriptFile);
			}
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

	private void executeScriptOnDocker(int port, String flag, DockerManager dm, String dockerName, String name, File scriptArchive, File postInitializeScriptFile) throws IOException {
		File tempScriptFile = new File(name);
		if (!tempScriptFile.exists()) {
			tempScriptFile.createNewFile();
		}
		ScriptHelper.convertScript(postInitializeScriptFile, tempScriptFile, port, flag);
		ArchiveHelper.buildTar(tempScriptFile, scriptArchive);
		dm.uploadArchive(dockerName, scriptArchive);
		dm.executeCommand(dockerName, "/" + name + "/" + name);
		tempScriptFile.delete();
		scriptArchive.delete();
	}

	public String getType() {
		return type;
	}

	public File getPostInitializeScriptFile() {
		return postInitializeScriptFile;
	}
}
