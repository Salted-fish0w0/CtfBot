package org.hackbug.saltedfish.fishbot.ctf.puzzle;

import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.Date;

public class PuzzleLog {
	private final CtfUser user;
	private final Puzzle puzzle;
	private final long timeUsed;
	private final Date finishedTime;

	public PuzzleLog(CtfUser user, Puzzle puzzle, long timeUsed, Date finishedTime) {
		this.user = user;
		this.puzzle = puzzle;
		this.timeUsed = timeUsed;
		this.finishedTime = finishedTime;
	}

	@Override
	public String toString() {
		return user.getQq() + " - " + puzzle.getTitle() + " - " + (timeUsed / 1000) + "秒 - " + finishedTime.toString();
	}
}
