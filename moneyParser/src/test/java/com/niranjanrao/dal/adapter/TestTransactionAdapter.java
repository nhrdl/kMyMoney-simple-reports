package com.niranjanrao.dal.adapter;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TestTransactionAdapter extends DataTestBase {

	@Autowired
	ITransactionAdapter transactionAdapter;

	@Test
	public void testGetterSetters() {

		assertNotNull("Oops could not get the transaction adapter",
				transactionAdapter);
	}
}
