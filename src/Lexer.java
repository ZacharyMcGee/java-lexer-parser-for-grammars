import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

public class Lexer {
	//--------------------------------------------------------------------------------------------------------------------
	//	Global initialization of JFrame elements
	//--------------------------------------------------------------------------------------------------------------------
	private static JFrame frame = new JFrame("Parser");
	
	private static JTextArea textArea, textAreaConsole;
	private static JScrollPane scrollPane, scrollPane2, scrollPaneConsole;
	private static DefaultTableModel lexerModel = new DefaultTableModel();
	private static JTable lexerTable = new JTable(lexerModel);
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Highlighters for coloring text in JFrame textareas to indicate error and if parse was accepted or not
	//--------------------------------------------------------------------------------------------------------------------
	private static DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
	private static DefaultHighlighter.DefaultHighlightPainter errorHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
	private static DefaultHighlighter.DefaultHighlightPainter goodHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);

	//--------------------------------------------------------------------------------------------------------------------
	//	Default accept state is true, but checks after parsing if it has been triggered to false, if so, parse failed
	//--------------------------------------------------------------------------------------------------------------------
	public static boolean accepted = true;
	public static boolean lexAccepted = true;
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Linked list to store all the lexemes after analyizing 
	//--------------------------------------------------------------------------------------------------------------------
	public static LinkedList<Lexeme> lexemes;
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Lexeme variable that is set to the first item of the lexemes LinkedList
	//--------------------------------------------------------------------------------------------------------------------
	static Lexeme lookahead;
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Variables for the lexer (Defining letters, digits, symbols, and keywords)
	//--------------------------------------------------------------------------------------------------------------------
    int position; // Position of scanner
    char[] expression; // String to scan
    
	static char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(); // Alphabet chars accepted
	static char[] digits = "0123456789".toCharArray(); // Numbers accepted
	static char symbols[] = {'=', '<', '+', '-', '*', '/', '(', ')', '\n', '\t', '\\'}; // Symbols
	static String keywords[] = {"true", "false", "or", "and", "not"}; // Keywords

	//--------------------------------------------------------------------------------------------------------------------
	//	The Lexer, with a String as the parameter
	//--------------------------------------------------------------------------------------------------------------------
    Lexer(String expression) {
        this.expression = expression.toCharArray();
        this.position = 0;
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The Parse Function, takes a LinkedList of Lexemes and starts from the top calling Expression
	//--------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
	public static void parse(LinkedList<Lexeme> Lexemes){
    	Lexemes = (LinkedList<Lexeme>) lexemes;
    	lookahead = Lexemes.getFirst();
    	
    	Expression();
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	Pops the top of the Lexemes list and if it's not empty, make the lookahead the new first
	//--------------------------------------------------------------------------------------------------------------------
    private static void nextToken()
    {
      lexemes.pop();
      if(lexemes.isEmpty() == false){
    	  lookahead = lexemes.getFirst();
      } 
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	Check if a keyword 
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean isKeyword(String value){
    	boolean result = false;
    	
    	int count = 0;
    	while(count < keywords.length && result == false){
    		if(value.equals(keywords[count])){
    			result = true;
    		}
    		count++;
    	}
    	return result;
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	Check if a symbol 
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean isSymbol(char value){
    	boolean result = false;
    	
    	int count = 0;
    	while(count < symbols.length && result == false){
    		if(value == symbols[count]){
    			result = true;
    		}
    		count++;
    	}
    	return result;
    }
    
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Check if alphabetic character
	//--------------------------------------------------------------------------------------------------------------------
	public static boolean isLetter(char a){
		boolean result = false;
		
		int count = 0;
		while(count < letters.length && result == false){
			if(a == letters[count]){
				result = true;
			}
			count++;
		}
		return result;
	}
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Check if numeric character
	//--------------------------------------------------------------------------------------------------------------------
	public static boolean isDigit(char a){
		boolean result = false;
		
		int count = 0;
		while(count < digits.length && result == false){
			if(a == digits[count]){
				result = true;
			}
			count++;
		}
		return result;
	}
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Check if the char is in the identifier alphabet
	//--------------------------------------------------------------------------------------------------------------------
	public static boolean idAlphabet(char a){
		boolean result = false;
		if(isDigit(a) || isLetter(a) || a == '_'){
			result = true;
		}
		return result;
	}
	
	//--------------------------------------------------------------------------------------------------------------------
	//	Check if the char is recognizable (belongs to: letters/numbers/symbols/_/whitespace)
	//--------------------------------------------------------------------------------------------------------------------
	public static boolean checkChar(char a){
		boolean result = false;
		if(idAlphabet(a) || isSymbol(a) || a == ' '){
			result = true;
		}
		return result;
	}

	//--------------------------------------------------------------------------------------------------------------------
	//	Each lexeme object has 3 values: kind, value, and position
	//--------------------------------------------------------------------------------------------------------------------
    class Lexeme {
        String kind, value, position;
        Lexeme(String kind, String value, String position) {
            this.kind = kind; // ID, NUM, etc...
            this.value = value; // only ID and NUM have values
            this.position = position; // position is from the starting character to the ending character
        }
    }

	//--------------------------------------------------------------------------------------------------------------------
	//	Get the next token
	//--------------------------------------------------------------------------------------------------------------------
    Lexeme getToken() {
        StringBuilder value = new StringBuilder(); // For appending the value
        boolean endOfToken = false;
        String kind = "";
        String startingPos = "";
        

        while (!endOfToken && hasMoreToken()) 
        {
    		//------------------------------------------------------------------------------------------------------------
    		//	Check if it's whitespace, increment and ignore position while it's whitespace
    		//------------------------------------------------------------------------------------------------------------
    		while(hasMoreToken() && expression[position] == ' '){
    			if(position < expression.length){
    				position++; 
    			}	else {
    				endOfToken = true;
    				break;
    			}
        	}

    		if(!endOfToken && hasMoreToken()){
    			if(checkChar(expression[position])){
        		//--------------------------------------------------------------------------------------------------------
        		//	First check if it's a comment by looking ahead one, if it is, increment and ignore position until new line
        		//--------------------------------------------------------------------------------------------------------
            	if(isSymbol(expression[position])){
            		if(expression[position] == '/' && expression[position + 1] == '/'){ 
                		startingPos = "" + position;
                		position++; // Increment twice since there are two "//"
                		position++;
                		while(hasMoreToken() && expression[position] != '\n'){
                			value.append(expression[position]);
                			position++;
                		}
                	
                    	kind = "COMMENT";
                    	endOfToken = true;
                    	position++;
                	} 
                	//----------------------------------------------------------------------------------------------------
                	//	If it's not a comment, add the symbol 
                	//----------------------------------------------------------------------------------------------------
            		else if(expression[position] == '\n'){
            			startingPos = "" + position;
            			kind = "NEWLINE";
            			endOfToken = true;
            			position++;
            		}
            		else if(expression[position] == '\t'){
            			startingPos = "" + position;
            			kind = "TAB";
            			endOfToken = true;
            			position++;
            		}
                	else
                	{
                		startingPos = "" + position;
                		kind = Character.toString(expression[position]) + " ";
                		endOfToken = true;
                		position++;
                	}
            	}
        		//--------------------------------------------------------------------------------------------------------
        		//	If not symbol, check if it's a letter
        		//--------------------------------------------------------------------------------------------------------
            	else 
            	{
            	if(isLetter(expression[position])){
            		boolean appending = true; 
            		startingPos = "" + position;
            	
            		//----------------------------------------------------------------------------------------------------
            		//	If it's a letter, then it must be a ID or keyword, append value while there's more and no whitespace
            		//----------------------------------------------------------------------------------------------------
            		while(hasMoreToken() && appending == true){
            			if(expression[position] != ' ' && !isSymbol(expression[position])){
            				if(idAlphabet(expression[position])){
            					value.append(expression[position]);
            					position++;
            				} 
            				//--------------------------------------------------------------------------------------------
            				//  Throw error if a character is not allowed in the ID alphabet
            				//--------------------------------------------------------------------------------------------
            				else
            				{
                		    	printError("Error: The character: " + expression[position] + " is not allowed in an ID");         				
            				}
            			}
            			else 
            			{
            				appending = false;
            			}
            		}
            		//----------------------------------------------------------------------------------------------------
            		//	Check if the appended value is equal to a keyword
            		//----------------------------------------------------------------------------------------------------
            		if(isKeyword(value.toString())){
            			kind = value.toString(); 
            			value.delete(0, value.length());
            		} 
            		else
            		{
            			kind = "ID";
            		}
                	endOfToken = true;
            	} 
        		//--------------------------------------------------------------------------------------------------------
        		//	If not a symbol or letter, check if digit
        		//--------------------------------------------------------------------------------------------------------
            	else 
            	{
            		if(isDigit(expression[position])){
                		boolean appending = true; 
                		startingPos = "" + position;

                		while(hasMoreToken() && appending == true){
                			if(expression[position] != ' ' && !isSymbol(expression[position])){
                				if(isDigit(expression[position])){
                					value.append(expression[position]);
                					position++;
                				}
                				//----------------------------------------------------------------------------------------
                				//	Nothing but digits can follow digits
                				//----------------------------------------------------------------------------------------
                				else
                				{
                					appending = false;
                					endOfToken = true;
                					printError("Error: The character: " + expression[position] + " is not allowed in a NUM");  				
                				}
                			}
                			else 
                			{
                				appending = false;
                			}
                		}
                		//------------------------------------------------------------------------------------------------
                		//	Check if the identifier is a keyword
                		//------------------------------------------------------------------------------------------------
                		if(isKeyword(value.toString())){
                			kind = value.toString(); 
                			value.delete(0, value.length());
                		} 
                		else
                		{
                			kind = "NUM";
                		}
                    		endOfToken = true;
            			}
            		}
            	}
     
        	} 
        	//------------------------------------------------------------------------------------------------------------
        	//	Throw an error if the character isn't in {Letters, Digits, Symbols, ' ', '_'}
        	//------------------------------------------------------------------------------------------------------------
        	else 
        	{
		    	printError("Error: The character: " + expression[position] + " is not allowed anywhere in the input");         				
        	}
        } 
     }

    //--------------------------------------------------------------------------------------------------------------------
    //	Return a new lexeme object with the values collected
    //--------------------------------------------------------------------------------------------------------------------
    return new Lexeme(kind, value.toString(), startingPos + "-" + position);
    }
    
    Lexeme endOfFile(){
    	return new Lexeme("END_OF_FILE", "",  "" + position);
    }
	//--------------------------------------------------------------------------------------------------------------------
	//	Make sure we haven't reached the end of the input
	//--------------------------------------------------------------------------------------------------------------------
    boolean hasMoreToken() {
        return position < expression.length; // Has more tokens if position is less than the strings length
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	Get the next character in the expression
	//--------------------------------------------------------------------------------------------------------------------
    char getNextChar(int position){
    	return expression[position + 1];
    }

	//--------------------------------------------------------------------------------------------------------------------
	//	The main class
	//--------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
    	
    	//----------------------------------------------------------------------------------------------------------------
    	//	Tabs for the Default Table Model
    	//----------------------------------------------------------------------------------------------------------------
    	lexerModel.addColumn("Position");
    	lexerModel.addColumn("Kind");
    	lexerModel.addColumn("Value");
    	
    	//----------------------------------------------------------------------------------------------------------------
    	//	The Parse Button
    	//----------------------------------------------------------------------------------------------------------------
	    JButton beginButton = new JButton();     
	    beginButton.setVisible(true);
	    beginButton.setText("Parse");
	    
    	//----------------------------------------------------------------------------------------------------------------
    	//	The top Text Area for input
    	//----------------------------------------------------------------------------------------------------------------
	    textArea = new JTextArea();
        EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 400, 300));
        textArea.setBorder(eb);
        
    	//----------------------------------------------------------------------------------------------------------------
    	//	The bottom Text Area for error messages and tracing
    	//----------------------------------------------------------------------------------------------------------------
	    textAreaConsole = new JTextArea(10, 100);
	    
    	//----------------------------------------------------------------------------------------------------------------
    	//	The two top Scroll Panes that hold the input Text Area and the Default Table Model
    	//----------------------------------------------------------------------------------------------------------------
	    scrollPane = new JScrollPane(textArea);
	    scrollPane2 = new JScrollPane(lexerTable);
	    
    	//----------------------------------------------------------------------------------------------------------------
    	//	The bottom ScrollPane that holds the Text Area Console
    	//----------------------------------------------------------------------------------------------------------------
	    scrollPaneConsole = new JScrollPane(textAreaConsole);

	   
    	//----------------------------------------------------------------------------------------------------------------
    	//	The JPanels
    	//----------------------------------------------------------------------------------------------------------------
	    JPanel jPan1 = new JPanel(new BorderLayout());
	    JPanel jPan2 = new JPanel(new BorderLayout());
	    JPanel jPan3 = new JPanel(new BorderLayout());
	    JPanel jPanMain = new JPanel(new BorderLayout());
	    
	    jPan1.add(scrollPane);
	    jPan1.add(scrollPane2, BorderLayout.EAST);
	    jPan3.add(jPan1);
	    jPan1.add(scrollPaneConsole, BorderLayout.SOUTH);

	    jPan2.add(beginButton, BorderLayout.SOUTH);
	    
	    jPanMain.add(jPan3, BorderLayout.NORTH);
	    jPanMain.add(jPan2, BorderLayout.SOUTH);
	    
	    frame.add(jPanMain);
	    
        
		//----------------------------------------------------------------------------------------------------
		//	Create JFrame
		//----------------------------------------------------------------------------------------------------
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 660);
		frame.setVisible(true);	
	    frame.pack();
        

    	//----------------------------------------------------------------------------------------------------------------
    	//	The Parse Button Listener
    	//----------------------------------------------------------------------------------------------------------------
	    beginButton.addActionListener(new java.awt.event.ActionListener() {
	          @Override
	          public void actionPerformed(java.awt.event.ActionEvent evt) {	  
	        	  accepted = true;
	        	  	        	  
	        	  textAreaConsole.setText("");
	        	  textAreaConsole.setBackground(Color.white);
	        	  
	              textAreaConsole.append("---------------------------STARTING LEXER---------------------------\n");
	        	  
	        	  clearLexerModel();
	        	  
	              String expression = textArea.getText(); // Practice string 
	              Lexer tokenizer = new Lexer(expression);
	              
	              LinkedList<Lexeme> lexemesList = new LinkedList<Lexeme>();
	              	              
	          	//----------------------------------------------------------------------------------------------------------------
	          	//	Main loop
	          	//----------------------------------------------------------------------------------------------------------------
	              while (tokenizer.hasMoreToken()) 
	              {
	                  Lexeme nextToken = tokenizer.getToken(); // Call the getToken() method 
	                  lexemesList.add(nextToken);
	                  
	                  lexerModel.addRow(new Object[] {nextToken.position, nextToken.kind, nextToken.value});
	                  textAreaConsole.append("Position: " + nextToken.position + "\t\tKind: " + nextToken.kind + "\tValue: " + nextToken.value + "\n");
	              }
	              
	              lexemes = lexemesList;
	              
	          	//----------------------------------------------------------------------------------------------------------------
	          	//	Create and print end of file while no more tokens are left
	          	//----------------------------------------------------------------------------------------------------------------
	              if(tokenizer.hasMoreToken() == false){
	              	Lexeme nextToken = tokenizer.endOfFile();
	              }
	              
	          	//----------------------------------------------------------------------------------------------------------------
	          	//	If the lexemes Linked List is not empty, start parsing a new expression
	          	//----------------------------------------------------------------------------------------------------------------
	              textAreaConsole.append("---------------------------STARTING PARSE---------------------------\n");
	              while(lexemes.isEmpty() == false){
	            	  if(lexemes.getFirst().kind == "COMMENT"){
	            		  lexemes.pop();
	            	  } else {
	            		  parse(lexemes);
	            	  }
	              }
	              
	          	//----------------------------------------------------------------------------------------------------------------
	          	//	After Parsing, check if the accepted boolean has been triggered, then print the result
	          	//----------------------------------------------------------------------------------------------------------------
	              if(accepted == true){
	            	  textAreaConsole.append("Accepted");
					 try {
						textAreaConsole.getHighlighter().addHighlight(textAreaConsole.getText().length() - 8, textAreaConsole.getText().length(), goodHighlightPainter);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	              } 
	              else 
	              {
	            	  textAreaConsole.append("Not Accepted");
					 try {
						textAreaConsole.getHighlighter().addHighlight(textAreaConsole.getText().length() - 12, textAreaConsole.getText().length(), errorHighlightPainter);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	              }
	          }
	    });
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	Clear the Lexer Table(For when the parse button is pressed again, so results don't mix)
	//--------------------------------------------------------------------------------------------------------------------
	public static void clearLexerModel(){
		if (lexerModel.getRowCount() > 0) {
		    for (int i = lexerModel.getRowCount() - 1; i > -1; i--) {
		        lexerModel.removeRow(i);
		    }
		}
	}
	
	//--------------------------------------------------------------------------------------------------------------------
	//	For printing errors to the textAreaConsole, uses the Highlighters to color the text
	//--------------------------------------------------------------------------------------------------------------------
	public static void printError(String str){
		try {
			textAreaConsole.append(str + '\n');
			textAreaConsole.getHighlighter().addHighlight(textAreaConsole.getText().length() - str.length() - 1, textAreaConsole.getText().length() - str.length() - 1 + str.length(), highlightPainter);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//--------------------------------------------------------------------------------------------------------------------
	//	An extra Expression() class for when expression is needed inside of an Expression()
	//--------------------------------------------------------------------------------------------------------------------
	public static boolean ExpressionNested() {
    	textAreaConsole.append("Enter expressionNested\n");

    	BooleanExpression();

    	textAreaConsole.append("Exit expressionNested\n");
    	
		return false;
	}
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The main Expression() class, which checks for TAB, COMMENT, NEWLINE
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean Expression() {
    	textAreaConsole.append("Enter expression\n");

    	BooleanExpression();
    	textAreaConsole.append("Exit expression\n");
    	
    	if(lexemes.isEmpty() == false){

    		if(lexemes.getFirst().kind != "NEWLINE"){
    			if(lexemes.getFirst().kind == "COMMENT"){
    				nextToken();
    			}
    			else if(lexemes.getFirst().kind == "TAB"){
    				while(lexemes.getFirst().kind == "TAB"){
    					if(lexemes.isEmpty() == false){
    					nextToken();
    					}
    				}
    			}
    			else 
    			{
        		accepted = false;
    			}
    		} else {
    			nextToken();
    		}
    	}
		return false;
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The BooleanExpression(): BooleanExpression  =  BooleanTerm { "or" BooleanTerm } .
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean BooleanExpression() {
    	textAreaConsole.append("Enter Boolean expression\n");
    	BooleanTerm();
    	
    	while(lookahead.kind.equals("or")){
    		nextToken();
    		BooleanTerm();
    	}
    	textAreaConsole.append("Exit Boolean Expression\n");
		return false;
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The BooleanTerm(): BooleanFactor { "and" BooleanFactor } .
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean BooleanTerm(){
    	textAreaConsole.append("Enter Boolean term\n");
    	
    	BooleanFactor();
    	
    	while(lookahead.kind.equals("and")){
    		nextToken();
    		BooleanFactor();
    	}
    	textAreaConsole.append("Exit Boolean Term\n");
		return false;
    	
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The BooleanFactor(): [ "not" ] ArithmeticExpression [ ( "=" | "<") ArithmeticExpression) ] .
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean BooleanFactor(){
    	textAreaConsole.append("Enter boolean factor\n");

    	if(lookahead.kind.equals("not")){
    		nextToken();
    	}
    	
    	ArithmeticExpression();
    	
    	if(lookahead.kind.equals("= ") || lookahead.kind.equals("< ")){
    		if(lexemes.isEmpty() == true){
    			accepted = false;
    			printError("Error: Must be an Arithmetic Expression after " + lookahead.kind);
    		} else {
    			nextToken();
    			ArithmeticExpression();
    		}
    	}
    	textAreaConsole.append("Exit boolean factor\n");

		return false;
    	
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The ArithmeticExpression(): Term { ("+" | "-") Term } .
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean ArithmeticExpression(){
    	textAreaConsole.append("Enter Arithmetric Expression\n");
    	
    	Term();
    	
    	while(lookahead.kind.equals("+ ") || lookahead.kind.equals("- ")){
    		if(lexemes.isEmpty() == true){
    			accepted = false;
    			String errorMessage = "Error: Must be a term after " + lookahead.kind;
    			printError(errorMessage);
    			break;
    		} else {
    			nextToken();
    			Term();
    		}
    	}
    	textAreaConsole.append("Exit Arithmetric Expression\n");

		return false;
    	
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The Term(): Factor { ("*" | "/") Factor } .
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean Term(){
    	textAreaConsole.append("Enter Term\n");

    	Factor();
    	
    	while(lookahead.kind.equals("* ") || lookahead.kind.equals("/ ")){
    		nextToken();
    		Factor();
    	}
    	textAreaConsole.append("Exit Term\n");

		return false;
    	
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The Factor(): Literal  |  Identifier  |  "(" Expression ")" .
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean Factor(){
    	textAreaConsole.append("Enter Factor" + lookahead.kind + "\n");
    	
    	if(lookahead.kind.equals("NEWLINE")){
    		nextToken();
    	}
    	
    	if(lookahead.kind.equals("TAB")){
    		nextToken();
    	}
    	
    	if(lookahead.kind.equals("true") || lookahead.kind.equals("false") || lookahead.kind.equals("NUM")){
    		Literal();
    	}
    	else if(lookahead.kind.equals("ID")){
    		Identifier();
    	}
    	else if(lookahead.kind.equals("( ")){
    		nextToken();
    		ExpressionNested();
    		if(lookahead.kind.equals(") ") == false){
    			accepted = false;
    			printError("Error: The character: '(' must be closed with: ')'");
    		} 
    		else 
    		{
    			nextToken();
    		}
    	}
    	else 
    	{
    		textAreaConsole.append("FAIL AT " + lookahead.kind);
    	}
    	textAreaConsole.append("Exit Factor\n");

		return false;
    	
    }
   
	//--------------------------------------------------------------------------------------------------------------------
	//	The Literal(): BooleanLiteral  |  IntegerLiteral .
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean Literal(){
    	textAreaConsole.append("Enter literal\n");

    	if(lookahead.kind.equals("true") || lookahead.kind.equals("false")){
    		BooleanLiteral();
    	}
    	
    	if(lookahead.kind.equals("NUM")){
    		IntegerLiteral();
    	}
    	textAreaConsole.append("Exit  literal\n");
    	return false;
    	
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The BooleanLiteral(): A terminal
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean BooleanLiteral(){
    	textAreaConsole.append("Enter boolean literal\n");
		nextToken();
		textAreaConsole.append("Exit boolean literal\n");
		return false;
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The IntegerLiteral(): A terminal
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean IntegerLiteral(){
    	textAreaConsole.append("Enter integer literal\n");
		nextToken();
		textAreaConsole.append("Exit integer literal\n");
		return false;
    }
    
	//--------------------------------------------------------------------------------------------------------------------
	//	The Identifier(): A terminal
	//--------------------------------------------------------------------------------------------------------------------
    public static boolean Identifier(){
    	textAreaConsole.append("Enter id = " + lookahead.value + "\n");
		nextToken();
		textAreaConsole.append("Exit id\n");
		return false;
    }
}