package com.markehme.spawnercontrol.listeners;

import java.util.Date;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.markehme.spawnercontrol.Spawner;
import com.markehme.spawnercontrol.SpawnerControl;
import com.markehme.spawnercontrol.util.ActiveList;
import com.markehme.spawnercontrol.util.Utilities;

public class PlayerListener implements Listener {

	private SpawnerControl sc;

	public PlayerListener(SpawnerControl sc) {
		this.sc = sc;
	}

	public void registerEvents() {
		sc.getServer().getPluginManager().registerEvents(this, sc);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || p.getItemInHand().getType() != sc.getTool()) {
			return;
		}

		Block block = e.getClickedBlock();

		if (block.getType() != Material.MOB_SPAWNER) {
			return;
		}

		CreatureSpawner cs = (CreatureSpawner) block.getState();
		Spawner s = sc.getSpawner(block.getLocation());

		if (s == null) {
			return;
		}

		if (s.getOwner().equals(p.getName()) || Utilities.hasPermission(p, "sc.bypass")) {
			EntityType currentMob = cs.getSpawnedType();
			ActiveList<EntityType> al = sc.buildAllowed(p);

			if (al.isEmpty()) {
				p.sendMessage(sc.getMsg("NO_PERMISSION_SET"));
				return;
			}

			if (al.contains(currentMob)) {
				al.setActive(currentMob);
			}

			al.setActive(al.next());
			s.setMobType(al.getActive());
			s.setDate(new Date());
			cs.setSpawnedType(al.getActive());
			cs.update();

			if (s.getOwner().equals(p.getName()) && currentMob != al.getActive()) {
				p.sendMessage(String.format(sc.getMsg("CHANGED_MONSTER"), currentMob.getName(), al.getActive().getName()));
			} else {
				p.sendMessage(String.format(sc.getMsg("CHANGED_MONSTER_OTHER"), s.getOwner(), currentMob.getName(), al.getActive().getName()));
			}

			sc.writeDataAsynchronously();
			return;
		}

		p.sendMessage(String.format(sc.getMsg("OWNER_IS"), s.getOwner()));
	}

}
