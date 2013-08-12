package com.markehme.spawnercontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.markehme.spawnercontrol.commands.SCCheck;
import com.markehme.spawnercontrol.listeners.BlockListener;
import com.markehme.spawnercontrol.listeners.PlayerListener;
import com.markehme.spawnercontrol.util.ActiveList;
import com.markehme.spawnercontrol.util.Utilities;

/**
 * SpawnerControl - a Bukkit plugin that allows players to place and control mob
 * spawners.
 * 
 * The original plugin was created by Stephen Gardner <StephenBGardner@gmail.com>.
 * 
 * @author Mark Hughes <mark@markeh.me>
 * 
 *         SpawnerControl is free software: you can redistribute it and/or
 *         modify it under the terms of the GNU General Public License as
 *         published by the Free Software Foundation, either version 3 of the
 *         License, or (at your option) any later version.
 * 
 *         SpawnerControl is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 * 
 *         You should have received a copy of the GNU General Public License
 *         along with SpawnerControl. If not, see
 *         <http://www.gnu.org/licenses/>.
 */

public class SpawnerControl extends JavaPlugin {

	private BlockListener bl;
	private PlayerListener pl;
	private Messages msg;

	int writeTask;
	File spawnerFile, indexFile;
	HashMap<String, Spawner> spawners;
	HashMap<String, ArrayList<String>> ownerIndex;

	private Material tool;

	public static Permission permission = null;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();

		getCommand("sccheck").setExecutor(new SCCheck(this));

		bl = new BlockListener(this);
		pl = new PlayerListener(this);
		msg = new Messages(this);

		bl.registerEvents();
		pl.registerEvents();
		msg.loadLang();

		writeTask = 0;
		spawners = new HashMap<String, Spawner>();
		ownerIndex = new HashMap<String, ArrayList<String>>();
		spawnerFile = new File(getDataFolder(), "spawners.db");
		indexFile = new File(getDataFolder(), "index");

		tool = Material.getMaterial(getConfig().getInt("SpawnerControl.Tool", 0));

		try {
			if (!spawnerFile.exists()) {
				spawnerFile.createNewFile();
				writeData();
			}

			if (!indexFile.exists()) {
				indexFile.createNewFile();
				writeData();
			}
		} catch (IOException e) {
			e.printStackTrace();
			log("Unable to access database. Unloading plugin.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		loadSpawnerData();
		loadOwnerIndex();
		
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		
		if (permission != null) {
			log("Hooked into Vault for permissions.");
		}
	}
	
	@Override
	public void onDisable() {
		if (writeTask != 0) {
			getServer().getScheduler().cancelTask(writeTask);
		}

		writeData();
		spawners.clear();
	}

	public void log(String log) {
		getServer().getLogger().info(String.format("[%s] %s", getDescription().getName(), log));
	}

	public String getMsg(String key) {
		return msg.getMsg(key);
	}

	public String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public ActiveList<EntityType> buildAllowed(Player p) {
		ActiveList<EntityType> al = new ActiveList<EntityType>();
		ArrayList<EntityType> wl = new ArrayList<EntityType>();

		// These are the list of entities that are supported
		wl.add(EntityType.BAT);
		wl.add(EntityType.BLAZE);
		wl.add(EntityType.CAVE_SPIDER);
		wl.add(EntityType.CHICKEN);
		wl.add(EntityType.COW);
		wl.add(EntityType.CREEPER);
		wl.add(EntityType.ENDER_DRAGON);
		wl.add(EntityType.ENDERMAN);
		wl.add(EntityType.GHAST);
		wl.add(EntityType.GIANT);
		wl.add(EntityType.IRON_GOLEM);
		wl.add(EntityType.MAGMA_CUBE);
		wl.add(EntityType.MUSHROOM_COW);
		wl.add(EntityType.OCELOT);
		wl.add(EntityType.PIG);
		wl.add(EntityType.PIG_ZOMBIE);
		wl.add(EntityType.SHEEP);
		wl.add(EntityType.SILVERFISH);
		wl.add(EntityType.SKELETON);
		wl.add(EntityType.SLIME);
		wl.add(EntityType.SNOWMAN);
		wl.add(EntityType.SPIDER);
		wl.add(EntityType.SQUID);
		wl.add(EntityType.VILLAGER);
		wl.add(EntityType.WITCH);
		wl.add(EntityType.WITHER);
		wl.add(EntityType.WOLF);
		wl.add(EntityType.ZOMBIE);
		wl.add(EntityType.HORSE);
		
		for (EntityType t : wl) {
			String perm = "sc.set." + t.toString().replaceAll("_", "").toLowerCase();
			if (Utilities.hasPermission(p, perm)) {
				al.add(t);
			}
		}

		return al;
	}

	public void writeData() {
		try {
			FileOutputStream fos;
			ObjectOutputStream oos;

			fos = new FileOutputStream(spawnerFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(spawners);
			oos.close();

			fos = new FileOutputStream(indexFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ownerIndex);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		writeTask = 0;
	}

	public void writeDataAsynchronously() {
		if (writeTask != 0) {
			return;
		}

		writeTask = getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {

			@Override
			public void run() {
				writeData();
			}

		}, 60 * 20L).getTaskId();
	}

	public void loadSpawnerData() {
		try {
			FileInputStream fis = new FileInputStream(spawnerFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			HashMap<?, ?> uncasted = (HashMap<?, ?>) ois.readObject();
			ois.close();

			for (Object key : uncasted.keySet()) {
				if (key instanceof String) {
					Object value = uncasted.get(key);

					if (value instanceof Spawner) {
						spawners.put((String) key, (Spawner) value);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadOwnerIndex() {
		try {
			FileInputStream fis = new FileInputStream(indexFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			HashMap<?, ?> uncasted = (HashMap<?, ?>) ois.readObject();
			ois.close();

			for (Object key : uncasted.keySet()) {
				if (key instanceof String) {
					Object value = uncasted.get(key);

					if (value instanceof ArrayList<?>) {
						ArrayList<String> list = new ArrayList<String>();

						for (Object loc : ((ArrayList<?>) value).toArray()) {
							if (loc instanceof String) {
								list.add((String) loc);
							}
						}

						ownerIndex.put((String) key, list);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String buildKey(UUID world, int locX, int locY, int locZ) {
		// Spawner key is World UUID + locX + locY + locZ
		return String.format("%s%d%d%d", world.toString(), locX, locY, locZ);
	}

	public Spawner getSpawner(Location loc) {
		String key = buildKey(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

		if (spawners.containsKey(key)) {
			return spawners.get(key);
		}

		return null;
	}

	public void addSpawner(Spawner s) {
		String key = buildKey(s.getWorld(), s.getLocX(), s.getLocY(), s.getLocZ());
		ArrayList<String> index = ownerIndex.get(s.getOwner());

		if (index == null) {
			index = new ArrayList<String>();
			ownerIndex.put(s.getOwner(), index);
		}

		index.add(key);
		spawners.put(key, s);
		writeDataAsynchronously();
	}

	public void removeSpawner(Location loc) {
		String key = buildKey(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		String owner = spawners.get(key).getOwner();
		ArrayList<String> index = ownerIndex.get(owner);

		index.remove(key);

		if (index.isEmpty()) {
			ownerIndex.remove(owner);
		}

		spawners.remove(key);
		writeDataAsynchronously();
	}

	public HashMap<String, Spawner> getSpawners() {
		return spawners;
	}

	public HashMap<String, ArrayList<String>> getOwnerIndex() {
		return ownerIndex;
	}

	public Material getTool() {
		return tool;
	}

}
