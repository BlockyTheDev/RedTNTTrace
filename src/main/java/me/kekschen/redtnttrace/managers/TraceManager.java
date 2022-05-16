package me.kekschen.redtnttrace.managers;

import me.kekschen.redtnttrace.RedTNTTrace;
import me.kekschen.redtnttrace.types.TraceOption;
import me.kekschen.redtnttrace.types.TraceRecord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TraceManager {
	private static final HashMap<Player, TraceRecord> records = new HashMap<>();
	private static final HashMap<UUID, ArrayList<TraceOption>> traceOptions = new HashMap<>();
	private static final HashMap<UUID, Integer[]> traceMasks = new HashMap<>();

	private static BukkitTask tntMoveTask = null;

	private static void startTrace() {
		if (tntMoveTask != null && !tntMoveTask.isCancelled()) return;
		tntMoveTask = new BukkitRunnable() {
			@Override
			public void run() {
				HashMap<UUID, Location> tntLocations = new HashMap<>();
				for(World world : Bukkit.getWorlds()) {
					for(TNTPrimed tnt : world.getEntitiesByClass(TNTPrimed.class)) {
						tntLocations.put(tnt.getUniqueId(), tnt.getLocation());
					}
				}
				if(tntLocations.size() != 0)
					reportTNTLocations(tntLocations);
			}
		}.runTaskTimer(RedTNTTrace.getINSTANCE(), 0, 1);
	}

	private static void stopTrace() {
		if (tntMoveTask == null) return;
		tntMoveTask.cancel();
		tntMoveTask = null;
	}

	public static void toggleTrace(Player player) {
		TraceRecord record = records.getOrDefault(player, new TraceRecord());
		record.isInProgress = !record.isInProgress;
		records.put(player, record);

		if (record.isInProgress) {
			record.tntLocations.clear();
			startTrace();
		} else if (records.values().stream().noneMatch(r -> r.isInProgress))
			stopTrace();
	}

	public static boolean isTracing(Player player) {
		return records.containsKey(player) && records.get(player).isInProgress;
	}

	public static void reportTNTLocation(UUID uuid, Location location) {
		for (Player player : records.keySet()) {
			TraceRecord record = records.get(player);
			if (record.isInProgress) {
				List<Location> locationList = record.tntLocations.getOrDefault(uuid, new ArrayList<>());
				locationList.add(location);
				record.tntLocations.put(uuid, locationList);
				records.put(player, record);
			}
		}
	}

	public static void reportTNTLocations(HashMap<UUID, Location> locations) {
		for (Player player : records.keySet()) {
			TraceRecord record = records.get(player);
			if (record.isInProgress) {
				for(UUID uuid : locations.keySet()) {
					List<Location> locationList = record.tntLocations.getOrDefault(uuid, new ArrayList<>());
					locationList.add(locations.get(uuid));
					record.tntLocations.put(uuid, locationList);
				}
				records.put(player, record);
			}
		}
	}

	public static TraceRecord getTraceRecord(Player player) {
		return records.getOrDefault(player, null);
	}

	public static boolean hasTrace(Player player) {
		return records.containsKey(player);
	}

	static HashMap<Player, List<FallingBlock>> spawnedTNTs = new HashMap<>();
	private static FallingBlock spawnTntPreset(Location location) {
		FallingBlock tnt = location.getWorld().spawnFallingBlock(location, Material.TNT.createBlockData());
		tnt.setDropItem(false);
		tnt.setCustomNameVisible(true);
		tnt.setGravity(false);
		tnt.setVelocity(new Vector());
		tnt.setTicksLived(1);
		return tnt;
	}

	private static void showTraceTnt(Player player, Location location, boolean exploded, int id, int tick) {
		FallingBlock tnt = spawnTntPreset(location);
		if(isTraceOptionEnabled(player, TraceOption.SHOW_ID_TAGS)) {
			if (exploded)
				tnt.setCustomName("§7#" + id + "-" + id + " §4§lBOOM!");
			else
				tnt.setCustomName("§7#" + id + "-" + tick);
		} else if (exploded)
			tnt.setCustomName("§4§lBOOM!");
		List<FallingBlock> tntList = spawnedTNTs.getOrDefault(player, new ArrayList<>());
		tntList.add(tnt);
		spawnedTNTs.put(player, tntList);
	}

	private static void showTracePart(Player player, int id, List<Location> locations) {
		int tick = 0;
		int amount = locations.size();
		Location firstLocation = locations.get(0);
		Location lastLocation = locations.get(amount - 1);
		if(!isTraceOptionEnabled(player, TraceOption.SHOW_FUEL)) {
			if (firstLocation.distance(lastLocation) < 10) return;
		}
		if(firstLocation.getWorld() != player.getWorld()) return;
		Location previousLocation = null;
		boolean onlyExplosions = isTraceOptionEnabled(player, TraceOption.ONLY_EXPLOSIONS);
		for(Location location : locations) {
			tick++;
			if(previousLocation != null && location.distance(previousLocation) < 1)
				continue;
			if(onlyExplosions && tick != amount)
				continue;
			showTraceTnt(player, location, tick == amount, id, tick);
			previousLocation = location;
		}
	}

	public static void showTrace(Player player) {
		TraceRecord record = records.getOrDefault(player, null);
		if(record == null) return;
		Integer[] pair = traceMasks.getOrDefault(player.getUniqueId(), new Integer[]{ 0, Integer.MAX_VALUE });
		int id = 0;
		for(UUID tntUuid : record.tntLocations.keySet()) {
			id++;
			if(id < pair[0] || id > pair[1]) continue;
			List<Location> locations = record.tntLocations.get(tntUuid);
			showTracePart(player, id, locations);
		}
		startAntiDespawnTask();
	}

	static BukkitTask antiDespawnTask = null;
	private static void startAntiDespawnTask() {
		if(spawnedTNTs.size() > 0 && antiDespawnTask == null) {
			antiDespawnTask = new BukkitRunnable() {
				@Override
				public void run() {
					for(Player player : spawnedTNTs.keySet()) {
						List<FallingBlock> fallingBlocks = spawnedTNTs.get(player);
						for(FallingBlock fallingBlock : fallingBlocks) {
							fallingBlock.setTicksLived(1);
						}
					}
				}
			}.runTaskTimer(RedTNTTrace.getINSTANCE(), 0, 500);
		}
	}

	public static boolean hideTrace(Player player) {
		if(!spawnedTNTs.containsKey(player)) return false;
		List<FallingBlock> tntList = spawnedTNTs.getOrDefault(player, new ArrayList<>());
		for(FallingBlock tnt : tntList) {
			tnt.remove();
		}
		if(antiDespawnTask != null && spawnedTNTs.size() == 0) {
			antiDespawnTask.cancel();
			antiDespawnTask = null;
		}
		return spawnedTNTs.remove(player) != null;
	}

	public static void hideAllTraces() {
		for(Player player : spawnedTNTs.keySet()) {
			hideTrace(player);
		}
	}

	public static boolean isTraceOptionEnabled(Player player, TraceOption option) {
		return isTraceOptionEnabled(player.getUniqueId(), option);
	}

	public static boolean isTraceOptionEnabled(UUID uuid, TraceOption option) {
		return traceOptions.getOrDefault(uuid, new ArrayList<>()).contains(option);
	}

	public static void enableTraceOption(Player player, TraceOption option) {
		enableTraceOption(player.getUniqueId(), option);
	}

	public static void disableTraceOption(Player player, TraceOption option) {
		disableTraceOption(player.getUniqueId(), option);
	}

	public static void enableTraceOption(UUID uuid, TraceOption option) {
		ArrayList<TraceOption> options = traceOptions.getOrDefault(uuid, new ArrayList<>());
		if(!options.contains(option)) {
			options.add(option);
			traceOptions.put(uuid, options);
		}
	}

	public static void disableTraceOption(UUID uuid, TraceOption option) {
		ArrayList<TraceOption> options = traceOptions.getOrDefault(uuid, new ArrayList<>());
		if(options.contains(option)) {
			options.remove(option);
			traceOptions.put(uuid, options);
		}
	}

	public static void setTraceMask(Player player, int from, int to) {
		setTraceMask(player.getUniqueId(), from, to);
	}

	public static void setTraceMask(UUID uuid, int from, int to) {
		traceMasks.put(uuid, new Integer[]{ from, to });
	}
}
