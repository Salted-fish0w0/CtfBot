package org.hackbug.saltedfish.fishbot.ctf.util;

import java.util.Random;
import java.util.UUID;

public class FlagHelper {
	private static final String[] fakeFlagContent = {
			"https://www.runoob.com",
			"别忘了百度是你的好伙伴，不好用的话试试bing",
			"你知道嘛，某鱼曾经想过用Minecraft搭建靶场",
			"好好看课程",
			"或许你需要理解下那些选项上的字表达的是什么，可能就是它的功能",
			"或许你需要好好学学英语，Don't you?",
			"干这行的都要有扎实的电脑基础",
			"要有想象力",
			"当没有思路的时候，试试喝点水，休息休息",
			"提问的智慧：https://ruby-china.org/topics/24325",
			"实在不会的话建议练练基础",
			"写代码的时候不要相信用户的任何输入",
			"owo",
			"这是一条tip，不是flag",
			"nmap --script=vuln 你要攻击的服务器",
			"不要乱整别人的网站啦",
			"nikto?",
			"或许这只FishBot会有bug，别忘了给某鱼说",
			"不要想着作弊",
			"什么？群里不能上传Writeup？试试给FishBot发句/upload_wp吧",
			"flag在哪？远在天边，近在眼前",
			"数学思想很重要",
			"不要坐太久了，起来活动活动",
			"看起来没什么用，万一有用了呢",
			"2333",
			"这条tip还没想好要说什么",
			"一起网抑！",
			"我是一只假的flag，是用来骗你的，也可能是用来占据原本flag的位置的",
			"不要欺负FishBot",
			"github上有很多好东西",
			"# echo 140.82.114.4 github.com >> /etc/hosts",
			"请使用/check_flag命令提交flag",
			"今晚审cms嘛？ --T4x0r",
			"重启解决80%问题 重装解决99%问题 --T4x0r; 换电脑解决100%问题 --某鱼",
			"如果能够反编译，我这波将绝杀，可惜反不得",
			"sqlmap请尽可能使用--dbs或--tables代替--dump或--dump-all",
			"日后你惹出祸来，不把为师说出来就行了",
			"为了能做到完美的咕咕咕，我们每天都有在努力！",
			"原来当黑客特别吸引同性",
			"#肖战联合火星人暗中操作美国大选#",
			"WE WILL BUILD A GREAT WALL ALONG THE SOUTHERN BORDER",
			"想审计FishBot？去https://github.com/Salted-fish0w0/CtfBot看看吧"
	};
	private static final Random random = new Random();

	public static String generateRandomFlag() {
		return "hackbug{" + UUID.randomUUID() + "}";
	}
	public static String generateFakeFlag() {
		int chosen = Math.abs(random.nextInt(fakeFlagContent.length));
		return "hackbug{Tip: " + fakeFlagContent[chosen] + "}";
	}
}
