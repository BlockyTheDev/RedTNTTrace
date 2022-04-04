package me.kekschen.redtnttrace.interfaces;

import me.kekschen.redtnttrace.annotations.MainCommand;
import me.kekschen.redtnttrace.annotations.Permission;
import me.kekschen.redtnttrace.annotations.RestrictTo;
import me.kekschen.redtnttrace.annotations.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class RedCommand implements CommandExecutor {
	Reflections reflections = new Reflections(
			new ConfigurationBuilder()
					.setUrls(ClasspathHelper.forClass(this.getClass()))
					.setScanners(Scanners.MethodsAnnotated)
	);

	protected RedCommand() { }

	public void registerSelf() {
		String command;
		if (this.getClass().isAnnotationPresent(MainCommand.class)) {
			command = this.getClass().getAnnotation(MainCommand.class).value();
		} else {
			throw new IllegalStateException("Class " + this.getClass().getSimpleName() + " is not annotated with @MainCommand");
		}
		Bukkit.getPluginCommand(command).setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Set<Method> subcommands = reflections.getMethodsAnnotatedWith(SubCommand.class);
		for (Method method : subcommands) {
			SubCommand subCommand = method.getAnnotation(SubCommand.class);
			String[] subcommandArgs = Arrays.stream(subCommand.value().split(" ")).filter(s -> !s.isEmpty()).toArray(String[]::new);
			if (args.length != subcommandArgs.length)
				continue;
			boolean found = true;
			for(int i = 0; i < args.length; i++) {
				if (!args[i].equalsIgnoreCase(subcommandArgs[i]) && !subcommandArgs[i].equals("*")) {
					found = false;
					break;
				}
			}
			if(!found)
				continue;

			if(method.isAnnotationPresent(Permission.class)) {
				Permission permission = method.getAnnotation(Permission.class);
				if(!sender.hasPermission(permission.value())) {
					sender.sendMessage("You don't have permission to use this command");
					return true;
				}
			}

			if(method.isAnnotationPresent(RestrictTo.class)) {
				RestrictTo restrict = method.getAnnotation(RestrictTo.class);
				Class<? extends CommandSender> clazz = restrict.value();
				if(!clazz.isInstance(sender)) {
					return true;
				}
			}

			try {
				method.invoke(this, sender, args);
			} catch (InvocationTargetException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return false;
	}
}
