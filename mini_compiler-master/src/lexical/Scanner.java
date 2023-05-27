package lexical;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import exceptions.LexicalException;
import utils.TokenType;

public class Scanner {

    int pos;
    char[] contentTXT;
    int state;
    char currentChar, previusChar;
    int Line = 1, Column = 0, previusColumn = 0, previusLine = 1;
    Hashtable<String, TokenType> reservedWords = new Hashtable<String, TokenType>();

    public Scanner(String filename) {
        ReservedWords();
        try {
            String contentBuffer = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
            this.contentTXT = contentBuffer.toCharArray();
            this.pos = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Token nextToken() {
        this.state = 0;
        String content = "";

        while (true) {
            if (isEOF()) {
                return null;
            }

            previusLine = Line;
            previusColumn = Column;
			previusChar = currentChar;
            currentChar = this.nextChar();

            if (isComment(currentChar)) {
                while (currentChar != '\n') {
                    currentChar = this.nextChar();
                }
            }
            if (currentChar == '\n') {
                Line++;
                Column = 0;
            }

            switch (state) {
                case 0:
                    if (this.isLetter(currentChar)) {
                        content += currentChar;
                        state = 1;
                    } else if (isSpace(currentChar)) {
                        state = 0;
                    } else if (isDigit(currentChar)) {
                        content += currentChar;
                        state = 2;
                    } else if (isMathOperator(currentChar)) {
                        content += currentChar;
                        return new Token(TokenType.MATH_OP, content);
                    }else if (isOperator(currentChar)) {
                        content += currentChar;
                        state = 5;
                    } else if (isLeftParenthesis(currentChar)) {
                        content += currentChar;
                        return new Token(TokenType.L_PARENTHESIS, content);
                    } else if (isRightParenthesis(currentChar)) {
                        content += currentChar;
                        return new Token(TokenType.R_PARENTHESIS, content);
                    } else if (currentChar == ';') {
						content += currentChar;
						return new Token(TokenType.SEMICOLON, content);
					} else if (currentChar == ':') {
						content += currentChar;
						return new Token(TokenType.COLON, content);
					}  else if (IsFloat(currentChar)) {
                        content += currentChar;
                        state = 6;
                    }	else if (isCadeia(currentChar)) {
						content += currentChar;
						state = 8;
					}
					 else {
                        throw new LexicalException("Invalid Character! Line " + Line + " Column " + Column + " = " + content + currentChar + "");
                    }
                    break;
                case 1:
                    if (this.isLetter(currentChar) || this.isDigit(currentChar)) {
                        content += currentChar;
                        state = 1;
                    } else {
                        this.back();
                        TokenType tokenType = reservedWords.getOrDefault(content, TokenType.IDENTIFIER);
                        return new Token(tokenType, content);
                    }
                    break;
                case 2:
                    if (isDigit(currentChar)) {
                        content += currentChar;
                        state = 2;
                    }  else if (IsFloat(currentChar)) {
                        content += currentChar;
                        state = 6;
                    } else if (isSpace(currentChar) || isOperator(currentChar) || isMathOperator(currentChar) || isLeftParenthesis(currentChar) || isRightParenthesis(currentChar)) {
                        this.back();
                        return new Token(TokenType.NUMBER, content);
                    } else {
						throw new LexicalException("Number Malformed! Line " + Line + " Column " + Column + " = " + content + currentChar + "");
					}
                    break;
                case 5:
				if (currentChar == '=') {							// para os casos de == | != | <= | >=
					content += currentChar;
					return new Token(TokenType.REL_OP, content);
				} else if(previusChar == '=') {						// para os casos de = 
					this.back();
					return new Token(TokenType.ASSIGNMENT, content);
				} else if(previusChar != '!') {						// para os casos de < | >
					this.back();
					return new Token(TokenType.REL_OP, content);
				}
				else {
					throw new RuntimeException(
							"Invalid Relacional Operator ! Line " + Line + " Column " +  Column  + " = " + content + currentChar + "");
				}
                case 6:
                    if (isDigit(currentChar)) {
                        content += currentChar;
                        state = 7;
                    } else {
                        throw new LexicalException("Float Malformed! Line " + Line + " Column " + Column + " = " + content + currentChar + "");
                    }
                    break;
                case 7:
                    if (isDigit(currentChar)) {
                        content += currentChar;
                        state = 7;
                    } else if (isLetter(currentChar) || IsFloat(currentChar)) {
                        throw new LexicalException("Float Malformed! Line " + Line + " Column " + Column + " = " + content + currentChar + "");
                    } else {
                        this.back();
                        return new Token(TokenType.FLOAT, content);
                    }
                    break;

				case 8:
                    if (isCadeia(currentChar)) {
                        content += currentChar;
                        return new Token(TokenType.CADEIA, content);
                    } else if (isEOF()) {
                        throw new LexicalException("String Malformed! Line " + Line + " Column " + Column + " = " + content + currentChar + "");
                    } else {
                        content += currentChar;
                        state = 8;
                    }
            }

        }
    }

    public int getLine() {
        if (currentChar == '\n')
            return this.previusLine;
        return this.Line;
    }

    public int getColumn() {
        if (currentChar == '\n')
            return this.previusColumn;
        return this.Column;
    }

    private char nextChar() {
        Column++;
        return this.contentTXT[this.pos++];
    }

    private void back() {
        if (currentChar != '\n') {
            this.pos--;
            Column--;
        }
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isMathOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean isEOF() {
        if (this.pos >= this.contentTXT.length) {
            return true;
        }
        return false;
    }

    private boolean isOperator(char c) {
        return c == '>' || c == '<' || c == '!' || c == '=';
    }

    private boolean isLeftParenthesis(char c) {
        return c == '(';
    }

    private boolean isRightParenthesis(char c) {
        return c == ')';
    }

    private boolean isSpace(char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    private boolean IsFloat(char c) {
        return c == '.';
    }

    private void ReservedWords() {
        reservedWords.put("ALGORITMO", TokenType.ALGORITMO);
        reservedWords.put("DECLARACOES", TokenType.DECLARACOES);
        reservedWords.put("INTEIRO", TokenType.INTEIRO);
        reservedWords.put("REAL", TokenType.REAL);
        reservedWords.put("INPUT", TokenType.INPUT);
        reservedWords.put("IF", TokenType.IF);
        reservedWords.put("THEN", TokenType.THEN);
        reservedWords.put("ELSE", TokenType.ELSE);
        reservedWords.put("ASSIGN", TokenType.ASSIGN);
        reservedWords.put("TO", TokenType.TO);
        reservedWords.put("AND", TokenType.AND);
        reservedWords.put("OR", TokenType.OR);
        reservedWords.put("PRINT", TokenType.PRINT);
        reservedWords.put("WHILE", TokenType.WHILE);
        reservedWords.put("END_ALGORITMO", TokenType.END_ALGORITMO);
    }

    private boolean isComment(char c) {
        return c == '#';
    }

	private boolean isCadeia(char c) {
		return c == '"';
	}
}
