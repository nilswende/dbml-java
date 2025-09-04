package com.wn.dbml.compiler.lexer;

import com.wn.dbml.compiler.Token;
import com.wn.dbml.compiler.token.TokenImpl;
import com.wn.dbml.compiler.token.TokenType;

import java.io.Reader;

/**
 * The default lexer implementation.
 */
public class LexerImpl extends AbstractLexer {
	private static final String OUTPUT_LINEBREAK = "\n";
	private static final String OUTPUT_SPACE = " ";
	
	public LexerImpl(String string) {
		super(string);
	}
	
	public LexerImpl(Reader reader) {
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
			case '~' -> new TokenImpl(TokenType.TILDE, next);
			case '\n', '\r' -> new TokenImpl(TokenType.LINEBREAK, OUTPUT_LINEBREAK);
			case ' ', '\t' -> new TokenImpl(TokenType.SPACE, OUTPUT_SPACE);
			case '\'' -> nextString(next);
			case '"' -> nextSingleLineString(next);
			case '`' -> nextExpression(next);
			case '/' -> nextComment(next);
			case '#' -> nextColorCode();
			default -> new TokenImpl(TokenType.ILLEGAL, next);
		};
	}
	
	private Token nextWord(int next) {
		var word = nextWholeWord(next);
		if (word.codePoints().allMatch(Char::isDigit)) {
			return nextNumber(word);
		}
		return new TokenImpl(word);
	}
	
	private String nextWholeWord(int next) {
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
	
	private TokenImpl nextNumber(String word) {
		if (reader.lookahead() == '.') {
			var dot = (char) reader.nextChar();
			var nextWord = lookaheadWholeWord();
			if (!nextWord.isEmpty() && nextWord.codePoints().allMatch(Char::isDigit)) {
				skipChars(nextWord.length());
				return new TokenImpl(TokenType.NUMBER, word + dot + nextWord);
			}
		}
		return new TokenImpl(TokenType.NUMBER, word);
	}
	
	private String lookaheadWholeWord() {
		int i = 1;
		while (true) {
			var lookahead = reader.lookahead(i);
			if (lookahead.length() < i) {
				return lookahead;
			}
			if (!lookahead.codePoints().allMatch(Char::isWordChar)) {
				return lookahead.substring(0, i - 1);
			}
			i++;
		}
	}
	
	private Token nextLTSymbol(int lt) {
		if (reader.lookahead() == '>') {
			skipChars(1);
			return new TokenImpl(TokenType.NE, "<>");
		}
		return new TokenImpl(TokenType.LT, lt);
	}
	
	private Token nextString(int quote) {
		var lookahead = reader.lookahead(2);
		if (quote == '\'' && lookahead.startsWith("''")) {
			skipChars(2);
			return nextMultiLineString("'''", TokenType.TSTRING);
		} else {
			return nextSingleLineString(quote);
		}
	}
	
	private Token nextMultiLineString(String quote, TokenType tokenType) {
		var multiLineSb = new MultiLineStringBuilder(OUTPUT_LINEBREAK);
		var sb = new StringBuilder();
		while (true) {
			var c = reader.nextChar();
			var lookahead = reader.lookahead(quote.length());
			if (c == -1) {
				return new TokenImpl(TokenType.ILLEGAL, c);
			} else if (c == quote.charAt(0)) {
				var next = Character.toString(c) + lookahead;
				if (next.startsWith(quote)) {
					skipChars(quote.length() - 1);
					break;
				}
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
				multiLineSb.appendLine(sb.toString());
				skipChars(quote.length());
				break;
			}
		}
		return new TokenImpl(tokenType, multiLineSb.toString());
	}
	
	private String appendEscaped(String escaped, StringBuilder sb) {
		sb.append(escaped);
		skipChars(escaped.length());
		return reader.lookahead(escaped.length());
	}
	
	private Token nextSingleLineString(int quote) {
		var sb = new StringBuilder();
		while (true) {
			var c = reader.nextChar();
			var lookahead = reader.lookahead();
			if (c == -1) {
				return new TokenImpl(TokenType.ILLEGAL, c);
			} else if (c == quote) {
				break;
			} else if (c == '\\') {
				if (lookahead == quote) {
					sb.append((char) lookahead);
					skipChars(1);
				} else {
					sb.append((char) c);
				}
			} else {
				sb.append((char) c);
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
	
	private Token nextExpression(int quote) {
		return nextSingleLineString(quote);
	}
	
	private Token nextComment(int forwardSlash) {
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
	
	private Token nextColorCode() {
		var lookahead = reader.lookahead(6);
		var color = lookahead.codePoints()
				.takeWhile(Char::isHexDigit)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
		var length = color.length();
		if (length == 6 || length == 3) {
			skipChars(length);
			return new TokenImpl(TokenType.COLOR_CODE, color);
		}
		return new TokenImpl(TokenType.ILLEGAL, color);
	}
	
	private void skipChars(int length) {
		for (int i = 0; i < length; i++) {
			reader.nextChar();
		}
	}
}
