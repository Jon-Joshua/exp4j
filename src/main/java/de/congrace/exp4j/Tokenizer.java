/*
   Copyright 2011 frank asseg

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package de.congrace.exp4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.congrace.exp4j.tokens.FunctionToken;
import de.congrace.exp4j.tokens.NumberToken;
import de.congrace.exp4j.tokens.OperatorToken;
import de.congrace.exp4j.tokens.ParenthesisToken;
import de.congrace.exp4j.tokens.Token;
import de.congrace.exp4j.tokens.VariableToken;
import de.congrace.exp4j.tokens.FunctionToken.Function;

/**
 * Class for tokenizing mathematical expressions by breaking an expression up
 * into multiple different {@link Token}s
 * 
 * @author fas@congrace.de
 */
class Tokenizer {
    private String[] variableNames;
    private final Set<String> keywords=new HashSet<String>();
    
    {
      keywords.add("abs");
      keywords.add("acos");
      keywords.add("asin");
      keywords.add("atan");
      keywords.add("cbrt");
      keywords.add("ceil");
      keywords.add("cos");
      keywords.add("cosh");
      keywords.add("exp");
      keywords.add("expm1");
      keywords.add("floor");
      keywords.add("log");
      keywords.add("sin");
      keywords.add("sinh");
      keywords.add("sqrt");
      keywords.add("tan");
      keywords.add("tanh");
    }

	/**
	 * construct a new Tokenizer that recognizes variable names
	 * 
	 * @param variableNames
	 *            the variable names in the expression
     * @throws IllegalArgumentException if the variablenames are not valid
	 */
	Tokenizer(String[] variableNames) throws IllegalArgumentException{
		super();
		this.variableNames = variableNames;
		if (variableNames != null){
		    for (String varName:variableNames){
		        if (keywords.contains(varName.toLowerCase())){
		            throw new IllegalArgumentException("Variable '" + varName + "' can not have the same name as a function");
		        }
		    }
		}
	}

	/**
	 * construct a simple tokenizer without variable names
	 */
	Tokenizer() {
		super();
	}

	/**
	 * tokenize an infix expression by breaking it up into different
	 * {@link Token} that can represent operations,functions,numbers,
	 * paranthesis or variables
	 * 
	 * @param infix
	 *            the infix espression to be tokenized
	 * @return the {@link Token}s representing the expression
	 * @throws UnparseableExpressionException
	 *             when the expression is invalid
	 * @throws UnknownFunctionException
	 *             when an unknown function name has been used.
	 */
	Token[] tokenize(String infix) throws UnparseableExpressionException,UnknownFunctionException {
		final List<Token> tokens = new ArrayList<Token>();
		final char[] chars = infix.toCharArray();
        // iterate over the chars and fork on different types of input
        Token lastToken;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == ' ') continue;
            if (isDigit(c)) {
            	final StringBuilder valueBuilder = new StringBuilder(1);
                // handle the numbers of the expression
                valueBuilder.append(c);
                int numberLen = 1;
                while (chars.length > i + numberLen && isDigit(chars[i + numberLen])) {
                    valueBuilder.append(chars[i + numberLen]);
                    numberLen++;
                }
                i += numberLen - 1;
                lastToken=new NumberToken(valueBuilder.toString());
            }else if (Character.isLetter(c) || c == '_'){
                // can be a variable or function
            	final StringBuilder nameBuilder=new StringBuilder();
                nameBuilder.append(c);
                int offset=1;
                while ( chars.length > i+offset && (Character.isLetter(chars[i + offset]) || Character.isDigit(chars[i + offset]) || chars[i+offset] == '_')){
                    nameBuilder.append(chars[i + offset++]);
                }
                String name=nameBuilder.toString();
                if (this.isVariable(name)){
                    // a variable
                    i += offset - 1;
                    lastToken=new VariableToken(name);
                }else if (this.isFunction(name)){
                    // might be a function
                    i += offset - 1;
                    lastToken=new FunctionToken(name);
                }else {
                 // an unknown symbol was encountered
                    throw new UnparseableExpressionException(c, i);
                }
            }else if (OperatorToken.isOperator(c)) {
            	lastToken=new OperatorToken(String.valueOf(c),OperatorToken.getOperation(c));
            }else if (c == '(' || c == ')' || c == '[' || c== ']' || c == '{' || c == '}'){
            	lastToken=new ParenthesisToken(String.valueOf(c));
            }else {
                // an unknown symbol was encountered
                throw new UnparseableExpressionException(c, i);
            }
            tokens.add(lastToken);
        }
        return tokens.toArray(new Token[tokens.size()]);
    }

	private boolean isFunction(String name) {
	    for (Function fn:Function.values()){
	        if (fn.name().equals(name.toUpperCase())){
	            return true;
	        }
	    }
	    return false;
	}

    /**
	 * check if a char is part of a number
	 * 
	 * @param c
	 *            the char to be checked
	 * @return true if the char is part of a number
	 */
	private boolean isDigit(char c) {
        return Character.isDigit(c) || c == '.';
    }

	/**
	 * check if a String is a variable name
	 * 
	 * @param name the variable name which is checked to be valid
	 *            the char to be checked
	 * @return true if the char is a variable name (e.g. x)
	 */
	private boolean isVariable(String name) {
		if (variableNames != null) {
			for (String var : variableNames) {
				if (name.equals(var)) {
					return true;
				}
			}
		}
		return false;
	}
}