import java.util.*;

public class LRParser {
    public static void main(String[] args) {
        // Use the assignment example as the default code
        String code = "id + (id * id)";
        
        // Allow code to be given as an argument
        if (args.length > 0) {
            code = args[0];
        }
        System.out.println(code);

        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer);
        parser.parse();
    }
}

class Parser {
    private Lexer lexer;
    private Token curToken;

    private Map<TokenType, Operation[]> parseTable;
    private Rule[] rules;
    private Map<String, int[]> gotoTable;

    private Stack<StackItem> stack;

    public Parser(Lexer l) {
        lexer = l;
        advanceToken();

        buildParseTable();
        buildGotoTable();
        buildRuleTable();
    }

    // Table constructors
    private void buildParseTable() {
        parseTable = new HashMap<TokenType, Operation[]>();

        parseTable.put(TokenType.Ident, new Operation[]{
            new Operation(ParseOperation.Shift, 5),
            null,
            null,
            null,
            new Operation(ParseOperation.Shift, 5),
            null,
            new Operation(ParseOperation.Shift, 5),
            new Operation(ParseOperation.Shift, 5),
            null,
            null,
            null,
            null
        });

        parseTable.put(TokenType.Plus, new Operation[]{
            null,
            new Operation(ParseOperation.Shift, 6),
            new Operation(ParseOperation.Reduce, 2),
            new Operation(ParseOperation.Reduce, 4),
            null,
            new Operation(ParseOperation.Reduce, 6),
            null,
            null,
            new Operation(ParseOperation.Shift, 6),
            new Operation(ParseOperation.Reduce, 1),
            new Operation(ParseOperation.Reduce, 3),
            new Operation(ParseOperation.Reduce, 5)
        });

        parseTable.put(TokenType.Asterisk, new Operation[]{
            null,
            null,
            new Operation(ParseOperation.Shift, 7),
            new Operation(ParseOperation.Reduce, 4),
            null,
            new Operation(ParseOperation.Reduce, 6),
            null,
            null,
            null,
            new Operation(ParseOperation.Shift, 7),
            new Operation(ParseOperation.Reduce, 3),
            new Operation(ParseOperation.Reduce, 5)
        });

        parseTable.put(TokenType.LParens, new Operation[]{
            new Operation(ParseOperation.Shift, 4),
            null,
            null,
            null,
            new Operation(ParseOperation.Shift, 4),
            null,
            new Operation(ParseOperation.Shift, 4),
            new Operation(ParseOperation.Shift, 4),
            null,
            null,
            null,
            null
        });

        parseTable.put(TokenType.RParens, new Operation[]{
            null,
            null,
            new Operation(ParseOperation.Reduce, 2),
            new Operation(ParseOperation.Reduce, 4),
            null,
            new Operation(ParseOperation.Reduce, 6),
            null,
            null,
            new Operation(ParseOperation.Shift, 11),
            new Operation(ParseOperation.Reduce, 1),
            new Operation(ParseOperation.Reduce, 3),
            new Operation(ParseOperation.Reduce, 5)
        });

        parseTable.put(TokenType.EOF, new Operation[]{
            null,
            new Operation(ParseOperation.Accept, 0),
            new Operation(ParseOperation.Reduce, 2),
            new Operation(ParseOperation.Reduce, 4),
            null,
            new Operation(ParseOperation.Reduce, 6),
            null,
            null,
            null,
            new Operation(ParseOperation.Reduce, 1),
            new Operation(ParseOperation.Reduce, 3),
            new Operation(ParseOperation.Reduce, 5)
        });
    }

    private void buildGotoTable() {
        gotoTable = new HashMap<String, int[]>();
        gotoTable.put("E", new int[]{1, -1, -1, -1, 8, -1, -1, -1, -1, -1, -1, -1});
        gotoTable.put("T", new int[]{2, -1, -1, -1, 2, -1, 9, -1, -1, -1, -1, -1});
        gotoTable.put("F", new int[]{3, -1, -1, -1, 3, -1, 3, 10, -1, -1, -1, -1});
    }

    private void buildRuleTable() {
        rules = new Rule[]{
            new Rule("E", new String[]{"E", "+", "T"}),
            new Rule("E", new String[]{"T"}),
            new Rule("T", new String[]{"T", "*", "F"}),
            new Rule("T", new String[]{"F"}),
            new Rule("F", new String[]{"(", "E", ")"}),
            new Rule("F", new String[]{"id"})
        };
    }

    // Utility functions
    private void printCurToken() {
        System.out.println("Current Token: "+curToken);
    }
    
    private void advanceToken() {
        curToken = lexer.nextToken();
        printCurToken();
    }

