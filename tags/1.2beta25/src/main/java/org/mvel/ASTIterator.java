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

public interface ASTIterator {
    public void reset();

    public ASTNode nextToken();

    public void skipToken();

    public ASTNode peekNext();

    public ASTNode peekToken();

    public ASTNode peekLast();

    //  public boolean peekNextTokenFlags(int flags);
    public void back();

    public ASTNode tokensBack(int offset);

    public boolean hasMoreTokens();

    public String showTokenChain();

    public ASTNode firstToken();

    public int size();

    public int index();

    public void finish();

}
