/*
 * Copyright (C) 2011 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.shared.layout;

import java.io.Serializable;

/**
 * Represents the libglom LayoutItem_Field class.
 * 
 * @author Ben Konrath <ben@bagu.org>
 */
@SuppressWarnings("serial")
public class LayoutItemField extends LayoutItemWithFormatting implements Serializable {
	public enum GlomFieldType {
		TYPE_INVALID, TYPE_NUMERIC, TYPE_TEXT, TYPE_DATE, TYPE_TIME, TYPE_BOOLEAN, TYPE_IMAGE;
	}

	private GlomFieldType type;
	private boolean addNavigation;
	private String navigationTableName = null;

	public GlomFieldType getType() {
		return type;
	}

	public void setType(GlomFieldType type) {
		this.type = type;
	}

	public boolean getAddNavigation() {
		return addNavigation;
	}

	public void setAddNavigation(boolean addNavigation) {
		this.addNavigation = addNavigation;
	}

	public String getNavigationTableName() {
		return navigationTableName;
	}

	public void setNavigationTableName(String navigationTableName) {
		this.navigationTableName = navigationTableName;
	}

}
