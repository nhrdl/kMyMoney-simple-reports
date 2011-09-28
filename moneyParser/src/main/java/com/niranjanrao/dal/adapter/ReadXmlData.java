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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.niranjanrao.dal.data.DataBase;

public class ReadXmlData<T extends DataBase> {

	private IInstanceFactory<T> factory;

	public ReadXmlData(IInstanceFactory<T> factory) {
		this.factory = factory;
	}

	public ArrayList<T> readData(String dataPath, InputStream input)
			throws SAXException, IOException, ParserConfigurationException,
			XPathExpressionException {
		ArrayList<T> list = new ArrayList<T>();

		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(input);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(dataPath);

		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		Node node;
		HashMap<String, Setter> setterMap = new HashMap<String, Setter>();
		for (int i = 0, max = nodes.getLength(); i < max; i++) {
			node = nodes.item(i);
			list.add(getItem(node, setterMap));
		}
		return list;
	}

	private  T getItem(Node node, HashMap<String, Setter> setterMap) {
		NamedNodeMap attributes = node.getAttributes();
		Node attribute;
		Setter setter;
		T dataObject = factory.createInstance();

		for (int i = 0, max = attributes.getLength(); i < max; i++) {
			attribute = attributes.item(i);
			setter = getSetter(dataObject, setterMap, attribute.getNodeName());
			setter.set(dataObject, attribute.getNodeValue(), null);
		}
		return dataObject;
	}

	private  Setter getSetter(T dataObject,
			HashMap<String, Setter> setterMap, String name) {
		if (setterMap.containsKey(name)) {
			return setterMap.get(name);
		}
		DirectPropertyAccessor propAccess = new DirectPropertyAccessor();
		Setter setter = propAccess.getSetter(dataObject.getClass(), name);
		setterMap.put(name, setter);
		return setter;
	}

}
