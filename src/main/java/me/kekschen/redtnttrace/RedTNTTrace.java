package me.kekschen.redtnttrace;

import me.kekschen.redtnttrace.api.MessageAPI;
import me.kekschen.redtnttrace.commands.RedTNTTraceCommand;
import me.kekschen.redtnttrace.listeners.PlayerListener;
import me.kekschen.redtnttrace.managers.TraceManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class RedTNTTrace extends JavaPlugin {

	private static RedTNTTrace instance;
	public static Plugin getInstance() {
		return instance;
	}

	@Override
	public void onLoad() {
		instance = this;
		MessageAPI.setPrefix("&8[&4§lRed§f§lTNTTrace&8] &7");
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
