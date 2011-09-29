package com.niranjanrao.dal.adapter;

import org.springframework.stereotype.Repository;

import com.niranjanrao.dal.data.Payee;

@Repository("payee")
public class PayeeAdapter extends GenericAdapter<Payee, Long> implements
		IPayeeAdapter {

}
