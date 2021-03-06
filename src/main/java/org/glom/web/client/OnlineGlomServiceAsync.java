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

import java.util.ArrayList;

import org.glom.web.shared.DataItem;
import org.glom.web.shared.DetailsLayoutAndData;
import org.glom.web.shared.DocumentInfo;
import org.glom.web.shared.Documents;
import org.glom.web.shared.NavigationRecord;
import org.glom.web.shared.Reports;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OnlineGlomServiceAsync {

	/**
	 * Utility class to get the RPC Async interface from client-side code
	 */
	final class Util {
		private static OnlineGlomServiceAsync instance;

		public static OnlineGlomServiceAsync getInstance() {
			if (instance == null) {
				instance = GWT.create(OnlineGlomService.class);
			}
			return instance;
		}

		private Util() {
			// Utility class should not be instantiated
		}
	}

	void getConfigurationErrorMessage(AsyncCallback<String> callback);

	void getDetailsData(String documentID, String tableName, TypedDataItem primaryKeyValue,
			AsyncCallback<DataItem[]> callback);

	void getDetailsLayoutAndData(String documentID, String tableName, TypedDataItem primaryKeyValue, String localeID,
			AsyncCallback<DetailsLayoutAndData> callback);

	void getDocumentInfo(String documentID, String localeID, AsyncCallback<DocumentInfo> callback);

	void getDocuments(AsyncCallback<Documents> callback);

	void getListViewData(String documentID, String tableName, String quickFind, int start, int length,
			int sortColumnIndex, boolean isAscending, AsyncCallback<ArrayList<DataItem[]>> callback);

	void getListViewLayout(String documentID, String tableName, final String localeID,
			AsyncCallback<LayoutGroup> callback);

	void getRelatedListData(String documentID, String tableName, LayoutItemPortal portal,
			TypedDataItem foreignKeyValue, int start, int length, int sortColumnIndex, boolean ascending,
			AsyncCallback<ArrayList<DataItem[]>> callback);

	void getRelatedListRowCount(String documentID, String tableName, LayoutItemPortal portal,
			TypedDataItem foreignKeyValue, AsyncCallback<Integer> callback);

	void getReportHTML(String documentID, String tableName, String reportName, String quickFind, String localeID,
			AsyncCallback<String> callback);

	void getReportsList(String documentID, String tableName, String localeID, AsyncCallback<Reports> callback);

	void getSuitableRecordToViewDetails(String documentID, String tableName, LayoutItemPortal portal,
			TypedDataItem primaryKeyValue, AsyncCallback<NavigationRecord> callback);
}
