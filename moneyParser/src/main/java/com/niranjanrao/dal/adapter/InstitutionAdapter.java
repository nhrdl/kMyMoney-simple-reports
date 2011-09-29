package com.niranjanrao.dal.adapter;

import org.springframework.stereotype.Repository;

import com.niranjanrao.dal.data.Institution;

@Repository("InstitutionAdapter")
class InstitutionAdapter extends GenericAdapter<Institution, Long> implements
		IInstitutionAdapter {

}
