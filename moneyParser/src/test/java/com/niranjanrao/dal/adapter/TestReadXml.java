package com.niranjanrao.dal.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.niranjanrao.dal.data.Institution;

public class TestReadXml extends DataTestBase{

	@Test
	public void testInstitutionRead() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
	{
		ReadXmlData<Institution> reader = new ReadXmlData<Institution>(new IInstanceFactory<Institution>() {

			@Override
			public  Institution createInstance() {
				return new Institution();
			}
		});
		InputStream input = TestReadXml.class.getResourceAsStream("/institution.xml");
		assertNotNull("Could not read the stream", input);
		try
		{
			ArrayList<Institution> list = reader.readData("//INSTITUTIONS/INSTITUTION", input);
			assertEquals("Did not get expected result back", 1, list.size());
			Institution ins = list.get(0);
			assertEquals("Not expected value", "manager", ins.getManager());
			assertEquals("Not expected value", "I000001", ins.getId());
			assertEquals("Not expected value", "An Institution", ins.getName());
			assertEquals("Not expected value", "", ins.getSortcode());
		}
		finally
		{
			input.close();
		}
	}
}
