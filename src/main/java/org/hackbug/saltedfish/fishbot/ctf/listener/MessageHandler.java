package org.hackbug.saltedfish.fishbot.ctf.listener;

import org.hackbug.saltedfish.fishbot.ctf.command.CommandBootstrap;
import org.hackbug.saltedfish.fishbot.ctf.util.MessageChainHelper;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MessageHandler extends SimpleListenerHost {
	private final List<Long> availableGroups = Arrays.asList(763416094L);

	@EventHandler
	public void onPrivateMessage(FriendMessageEvent event) {
		User user = event.getSender();
		String message = event.getMessage().contentToString();
		if (message.startsWith("/")) {
			CommandBootstrap.executeCommand(user, message.substring(1), (s) -> event.getFriend().sendMessage(s), false);
		}
	}
	@EventHandler
	public void onGroupMessage(GroupMessageEvent event) {
		if (!availableGroups.contains(event.getGroup().getId())) {
			event.getGroup().quit();
			return;
		}

		User user = event.getSender();
		Iterator<SingleMessage> msgIterator = event.getMessage().iterator();
		StringBuilder message = new StringBuilder();
		while (msgIterator.hasNext()) {
			SingleMessage sm = msgIterator.next();
			if (sm instanceof At) {
				message.append(((At) sm).getTarget());
			} else {
				message.append(sm.contentToString());
			}
		}

		if (message.toString().startsWith("/")) {
			Group g = event.getGroup();
			CommandBootstrap.executeCommand(user, message.substring(1), (s) -> {
				MessageChainBuilder mcb = new MessageChainBuilder();
				mcb.add(new At(g.get(user.getId())));
				mcb.add(s);
				g.sendMessage(mcb.build());
			}, true);
		}
	}
	@EventHandler
	public void onGroupInvite(BotInvitedJoinGroupRequestEvent event) {
		if (!availableGroups.contains(event.getGroupId())) {
			event.ignore();
		}
		event.accept();
	}
	@EventHandler
	public void onFriendRequest(NewFriendRequestEvent event) {
		event.accept();
	}
	@EventHandler
	public void onGroupJoin(MemberJoinEvent event) {
		if (!availableGroups.contains(event.getGroup().getId())) {
			event.getGroup().quit();
			return;
		}

		event.getGroup().sendMessage(MessageChainHelper.asMessageChain("欢迎新大佬 ")
				.plus(new At(event.getMember()))
				.plus("，请详细阅读群公告，因没有阅读群公告造成的一切后果请自行处理"));
	}
}
