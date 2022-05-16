package me.kekschen.redtnttrace.interfaces;

import me.kekschen.redtnttrace.RedTNTTrace;
import me.kekschen.redtnttrace.annotations.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedCommand implements CommandExecutor, TabCompleter {
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
		PluginCommand cmd = Bukkit.getPluginCommand(command);
		assert cmd != null;
		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
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
			for (int i = 0; i < args.length; i++) {
				if (!args[i].equalsIgnoreCase(subcommandArgs[i]) && !subcommandArgs[i].equals("*")) {
					found = false;
					break;
				}
			}
			if (!found)
				continue;

			if (method.isAnnotationPresent(Permission.class)) {
				Permission permission = method.getAnnotation(Permission.class);
				if (!sender.hasPermission(permission.value())) {
					sender.sendMessage(RedTNTTrace.LANG.getString("no-permission"));
					return true;
				}
			}

			if (method.isAnnotationPresent(RestrictTo.class)) {
				RestrictTo restrict = method.getAnnotation(RestrictTo.class);
				Class<? extends CommandSender> clazz = restrict.value();
				if (!clazz.isInstance(sender)) {
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

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();
		Set<Method> subcommands = reflections.getMethodsAnnotatedWith(SubCommand.class);
		for (Method method : subcommands) {
			SubCommand subCommandAnnotation = method.getAnnotation(SubCommand.class);
			String[] subcommandArgs = Arrays.stream(subCommandAnnotation.value().split(" ")).filter(s -> !s.isEmpty()).toArray(String[]::new);
			if (args.length > subcommandArgs.length)
				continue;
			if (args.length > 1 && !subcommandArgs[args.length - 2].equals(args[args.length - 2]) && !subcommandArgs[args.length - 2].equals("*"))
				continue;
			if (!subcommandArgs[args.length - 1].equals("*")) {
				completions.add(subcommandArgs[args.length - 1]);
				continue;
			}
			if (!method.isAnnotationPresent(DynamicTabComplete.class))
				continue;
			int offset = 0;
			for (String arg : subcommandArgs) {
				if (!arg.equals("*"))
					offset++;
				else
					break;
			}
			offset = args.length - offset;
			DynamicTabComplete tabComplete = method.getAnnotation(DynamicTabComplete.class);
			String[] dynamicArgArrays = tabComplete.value();
			for (String dynamicArgArray : dynamicArgArrays) {
				String[] dynamicArgs = dynamicArgArray.split(",");
				int finalOffset = offset;
				completions.addAll(
						Arrays.stream(dynamicArgs)
								.filter(s -> s.split(" ").length == finalOffset)
								.map(s -> s.split(" ")[finalOffset - 1])
								.filter(s -> !s.equals("*"))
								.collect(Collectors.toList())
				);
			}
		}
		return completions;
	}
}
