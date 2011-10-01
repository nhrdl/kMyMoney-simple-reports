package com.niranjanrao.orientdb;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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

	interface IValueConverter {
		public Object convert(String value);

	}

	HashMap<String, IValueConverter> valueConverters;

	public OrientDAL() {
		valueConverters = new HashMap<String, OrientDAL.IValueConverter>();
		final IValueConverter numberConverter = new IValueConverter() {

			@Override
			public Object convert(final String value) {
				final Matcher m = NUMBER_PATTERN.matcher(value);

				if (m.matches()) {
					final String[] data = value.split("/");
					final float numerator = Float.parseFloat(data[0]), denominator = Float
							.parseFloat(data[1]);
					final float units = numerator / denominator;
					return units;
				}
				return value;
			}
		};

		valueConverters.put("shares", numberConverter);
		valueConverters.put("value", numberConverter);

		final IValueConverter dateConverter = new IValueConverter() {

			@Override
			public Object convert(final String value) {
				final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

				try {
					return formatter.parse(value);
				} catch (final ParseException e) {
					e.printStackTrace();
					return value;
				}
			}
		};

		valueConverters.put("postdate", dateConverter);
		valueConverters.put("entrydate", dateConverter);
		valueConverters.put("opened", dateConverter);

	}

	public void loadOrientDB(final ODatabaseDocumentTx db,
			final InputStream input) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		final DocumentBuilder builder = domFactory.newDocumentBuilder();
		final Document xmlDoc = builder.parse(input);

		loadAccounts(db, xmlDoc);

		loadTransactions(db, xmlDoc);

		// loadInstitutions(db, xmlDoc);

	}

	private void loadTransactions(final ODatabaseDocumentTx db,
			final Document xmlDoc) throws XPathExpressionException {
		final NodeList nodes = getXPathResults(xmlDoc,
				"//TRANSACTIONS/TRANSACTION");
		Element element;
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			element = (Element) nodes.item(i);
			final ODocument doc = db.newInstance(element.getNodeName());
			addAttributes(doc, element.getAttributes(), valueConverters);
			doc.field("SPLITS", getSplits(db, element, doc));
			doc.save();
		}
	}

	private ArrayList<ODocument> getSplits(final ODatabaseDocumentTx db,
			final Element parentElement, final ODocument transactionDoc)
			throws XPathExpressionException {
		final ArrayList<ODocument> docList = new ArrayList<ODocument>();
		final NodeList nodes = getXPathResults(parentElement, "./SPLITS/SPLIT");
		Element element;
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			element = (Element) nodes.item(i);
			final ODocument doc = db.newInstance(element.getNodeName());
			addAttributes(doc, element.getAttributes(), valueConverters);
			doc.field("___transaction", transactionDoc);
			docList.add(doc);
		}
		return docList;
	}

	final Pattern NUMBER_PATTERN = Pattern
			.compile("^-?\\p{Digit}+/\\p{Digit}+");

	private void addAttributes(final ODocument doc, final NamedNodeMap attrMap,
			final HashMap<String, IValueConverter> converterMap) {
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
			if (converterMap.containsKey(name)) {
				toWrite = converterMap.get(name).convert(value);
			}
			doc.field(attr.getNodeName(), toWrite);
		}
	}

	private void loadAccounts(final ODatabaseDocumentTx db,
			final Document xmlDoc) throws XPathExpressionException {
		final NodeList nodes = getXPathResults(xmlDoc, "//ACCOUNTS/ACCOUNT");
		Element element;
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			element = (Element) nodes.item(i);
			final ODocument doc = db.newInstance(element.getNodeName());
			addAttributes(doc, element.getAttributes(), valueConverters);
			doc.save();
		}
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			element = (Element) nodes.item(i);
			final ArrayList<ODocument> subAccountList = getSubAccounts(element,
					db);
			final ODocument doc = findAccountById(getAttrValue("id", element),
					db);
			doc.field("SUBACCOUNTS", subAccountList);
			doc.save();
		}
	}

	private String getAttrValue(final String query, final Node node)
			throws XPathExpressionException {
		final XPathFactory factory = XPathFactory.newInstance();
		final XPath xpath = factory.newXPath();
		final XPathExpression expr = xpath.compile("./@id");

		final Node result = (Node) expr.evaluate(node, XPathConstants.NODE);
		return result.getTextContent();
	}

	ODocument findAccountById(final String id, final ODatabaseDocumentTx db) {
		final List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(
				"select * from ACCOUNT where id = '" + id + "'"));

		return result.get(0);
	}

	private ArrayList<ODocument> getSubAccounts(final Element element,
			final ODatabaseDocumentTx db) throws XPathExpressionException {
		final ArrayList<ODocument> list = new ArrayList<ODocument>();
		final NodeList nodes = getXPathResults(element,
				"./SUBACCOUNTS/SUBACCOUNT/@id");
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			list.add(findAccountById(nodes.item(i).getTextContent(), db));
		}
		return list;
	}

	NodeList getXPathResults(final Node node, final String query)
			throws XPathExpressionException {
		final XPathFactory factory = XPathFactory.newInstance();
		final XPath xpath = factory.newXPath();
		final XPathExpression expr = xpath.compile(query);

		final Object result = expr.evaluate(node, XPathConstants.NODESET);
		final NodeList nodes = (NodeList) result;

		return nodes;
	}

	public ODocument buildAccountData(final ODatabaseDocumentTx db,
			final String account) {

		final String selectStmt = "select * from ACCOUNT where id = '"
				+ account + "'";
		final List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(
				selectStmt));

		if (result.size() > 1) {
			throw new RuntimeException(
					"Something very wrong, got more than one account for account id:"
							+ account);
		}
		final ArrayList<ODocument> subAccounts = result.get(0).field(
				"SUBACCOUNTS");
		String acctName;
		final ODocument retDoc = new ODocument();

		ArrayList<ODocument> acctList;
		final HashMap<String, Date> rangeMap = getRangeMap();
		for (final Entry<String, Date> entry : rangeMap.entrySet()) {
			acctList = new ArrayList<ODocument>();

			for (final ODocument subAcct : subAccounts) {
				final ODocument doc = new ODocument();
				acctName = subAcct.field("name");
				// retDoc.field(acctName, subAcct.field("id"));

				final float total = buildSumForAccount(subAcct, db,
						entry.getValue());
				if (total == 0) {
					continue;
				}

				// doc.field(acctName, total);
				doc.field(
						"label",
						formatLabel(acctName + "(" + total + ")",
								(String) subAcct.field("id")));
				doc.field("data", total);
				doc.field("acct", subAcct.field("id"));
				acctList.add(doc);
			}
			retDoc.field(entry.getKey(), acctList);

		}

		// retDoc.field("accountData", doc);
		return retDoc;
	}

	private String formatLabel(final String label, final String acctId) {
		return "<a href='#' onclick='javascript:showaccount(\"" + acctId
				+ "\");'>" + label + "</a>";
	}

	private HashMap<String, Date> getRangeMap() {
		final HashMap<String, Date> map = new HashMap<String, Date>();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -7);
		map.put("Week", cal.getTime());

		cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -30);
		map.put("Month", cal.getTime());

		cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -365);
		map.put("Year", cal.getTime());

		map.put("Overall", null);

		return map;
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private float buildSumForAccount(final ODocument acct,
			final ODatabaseDocumentTx db, final Date range) {
		final String acctId = acct.field("id");
		String selectStmt = "select sum(value) from SPLIT where account = '"
				+ acctId + "'";
		if (range != null) {

			selectStmt = selectStmt + " and ___transaction.postdate >= '"
					+ dateFormat.format(range) + "'";
		}
		final List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(
				selectStmt));
		float subs = 0;
		final ArrayList<ODocument> subAccounts = acct.field("SUBACCOUNTS");
		for (final ODocument subAcct : subAccounts) {
			subs += buildSumForAccount(subAcct, db, range);
		}
		final Float val = result.get(0).field("sum");
		final float retVal = val != null ? val : 0;
		return retVal + subs;
	}

}
