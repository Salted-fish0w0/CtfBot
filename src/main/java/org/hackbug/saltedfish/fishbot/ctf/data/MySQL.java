package org.hackbug.saltedfish.fishbot.ctf.data;

import org.hackbug.saltedfish.fishbot.ctf.configuration.JsonConfiguration;
import org.hackbug.saltedfish.fishbot.ctf.puzzle.Puzzle;
import org.hackbug.saltedfish.fishbot.ctf.puzzle.PuzzleLog;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MySQL {
	private final String host;
	private final String port;
	private final String user;
	private final String pass;
	private final String db;

	private Connection connection;
	private final Map<String, PreparedStatement> preparedStatements = new HashMap<>();

	public MySQL(JsonConfiguration config) throws Exception {
		host = config.getString("hostname");
		port = config.getString("port");
		user = config.getString("username");
		pass = config.getString("password");
		db = config.getString("database");

		initConnection();
		initDatabase();
		initStatements();

		if (getUser(2293652212L) == null) {
			registerUser(new CtfUser(0, 2293652212L, true, null, 0, UUID.randomUUID()));
		}
	}

	public static void initConfig(JsonConfiguration config) {
		config.set("hostname", "127.0.0.1");
		config.set("port", "3306");
		config.set("username", "root");
		config.set("password", "fish");
		config.set("database", "bot");
	}

	private void initConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true", user, pass);
	}
	
	private void initDatabase() {
		try {
			PreparedStatement ps = connection.prepareStatement("create table if not exists `user` (" +
					"`id` int(64) not null auto_increment," +
					"`qq` bigint not null unique," +
					"`operator` boolean," +
					"`solving_puzzle` varchar(16) default null," +
					"`solving_time` bigint default 0," +
					"`file_uuid` varchar(64) not null," +
					"`money` int default -1," + // i swear it will be used soon.
					"primary key (id))engine=InnoDB default charset=UTF8;");
			ps.executeUpdate();
			ps = connection.prepareStatement("create table if not exists `flag` (" +
					"`id` int(64) not null auto_increment," +
					"`user_id` int not null," +
					"`flag` varchar(128) not null," +
					"primary key (id))engine=InnoDB default charset=UTF8;");
			ps.executeUpdate();
			ps = connection.prepareStatement("create table if not exists `container` (" +
					"`container_id` varchar(128) not null unique," +
					"`puzzle_title` varchar(16) not null," +
					"`user_id` int not null," +
					"primary key (container_id))engine=InnoDB default charset=UTF8;");
			ps.executeUpdate();
			ps = connection.prepareStatement("create table if not exists `puzzles` (" +
					"`title` varchar(16) not null unique," +
					"`description` varchar(256) default null," +
					"`type` varchar(8) not null," +
					"`initialize_script` varchar(64) default null," +
					"`post_initialize_script` varchar(64) default null," +
					"`template_directory` varchar(64) default null," +
					"`user_required_file` varchar(64) default null," +
					"`user_file_script` varchar(64) default null," +
					"`use_docker` boolean default false," +
					"`image` varchar(16) default null," +
					"primary key (title))engine=InnoDB default charset=UTF8;");
			ps.executeUpdate();
			ps = connection.prepareStatement("create table if not exists `files` (" +
					"`id` varchar(64) not null unique," +
					"`filename` varchar(256) not null," +
					"primary key (id))engine=InnoDB default charset=UTF8;");
			ps.executeUpdate();
			ps = connection.prepareStatement("create table if not exists `log` (" +
					"`id` int not null auto_increment," +
					"`user_id` bigint not null," +
					"`puzzle_title` varchar(16) not null," +
					"`finished_time` bigint not null," +
					"`time_spent` bigint not null," +
					"primary key (id))engine=InnoDB default charset=UTF8;");
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initStatements() {
		try {
			preparedStatements.put("putPuzzle", connection.prepareStatement("insert into puzzles (`title`,`type`,`description`,initialize_script,post_initialize_script,template_directory,user_required_file,user_file_script,`use_docker`,`image`) values (?,?,?,?,?,?,?,?,?,?);"));
			preparedStatements.put("getPuzzle", connection.prepareStatement("select * from puzzles where title=?;"));
			preparedStatements.put("removePuzzle", connection.prepareStatement("delete from puzzles where title=?;"));
			preparedStatements.put("getUser", connection.prepareStatement("select * from user where qq=?;"));
			preparedStatements.put("getUserFile", connection.prepareStatement("select * from user where file_uuid=?;"));
			preparedStatements.put("putUser", connection.prepareStatement("insert into user (qq, operator,file_uuid) values (?, ?, ?)"));
			preparedStatements.put("setAdmin", connection.prepareStatement("update user set operator=? where qq=?;"));
			preparedStatements.put("putContainer", connection.prepareStatement("insert into container (container_id,puzzle_title,`user_id`) VALUES (?,?,?);"));
			preparedStatements.put("removeContainer", connection.prepareStatement("delete from container where user_id=?;"));
			preparedStatements.put("getContainer", connection.prepareStatement("select * from container where user_id=?;"));
			preparedStatements.put("startSolving", connection.prepareStatement("update user set solving_puzzle=?,solving_time=? where id=?"));
			preparedStatements.put("finishSolving", connection.prepareStatement("update user set solving_puzzle=null where id=?"));
			preparedStatements.put("logSolving", connection.prepareStatement("insert into log (user_id, puzzle_title, finished_time, time_spent) VALUES (?,?,?,?);"));
			preparedStatements.put("getLogByTitle", connection.prepareStatement("select * from log where puzzle_title=?;"));
			preparedStatements.put("putFile", connection.prepareStatement("insert into files (id, filename) values (?,?);"));
			preparedStatements.put("getFile", connection.prepareStatement("select * from files where id=?;"));
			preparedStatements.put("putFlag", connection.prepareStatement("insert into flag (user_id, flag) VALUES (?,?)"));
			preparedStatements.put("getFlag", connection.prepareStatement("select * from flag where user_id=?;"));
			preparedStatements.put("dropFlag", connection.prepareStatement("delete from flag where user_id=?;"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void putPuzzle(Puzzle puzzle) throws Exception {
		PreparedStatement ps = preparedStatements.get("putPuzzle");
		ps.setString(1, puzzle.getTitle());
		ps.setString(2, puzzle.getType());
		ps.setString(3, puzzle.getDescription());
		if (puzzle.getInitializeScriptFile() != null) {
			ps.setString(4, puzzle.getInitializeScriptFile().getAbsolutePath());
		} else {
			ps.setString(4, null);
		}
		if (puzzle.getPostInitializeScriptFile() != null) {
			ps.setString(5, puzzle.getPostInitializeScriptFile().getAbsolutePath());
		} else {
			ps.setString(5, null);
		}
		if (puzzle.getFileToArchive() != null) {
			ps.setString(6, puzzle.getFileToArchive().getAbsolutePath());
		} else {
			ps.setString(6, null);
		}
		if (puzzle.getUserRequiredFileName() != null) {
			ps.setString(7, puzzle.getUserRequiredFileName());
		} else {
			ps.setString(7, null);
		}
		if (puzzle.getUserFileModificationScriptFile() != null) {
			ps.setString(8, puzzle.getUserFileModificationScriptFile().getAbsolutePath());
		} else {
			ps.setString(8, null);
		}
		ps.setBoolean(9, puzzle.doUseDocker());
		if (puzzle.getDockerImage() != null) {
			ps.setString(10, puzzle.getDockerImage());
		} else {
			ps.setString(10, null);
		}
		ps.executeUpdate();
	}

	public Puzzle getPuzzle(String title) throws Exception {
		PreparedStatement ps = preparedStatements.get("getPuzzle");
		ps.setString(1, title);
		ResultSet rs = ps.executeQuery();
		if (!rs.first()) {
			return null;
		}
		return new Puzzle(rs.getString("title"), rs.getString("type"), rs.getString("description"), rs.getString("template_directory"), rs.getString("initialize_script"), rs.getString("post_initialize_script"), rs.getString("user_required_file"), rs.getString("user_file_script"), rs.getBoolean("use_docker"), rs.getString("image"));
	}

	public CtfUser getUser(long qq) throws Exception {
		PreparedStatement ps = preparedStatements.get("getUser");
		ps.setLong(1, qq);
		ResultSet rs = ps.executeQuery();
		if (!rs.first()) {
			return null;
		}

		return new CtfUser(rs.getInt("id"), rs.getLong("qq"), rs.getBoolean("operator"), rs.getString("solving_puzzle"), rs.getLong("solving_time"), UUID.fromString(rs.getString("file_uuid")));
	}

	public void registerUser(CtfUser user) throws Exception {
		PreparedStatement ps = preparedStatements.get("putUser");
		ps.setLong(1, user.getQq());
		ps.setBoolean(2, user.isOperator());
		ps.setString(3, user.getFileUuid().toString());
		
		ps.executeUpdate();
	}

	public void putContainer(String containerId, String puzzleTitle, int userId) throws Exception {
		PreparedStatement ps = preparedStatements.get("putContainer");
		ps.setString(1, containerId);
		ps.setString(2, puzzleTitle);
		ps.setInt(3, userId);
		ps.executeUpdate();
	}

	public String getContainer(int userId) throws Exception {
		PreparedStatement ps = preparedStatements.get("getContainer");
		ps.setInt(1, userId);
		ResultSet rs = ps.executeQuery();
		if (rs.first()) {
			return rs.getString("container_id");
		} else {
			return null;
		}
	}

	public void dropContainer(int userId) throws Exception {
		PreparedStatement ps = preparedStatements.get("removeContainer");
		ps.setInt(1, userId);
		ps.executeUpdate();
	}

	public void startSolving(CtfUser user, String puzzle) throws Exception {
		PreparedStatement ps = preparedStatements.get("startSolving");
		ps.setString(1, puzzle);
		ps.setLong(2, System.currentTimeMillis());
		ps.setInt(3, user.getId());
		ps.executeUpdate();
	}

	public void endSolving(CtfUser user) throws Exception {
		PreparedStatement ps = preparedStatements.get("finishSolving");
		ps.setInt(1, user.getId());
		ps.executeUpdate();
	}

	public void finishSolving(CtfUser user) throws Exception {
		endSolving(user);
		// update solving data.
		user = getUser(user.getQq());
		PreparedStatement ps = preparedStatements.get("logSolving");
		ps.setLong(1, user.getQq());
		ps.setString(2, user.getSolvingPuzzle());
		ps.setLong(3, System.currentTimeMillis());
		ps.setLong(4, System.currentTimeMillis() - user.getStartTime());
	}

	public void putFile(UUID fileUid, String fileName) throws Exception {
		PreparedStatement ps = preparedStatements.get("putFile");
		ps.setString(1, fileUid.toString());
		ps.setString(2, fileName);
		ps.executeUpdate();
	}

	public void putFlag(CtfUser user, String flag) throws Exception {
		PreparedStatement ps = preparedStatements.get("putFlag");
		ps.setInt(1, user.getId());
		ps.setString(2, flag);
		ps.executeUpdate();
	}

	public boolean checkFlag(int userId, String flag) throws Exception {
		PreparedStatement ps = preparedStatements.get("getFlag");
		ps.setInt(1, userId);
		ResultSet rs = ps.executeQuery();
		if (!rs.first()) {
			throw new IllegalStateException("The flag does not exist");
		}
		return rs.getString("flag").equals(flag);
	}

	public void dropFlag(int userId) throws Exception {
		PreparedStatement ps = preparedStatements.get("dropFlag");
		ps.setInt(1, userId);
		ps.executeUpdate();
	}

	public void dropPuzzle(String puzzle) throws Exception {
		PreparedStatement ps = preparedStatements.get("removePuzzle");
		ps.setString(1, puzzle);
		ps.executeUpdate();
	}

	public void setOperator(long qq, boolean op) throws Exception {
		if (getUser(qq) == null) {
			registerUser(new CtfUser(0, qq, true, null, 0, UUID.randomUUID()));
			return;
		}

		PreparedStatement ps = preparedStatements.get("setAdmin");
		ps.setBoolean(1, op);
		ps.setLong(2, qq);
		ps.executeUpdate();
	}

	public CtfUser getUserFromFileUuid(UUID uuid) throws Exception {
		PreparedStatement ps = preparedStatements.get("getUserFile");
		ps.setString(1, uuid.toString());
		ResultSet rs = ps.executeQuery();
		if (!rs.first()) {
			return null;
		}
		return getUser(rs.getLong("qq"));
	}

	public String getFile(UUID fileUuid) throws Exception {
		PreparedStatement ps = preparedStatements.get("getFile");
		ps.setString(1, fileUuid.toString());
		ResultSet rs = ps.executeQuery();
		if (!rs.first()) {
			return null;
		}
		return rs.getString("filename");
	}

	public List<PuzzleLog> getPuzzleLogByTitle(String title) throws Exception {
		List<PuzzleLog> logs = new ArrayList<>();
		PreparedStatement ps = preparedStatements.get("getLogByTitle");
		ps.setString(1, title);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			logs.add(new PuzzleLog(getUser(rs.getLong("user_id")), getPuzzle(title), rs.getLong("time_spent"), new Date(rs.getLong("finished_time"))));
		}

		return logs;
	}
}
