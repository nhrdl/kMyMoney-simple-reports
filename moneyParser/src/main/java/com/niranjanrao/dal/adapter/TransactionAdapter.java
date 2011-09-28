package com.niranjanrao.dal.adapter;

import org.springframework.stereotype.Repository;

import com.niranjanrao.dal.data.Transaction;

@Repository("TransactionAdapter")
public class TransactionAdapter extends GenericAdapter<Transaction, Long>
		implements ITransactionAdapter {

}
