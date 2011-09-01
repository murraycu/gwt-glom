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

package org.glom.web.client.ui.details;

import org.glom.web.shared.layout.LayoutItemPortal;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class Portal extends Composite {
	private FlowPanel mainPanel = new FlowPanel();

	@SuppressWarnings("unused")
	private Portal() {
		// disable default constructor
	}

	/**
	 * Creates a new widget for a Portal.
	 * 
	 * @param layoutItemPortal
	 *            The DTO that holds the Portal layout information
	 * @param mainTitleSet
	 *            true if the main title for the Group has been set, false if hasn't been set yet
	 */
	public Portal(LayoutItemPortal layoutItemPortal, boolean mainTitleSet) {
		mainPanel.setStyleName("subgroup");
		mainPanel.setHeight("12em");

		// using the same style as the (sub)group-title and group-contents elements
		Label portalTitle = new Label(layoutItemPortal.getTitle());
		portalTitle.setStyleName(mainTitleSet ? "subgroup-title" : "group-title");
		mainPanel.add(portalTitle);

		FlowPanel portalContents = new FlowPanel();
		portalContents.setStyleName("group-contents");

		// TODO add proper CellTable
		Label tempPortalData = new Label("Portal Data");
		tempPortalData.getElement().getStyle().setFontSize(2, Unit.EM);
		portalContents.add(tempPortalData);

		mainPanel.add(portalContents);
		initWidget(mainPanel);
	}

}
