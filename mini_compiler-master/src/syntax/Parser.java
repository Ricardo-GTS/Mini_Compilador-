package syntax;

import exceptions.SyntaxException;
import lexical.Scanner;
import lexical.Token;
import utils.TokenType;

public class Parser {
    private Scanner scanner;
    private Token token;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    public void E() {
		this.token = this.scanner.nextToken(); // Consumir o primeiro token
        Prog();
    }

    private void match(TokenType expectedType) {
        if (this.token.getType() == expectedType) {
            this.token = this.scanner.nextToken();
			System.out.println(token);
        } else {
            throw new SyntaxException("Expected token type: " + expectedType + ", found: " + token.getType() + " at line: " + scanner.getLine() + " and column: " + scanner.getColumn());
        }
    }

    private void Prog() {	// Estrutura da Gramatica

        match(TokenType.COLON);
        match(TokenType.DECLARACOES);
        ListaDeclaracoes();
		match(TokenType.COLON);
        match(TokenType.ALGORITMO);
        ListaComandos();
		match(TokenType.COLON);
		match(TokenType.END_ALGORITMO);
    }

    private void ListaDeclaracoes() {
		if (this.token.getType() == TokenType.COLON)
			return; // Fim da lista de declaracoes
		Declaracao();	
  		ListaDeclaracoes();	// Chamada recursiva ate o fim da lista de declaracoes
    }

    private void Declaracao() { // Estrutura da Declaracao
        TipoVar();
        match(TokenType.COLON);
        match(TokenType.IDENTIFIER);
		match(TokenType.SEMICOLON);

    }	

	private void TipoVar() {	// Verifica se o tipo da variavel e inteiro ou real
		if (this.token.getType() == TokenType.INTEIRO || this.token.getType() == TokenType.REAL) {
			match(this.token.getType());
		} else {
			throw new SyntaxException("Invalid variable type: " + token.getType() + ", found: " + token.getType() + " at line: " + scanner.getLine() + " and column: " + scanner.getColumn());
		}
	}

    private void ListaComandos() {
		Comando();
		ListaComandos2();
	}
	
	private void ListaComandos2() {			// Chamada recursiva ate o fim da lista de comandos.
		if (this.token.getType() != TokenType.COLON) { //Detecta o fim da lista de comandos Quando encontra o token ":" Encerra a Recursividade
			ListaComandos();
		} 
	}
	
	private void Comando() {
		if (this.token.getType() == TokenType.ASSIGN) {
			match(TokenType.ASSIGN);
			expressaoAritmetica();
			match(TokenType.TO);
			match(TokenType.IDENTIFIER);
			match(TokenType.SEMICOLON);
		} else if (this.token.getType() == TokenType.INPUT) {
			match(TokenType.INPUT);
			match(TokenType.IDENTIFIER);
			match(TokenType.SEMICOLON);
		} else if (this.token.getType() == TokenType.PRINT) {
			match(TokenType.PRINT);
			if (this.token.getType() == TokenType.IDENTIFIER || this.token.getType() == TokenType.CADEIA) {
				match(this.token.getType());
			} else {
				throw new SyntaxException("Invalid output value: " + token.getType() + ", found: " + token.getType() + " at line: " + scanner.getLine() + " and column: " + scanner.getColumn());
			}
			match(TokenType.SEMICOLON);
		} else if (this.token.getType() == TokenType.IF) {
			match(TokenType.IF);
			ExpressaoRelacional();
			match(TokenType.THEN);
			Comando();				// De acordo Com a gramatica Somente é aceito um comando opos o IF
			ComandoCondicao2();
		} else if (this.token.getType() == TokenType.WHILE) {
			match(TokenType.WHILE);
			ExpressaoRelacional();	
			Comando();				// De acordo Com a gramatica Somente é aceito um comando opos o WHILE
		} else {
			throw new SyntaxException("Invalid command: " + token.getType() + ", found: " + token.getType() + " at line: " + scanner.getLine() + " and column: " + scanner.getColumn());
		}
	}
	
	private void ComandoCondicao2() {
		if (this.token.getType() == TokenType.ELSE) {
			match(TokenType.ELSE);
			Comando();			// De acordo Com a gramatica Somente é aceito um comando opos o ELSE
		}
	}


//Parte	expressaoAritmetica;
//-----------------------------------------------------------------------------------------------------------------
	private void expressaoAritmetica() {
		termoAritmetico();
	}
	

	private void termoAritmetico() {
		fatorAritmetico();	
		termoAritmetico2();
	}

	
	private void termoAritmetico2() { // 2º Verificar se o segundo elemento da expressaoAritmetica é um operador matematico	
		if (this.token.getType() == TokenType.MATH_OP) { // Se for um operador matematico então repete o processo
			match(this.token.getType());
			fatorAritmetico(); 		// 3º Verificar se o terceiro elemento da expressaoAritmetica é um numero ou um identificador
			termoAritmetico2();		//Loop de recusividade continua até que não haja mais expressaoAritmetica
		}
	}
	
	// 1º Verificar o primeiro elemento da expressaoAritmetica é um numero ou um identificador
	private void fatorAritmetico() {					
		if (this.token.getType() == TokenType.NUMBER || this.token.getType() == TokenType.FLOAT ||
			this.token.getType() == TokenType.IDENTIFIER) {
			match(this.token.getType());
		} else {
			throw new SyntaxException("Invalid arithmetic factor: " + token.getType() +
					" at line: " + scanner.getLine() + " and column: " + scanner.getColumn());
		}
	}

//-----------------------------------------------------------------------------------------------------------------
//Fim Parte	expressaoAritmetica;




//Parte	ExpressaoRelacional;
//-----------------------------------------------------------------------------------------------------------------
	private void ExpressaoRelacional() {
		TermoRelacional();
		ExpressaoRelacionalAND_OR();
	}
	
	private void ExpressaoRelacionalAND_OR() {	// 4º Verificar se o quarto elemento da expressaoRelacional é um operador AND ou OR
		if (this.token.getType() == TokenType.AND || this.token.getType() == TokenType.OR) {
			match(this.token.getType());
			ExpressaoRelacional();		//Loop Principal continua até que não haja mais operadores AND ou OR
		}
	}

	private void TermoRelacional() { // 1º Verificar se o primeiro elemento da expressaoRelacional é um numero ou um identificador
		if (this.token.getType() == TokenType.IDENTIFIER || this.token.getType() == TokenType.NUMBER || this.token.getType() == TokenType.FLOAT) {
			expressaoAritmetica();	// Como na gramaica o termoRelacional é um termoAritmetico, então chama a função expressaoAritmetica
			OperadorRelacional();	// 2º Verificar se o segundo elemento da expressaoRelacional é um operador relacional
			expressaoAritmetica();	// 3º Verificar se o terceiro elemento da expressaoRelacional é um numero ou um identificador
		} else {
			throw new SyntaxException("Invalid relational term: " + token.getType() + ", found: " + token.getType() + " at line: " + scanner.getLine() + " and column: " + scanner.getColumn());
		}
	}

	
	private void OperadorRelacional() {	//Verifica se o token é um operador relacional
		if (this.token.getType() == TokenType.REL_OP) {
			match(TokenType.REL_OP);
		} else {
			throw new SyntaxException("Relational operator expected, found: " + token.getType() + ", found: " + token.getType() + " at line: " + scanner.getLine() + " and column: " + scanner.getColumn());
		}
	}
//-----------------------------------------------------------------------------------------------------------------
//Fim Parte	ExpressaoRelacional;



}