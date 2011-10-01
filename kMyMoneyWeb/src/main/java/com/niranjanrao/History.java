package com.niranjanrao;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.niranjanrao.orientdb.OrientDAL;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class History extends WebPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void renderHead(final IHeaderResponse response) {
		// <script language="javascript" type="text/javascript"
		// src="./flot/jquery.js"></script>
		// <script language="javascript" type="text/javascript"
		// src="./flot/jquery.flot.js"></script>
		//
		// <script language="javascript" type="text/javascript"
		// src="./flot/jquery.flot.pie.js"></script>

		response.renderJavaScriptReference(new PackageResourceReference(
				History.class, "js/flot/jquery.js"));
		response.renderJavaScriptReference(new PackageResourceReference(
				History.class, "js/flot/jquery.flot.js"));
		response.renderJavaScriptReference(new PackageResourceReference(
				History.class, "js/flot/jquery.flot.pie.js"));
		response.renderJavaScriptReference(new PackageResourceReference(
				History.class, "js/initHistory.js"));
		response.renderJavaScript(getHistory(), "history");
		super.renderHead(response);
	}

	private CharSequence getHistory() {
		final ODatabaseDocumentTx db = WicketApplication.db;

		try {
			final OrientDAL dal = new OrientDAL();
			final ODocument doc = dal.buildAccountData(db, "AStd::Expense");
			return "var myData = " + doc.toJSON("fetchPlan:*:-1") + ";";
		} finally {
			db.commit();
			// db.close();
		}
	}
}
