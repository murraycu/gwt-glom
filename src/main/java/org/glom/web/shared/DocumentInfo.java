/*
 * Copyright (C) 2010, 2011 Openismus GmbH
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

package org.glom.web.shared;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * DTO to hold basic information about tables of a Glom document.
 */
@SuppressWarnings("serial")
public class DocumentInfo implements Serializable {
	private String title;
	// could consider a LinkedHashMap if we need to support adding or removing tables
	// order must be consistent between these two arrays
	private ArrayList<String> tableNames;
	private ArrayList<String> tableTitles;
	private int defaultTableIndex;

	private ArrayList<String> localeIDs;
	private ArrayList<String> localeTitles;

	public DocumentInfo() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public ArrayList<String> getTableNames() {
		return tableNames;
	}

	public void setTableNames(final ArrayList<String> tableNames) {
		this.tableNames = tableNames;
	}

	public ArrayList<String> getTableTitles() {
		return tableTitles;
	}

	public void setTableTitles(final ArrayList<String> tableTitles) {
		this.tableTitles = tableTitles;
	}

	public int getDefaultTableIndex() {
		return defaultTableIndex;
	}

	public void setDefaultTableIndex(final int defaultTable) {
		this.defaultTableIndex = defaultTable;
	}

	public ArrayList<String> getLocaleIDs() {
		return localeIDs;
	}

	public void setLocaleIDs(final ArrayList<String> localeIDs) {
		this.localeIDs = localeIDs;
	}

	public ArrayList<String> getLocaleTitles() {
		return localeTitles;
	}

	public void setLocaleTitles(final ArrayList<String> localeTitles) {
		this.localeTitles = localeTitles;
	}

}
