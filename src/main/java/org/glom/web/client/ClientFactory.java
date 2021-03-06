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

package org.glom.web.client;

import org.glom.web.client.ui.DetailsView;
import org.glom.web.client.ui.DocumentLoginView;
import org.glom.web.client.ui.DocumentSelectionView;
import org.glom.web.client.ui.ListView;
import org.glom.web.client.ui.ReportView;
import org.glom.web.client.ui.TableSelectionView;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

/**
 * This creates the various views and other objects for OnlineGlom.
 *
 * GWT.create(ClientFactory.class) actually instantiates ClientFactoryImpl (or an alternative) according to the rules in
 * our OnlineGlom.gwt.xml. See http://code.google.com/webtoolkit/doc/latest/DevGuideMvpActivitiesAndPlaces.html
 *
 * The *View types returned here are just interfaces, allowing alternative ClientFactory implementations to return
 * instances of different actual view classes.
 */
public interface ClientFactory {

	EventBus getEventBus();

	PlaceController getPlaceController();

	DocumentSelectionView getDocumentSelectionView();

	DocumentLoginView getDocumentLoginView();

	TableSelectionView getTableSelectionView();

	ListView getListView();

	DetailsView getDetailsView();

	ReportView getReportView();

}
