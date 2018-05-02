package cz.cvut.fel.horovtom.logic.converters;

import java.util.logging.Logger;

/**
 * This class holds the tree for Regular expression {@link Regex}
 */
class Node {
    private static final Logger LOGGER = Logger.getLogger(Node.class.getName());

    public enum NodeType {
        LETTER, OR, KLEENE, DOT
    }

    private NodeType type;
    private Node children[] = new Node[2];
    private String content;
    private boolean compiled = false;

    /**
     * Private constructor, needed for {@link #copy()}
     */
    private Node() {
    }

    /**
     * This method is used as a constructor. It will automatically initialize whole tree.
     */
    public static Node compile(String regex) {
        Node n = new Node();
        n.type = NodeType.LETTER;
        n.content = regex;
        n.compile();
        return n;
    }

    public void setContent(String content) {
        compiled = false;
        this.content = content;
        compile();
    }

    public void setLeftChild(Node child) {
        children[0] = child.copy();
        children[0].compile();
    }

    public void setRightChild(Node child) {
        children[1] = child.copy();
        children[1].compile();
    }

    public String getInString(String prefix) {
        StringBuilder sb = new StringBuilder(prefix).append(content).append('\n');
        switch (type) {
            case OR:
            case DOT:
                sb.append(children[0].getInString(prefix + "  "));
                sb.append(children[1].getInString(prefix + "  "));
                break;
            case KLEENE:
                sb.append(children[0].getInString(prefix + "  "));
                break;
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getListString();
    }

    private String getListString() {
        StringBuilder sb = new StringBuilder(content);

        switch (type) {
            case OR:
            case DOT:
                sb.append("(").append(children[0].getListString()).append(",");
                sb.append(children[1].getListString()).append(")");
                break;
            case KLEENE:
                sb.append("(").append(children[0].getListString()).append(")");
                break;
        }

        return sb.toString();
    }

    /**
     * This method will compile the tree and construct its children properly
     *
     * @throws UnsupportedOperationException
     */
    private void compile() throws UnsupportedOperationException {
        if (!type.equals(NodeType.LETTER)) return;
        if (compiled) return;
        if (content.length() == 1) {
            compiled = true;
            return;
        }

        if (findTopLevel()) {
            compiled = true;
            return;
        }
        //It is a kleene, or a letter sequence in brackets
        //Is it a kleene?
        if (content.charAt(content.length() - 1) == '*') {
            this.type = NodeType.KLEENE;
            Node child;
            if (content.charAt(content.length() - 2) == ')' &&
                    content.charAt(0) == '(') {
                child = Node.compile(content.substring(1, content.length() - 2));
            } else {
                child = Node.compile(content.substring(0, content.length() - 1));
            }
            children[0] = child;
            content = "*";
            compiled = true;
        } else if (content.charAt(content.length() - 1) == ')') {
            //It is not kleene! It has to be brackets?
            if (content.charAt(0) == '(') {
                content = content.substring(1, content.length() - 1);
                compile();
                compiled = true;
                return;
            } else {
                //Okay, seriously, no clue, what this is!
                LOGGER.severe("Unknown parsing of: " + content);
                compiled = true;
                return;
            }
        }
    }

    /**
     * Performs a deep copy of this object
     */
    public Node copy() {
        Node copy1 = null, copy2 = null;
        if (children[0] != null) {
            copy1 = children[0].copy();
            if (children[1] != null) {
                copy2 = children[1].copy();
            }
        }
        Node n = new Node();
        n.content = this.content;
        n.children = new Node[2];
        n.children[0] = copy1;
        n.children[1] = copy2;
        n.compiled = this.compiled;
        n.type = this.type;
        return n;
    }

    /**
     * Looks for Binary operators in top level in current regex. If it does not find it, it will return false.
     * If it does find it, it constructs both children and return true
     *
     * @throws UnsupportedOperationException
     */
    private boolean findTopLevel() throws UnsupportedOperationException {
        char ch;
        boolean foundTopLevel = false;
        int inBracket = 0;
        StringBuilder leftBuf = new StringBuilder(), rightBuf = new StringBuilder();
        boolean loadingLeftBuf = true, remade = false;
        int breakingPoint = -1;
        for (int i = 0; i < content.length(); i++) {
            ch = content.charAt(i);
            if (loadingLeftBuf) {
                if (inBracket > 0) {
                    if (ch == '(') {
                        inBracket++;
                    } else if (ch == ')') {
                        inBracket--;
                    }
                    leftBuf.append(ch);
                } else {
                    switch (ch) {
                        case '(':
                            inBracket++;
                            leftBuf.append(ch);
                            break;
                        case ')':
                            return unsupportedRegex(i);
                        case '·':
                            if (leftBuf.length() == 0 || i == content.length() - 1) return unsupportedRegex(i);

                            type = NodeType.DOT;
                            foundTopLevel = true;
                            loadingLeftBuf = false;
                            breakingPoint = i;
                            break;
                        case '+':
                            type = NodeType.OR;
                            foundTopLevel = true;
                            loadingLeftBuf = false;
                            remade = true;
                            break;
                        default:
                            leftBuf.append(ch);
                    }
                }
            } else if (!remade) {
                if (inBracket == 0) {
                    if (ch == '(') {
                        inBracket++;
                    } else if (ch == '+') {
                        remade = true;
                        leftBuf.append(rightBuf);
                        rightBuf = new StringBuilder();
                        type = NodeType.OR;
                        leftBuf.insert(breakingPoint, "·");
                        continue;
                    }
                } else if (ch == ')') {
                    inBracket--;
                }
                rightBuf.append(ch);
            } else {
                rightBuf.append(ch);
            }
        }

        if (foundTopLevel) {
            children[0] = Node.compile(leftBuf.toString());
            children[1] = Node.compile(rightBuf.toString());

            if (type.equals(NodeType.DOT)) content = "·";
            else if (type.equals(NodeType.OR)) content = "+";

            return true;
        } else {
            return false;
        }
    }

    /**
     * Throws exception, called when regex is invalid
     */
    private boolean unsupportedRegex(int i) throws UnsupportedOperationException {
        LOGGER.severe("Invalid regular expression! Unexpected token at: " + i + " in " + content);
        throw new UnsupportedOperationException("Invalid regular expression! Unexpected token at: " + i + " in " + content);
    }
}
