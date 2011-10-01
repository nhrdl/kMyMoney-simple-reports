package com.niranjanrao;

import org.apache.wicket.protocol.http.WebApplication;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see com.niranjanrao.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
	public static ODatabaseDocumentTx db;

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<History> getHomePage() {
		return History.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init() {
		super.init();

		// add your configuration here
	}
}
