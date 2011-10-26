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

package org.glom.web.shared;

import java.io.Serializable;
import java.util.ArrayList;

import org.glom.web.shared.layout.LayoutGroup;

/**
 * Wrapper DTO for details view layout and data.
 * 
 * @author Ben Konrath <ben@bagu.org>
 */
@SuppressWarnings("serial")
public class DetailsLayoutAndData implements Serializable {
	private ArrayList<LayoutGroup> layout;
	private DataItem[] data;

	public ArrayList<LayoutGroup> getLayout() {
		return layout;
	}

	public void setLayout(ArrayList<LayoutGroup> layout) {
		this.layout = layout;
	}

	public DataItem[] getData() {
		return data;
	}

	public void setData(DataItem[] data) {
		this.data = data;
	}

}