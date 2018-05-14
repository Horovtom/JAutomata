package cz.cvut.fel.horovtom.automata.logic.converters;

import cz.cvut.fel.horovtom.automata.logic.Automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * This class holds the tree for Regular expression {@link RegexTree}
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
    private ArrayList<HashSet<Integer>> followers = new ArrayList<>();

    /**
     * This holds the information for firstPos, lastPos traversal, that this node is nullable (can be left out)
     */
    private boolean nullable = false;
    private ArrayList<Integer> firstPos = new ArrayList<>(), lastPos = new ArrayList<>();
    private int letterIndex = -1;
    private char[] letterIndices;

    /**
     * Private constructor, needed for {@link #copy()}
     */
    private Node() {
    }

    char[] getLetterIndices() {
        return Arrays.copyOf(letterIndices, letterIndices.length);
    }

    public int[] getStartingIndices() {
        return firstPos.stream().mapToInt(a -> a).toArray();
    }

    public int[] getEndingIndices() {
        return lastPos.stream().mapToInt(a -> a).toArray();
    }

    public boolean isNullable() {
        return nullable;
    }

    public ArrayList<int[]> getFollowers() {
        ArrayList<int[]> ret = new ArrayList<>();
        for (HashSet<Integer> follower : followers) {
            ret.add(follower.stream().mapToInt(a -> a).toArray());
        }
        return ret;
    }

    private static Node innerCompile(String regex, char[] letterIndices, ArrayList<HashSet<Integer>> followers) {
        Node n = new Node();
        n.type = NodeType.LETTER;
        n.content = regex;
        n.letterIndices = letterIndices;
        n.followers = followers;
        n.compile();
        return n;
    }

    /**
     * This method is used as a constructor. It will automatically initialize whole tree.
     */
    public static Node compile(String regex) {
        Node n = new Node();
        n.type = NodeType.LETTER;
        n.content = regex;
        n.calculateLetterIndices();
        n.initializeFollowers();
        n.compile();
        int res = n.calculateLetterIndex(0);
        if (res != n.letterIndices.length && n.letterIndices.length != 0) {
            LOGGER.warning("Number of parsed letters: " + n.letterIndices.length + " was not the same as number of indexed letters: " + res + "!");
        }
        n.calculateFirstLast();
        n.calculateFollowers();

        return n;
    }

    /**
     * Initializes Followers list to have the right amount of empty HashSets in order to be ready for filling
     * This method should be called only if we are compiling new root from the outside world. It should be called only
     * from {@link Node#compile(String)}
     */
    private void initializeFollowers() {
        for (char ignored : letterIndices) {
            followers.add(new HashSet<>());
        }
    }

    /**
     * Calculates and fills whole follower array
     */
    private void calculateFollowers() {
        if (children[0] != null) children[0].calculateFollowers();
        if (children[1] != null) children[1].calculateFollowers();

        if (type == NodeType.KLEENE) {
            for (Integer last : lastPos) {
                for (Integer first : firstPos) {
                    followers.get(last).add(first);
                }
            }
        } else if (type == NodeType.DOT) {
            //This is just for safety. It should never happen, because the tree should be already compiled in the right way
            if (children[0] != null && children[1] != null) {
                for (Integer last : children[0].getLastPos()) {
                    for (Integer first : children[1].getFirstPos()) {
                        followers.get(last).add(first);
                    }
                }
            }
        }
    }

    /**
     * Fills array {@link #letterIndices}
     */
    private void calculateLetterIndices() {
        HashSet<Character> letters = new HashSet<>();
        letters.add('+');
        letters.add('*');
        letters.add('(');
        letters.add(')');
        letters.add('·');
        letters.add('ε');
        ArrayList<Character> aList = new ArrayList<>();
        for (int i = 0; i < content.length(); i++) {
            if (!letters.contains(content.charAt(i))) {
                aList.add(content.charAt(i));
            }
        }
        letterIndices = new char[aList.size()];
        for (int i = 0; i < letterIndices.length; i++) {
            letterIndices[i] = aList.get(i);
        }
    }

    private ArrayList<Integer> getFirstPos() {
        return firstPos;
    }

    private ArrayList<Integer> getLastPos() {
        return lastPos;
    }

    private void makeToEmptyNode() {
        content = "ε";
        nullable = true;
        type = NodeType.LETTER;
        calculateFirstLast();
    }

    /**
     * This recursive function will calculate firstPos and lastPos arrayLists.
     * https://www.youtube.com/watch?v=uPnpkWwO9hE
     */
    private void calculateFirstLast() {
        if (children[0] != null) children[0].calculateFirstLast();
        if (children[1] != null) children[1].calculateFirstLast();
        if (type == NodeType.LETTER) {
            if (!Automaton.isEpsilonName(content)) {
                firstPos.add(letterIndex);
                lastPos.add(letterIndex);
            }
        } else if (type == NodeType.KLEENE) {
            if (children[1] != null) {
                if (children[0] == null) {
                    children[0] = children[1];
                } else {
                    children[1] = null;
                }
            } else if (children[0] == null) {
                LOGGER.warning("Found a node that is empty, fixing!");
                makeToEmptyNode();
                return;
            }
            firstPos.addAll(children[0].getFirstPos());
            lastPos.addAll(children[0].getLastPos());
        } else {
            if (children[0] == null && children[1] == null) {
                LOGGER.warning("Found a node that is empty, fixing!");
                makeToEmptyNode();
                return;
            } else if (children[1] == null || children[0] == null) {
                Node child = children[0] == null ? children[1] : children[0];
                assert (child != null);
                LOGGER.warning("Found a node that is unnecessary, fixing!");
                nullable = child.nullable;
                type = child.type;
                content = child.content;
                compiled = child.compiled;
                firstPos = child.firstPos;
                lastPos = child.lastPos;
                letterIndex = child.letterIndex;
                children = child.children;
                calculateFirstLast();
                return;
            }
            if (type == NodeType.DOT) {
                firstPos.addAll(children[0].firstPos);
                if (children[0].nullable) {
                    firstPos.addAll(children[1].firstPos);
                }

                lastPos.addAll(children[1].lastPos);
                if (children[1].nullable) {
                    lastPos.addAll(children[0].lastPos);
                }
            } else if (type == NodeType.OR) {
                firstPos.addAll(children[0].firstPos);
                firstPos.addAll(children[1].firstPos);
                lastPos.addAll(children[0].lastPos);
                lastPos.addAll(children[1].lastPos);
            }
        }
    }

    /**
     * This function will DFS the tree and mark all leaf nodes with its indices. Marking will start from startIndex and
     * it will return the next free index.
     * It will also set nullable flag on all nodes in the tree
     */
    private int calculateLetterIndex(int startIndex) {
        if (this.type == NodeType.LETTER) {
            if (children[0] != null || children[1] != null) {
                LOGGER.warning("There was an error in typing of node in syntax tree! It was marked as letter, but it had children!");
                LOGGER.warning("Source: " + this);
            }

            if (Automaton.isEpsilonName(content)) {
                nullable = true;
                return startIndex;
            } else {
                letterIndex = startIndex;
                return startIndex + 1;
            }
        } else if (this.type == NodeType.KLEENE) {
            if (children[0] == null) {
                LOGGER.warning("There was an error in typing of node in syntax tree! It was marked as kleene, but it had no first child!");
                LOGGER.warning("Source: " + this);
                if (children[1] == null) {
                    LOGGER.warning("This kleene node does not have any children whatsoever! It should not even be there!");
                    return startIndex;
                } else {
                    LOGGER.warning("This kleene node had it's children switched... Fixing!");
                    children[0] = children[1];
                    children[1] = null;
                }
            }
            nullable = true;
            return children[0].calculateLetterIndex(startIndex);
        }
        //It is a plus, or dot
        if (children[0] == null) {
            LOGGER.warning("There was an error in constructed syntax tree! Node: " + this + " is marked as " + type.toString() + " but it has no right child! ");
            children[0] = children[1];
            children[1] = null;
            int ret = children[0].calculateLetterIndex(startIndex);
            if (type == NodeType.OR && children[0].nullable) nullable = true;
            return ret;
        } else if (children[1] == null) {
            LOGGER.warning("There was an error in constructed syntax tree! Node: " + this + " is marked as " + type.toString() + " but it has no left child!");
            int ret = children[0].calculateLetterIndex(startIndex);
            if (type == NodeType.OR && children[0].nullable) nullable = true;
            return ret;
        } else {
            int ret = children[1].calculateLetterIndex(children[0].calculateLetterIndex(startIndex));
            if (type == NodeType.OR && (children[0].nullable || children[1].nullable)) nullable = true;
            else if (type == NodeType.DOT && children[0].nullable && children[1].nullable) nullable = true;
            return ret;
        }
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

    /**
     * This function will simplify the tree under this node.
     * <p>
     * NOTE: OPTIONAL TO IMPLEMENT
     */
    public void simplify() {
        //TODO: IMPLEMENT
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
     */
    private void compile() throws UnsupportedOperationException {
        if (!type.equals(NodeType.LETTER)) return;
        if (compiled) return;
        if (content.length() == 1) {
            compiled = true;
            type = NodeType.LETTER;
            return;
        } else if (content.length() == 0) {
            compiled = true;
            content = "ε";
            nullable = true;
            type = NodeType.LETTER;
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
                child = Node.innerCompile(content.substring(1, content.length() - 2), letterIndices, followers);
            } else {
                child = Node.innerCompile(content.substring(0, content.length() - 1), letterIndices, followers);
            }

            nullable = true;
            children[0] = child;
            content = "*";
            compiled = true;
        } else if (content.charAt(content.length() - 1) == ')') {
            //It is not kleene! It has to be brackets?
            if (content.charAt(0) == '(') {
                content = content.substring(1, content.length() - 1);
                compile();
                compiled = true;
            } else {
                //Okay, seriously, no clue, what this is!
                LOGGER.severe("Unknown parsing of: " + content);
                compiled = true;
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
        ArrayList<HashSet<Integer>> followers = new ArrayList<>();
        for (HashSet<Integer> follower : this.followers) {
            HashSet<Integer> f = new HashSet<>(follower);
            followers.add(f);
        }
        n.followers = followers;
        n.letterIndices = Arrays.copyOf(this.letterIndices, this.letterIndices.length);
        n.lastPos = new ArrayList<>();
        n.lastPos.addAll(lastPos);
        n.firstPos = new ArrayList<>();
        n.firstPos.addAll(firstPos);
        n.nullable = nullable;
        return n;
    }

    /**
     * Looks for Binary operators in top level in current regex. If it does not find it, it will return false.
     * If it does find it, it constructs both children and return true
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
                } else if (ch == '(') {
                    inBracket++;
                } else if (ch == ')') {
                    inBracket--;
                }
                rightBuf.append(ch);
            } else {
                rightBuf.append(ch);
            }
        }

        if (foundTopLevel) {
            children[0] = Node.innerCompile(leftBuf.toString(), letterIndices, followers);
            children[1] = Node.innerCompile(rightBuf.toString(), letterIndices, followers);

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
