package com.markehme.spawnercontrol.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.markehme.spawnercontrol.Spawner;
import com.markehme.spawnercontrol.SpawnerControl;
import com.markehme.spawnercontrol.util.Utilities;

public class SCCheck implements CommandExecutor {

	private SpawnerControl sc;

	public SCCheck(SpawnerControl sc) {
		this.sc = sc;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("sccheck")) {
			String owner;

			if (args.length < 1) {
				if (!Utilities.hasPermission(sender, "sc.check")) {
					sender.sendMessage(sc.getMsg("NO_PERMISSION"));
					return true;
				}
				
				// Ensure that the sender is a player, as the console can't be the owner
				if( !( sender instanceof Player ) ) {
					sender.sendMessage(sc.getMsg("NOT_A_PLAYER"));
					return true;
				}
				
				owner = sender.getName();
				
			} else {
				if (!Utilities.hasPermission(sender, "sc.check.others")) {
					sender.sendMessage(sc.getMsg("NO_PERMISSION"));
					return true;
				}

				OfflinePlayer op = sc.getServer().getOfflinePlayer(args[0]);
				if (!op.hasPlayedBefore()) {
					sender.sendMessage(sc.getMsg("PLAYER_NOT_EXIST"));
					return true;
				}

				owner = op.getName();
			}

			HashMap<String, Spawner> spawners = sc.getSpawners();
			HashMap<String, ArrayList<String>> ownerIndex = sc.getOwnerIndex();
			ArrayList<String> toSend = new ArrayList<String>();

			toSend.add(sc.getMsg("BORDER"));
			toSend.add(String.format(sc.getMsg("SPAWNERS_TITLE"), owner));
			toSend.add(sc.getMsg("BORDER"));

			if (ownerIndex.containsKey(owner)) {
				ArrayList<String> ownerData = ownerIndex.get(owner);
				Collections.sort(ownerData);

				for (String key : ownerData) {
					Spawner s = spawners.get(key);
					SimpleDateFormat date = new SimpleDateFormat("MM/dd/yy '@' HH:mm:ss");

					toSend.add(color(String.format(" &a(%d, %d, %d)", s.getLocX(), s.getLocY(), s.getLocZ())));
					toSend.add(color(String.format("  &aType: &f%s", s.getMobType().getName())));
					toSend.add(color(String.format("  &aWorld: &f%s", sc.getServer().getWorld(s.getWorld()).getName())));
					toSend.add(color(String.format("  &aLast Modified: &f&o%s", date.format(s.getDate()))));

					if (ownerData.indexOf(key) != ownerData.size() - 1) {
						toSend.add(" ");
					}
				}
			} else {
				toSend.add(sc.getMsg("NONE"));
			}

			toSend.add(sc.getMsg("BORDER"));

			for (String msg : toSend) {
				sender.sendMessage(msg);
			}
		}

		return true;
	}

	public String color(String msg) {
		return sc.color(msg);
	}

}