    public void parse() {
        stack = new Stack<StackItem>();
        stack.add(new StackItem(StackItemType.State, 0));

        try {
            parseProgram();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void parseProgram() throws ParseException {
        int opsPerformed = 1;
        while (true) {
            printStack();

            Operation op = parseTable.get(curToken.type)[stack.lastElement().state];

            if (op == null) {
                throw new ParseException("Unexpected "+curToken);
            }

            System.out.println(op);

            if (op.op == ParseOperation.Accept) {
                System.out.println("OPS: "+opsPerformed);
                return;
            } else if (op.op == ParseOperation.Shift) {
                stack.add(new StackItem(StackItemType.Token, curToken.literal));
                stack.add(new StackItem(StackItemType.State, op.stateRule));
                advanceToken();
            } else if (op.op == ParseOperation.Reduce) {
                Rule r = rules[op.stateRule-1];

                // Match rule and replace RHS with LHS of rule
                for (int i = r.match.length-1; i >= 0; i--) {
                    stack.pop(); // State item
                    if (stack.lastElement().value.equals(r.match[i])) {
                        stack.pop();
                        continue;
                    }

                    throw new ParseException("Failed to reduce");
                }

                int nextState = gotoTable.get(r.replacement)[stack.lastElement().state];
                stack.add(new StackItem(StackItemType.Token, r.replacement));
                stack.add(new StackItem(StackItemType.State, nextState));
            }

            opsPerformed++;
        }
    }

    private void printStack() {
        for (StackItem item : stack) {
            System.out.print(item + " | ");
        }
        System.out.print("\n\n");
    }
}

enum StackItemType {
    Token, State;
}

class StackItem {
    StackItemType type;
    String value;
    int state;

    public StackItem(StackItemType t, String val) {
        type = t;
        value = val;
    }

    public StackItem(StackItemType t, int s) {
        type = t;
        state = s;
    }

    public String toString() {
        if (type == StackItemType.Token) {
            return value;
        }
        return String.valueOf(state);
    }
}

// ParseException is used to tell the user that an unexpected token was encountered
class ParseException extends Exception {
    public ParseException(String msg) {
        super(msg);
    }
}

enum ParseOperation {
    Shift,
    Reduce,
    Accept;
}

class Operation {
    ParseOperation op;
    int stateRule;

    public Operation(ParseOperation op, int stateRule) {
        this.op = op;
        this.stateRule = stateRule;
    }

    public String toString() {
        return op.name().charAt(0) + String.valueOf(stateRule);
    }
}

class Rule {
    String replacement;
    String[] match;

    public Rule(String lhs, String[] rhs) {
        replacement = lhs;
        match = rhs;
    }
}

class Token {
    String literal;
    TokenType type;

    public Token(String lit, TokenType type) {
        this.literal = lit;
        this.type = type;
    }

    public String toString() {
        return "Literal: " + this.literal + " Type: " + this.type;
    }
}

enum TokenType {
    EOF,
    Illegal,
    Plus,
    Asterisk,
    Ident,
    LParens,
    RParens;

    public String toString() {
        return this.name();
    }

    public static TokenType identOrKeyword(String ident) {
        ident = ident.toUpperCase();

        if (ident.equals("ID")) {
            return Ident;
        }

        // Otherwise, IDK what it is
        return Illegal;
    }
}

class Lexer {
    private String input;
    private int inputLoc;

    public Lexer(String input) {
        this.input = input;
        this.inputLoc = 0;
    }

    // Returns the character as the current lex location.
    // Will return null (0x00) if at end of input.
    private char curChar() {
        if (inputLoc == input.length()) {
            return 0;
        }
        return input.charAt(inputLoc);
    }
    
    // Returns the character as the next lex location.
    // Will return null (0x00) if next char would be end of input.
    private char nextChar() {
        if (inputLoc+1 == input.length()) {
            return 0;
        }
        return input.charAt(inputLoc+1);
    }

    private void advanceChar() {
        inputLoc++;
    }

    public Token nextToken() {
        skipWhitespace(); // Whitespace is insignificant and not needed for parsing
        Token token;

        switch(curChar()) {
            // End of file
            case 0:
                token = new Token("EOF", TokenType.EOF);
                break;

            // Operators
            case '+':
                token = new Token("+", TokenType.Plus);
                break;
            case '*':
                token = new Token("*", TokenType.Asterisk);
                break;

            // Groupings
            case '(':
                token = new Token("(", TokenType.LParens);
                break;
            case ')':
                token = new Token(")", TokenType.RParens);
                break;

            // Complex types/idents
            default:
                if (isLetter()) {
                    String ident = lexIdent();
                    token = new Token(ident, TokenType.identOrKeyword(ident));
                } else {
                    token = new Token(Character.toString(curChar()), TokenType.Illegal);
                }
        }

        advanceChar();
        return token;
    }

    private String lexIdent() {
        int start = inputLoc;

        while(isLetter(nextChar())) {
            advanceChar();
        }

        return input.substring(start, inputLoc+1);
    }

    private void skipWhitespace() {
        while(isWhitespace()) {
            advanceChar();
        }
    }

    private boolean isWhitespace() {
        char curChar = curChar();
        return (curChar == '\n' || curChar == '\r' || curChar == '\t' || curChar == ' ');
    }

    private boolean isLetter() {
        char curChar = curChar();
        return (curChar >= 'A' && curChar <= 'z');
    }

    private boolean isLetter(char c) {
        return (c >= 'A' && c <= 'z');
    }
}
