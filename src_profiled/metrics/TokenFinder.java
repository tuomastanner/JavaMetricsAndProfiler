package metrics;

public class TokenFinder {
    private char[][] tokensArr;
    private char[] textArr;
    
    /** Inits TokenFinder with text and tokens
     * 
     * @param text
     * @param tokens
     */
    public TokenFinder(String text, String[] tokens) { metrics.Profiler.write("entry;metrics.TokenFinder;public TokenFinder(String text, String[] tokens)"); 
        textArr = text.toCharArray();
        
        tokensArr = new char[tokens.length][];
        for (int i = 0; i < tokens.length; ++i) {
            tokensArr[i] = tokens[i].toCharArray();
        }
        
     metrics.Profiler.write("exit;metrics.TokenFinder;public TokenFinder(String text, String[] tokens)"); }
        
    /** 
     * "
     * @param startFrom
     * @return the position of the next token in the text + which token it is
     */
    public int[] getNextToken(int startFrom) { metrics.Profiler.write("entry;metrics.TokenFinder;public int[] getNextToken(int startFrom)"); 
        int curToken = 0;
        int tokenPos = 0;
        boolean stringLiteral = false;
        boolean inComment = false;
        boolean blockComment = false;
        for (int i = startFrom; i < textArr.length; ++i) {
             if (blockComment) {
                if (textArr[i] == '\n') {
                    ++i;
                    blockComment = false;
                }
                else { //still in comment - advance
                    continue;
                }
            }
            if (inComment) {
                if (textArr[i] == '*' && textArr[i+1] == '/') { //comment ends
                    inComment = false;
                    i += 2;
                }
                else { //still in comment - advance
                    continue;
                }
            }
            
            if (stringLiteral) { 
                if ((textArr[i] == '"' || textArr[i] == '\'') && textArr[i-1] != '\\') {
                    stringLiteral = false;
                }
                else { //jump over string literal
                    continue;
                }
            }
            else if ((textArr[i] == '"' || textArr[i] == '\'') && textArr[i-1] != '\\') {
                stringLiteral = true;
                tokenPos = 0;
                continue;
            }
            
            //check for comment token
            if (textArr[i] == '/') {
                if (textArr[i+1] == '*') {
                    inComment = true;
                    ++i;
                    continue;
                }
                if (textArr[i+1] == '/') {
                    blockComment = true;
                    ++i;
                    continue;
                }
            }
            
            while (curToken < tokensArr.length) { //match current character to each token
                //try to match this token
                while (i + tokenPos < textArr.length &&
                        textArr[i + tokenPos] == tokensArr[curToken][tokenPos]) { 
                    ++tokenPos;
                    if (tokenPos == tokensArr[curToken].length) { //found complete token
                        int[] ret = {i, curToken};
                         metrics.Profiler.write("exit;metrics.TokenFinder;public int[] getNextToken(int startFrom)"); return ret;
                    }
                }
                //wasn't it, try next token
                ++curToken;
                tokenPos = 0; //reset token pos
            }
            //none of the tokens matched - advance
            curToken = 0;
            
        }//end for
        
        //no token found
        int[] nothing = {-1, -1};
         metrics.Profiler.write("exit;metrics.TokenFinder;public int[] getNextToken(int startFrom)"); return nothing;
    }
    
    /** Finds position of specified token if not inside a string literal
     * 
     * @param token
     * @param row
     * @param startFrom
     * @return position of token or -1 if not found
     */
    public int findToken(String token, int startFrom) { metrics.Profiler.write("entry;metrics.TokenFinder;public int findToken(String token, int startFrom)"); 
        //load temporary token
        char[][] origTokens = this.tokensArr;
        this.tokensArr = new char[1][1];
        this.tokensArr[0] = token.toCharArray();
        
        int tokenPos = getNextToken(startFrom)[0];
        
        this.tokensArr = origTokens; //set original tokens
         metrics.Profiler.write("exit;metrics.TokenFinder;public int findToken(String token, int startFrom)"); return tokenPos;
    }
    
    /** Otherwise same as findToken(token, startFrom), but sets text also
     * 
     * @param token
     * @param text
     * @param startFrom
     * @return
     */
    public int findToken(String token, String text, int startFrom) { metrics.Profiler.write("entry;metrics.TokenFinder;public int findToken(String token, String text, int startFrom)"); 
        textArr = text.toCharArray();
         metrics.Profiler.write("exit;metrics.TokenFinder;public int findToken(String token, String text, int startFrom)"); return findToken(token, startFrom);
    }
    
    public int findPrevious(char c, int startFrom) { metrics.Profiler.write("entry;metrics.TokenFinder;public int findPrevious(char c, int startFrom)"); 
        for (int i = startFrom; i >= 0; --i) {
            if (textArr[i] == c) {
                 metrics.Profiler.write("exit;metrics.TokenFinder;public int findPrevious(char c, int startFrom)"); return i;
            }
        }
         metrics.Profiler.write("exit;metrics.TokenFinder;public int findPrevious(char c, int startFrom)"); return 0;
    }
}
