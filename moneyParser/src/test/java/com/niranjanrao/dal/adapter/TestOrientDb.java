package com.niranjanrao.dal.adapter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.niranjanrao.orientdb.OrientDAL;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class TestOrientDb {

	@Test
	public void testOrientDB() {
		final ODatabaseDocumentTx db = new ODatabaseDocumentTx(
				"memory:moneyScript");

		db.create();
		db.addPhysicalCluster("test");

		final ODocument doc = db.newInstance();

		doc.field("a", "1");
		doc.field("b", "2");

		doc.save("test");

		System.out.println(doc.toJSON());
		db.close();
		db.delete();
	}

	static final Logger log = LoggerFactory.getLogger(TestOrientDb.class);

	// @Test
	public void testOrientDBXML() throws IOException,
			ParserConfigurationException, SAXException,
			XPathExpressionException {
		final ODatabaseDocumentTx db = new ODatabaseDocumentTx(
				"memory:moneyScript");

		db.create();
		// db.open("admin", "admin");
		final String CLUSTER_NAME = "test";
		db.addPhysicalCluster(CLUSTER_NAME);
		final InputStream input = TestReadXml.class
				.getResourceAsStream("/institution.xml");
		assertNotNull("Could not read the stream", input);
		try {

			final OrientDAL rdr = new OrientDAL();
			rdr.loadOrientDB(db, input);

			db.browseClass("INSTITUTION").setFetchPlan("*:-1 __CHILDREN:-1");
			log.debug("DB:{}", db.toString());
			for (final ODocument doc : db.browseClass("INSTITUTION")) {
				log.debug(doc
						.toJSON("rid,version,class,type,attribSameRow,fetchPlan:*:-1"));
			}
			for (final ODocument doc : db.browseClass("ADDRESS")) {
				log.debug(doc.toJSON());
			}
			db.browseClass("ACCOUNTIDS").setFetchPlan("*:-1");
			for (final ODocument doc : db.browseClass("ACCOUNTIDS")) {
				log.debug(doc
						.toJSON("rid,version,class,type,attribSameRow,fetchPlan:*:-1"));
			}
		} finally {
			input.close();
			db.close();
			db.delete();
		}
	}

	@Test
	public void testRegex() {
		final Pattern NUMBER_PATTERN = Pattern
				.compile("^\\p{Digit}+/\\p{Digit}+");
		final String[] toMatch = { "000000000  0512799453", "608731/100" };

		log.debug("Matching {} entries", toMatch.length);

		final Matcher m = NUMBER_PATTERN.matcher("608731/100");
		assertTrue("Oops should have matched", m.matches());
	}

	@Test
	public void testOrientDBActualFile() throws IOException,
			ParserConfigurationException, SAXException,
			XPathExpressionException {
		final ODatabaseDocumentTx db = new ODatabaseDocumentTx(
				"memory:moneyScript");

		db.create();
		// db.open("admin", "admin");
		final String CLUSTER_NAME = "test";
		db.addPhysicalCluster(CLUSTER_NAME);
		final FileInputStream input = new FileInputStream(
				"/media/truecrypt/niranjan.kmy.xml");
		try {

			final OrientDAL rdr = new OrientDAL();
			rdr.loadOrientDB(db, input);

			final List<ODocument> result = db
					.query(new OSQLSynchQuery<ODocument>(
							"select * from SPLIT where __payee.name = 'BHARAT BAZAR 6500000FREMONT'"));
			for (final ODocument obj : result) {
				log.debug("Result:" + obj.toJSON());
			}
		} finally {
			input.close();
			db.close();
			db.delete();
		}
	}
}
