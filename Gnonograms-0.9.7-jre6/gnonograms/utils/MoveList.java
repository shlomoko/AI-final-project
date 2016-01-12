/* MoveList class for gnonograms-java
 * Manage Move history during manual solving, allowing undo and redo
 * Copyright 2012 Jeremy Paul Wootten <jeremywootten@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 *
 */
 
package gnonograms.utils;

import java.util.LinkedList;
import static java.lang.System.out;

public class MoveList extends LinkedList<Move> {
  int numberOfMoves;
  int currentMove;
  //private static final long serialVersionUID = 1; 
  public MoveList(){
    this.initialize();
  }
  
  public void initialize(){
    this.clear();
    this.numberOfMoves=0;
    this.currentMove=-1;
  }
  
  public void recordMove(Cell newCell, int previousState){
    currentMove++;
    this.add(currentMove,new Move(newCell,previousState));
    numberOfMoves=currentMove+1;
  }
  
  public Move getLastMove(){
    if(currentMove>=0)return this.get(currentMove--);
    else return null;
  }
  
  public Move getNextMove(){
    if(currentMove<numberOfMoves-1) return this.get(++currentMove);
    else return null;
  }
}
