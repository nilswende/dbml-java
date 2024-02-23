package com.wn.dbml.compiler;

import com.wn.dbml.compiler.lexer.LexerImpl;
import com.wn.dbml.compiler.parser.ParserImpl;
import com.wn.dbml.model.Database;

import java.io.Reader;

/**
 * Creates a database representation using DBML.
 */
public final class DbmlParser {
    /**
     * Creates a database representation using a DBML string.
     *
     * @param dbml a DBML string
     */
    public static Database parse(final String dbml) {
        return new ParserImpl().parse(new LexerImpl(dbml));
    }

    /**
     * Creates a database representation using a DBML reader.
     *
     * @param dbml a DBML reader
     */
    public static Database parse(final Reader dbml) {
        return new ParserImpl().parse(new LexerImpl(dbml));
    }
}
