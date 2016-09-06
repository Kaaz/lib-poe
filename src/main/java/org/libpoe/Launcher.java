package org.libpoe;

import org.libpoe.model.StashTab;
import org.libpoe.model.item.Item;
import org.libpoe.model.property.MinMaxProperty;
import org.libpoe.model.property.Property;
import org.libpoe.net.AuthInfo;
import org.libpoe.net.DataReader;
import org.libpoe.util.League;

import java.util.*;


public class Launcher {

	public static void main(String... args) throws Exception {

		HashMap<String, Integer> currency = new HashMap<>();
		AuthInfo account = new AuthInfo("0b51d227326eb301938baf98a4e6ac98");
		DataReader reader = new DataReader(account);
		if (!reader.authenticate()) {
			return;
		}
		int max = 1;
		for (int i = 0; i < max; i++) {
			StashTab stashTab = reader.getStashTab(League.ESSENCE.getId(), i);
			if (stashTab != null) {
				if (max == 1) {
					max = stashTab.getNumTabs();
				}
				System.out.println((i+1) + " out of " + stashTab.getNumTabs());
				for (Item item : stashTab.getItems()) {
					if (item.getTypeLine().toLowerCase().contains("orb")) {
						Property property = item.getProperty("Stack Size");
						if (property instanceof MinMaxProperty) {
							MinMaxProperty p = (MinMaxProperty) property;
							if (!currency.containsKey(item.getTypeLine())) {
								currency.put(item.getTypeLine(), 0);
							}
							currency.put(item.getTypeLine(), currency.get(item.getTypeLine()) + p.getMinValue());
						}
					}
				}
			}
			else{
				System.out.println("STASH TAB GOT EBOLA!!!!");
			}
		}
		sortByValue(currency).forEach((k, v) -> System.out.println(String.format("%-20s %s", k, v)));

	}

	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, (o1, o2) -> -(o1.getValue()).compareTo(o2.getValue()));

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
