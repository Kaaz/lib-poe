package org.libpoe.model;

import org.libpoe.model.item.Item;

/**
 * User: Johan
 * Date: 2013-12-02
 * Time: 09:44
 */
public class StashTab {

	private int numTabs = 0;
	private Item[] items;

	public Item[] getItems() {
		return items;
	}

	public int getNumTabs() {
		return numTabs;
	}
}
