package com.wn.dbml.model;

import com.wn.dbml.visitor.DatabaseVisitor;

public class TablePartial extends Table {
	TablePartial(Schema schema, String name) {
		super(schema, name);
	}
	
	@Override
	public void setAlias(Alias alias) {
		throw new UnsupportedOperationException("A TablePartial shouldn't have an alias");
	}
	
	@Override
	public void accept(DatabaseVisitor visitor) {
		visitor.visit(this);
	}
}
