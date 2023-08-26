package com.wn.dbml.compiler.lexer;

import com.wn.dbml.compiler.Token;
import com.wn.dbml.compiler.token.KeywordType;
import com.wn.dbml.compiler.token.TokenImpl;
import com.wn.dbml.compiler.token.TokenType;

import java.io.Reader;

/**
 * The default lexer implementation.
 */
public class LexerImpl extends AbstractLexer {
	private static final String OUTPUT_LINEBREAK = "\n";
	private static final String OUTPUT_SPACE = " ";
	
	public LexerImpl(final String string) {
		super(string);
	}
	
	public LexerImpl(final Reader reader) {
		super(reader);
	}
	
	@Override
	protected Token nextTokenImpl() {
		int next = reader.nextChar();
		if (Char.isWordChar(next)) {
			return nextWord(next);
		}
		return switch (next) {
			case -1 -> new TokenImpl(TokenType.EOF, next);
			case '-' -> new TokenImpl(TokenType.MINUS, next);
			case '<' -> nextLTSymbol(next);
			case '>' -> new TokenImpl(TokenType.GT, next);
			case '(' -> new TokenImpl(TokenType.LPAREN, next);
			case '[' -> new TokenImpl(TokenType.LBRACK, next);
			case '{' -> new TokenImpl(TokenType.LBRACE, next);
			case ')' -> new TokenImpl(TokenType.RPAREN, next);
			case ']' -> new TokenImpl(TokenType.RBRACK, next);
			case '}' -> new TokenImpl(TokenType.RBRACE, next);
			case ':' -> new TokenImpl(TokenType.COLON, next);
			case ',' -> new TokenImpl(TokenType.COMMA, next);
			case '.' -> new TokenImpl(TokenType.DOT, next);
			case '\n', '\r' -> new TokenImpl(TokenType.LINEBREAK, OUTPUT_LINEBREAK);
			case ' ', '\t' -> new TokenImpl(TokenType.SPACE, OUTPUT_SPACE);
			case '\'' -> nextString(next);
			case '"' -> nextSingleLineString(next);
			case '`' -> nextExpression(next);
			case '/' -> nextComment(next);
			case '#' -> nextColor();
			default -> new TokenImpl(TokenType.ILLEGAL, next);
		};
	}
	
	private Token nextWord(final int next) {
		var word = nextWholeWord(next);
		return KeywordType.of(word) == KeywordType.MULTI
				? nextMultiKeyword(word)
				// integers can be identifiers too, so we can't make the distinction here
				// e.g. 1.2 can be a float or a schema.table name. Only the Parser can decide
				: new TokenImpl(word);
	}
	
	private String nextWholeWord(final int next) {
		var sb = new StringBuilder();
		var c = next;
		while (true) {
			sb.append((char) c);
			if (Char.isWordChar(reader.lookahead())) {
				c = reader.nextChar();
			} else break;
		}
		return sb.toString();
	}
	
	private Token nextMultiKeyword(final String prefix) {
		var separatorToken = nextTokenImpl();
		var separatorValue = separatorToken.getValue();
		if (!TokenType.isMultiSeparator(separatorValue)) {
			reader.pushback(separatorValue);
			return new TokenImpl(prefix);
		}
		var nextToken = nextTokenImpl();
		var word = nextToken.getValue();
		var multiWord = prefix + separatorValue + word;
		return switch (KeywordType.of(multiWord)) {
			case YES -> new TokenImpl(multiWord);
			case NO -> {
				if (KeywordType.of(word) == KeywordType.MULTI) {
					var multiKeyword = nextMultiKeyword(multiWord);
					if (KeywordType.of(multiKeyword.getValue()) == KeywordType.YES) {
						yield multiKeyword;
					}
				}
				reader.pushback(word);
				reader.pushback(separatorValue);
				yield new TokenImpl(prefix);
			}
			case MULTI -> throw new IllegalStateException("Cannot be MULTI here: " + multiWord);
		};
	}
	
	private Token nextLTSymbol(final int lt) {
		if (reader.lookahead() == '>') {
			skipChars(1);
			return new TokenImpl(TokenType.NE, "<>");
		}
		return new TokenImpl(TokenType.LT, lt);
	}
	
