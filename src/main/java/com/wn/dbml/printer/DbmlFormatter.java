package com.wn.dbml.printer;

import java.util.Objects;

/**
 * Formatting DBML.
 */
public class DbmlFormatter {
	private final String indentation;
	private final String linebreak;
	
	private DbmlFormatter(String indentation, String linebreak) {
		this.indentation = indentation;
		this.linebreak = linebreak;
	}
	
	public String getIndentation() {
		return indentation;
	}
	
	public String getLinebreak() {
		return linebreak;
	}
	
	/**
	 * For configuring the formatting.
	 */
	public static class Builder {
		private String indentation = "  ";
		private String linebreak = "\n";
		
		public DbmlFormatter build() {
			return new DbmlFormatter(indentation, linebreak);
		}
		
		public Builder setIndentation(String indentation) {
			this.indentation = Objects.requireNonNull(indentation);
			return this;
		}
		
		public Builder setLinebreak(String linebreak) {
			this.linebreak = Objects.requireNonNull(linebreak);
			return this;
		}
	}
}
