package com.niranjanrao.dal.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import com.niranjanrao.dal.data.Institution;
import com.niranjanrao.dal.data.Payee;

public class TestReadXml extends DataTestBase {

	@Test
	public void testInstitutionRead() throws IOException, SAXException,
			ParserConfigurationException, XPathExpressionException {
		final ReadXmlData<Institution> reader = new ReadXmlData<Institution>(
				new IInstanceFactory<Institution>() {

					@Override
					public Institution createInstance() {
						return new Institution();
					}
				});
		final InputStream input = TestReadXml.class
				.getResourceAsStream("/institution.xml");
		assertNotNull("Could not read the stream", input);
		try {
			final ArrayList<Institution> list = reader.readData(
					"//INSTITUTIONS/INSTITUTION", input);
			assertEquals("Did not get expected result back", 1, list.size());
			final Institution ins = list.get(0);
			assertEquals("Not expected value", "manager", ins.getManager());
			assertEquals("Not expected value", "I000001", ins.getId());
			assertEquals("Not expected value", "An Institution", ins.getName());
			assertEquals("Not expected value", "", ins.getSortcode());
		} finally {
			input.close();
		}
	}

	@Autowired
	IInstitutionAdapter institutionAdapter;

	// @Test
	public void testInstitutionSave() throws IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		assertNotNull("Could not get adapter", institutionAdapter);
		final ReadXmlData<Institution> reader = new ReadXmlData<Institution>(
				new IInstanceFactory<Institution>() {

					@Override
					public Institution createInstance() {
						return new Institution();
					}
				});
		final InputStream input = TestReadXml.class
				.getResourceAsStream("/institution.xml");
		assertNotNull("Could not read the stream", input);
		try {
			final ArrayList<Institution> list = reader.readData(
					"//INSTITUTIONS/INSTITUTION", input);
			institutionAdapter.bulkSave(list);
			log.debug("Sample record saved {}", list.get(0).toString());
		} finally {
			input.close();
		}
	}

	@Autowired
	IPayeeAdapter payeeAdapter;

	@Test
	public void testPayeeSave() throws IOException, XPathExpressionException,
			SAXException, ParserConfigurationException {
		assertNotNull("Could not get adapter", institutionAdapter);
		final ReadXmlData<Payee> reader = new ReadXmlData<Payee>(
				new IInstanceFactory<Payee>() {

					@Override
					public Payee createInstance() {
						return new Payee();
					}
				});
		final InputStream input = TestReadXml.class
				.getResourceAsStream("/payee.xml");
		assertNotNull("Could not read the stream", input);
		try {
			final ArrayList<Payee> list = reader.readData("//PAYEES/PAYEE",
					input);
			payeeAdapter.bulkSave(list);
			log.debug("Sample record saved {}", list.get(0).toString());
		} finally {
			input.close();
		}
	}

	static final Logger log = LoggerFactory.getLogger(TestReadXml.class);
}
