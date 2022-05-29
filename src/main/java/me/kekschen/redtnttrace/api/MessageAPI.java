package me.kekschen.redtnttrace.api;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageAPI {
	private static String prefix = "&7[&cCool-Unset-Plugin-Prefix&7] ";

	public static void setPrefix(String prefix) {
		MessageAPI.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
	}

	public static String getPrefix() {
		return prefix;
	}

	public static void sendMessage(CommandSender sender, String message) {
		sendMessageRaw(sender, getPrefix() + message);
	}

	public static void sendMessageRaw(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
}
