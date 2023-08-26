package com.wn.dbml.model;

/**
 * An element that contains settings.
 *
 * @param <T> the element's type of settings
 */
public interface SettingHolder<T extends Setting> {
	/**
	 * Adds the setting to this element, overwriting any previous value.
	 *
	 * @param setting the setting
	 * @param value   the value
	 */
	void addSetting(T setting, String value);
}
