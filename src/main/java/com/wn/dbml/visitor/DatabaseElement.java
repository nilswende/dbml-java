package com.wn.dbml.visitor;

public interface DatabaseElement {
	void accept(DatabaseVisitor visitor);
}
