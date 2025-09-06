package com.wn.dbml.visitor;

/**
 * A database element that can be visited by a {@link DatabaseVisitor}.
 */
public interface DatabaseElement {
	void accept(DatabaseVisitor visitor);
}
