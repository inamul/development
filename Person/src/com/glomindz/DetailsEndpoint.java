package com.glomindz;

import com.glomindz.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "detailsendpoint", namespace = @ApiNamespace(ownerDomain = "glomindz.com", ownerName = "glomindz.com", packagePath = ""))
public class DetailsEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listDetails")
	public CollectionResponse<Details> listDetails(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Details> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Details.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Details>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Details obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Details> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getDetails")
	public Details getDetails(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Details details = null;
		try {
			details = mgr.getObjectById(Details.class, id);
		} finally {
			mgr.close();
		}
		return details;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param details the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertDetails")
	public Details insertDetails(Details details) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsDetails(details)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(details);
		} finally {
			mgr.close();
		}
		return details;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param details the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateDetails")
	public Details updateDetails(Details details) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsDetails(details)) {
			//	throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(details);
		} finally {
			mgr.close();
		}
		return details;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeDetails")
	public void removeDetails(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Details details = mgr.getObjectById(Details.class, id);
			mgr.deletePersistent(details);
		} finally {
			mgr.close();
		}
	}

	private boolean containsDetails(Details details) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Details.class);
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
