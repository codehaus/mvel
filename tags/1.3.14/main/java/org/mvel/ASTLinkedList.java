/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the Codehaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mvel;

public class ASTLinkedList implements ASTIterator {
    private ASTNode firstASTNode;
    private ASTNode current;
    private int size;

    public ASTLinkedList() {
    }

    public ASTLinkedList(ASTNode firstASTNode) {
        this.current = this.firstASTNode = firstASTNode;
    }

    public ASTLinkedList(ASTNode firstASTNode, int size) {
        this.current = this.firstASTNode = firstASTNode;
        this.size = size;
    }

    public void addTokenNode(ASTNode astNode) {
        size++;

        if (this.firstASTNode == null) {
            this.firstASTNode = this.current = astNode;
        }
        else {
            this.current = (this.current.nextASTNode = astNode);
        }
    }

    public void addTokenNode(ASTNode astNode, ASTNode token2) {
        if (this.firstASTNode == null) {
            this.current = ((this.firstASTNode = astNode).nextASTNode = token2);
        }
        else {
            this.current = (this.current.nextASTNode = astNode).nextASTNode = token2;
        }
    }


    public ASTNode firstNode() {
        return firstASTNode;
    }

    public void reset() {
        this.current = firstASTNode;
    }

    public boolean hasMoreNodes() {
        return this.current != null;
    }

    public ASTNode nextNode() {
        if (current == null) return null;
        try {
            return current;
        }
        finally {
            current = current.nextASTNode;
        }
    }

    public void skipNode() {
        if (current != null)
            current = current.nextASTNode;
    }

    public ASTNode peekNext() {
        if (current != null && current.nextASTNode != null)
            return current.nextASTNode;
        else
            return null;
    }

    public ASTNode peekNode() {
        if (current == null) return null;
        return current.nextASTNode;
    }

    public void removeToken() {
        if (current != null) {
            current = current.nextASTNode;
        }
    }

    public ASTNode peekLast() {
        throw new RuntimeException("unimplemented");
    }

    public ASTNode nodesBack(int offset) {
        throw new RuntimeException("unimplemented");
    }

    public void back() {
        throw new RuntimeException("unimplemented");
    }

    public String showNodeChain() {
        throw new RuntimeException("unimplemented");
    }

    public int size() {
        return size;
    }

    public int index() {
        return -1;
    }

    public void setCurrentNode(ASTNode node) {
        this.current = node;
    }

    public void finish() {
        reset();

        ASTNode last = null;
        ASTNode curr;

        while (hasMoreNodes()) {
            curr = nextNode();

            if (curr.isDiscard()) {
                if (last == null) {
                    firstASTNode = nextNode();
                }
                else {
                    last.nextASTNode = nextNode();
                }
                continue;
            }

            if (!hasMoreNodes()) break;

            last = curr;
        }

        reset();
    }

}
