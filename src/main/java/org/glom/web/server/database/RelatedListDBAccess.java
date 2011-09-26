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

package org.glom.web.server.database;

import java.util.ArrayList;

import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.FieldVector;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItem;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_Portal;
import org.glom.libglom.Relationship;
import org.glom.libglom.SortClause;
import org.glom.web.server.Log;
import org.glom.web.shared.DataItem;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class RelatedListDBAccess extends ListDBAccess {
	private String foreignKeyValue = null;
	private LayoutItem_Portal portal = null;
	private String parentTable = null;
	private String whereClauseToTableName = null;
	private Field whereClauseToKeyField = null;

	public RelatedListDBAccess(Document document, String documentID, ComboPooledDataSource cpds, String tableName,
			String relationshipName) {
		super(document, documentID, cpds, tableName);

		// find the LayoutItem_Portal for the related list name
		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("details", tableName);
		// LayoutItem_Portal portal = null;
		for (int i = 0; i < layoutGroupVec.size(); i++) {
			org.glom.libglom.LayoutGroup layoutGroup = layoutGroupVec.get(i);
			portal = getPortalForRelationship(relationshipName, layoutGroup);
			if (portal != null) {
				break;
			}
		}

		if (portal == null) {
			Log.error(documentID, tableName, "Couldn't find LayoutItem_Portal \"" + relationshipName + "\" in table \""
					+ tableName + "\". " + "Cannot retrive data for the related list.");
			return;
		}

		parentTable = tableName;
		// Reassign the tableName variable to table that is being used for the related list. This needs to be set before
		// getFieldsToShowForSQLQuery().
		this.tableName = portal.get_table_used("" /* parent table - not relevant */);

		// Convert the libglom LayoutGroup object into a LayoutFieldVector suitable for SQL queries.
		LayoutGroupVector tempLayoutGroupVec = new LayoutGroupVector();
		tempLayoutGroupVec.add(portal);
		fieldsToGet = getFieldsToShowForSQLQuery(tempLayoutGroupVec);

		/*
		 * The code from the rest of this method was ported from Glom: Base_DB::set_found_set_where_clause_for_portal()
		 */
		Relationship relationship = portal.get_relationship();

		// Notice that, in the case that this is a portal to doubly-related records,
		// The WHERE clause mentions the first-related table (though by the alias defined in extra_join)
		// and we add an extra JOIN to mention the second-related table.

		whereClauseToTableName = relationship.get_to_table();
		whereClauseToKeyField = getFieldInTable(relationship.get_to_field(), relationship.get_to_table());

		Relationship relationshipRelated = portal.get_related_relationship();
		if (relationshipRelated != null) {
			// FIXME port this Glom code to Java
			// @formatter:off
			/* 
		    //Add the extra JOIN:
		    sharedptr<UsesRelationship> uses_rel_temp = sharedptr<UsesRelationship>::create();
		    uses_rel_temp->set_relationship(relationship);
		    found_set.m_extra_join = relationship;

		    //Adjust the WHERE clause appropriately for the extra JOIN:
		    whereClauseToTableName = uses_rel_temp->get_sql_join_alias_name();

		    const Glib::ustring to_field_name = uses_rel_temp->get_to_field_used();
		    where_clause_to_key_field = get_fields_for_table_one_field(relationship->get_to_table(), to_field_name);
		    std::cout << "extra_join=" << found_set.m_extra_join << std::endl;
		    std::cout << "extra_join where_clause_to_key_field=" << where_clause_to_key_field->get_name() << std::endl;
		    */
		    // @formatter:on
		}

	}

	public ArrayList<DataItem[]> getData(int start, int length, String foreignKeyValue, boolean useSortClause,
			int sortColumnIndex, boolean isAscending) {

		if (tableName == null || foreignKeyValue == null || foreignKeyValue.isEmpty()) {
			return null;
		}

		// Set the foreignKeyValue
		this.foreignKeyValue = foreignKeyValue;

		return getListData(start, length, useSortClause, sortColumnIndex, isAscending);
	}

	private LayoutItem_Portal getPortalForRelationship(String relationshipName, org.glom.libglom.LayoutGroup layoutGroup) {

		if (relationshipName == null)
			return null;

		LayoutItemVector items = layoutGroup.get_items();
		for (int i = 0; i < items.size(); i++) {
			LayoutItem layoutItem = items.get(i);

			LayoutItem_Field layoutItemField = LayoutItem_Field.cast_dynamic(layoutItem);
			if (layoutItemField != null) {
				// the layoutItem is a LayoutItem_Field
				continue;

			} else {
				// the layoutItem is not a LayoutItem_Field
				org.glom.libglom.LayoutGroup subLayoutGroup = org.glom.libglom.LayoutGroup.cast_dynamic(layoutItem);
				if (subLayoutGroup != null) {
					// the layoutItem is a LayoutGroup
					LayoutItem_Portal layoutItemPortal = LayoutItem_Portal.cast_dynamic(layoutItem);
					if (layoutItemPortal != null) {
						// The subGroup is a LayoutItem_Protal
						if (relationshipName.equals(layoutItemPortal.get_relationship_name_used())) {
							// yey, we found it!
							return layoutItemPortal;
						}
					} else {
						// The subGroup is not a LayoutItem_Portal.
						LayoutItem_Portal retval = getPortalForRelationship(relationshipName, subLayoutGroup);
						if (retval != null) {
							return retval;
						}
					}
				}
			}
		}

		// the LayoutItem_Portal with relationshipName was not found
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getExpectedResultSize()
	 */
	public int getExpectedResultSize(String foreignKeyValue) {

		// Set the foreignKeyValue
		this.foreignKeyValue = foreignKeyValue;

		if (fieldsToGet == null || fieldsToGet.size() <= 0 || this.foreignKeyValue == null)
			return -1;

		return getResultSizeOfSQLQuery();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getSelectQuery(org.glom.libglom.LayoutFieldVector,
	 * org.glom.libglom.SortClause)
	 */
	@Override
	protected String getSelectQuery(SortClause sortClause) {

		if (portal == null) {
			Log.error(documentID, parentTable,
					"The Portal has not been found. Cannot build query for the related list.");
			return "";
		}

		if (foreignKeyValue == null || foreignKeyValue.isEmpty()) {
			Log.error(documentID, parentTable,
					"The value for the foreign key has not been set. Cannot build query for the related list.");
			return "";
		}

		return Glom.build_sql_select_with_where_clause(tableName, fieldsToGet, whereClauseToTableName,
				whereClauseToKeyField, foreignKeyValue, null, sortClause);

	}

	private Field getFieldInTable(String fieldName, String tableName) {

		if (tableName.isEmpty() || fieldName.isEmpty())
			return null;

		FieldVector fields = document.get_table_fields(tableName);
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			if (fieldName.equals(field.get_name())) {
				return field;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getCountQuery()
	 */
	@Override
	protected String getCountQuery() {
		if (portal == null) {
			Log.error(documentID, parentTable,
					"The Portal has not been found. Cannot build query for the related list.");
			return "";
		}

		if (foreignKeyValue == null || foreignKeyValue.isEmpty()) {
			Log.error(documentID, parentTable,
					"The value for the foreign key has not been set. Cannot build query for the related list.");
			return "";
		}

		return Glom.build_sql_select_count_with_where_clause(tableName, fieldsToGet, whereClauseToTableName,
				whereClauseToKeyField, foreignKeyValue);
	}

}
