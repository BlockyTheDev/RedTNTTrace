package me.kekschen.redtnttrace.listeners;

import me.kekschen.redtnttrace.managers.TraceManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		TraceManager.hideTrace(event.getPlayer());
	}

	@EventHandler
	public void onTntExplode(EntityExplodeEvent event) {
		if(event.getEntity() instanceof TNTPrimed) {
			TraceManager.reportTNTLocation(event.getEntity().getUniqueId(), event.getLocation());
		}
	}

	@EventHandler
	public void onEntityBlockChangeEvent(EntityChangeBlockEvent e) {
		if(e.getEntityType() != EntityType.FALLING_BLOCK) return;
		FallingBlock fallingBlock = (FallingBlock) e.getEntity();
		if(fallingBlock.getBlockData().getMaterial() == Material.TNT) {
			e.setCancelled(true);
		}
	}
}
