package lox;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static
    {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    Scanner(String source)
    {
        this.source = source;
    }

    List<Token> scanTokens()
    {
        while (!isAtEnd())
        // beginning of the next lexeme
        {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd()
    {
        return current >= source.length();
    }

    private char advance()
    {
        current++;
        return source.charAt(current - 1); 
    }

    private void addToken(TokenType type)
    {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected)
    {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    private char peek()
    {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private void string()
    {
        while (peek() != '"' && !isAtEnd())
        {
            if (peek() == '\n')
                line++;
            advance();
        }
        if (isAtEnd())
        {
            Lox.error(line, "Unterminated string.");
            return;
        }
        // skip over terminating '"'
        advance();
        // trim quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private char peekNext()
    {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private void number()
    {
        while (isDigit(peek()))
            advance();
        if (peek() == '.' && isDigit(peekNext()))
        {
            // Consume decimal point
            advance();
            // read fractional part
            while (isDigit(peek()))
                advance();
        }
        double value = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, value);
    }

    private boolean isAlpha(char c)
    {
        if (c >= 'a' && c <= 'z')
            return true;
        else if (c >= 'A' && c <= 'Z')
            return true;
        else if (c == '_')
            return true;
        return false;
    }

    private boolean isAlphanumeric(char c)
    {
        return (isAlpha(c) || isDigit(c));
    }

    private void identifier()
    {
        while (isAlphanumeric(peek()))
            advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
        return;
        
    }

    private void scanToken()
    {
        char c = advance();
        switch(c)
        {
            case '(': 
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')': 
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{': 
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}': 
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',': 
                addToken(TokenType.COMMA);
                break;
            case '.': 
                addToken(TokenType.DOT);
                break;
            case '+': 
                addToken(TokenType.PLUS);
                break;
            case '-': 
                addToken(TokenType.MINUS);
                break;
            case ';': 
                addToken(TokenType.SEMICOLON);
                break;
            case '*': 
                addToken(TokenType.STAR);
                break;
            case '!':
                addToken(match('=')? TokenType.BANG_EQUAL : TokenType.BANG); 
                break;
            case '>':
                addToken(match('=')? TokenType.GREATER_EQUAL : TokenType.GREATER); 
                break;
            case '<':
                addToken(match('=')? TokenType.LESS_EQUAL : TokenType.LESS); 
                break;
            case '=':
                addToken(match('=')? TokenType.EQUAL_EQUAL : TokenType.EQUAL); 
                break;
            case '/':
                if (match('/'))
                {
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                }
                else if(match('*'))
                {
                    while (!(isAtEnd() || (peek() == '*' && peekNext() == '/')))
                    {
                        if (peek() == '\n')
                            line++;
                        advance();
                    }
                    if (isAtEnd())
                    {
                        Lox.error(line, "Unterminated comment.");
                        return;
                    }
                    // skip '*'
                    advance();
                    // skip '/'
                    advance();
                }
                else
                    addToken(TokenType.SLASH);
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;

            default:
                if (isDigit(c))
                    number();
                else if (isAlpha(c))
                    identifier();
                else
                {
                    Lox.error(line, "Unexpected character.");
                    break;
                }
        }
    }
}
