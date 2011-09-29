package com.niranjanrao.dal.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.niranjanrao.dal.data.DataBase;

public class ReadXmlData<T extends DataBase> {

	private final IInstanceFactory<T> factory;

	public ReadXmlData(final IInstanceFactory<T> factory) {
		this.factory = factory;
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
