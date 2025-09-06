package com.wn.dbml;

import java.util.HashSet;
import java.util.List;

public final class CollectionUtil {
	private CollectionUtil() {
	}
	
	public static <T> T firstDuplicate(List<T> list) {
		var set = new HashSet<T>();
		for (var s : list) {
			var added = set.add(s);
			if (!added) return s;
		}
		return null;
	}
}