	private Token nextString(final int quote) {
		var lookahead = reader.lookahead(2);
		if (quote == '\'' && lookahead.startsWith("''")) {
			skipChars(2);
			return nextMultiLineString("'''", TokenType.TSTRING);
		} else {
			return nextSingleLineString(quote);
		}
	}
	
	private Token nextMultiLineString(final String quote, final TokenType tokenType) {
		var multiLineSb = new MultiLineStringBuilder(OUTPUT_LINEBREAK);
		var sb = new StringBuilder();
		while (true) {
			var c = reader.nextChar();
			var lookahead = reader.lookahead(quote.length());
			if (c == -1) {
				return new TokenImpl(TokenType.ILLEGAL, c);
			} else if (c == '\\') {
				if (lookahead.startsWith("\\")) {
					lookahead = appendEscaped(lookahead.substring(0, 1), sb);
				} else if (lookahead.equals(quote)) {
					lookahead = appendEscaped(lookahead, sb);
				} else if (!lookahead.isEmpty() && Char.isLinebreak(lookahead.charAt(0))) {
					// line continuation
					skipChars(1);
				} else {
					sb.append((char) c);
				}
			} else if (Char.isLinebreak(c)) {
				multiLineSb.appendLine(sb.toString());
				sb.setLength(0);
			} else {
				sb.append((char) c);
			}
			if (lookahead.equals(quote)) {
				skipChars(quote.length());
				break;
			}
		}
		return new TokenImpl(tokenType, multiLineSb.toString());
	}
	
	private String appendEscaped(final String escaped, final StringBuilder sb) {
		sb.append(escaped);
		skipChars(escaped.length());
		return reader.lookahead(escaped.length());
	}
	
	private Token nextSingleLineString(final int quote) {
		var sb = new StringBuilder();
		while (true) {
			var c = reader.nextChar();
			var lookahead = reader.lookahead();
			if (c == -1) {
				return new TokenImpl(TokenType.ILLEGAL, c);
			} else if (c == '\\') {
				if (lookahead == quote) {
					lookahead = appendEscaped(lookahead, sb);
				} else {
					sb.append((char) c);
				}
			} else {
				sb.append((char) c);
			}
			if (lookahead == quote) {
				skipChars(1);
				break;
			}
		}
		var type = switch (quote) {
			case '\'' -> TokenType.SSTRING;
			case '"' -> TokenType.DSTRING;
			case '`' -> TokenType.EXPR;
			default -> throw new IllegalStateException("Unexpected value: " + quote);
		};
		return new TokenImpl(type, sb.toString());
	}
	
	private int appendEscaped(final int escaped, final StringBuilder sb) {
		sb.append((char) escaped);
		skipChars(1);
		return reader.lookahead();
	}
	
	private Token nextExpression(final int quote) {
		return nextSingleLineString(quote);
	}
	
	private Token nextComment(final int forwardSlash) {
		var next = reader.lookahead();
		if (next == '/') {
			skipChars(1);
			return nextSingleLineComment();
		} else if (next == '*') {
			skipChars(1);
			return nextMultiLineString("*/", TokenType.COMMENT);
		}
		return new TokenImpl(TokenType.ILLEGAL, forwardSlash);
	}
	
	private Token nextSingleLineComment() {
		var sb = new StringBuilder();
		while (!Char.isLinebreak(reader.lookahead())) {
			var c = reader.nextChar();
			if (c == -1) break;
			sb.append((char) c);
		}
		return new TokenImpl(TokenType.COMMENT, sb.toString());
	}
	
	private Token nextColor() {
		var lookahead = reader.lookahead(6);
		var color = lookahead.codePoints()
				.takeWhile(i -> !Character.isWhitespace(i))
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
		if ((color.length() == 6 || color.length() == 3) && color.chars().allMatch(Char::isHexDigit)) {
			skipChars(color.length());
			return new TokenImpl(TokenType.COLOR, color);
		}
		return new TokenImpl(TokenType.ILLEGAL, color);
	}
	
	private void skipChars(final int length) {
		for (int i = 0; i < length; i++) {
			reader.nextChar();
		}
	}
}
