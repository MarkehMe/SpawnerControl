package com.markehme.spawnercontrol.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.markehme.spawnercontrol.Spawner;
import com.markehme.spawnercontrol.SpawnerControl;
import com.markehme.spawnercontrol.util.ActiveList;
import com.markehme.spawnercontrol.util.Utilities;

public class BlockListener implements Listener {

	private SpawnerControl sc;

	public BlockListener(SpawnerControl sc) {
		this.sc = sc;
	}

	public void registerEvents() {
		sc.getServer().getPluginManager().registerEvents(this, sc);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Block block = e.getBlockPlaced();
		Player p = e.getPlayer();

		if (block.getType() != Material.MOB_SPAWNER) {
			return;
		}

		CreatureSpawner cs = (CreatureSpawner) block.getState();
		ActiveList<EntityType> al = sc.buildAllowed(p);

		if (al.isEmpty()) {
			p.sendMessage(sc.getMsg("NO_PERMISSION_PLACE"));
			e.setCancelled(true);
			return;
		}

		if (!al.contains(cs.getSpawnedType())) {
			cs.setSpawnedType(al.getActive());
		}

		sc.addSpawner(new Spawner(p.getName(), cs.getSpawnedType(), cs.getLocation()));
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Location loc = e.getBlock().getLocation();
		Player p = e.getPlayer();

		if (e.getBlock().getType() != Material.MOB_SPAWNER) {
			return;
		}

		Spawner s = sc.getSpawner(loc);

		if (s == null) {
			return;
		}

		if (s.getOwner().equals(p.getName()) || Utilities.hasPermission(p, "sc.bypass")) {
			sc.removeSpawner(loc);

			if (!s.getOwner().equals(p.getName())) {
				p.sendMessage(String.format(sc.getMsg("DESTROYED"), s.getOwner(), s.getMobType().getName()));
			}

			if (p.getGameMode() == GameMode.CREATIVE) {
				return;
			}

			ItemStack drop = new ItemStack(Material.MOB_SPAWNER, 1);
			p.getWorld().dropItem(loc, drop);
			e.setExpToDrop(0);
		} else {
			e.setCancelled(true);
			p.sendMessage(sc.getMsg("NOT_OWNER"));
		}
	}

}
