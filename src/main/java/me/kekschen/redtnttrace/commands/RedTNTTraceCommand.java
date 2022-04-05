package me.kekschen.redtnttrace.commands;

import me.kekschen.redtnttrace.annotations.*;
import me.kekschen.redtnttrace.api.MessageAPI;
import me.kekschen.redtnttrace.interfaces.RedCommand;
import me.kekschen.redtnttrace.managers.TraceManager;
import me.kekschen.redtnttrace.types.TraceOption;
import me.kekschen.redtnttrace.types.TraceRecord;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@MainCommand("trace")
public class RedTNTTraceCommand extends RedCommand {

	@SubCommand("toggle")
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void toggleTrace(Player player, String[] args) {
		TraceManager.toggleTrace(player);
		MessageAPI.sendMessage(player, "§7TNT tracing is now " + (TraceManager.isTracing(player) ? "§aenabled" : "§cdisabled"));
	}

	@SubCommand("show")
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void viewTrace(Player player, String[] args) {
		TraceRecord record = TraceManager.getTraceRecord(player);
		if (record == null) {
			MessageAPI.sendMessage(player, "§7You haven't recorded any TNT trace yet");
			return;
		}
		if (record.isInProgress) {
			MessageAPI.sendMessage(player, "§7Your TNT trace is still in progress. Use §f/trace toggle §7to stop it.");
			return;
		}
		if (record.tntLocations.size() == 0) {
			MessageAPI.sendMessage(player, "§7You did not trace any TNT. Use §f/trace toggle §7to start tracing.");
			return;
		}

		TraceManager.showTrace(player);
	}

	@SubCommand("hide")
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void hideTrace(Player player, String[] args) {
		boolean hidden = TraceManager.hideTrace(player);
		if (hidden) {
			MessageAPI.sendMessage(player, "§7TNT trace is now §ahidden");
		} else {
			MessageAPI.sendMessage(player, "§cThere is no TNT trace to hide");
		}
	}

	@SubCommand("option * *")
	@DynamicTabComplete({"show_id_tags,show_fuel,only_explosions", "* true,* false"})
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void setOption(Player player, String[] args) {
		TraceOption option;
		try {
			option = TraceOption.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			MessageAPI.sendMessage(player, "§cThe option §f" + args[1] + "§c does not exist");
			return;
		}
		boolean state = Boolean.parseBoolean(args[2]);
		if (state)
			TraceManager.enableTraceOption(player, option);
		else
			TraceManager.disableTraceOption(player, option);
		if (TraceManager.hideTrace(player) || TraceManager.hasTrace(player))
			TraceManager.showTrace(player);
		MessageAPI.sendMessage(player, "§7Option §f" + args[1] + "§7 is now " + (state ? "§aenabled" : "§cdisabled"));
	}

	@SubCommand("option list")
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void listOptions(Player player, String[] args) {
		TraceOption[] options = TraceOption.values();
		MessageAPI.sendMessage(player, "§7All available trace options are:");
		for (TraceOption option : options) {
			MessageAPI.sendMessage(player, "§7- §f" + option.name().toLowerCase());
		}
	}

	@SubCommand("mask *")
	@DynamicTabComplete({"1-100000"})
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void setMask(Player player, String[] args) {
		String[] masks = args[1].split("-");
		int min, max;
		try {
			min = Integer.parseInt(masks[0]);
			if (masks.length == 2)
				max = Integer.parseInt(masks[1]);
			else
				max = min;
			if (min < 0 || max < 0 || min > max)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			MessageAPI.sendMessage(player, "§cThe mask §f" + args[1] + "§c is not valid");
			return;
		}
		TraceManager.setTraceMask(player, min, max);
		if (TraceManager.hideTrace(player) || TraceManager.hasTrace(player))
			TraceManager.showTrace(player);
		MessageAPI.sendMessage(player, "§7You are now tracing TNTs with a mask of §f" + min + "-" + max);
	}

	@SubCommand("help")
	@Permission("rwm.redtnttrace.use")
	public void help(CommandSender sender, String[] args) {
		MessageAPI.sendMessage(sender, "§4§lRed§r§lTNTTrace §7- §fA plugin to trace TNT explosions");
		MessageAPI.sendMessage(sender, "§7/trace toggle §f- §7Toggle TNT tracing");
		MessageAPI.sendMessage(sender, "§7/trace show §f- §7Show recorded TNT trace");
		MessageAPI.sendMessage(sender, "§7/trace hide §f- §7Hide recorded TNT trace");
		MessageAPI.sendMessage(sender, "§7/trace option <option> <state> §f- §7Set option for tracing.");
		MessageAPI.sendMessage(sender, "§7/trace option list §f- §7List options for tracing.");
		MessageAPI.sendMessage(sender, "§7/trace mask <min>-<max> §f- §7Set TNT mask for viewing the trace.");
	}

	@SubCommand("")
	@Permission("rwm.redtnttrace.use")
	public void fallback(CommandSender sender, String[] args) {
		help(sender, args);
	}
}
