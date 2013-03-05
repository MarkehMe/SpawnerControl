package com.github.stephengardner.spawnercontrol;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class Spawner implements Serializable {

	private static final long serialVersionUID = -4503074785605572337L;

	private String owner;
	private EntityType mobType;
	private UUID world;
	private int locX;
	private int locY;
	private int locZ;
	private Date lastUpdate;

	public Spawner(String owner, EntityType mobType, Location loc) {
		world = loc.getWorld().getUID();
		locX = loc.getBlockX();
		locY = loc.getBlockY();
		locZ = loc.getBlockZ();

		setOwner(owner);
		setMobType(mobType);
		setDate(new Date());
	}

	public UUID getWorld() {
		return world;
	}

	public int getLocX() {
		return locX;
	}

	public int getLocY() {
		return locY;
	}

	public int getLocZ() {
		return locZ;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public EntityType getMobType() {
		return mobType;
	}

	public void setMobType(EntityType mobType) {
		this.mobType = mobType;
	}

	public Date getDate() {
		return lastUpdate;
	}

	public void setDate(Date date) {
		this.lastUpdate = date;
	}

}
