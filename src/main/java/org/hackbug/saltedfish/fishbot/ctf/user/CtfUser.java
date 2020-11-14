package org.hackbug.saltedfish.fishbot.ctf.user;

import java.util.UUID;

public class CtfUser {
	private final int id;
	private final long qq;
	private final boolean operator;
	private final String solvingPuzzle;
	private final long startTime;
	private final UUID fileUuid;

	public CtfUser(int id, long qq, boolean operator, String solvingPuzzle, long startTime, UUID fileUuid) {
		this.id = id;
		this.qq = qq;
		this.operator = operator;
		this.solvingPuzzle = solvingPuzzle;
		this.startTime = startTime;
		this.fileUuid = fileUuid;
	}

	public int getId() {
		return id;
	}

	public long getQq() {
		return qq;
	}

	public boolean isOperator() {
		return operator;
	}

	public String getSolvingPuzzle() {
		return solvingPuzzle;
	}

	public long getStartTime() {
		return startTime;
	}

	public UUID getFileUuid() {
		return fileUuid;
	}
}
