package com.github.stephengardner.spawnercontrol.util;

import java.util.ArrayList;
import java.util.Collection;

public class ActiveList<E> {

	private ArrayList<E> activeList;
	private E active;

	public ActiveList() {
		activeList = new ArrayList<E>();
		updateActive();
	}

	public E getActive() {
		return active;
	}

	public void setActive(E active) {
		this.active = active;
	}

	public Boolean add(E e) {
		if (!activeList.contains(e)) {
			activeList.add(e);
			updateActive();
			return true;
		}

		return false;
	}

	public Boolean addAll(Collection<? extends E> c) {
		Boolean changed = false;

		for (E e : c) {
			if (!activeList.contains(e)) {
				activeList.add(e);
				changed = true;
			}
		}

		updateActive();
		return changed;
	}

	public Boolean contains(E e) {
		return activeList.contains(e);
	}

	public Boolean isEmpty() {
		return activeList.isEmpty();
	}

	public E next() {
		E e;

		if (active.equals(activeList.get(activeList.size() - 1))) {
			e = activeList.get(0);
		} else {
			e = activeList.get(activeList.indexOf(active) + 1);
		}

		return e;
	}

	private void updateActive() {
		if (activeList.size() > 0) {
			if (active == null || !activeList.contains(active)) {
				active = activeList.get(0);
			}
		}

		if (activeList.size() == 0) {
			active = null;
		}
	}

}
