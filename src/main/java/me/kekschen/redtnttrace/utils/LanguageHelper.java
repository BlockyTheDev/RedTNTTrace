package me.kekschen.redtnttrace.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageHelper {
	private final FileConfiguration config;
	public LanguageHelper(FileConfiguration config) {
		this.config = config;
	}

	public String getString(String path, String def) {
		return config.getString(path, def);
	}

	public String getString(String path) {
		return getString("messages." + path, "§cMissing language string: " + path);
	}

	public String getPrefix() {
		return getString("prefix", "§cMissing prefix");
	}
}
