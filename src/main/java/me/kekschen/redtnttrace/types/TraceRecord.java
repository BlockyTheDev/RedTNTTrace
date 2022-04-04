package me.kekschen.redtnttrace.types;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TraceRecord {
	public boolean isInProgress;
	public HashMap<UUID, List<Location>> tntLocations = new HashMap<>();
}
