package com.wn.dbml.visitor;

import com.wn.dbml.model.Column;
import com.wn.dbml.model.Database;
import com.wn.dbml.model.Enum;
import com.wn.dbml.model.Index;
import com.wn.dbml.model.NamedNote;
import com.wn.dbml.model.Project;
import com.wn.dbml.model.Relationship;
import com.wn.dbml.model.Schema;
import com.wn.dbml.model.Table;
import com.wn.dbml.model.TableGroup;
import com.wn.dbml.model.TablePartial;

/**
 * Defines a visitor for database elements as per the <i>visitor pattern</i>.
 */
public interface DatabaseVisitor {
	void visit(Column column);
	
	void visit(Database database);
	
	void visit(Enum anEnum);
	
	void visit(Index index);
	
	void visit(NamedNote namedNote);
	
	void visit(Project project);
	
	void visit(Relationship relationship);
	
	void visit(Schema schema);
	
	void visit(Table table);
	
	void visit(TableGroup tableGroup);
	
	void visit(TablePartial tablePartial);
}
