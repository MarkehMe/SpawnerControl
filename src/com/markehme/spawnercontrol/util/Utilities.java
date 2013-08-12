package com.markehme.spawnercontrol.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.markehme.spawnercontrol.SpawnerControl;

public class Utilities {
	public static boolean hasPermission(Player player, String permission) {
		if(SpawnerControl.permission == null) {
			return(player.hasPermission(permission));
		} else {
			return(SpawnerControl.permission.has(player, permission));
		}
	}
	
	public static boolean hasPermission(CommandSender player, String permission) {
		return(hasPermission(Bukkit.getPlayer(player.getName()), permission));
	}
}
