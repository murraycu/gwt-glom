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

package org.glom.web.client.ui;

import java.util.List;

import org.glom.web.client.ui.details.DetailsCell;
import org.glom.web.client.ui.details.Portal;
import org.glom.web.shared.libglom.layout.LayoutGroup;

/**
 *
 */
public interface DetailsView extends View {

	void addGroup(LayoutGroup layoutGroup);

	List<DetailsCell> getCells();

	List<Portal> getPortals();
}
