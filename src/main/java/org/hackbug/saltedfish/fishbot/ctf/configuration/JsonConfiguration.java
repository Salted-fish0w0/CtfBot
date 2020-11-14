package org.hackbug.saltedfish.fishbot.ctf.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConfiguration {
	private Map<String, Object> parsedMap;

	public JsonConfiguration(File jsonFile) throws IOException {
		StringBuilder result = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
			String s;
			while ((s = br.readLine()) != null) {
				result.append(s);
			}
		}
		Gson g = new Gson();
		parsedMap = g.fromJson(result.toString(), new TypeToken<Map<String, Object>>() {}.getType());
		if (parsedMap == null) {
			parsedMap = new HashMap<>();
		}
	}

	public JsonConfiguration() {
		parsedMap = new HashMap<>();
	}

	private JsonConfiguration(Map<String, Object> parsedMap) {
		this.parsedMap = parsedMap;
	}

	public Object get(String entry) {
		return parsedMap.get(entry);
	}

	@SuppressWarnings({"raw-type", "unchecked"})
	public JsonConfiguration getSection(String entry) {
		Object target = get(entry);
		if (!(target instanceof Map)) {
			return null;
		}
		return new JsonConfiguration((Map<String, Object>) target);
	}

	public int getInt(String entry) {
		Object target = get(entry);
		if (!(target instanceof Number)) {
			return (int) Double.NaN;
		}
		return ((Number) target).intValue();
	}

	public double getDouble(String entry) {
		Object target = get(entry);
		if (!(target instanceof Number)) {
			return Double.NaN;
		}
		return ((Number) target).doubleValue();
	}

	public float getFloat(String entry) {
		Object target = get(entry);
		if (!(target instanceof Number)) {
			return (float) Double.NaN;
		}
		return ((Number) target).floatValue();
	}

	public String getString(String entry) {
		return get(entry).toString();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String entry) {
		Object target = get(entry);
		if (!(target instanceof List)) {
			return new ArrayList<>();
		}
		return (List<T>) target;
	}

	public boolean getBoolean(String entry) {
		Object target = get(entry);
		if (!(target instanceof Boolean)) {
			return false;
		}

		return (Boolean) target;
	}

	public void set(String entry, Object value) {
		if (value instanceof JsonConfiguration) {
			set(entry, ((JsonConfiguration) value).parsedMap);
			return;
		}
		parsedMap.put(entry, value);
	}

	public JsonConfiguration createSection(String entry) {
		JsonConfiguration jc = new JsonConfiguration(new HashMap<>());
		set(entry, jc);
		return jc;
	}

	public void saveTo(File file) throws IOException {
		try (FileWriter fw = new FileWriter(file)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			fw.write(gson.toJson(parsedMap));
			fw.write(System.getProperty("line.separator"));
		}
	}
}
