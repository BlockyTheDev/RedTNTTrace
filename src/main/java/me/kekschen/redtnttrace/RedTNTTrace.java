package me.kekschen.redtnttrace;

import dev.tehbrian.restrictionhelper.core.RestrictionHelper;
import dev.tehbrian.restrictionhelper.core.RestrictionLoader;
import dev.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import dev.tehbrian.restrictionhelper.spigot.SpigotRestrictionLoader;
import dev.tehbrian.restrictionhelper.spigot.restrictions.R_PlotSquared_6;
import dev.tehbrian.restrictionhelper.spigot.restrictions.R_WorldGuard_7;
import me.kekschen.redtnttrace.api.MessageAPI;
import me.kekschen.redtnttrace.commands.RedTNTTraceCommand;
import me.kekschen.redtnttrace.listeners.PlayerListener;
import me.kekschen.redtnttrace.utils.LanguageHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public final class RedTNTTrace extends JavaPlugin {

	private static RedTNTTrace instance;

	public static LanguageHelper lang;
	private RestrictionHelper restrictionHelper;

	@Override
	public void onLoad() {
		
		instance = this;
		
		saveDefaultConfig();
		lang = new LanguageHelper(getConfig());
		MessageAPI.setPrefix(lang.getPrefix());
	}

	@Override
	public void onEnable() {
		
		new RedTNTTraceCommand().registerSelf();
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		
		this.restrictionHelper = new SpigotRestrictionHelper();

		final RestrictionLoader loader = new SpigotRestrictionLoader(
				this.getSLF4JLogger(),
				Arrays.asList(this.getServer().getPluginManager().getPlugins()),
				List.of(R_PlotSquared_6.class, R_WorldGuard_7.class)
		);

		loader.load(this.restrictionHelper);
	}

	@Override
	public void onDisable() {
		
	}

	public static RedTNTTrace getInstance() {
		return instance;
	}
	
	public RestrictionHelper getRestrictionHelper() {
		return restrictionHelper;
	}
}
