package com.github.stephengardner.spawnercontrol;

import java.util.HashMap;

public class Messages {

	private SpawnerControl sc;
	private HashMap<String, String> messages;

	public Messages(SpawnerControl sc) {
		this.sc = sc;
		messages = new HashMap<String, String>();
	}

	public void loadLang() {
		messages.put("BORDER", color(sc.getConfig().getString("BORDER", "&2------------------------------------------")));
		messages.put("CHANGED_MONSTER", color(sc.getConfig().getString("CHANGED_MONSTER", "Changed &a%s &fspawner to a &a%s &fspawner.")));
		messages.put("CHANGED_MONSTER_OTHER", color(sc.getConfig().getString("CHANGED_MONSTER_OTHER", "Changed &6%s's &a%s &fspawner to a &a%s &fspawner.")));
		messages.put("DESTROYED", color(sc.getConfig().getString("DESTROYED", "Destroyed &6%s's &a%s &fspawner.")));
		messages.put("NO_PERMISSION_PLACE", color(sc.getConfig().getString("NO_PERMISSION_PLACE", "&cYou do not have permission to place spawners.")));
		messages.put("NO_PERMISSION_SET", color(sc.getConfig().getString("NO_PERMISSION_SET", "&cYou do not have permission to set spawners.")));
		messages.put("NOT_OWNER", color(sc.getConfig().getString("NOT_OWNER", "This spawner does not belong to you!")));
		messages.put("OWNER_IS", color(sc.getConfig().getString("OWNER_IS", "&cThis spawner belongs to &6%s&f.")));
		messages.put("NONE", color(sc.getConfig().getString("NONE", " &oNone")));
		messages.put("NO_PERMISSION", color(sc.getConfig().getString("NO_PERMISSION", "&cYou do not have permission to pass this command.")));
		messages.put("PLAYER_NOT_EXIST", color(sc.getConfig().getString("PLAYER_NOT_EXIST", "&cPlayer doesn't exist.")));
		messages.put("SPAWNERS_TITLE", color(sc.getConfig().getString("SPAWNERS_TITLE", " &5Spawners - %s")));
	}

	public String color(String msg) {
		return sc.color(msg);
	}

	public String getMsg(String key) {
		return messages.get(key);
	}

}
