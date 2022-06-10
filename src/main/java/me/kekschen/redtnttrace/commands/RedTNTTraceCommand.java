package me.kekschen.redtnttrace.commands;

import me.kekschen.redtnttrace.RedTNTTrace;
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
		MessageAPI.sendMessage(player, TraceManager.isTracing(player) ? RedTNTTrace.LANG.getString("trace.tnt_trace_enabled") : RedTNTTrace.LANG.getString("trace.tnt_trace_disabled"));
	}

	@SubCommand("show")
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void viewTrace(Player player, String[] args) {
		TraceRecord record = TraceManager.getTraceRecord(player);
		if (record == null) {
			MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("trace.no_trace_available"));
			return;
		}
		if (record.isInProgress) {
			MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("trace.trace_in_progress"));
			return;
		}
		if (record.tntLocations.size() == 0) {
			MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("trace.no_tnt_traced"));
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
			MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("trace.hid_trace"));
		} else {
			MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("trace.no_trace_to_hide"));
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
			MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("option.invalid_option").replace("%name%", args[1]));
			return;
		}
		boolean state = Boolean.parseBoolean(args[2]);
		if (state)
			TraceManager.enableTraceOption(player, option);
		else
			TraceManager.disableTraceOption(player, option);
		if (TraceManager.hideTrace(player) || TraceManager.hasTrace(player))
			TraceManager.showTrace(player);
		MessageAPI.sendMessage(player, (state ? RedTNTTrace.LANG.getString("option.option_enabled") : RedTNTTrace.LANG.getString("option.option_disabled")).replace("%name%", args[1]));
	}

	@SubCommand("option list")
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void listOptions(Player player, String[] args) {
		TraceOption[] options = TraceOption.values();
		MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("option.option_list_header"));
		for (TraceOption option : options) {
			MessageAPI.sendMessage(player, "§7- §f" + option.name().toLowerCase());
		}
	}

	@SubCommand("option")
	@Permission("rwm.redtnttrace.use")
	@RestrictTo(Player.class)
	public void options(Player player, String[] args) {
		listOptions(player, args);
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
			MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("mask.invalid_mask").replace("%mask%", args[1]));
			return;
		}
		TraceManager.setTraceMask(player, min, max);
		if (TraceManager.hideTrace(player) || TraceManager.hasTrace(player))
			TraceManager.showTrace(player);
		MessageAPI.sendMessage(player, RedTNTTrace.LANG.getString("mask.tracing_mask").replace("%min%", min + "").replace("%max%", max + ""));
	}

	@SubCommand("help")
	@Permission("rwm.redtnttrace.use")
	public void help(CommandSender sender, String[] args) {
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("plugin_header"));
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("help.toggle_help"));
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("help.show_help"));
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("help.hide_help"));
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("help.option_help"));
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("help.option_list_help"));
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("help.mask_help"));
		MessageAPI.sendMessageRaw(sender, RedTNTTrace.LANG.getString("plugin_footer"));
	}

	@SubCommand("")
	@Permission("rwm.redtnttrace.use")
	public void fallback(CommandSender sender, String[] args) {
		help(sender, args);
	}
}
