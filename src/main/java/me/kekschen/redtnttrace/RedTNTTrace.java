package me.kekschen.redtnttrace;

import me.kekschen.redtnttrace.api.MessageAPI;
import me.kekschen.redtnttrace.commands.RedTNTTraceCommand;
import me.kekschen.redtnttrace.listeners.PlayerListener;
import me.kekschen.redtnttrace.utils.LanguageHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class RedTNTTrace extends JavaPlugin {

	private static RedTNTTrace INSTANCE;
	public static Plugin getINSTANCE() {
		return INSTANCE;
	}
	public static LanguageHelper LANG;
	@Override
	public void onLoad() {
		INSTANCE = this;
		saveDefaultConfig();
		LANG = new LanguageHelper(getConfig());
		MessageAPI.setPrefix(LANG.getPrefix());
	}

	@Override
	public void onEnable() {
		new RedTNTTraceCommand().registerSelf();
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
	}

	@Override
	public void onDisable() {
	}
}
