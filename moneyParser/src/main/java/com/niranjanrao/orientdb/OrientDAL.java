package com.niranjanrao.orientdb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class OrientDAL {

	public void loadOrientDB(final ODatabaseDocumentTx db,
			final InputStream input) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		final DocumentBuilder builder = domFactory.newDocumentBuilder();
		final Document xmlDoc = builder.parse(input);

		final String[] interestingElements = { "//INSTITUTIONS/INSTITUTION",
				"//PAYEES/PAYEE", "//ACCOUNTS/ACCOUNT",
				"//TRANSACTIONS/TRANSACTION" };

		for (final String query : interestingElements) {
			loadData(query, xmlDoc, db);
		}
		denormalize(db);
	}

	private void denormalize(final ODatabaseDocumentTx db) {
		final List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(
				"select * from PAYEE"));

		for (final ODocument payee : result) {
			final List<ODocument> splits = getSplitsForPayee(payee, db);
			for (final ODocument split : splits) {
				split.field("__payee", payee);
				split.save();
			}
		}

		final List<ODocument> accounts = db
				.query(new OSQLSynchQuery<ODocument>("select * from ACCOUNT"));

		for (final ODocument account : accounts) {
			final List<ODocument> splits = getSplitsForAccount(account, db);
			for (final ODocument split : splits) {
				split.field("__account", account);
				split.save();
			}
		}
	}

	private List<ODocument> getSplitsForAccount(final ODocument account,
			final ODatabaseDocumentTx db) {
		final String query = "select * from SPLIT where account = '"
				+ account.field("id") + "'";
		return db.query(new OSQLSynchQuery<ODocument>(query));
	}

	private List<ODocument> getSplitsForPayee(final ODocument doc,
			final ODatabaseDocumentTx db) {
		final String query = "select * from SPLIT where payee = '"
				+ doc.field("id") + "'";
		return db.query(new OSQLSynchQuery<ODocument>(query));
	}

	private void loadData(final String query, final Document xmlDoc,
			final ODatabaseDocumentTx db) throws XPathExpressionException {
		final XPathFactory factory = XPathFactory.newInstance();
		final XPath xpath = factory.newXPath();
		final XPathExpression expr = xpath.compile(query);

		final Object result = expr.evaluate(xmlDoc, XPathConstants.NODESET);
		final NodeList nodes = (NodeList) result;
		Element element;
		NamedNodeMap attrMap;
		NodeList children;
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			element = (Element) nodes.item(i);
			final ODocument doc = db.newInstance(element.getNodeName());
			attrMap = element.getAttributes();
			addAttributes(doc, attrMap);
			children = element.getElementsByTagName("*");
			addChildElements(children, doc, db);
			doc.save();
		}
	}

	private void addChildElements(final NodeList children,
			final ODocument parentDoc, final ODatabaseDocumentTx db) {
		ODocument childDoc;
		Element element;
		NamedNodeMap attrMap;
		// final ODocument[] childrenDocs = new ODocument[children.getLength()];
		final ArrayList<ODocument> list = new ArrayList<ODocument>();

		for (int i = 0, max = children.getLength(); i < max; i++) {
			element = (Element) children.item(i);
			childDoc = db.newInstance(element.getNodeName());
			attrMap = element.getAttributes();
			addAttributes(childDoc, attrMap);
			list.add(childDoc);
			// parentDoc.field(element.getNodeName(), childDoc);
			addChildElements(element.getElementsByTagName("*"), childDoc, db);
		}

		parentDoc.field("__CHILDREN", list);
	}

	final Pattern NUMBER_PATTERN = Pattern
			.compile("^-?\\p{Digit}+/\\p{Digit}+");

	private void addAttributes(final ODocument doc, final NamedNodeMap attrMap) {
		Node attr;
		Object toWrite;
		for (int i = 0, max = attrMap.getLength(); i < max; i++) {
			attr = attrMap.item(i);
			final String value = attr.getTextContent();
			if (value.trim().isEmpty()) {
				continue;
			}
			toWrite = value;
			final String name = attr.getNodeName();
			final Matcher m = NUMBER_PATTERN.matcher(value);

			if (name.equals("shares") || name.equals("value") && m.matches()) {
				final String[] data = value.split("/");
				final float numerator = Float.parseFloat(data[0]), denominator = Float
						.parseFloat(data[1]);
				final float units = numerator / denominator;
				toWrite = units;
			}
			doc.field(attr.getNodeName(), toWrite);
		}
	}

}
