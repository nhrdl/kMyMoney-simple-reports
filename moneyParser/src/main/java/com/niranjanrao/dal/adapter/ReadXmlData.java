package com.niranjanrao.dal.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.hibernate.property.DirectPropertyAccessor;
import org.hibernate.property.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.niranjanrao.dal.data.DataBase;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class ReadXmlData<T extends DataBase> {

	private final IInstanceFactory<T> factory;

	public ReadXmlData(final IInstanceFactory<T> factory) {
		this.factory = factory;
	}

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

	final Pattern NUMBER_PATTERN = Pattern.compile("^\\p{Digit}+/\\p{Digit}+");

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

			if (name.equals("share") || name.equals("value") && m.matches()) {
				final String[] data = value.split("/");
				final float numerator = Float.parseFloat(data[0]), denominator = Float
						.parseFloat(data[1]);
				final float units = numerator / denominator;
				toWrite = units;
			}
			doc.field(attr.getNodeName(), toWrite);
		}
	}

	public ArrayList<T> readData(final String dataPath, final InputStream input)
			throws SAXException, IOException, ParserConfigurationException,
			XPathExpressionException {
		final ArrayList<T> list = new ArrayList<T>();

		final DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		final DocumentBuilder builder = domFactory.newDocumentBuilder();
		final Document doc = builder.parse(input);

		final XPathFactory factory = XPathFactory.newInstance();
		final XPath xpath = factory.newXPath();
		final XPathExpression expr = xpath.compile(dataPath);

		final Object result = expr.evaluate(doc, XPathConstants.NODESET);
		final NodeList nodes = (NodeList) result;
		Node node;
		final HashMap<String, Setter> setterMap = new HashMap<String, Setter>();
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			node = nodes.item(i);
			list.add(getItem(node, setterMap));
		}
		return list;
	}

	private T getItem(final Node node, final HashMap<String, Setter> setterMap) {
		final NamedNodeMap attributes = node.getAttributes();
		Node attribute;
		Setter setter;
		final T dataObject = factory.createInstance();
		for (int i = 0, max = attributes.getLength(); i < max; i++) {
			attribute = attributes.item(i);
			setter = getSetter(dataObject, setterMap, attribute.getNodeName());
			setter.set(dataObject, attribute.getNodeValue(), null);
		}
		return dataObject;
	}

	static final Logger log = LoggerFactory.getLogger(ReadXmlData.class);

	private Setter getSetter(final T dataObject,
			final HashMap<String, Setter> setterMap, final String name) {
		if (setterMap.containsKey(name)) {
			return setterMap.get(name);
		}
		final DirectPropertyAccessor propAccess = new DirectPropertyAccessor();
		final Setter setter = propAccess.getSetter(dataObject.getClass(), name);
		setterMap.put(name, setter);
		return setter;
	}

}
