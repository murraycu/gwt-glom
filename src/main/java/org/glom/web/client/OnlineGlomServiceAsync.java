package org.glom.web.client;

import java.util.ArrayList;

import org.glom.web.shared.GlomDocument;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OnlineGlomServiceAsync {

	void getGlomDocument(AsyncCallback<GlomDocument> callback);

	void getLayoutListHeaders(String table, AsyncCallback<String[]> callback);

	void getTableData(int start, int length, String table, AsyncCallback<ArrayList<String[]>> callback);

	/**
	 * Utility class to get the RPC Async interface from client-side code
	 */
	public static final class Util {
		private static OnlineGlomServiceAsync instance;

		public static final OnlineGlomServiceAsync getInstance() {
			if (instance == null) {
				instance = (OnlineGlomServiceAsync) GWT.create(OnlineGlomService.class);
			}
			return instance;
		}

		private Util() {
			// Utility class should not be instanciated
		}
	}
}