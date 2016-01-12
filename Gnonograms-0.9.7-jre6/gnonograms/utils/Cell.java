/* Cell class for Gnonograms-java
 * Holds position and state of one 'pixel' of a puzzle pattern
 * Copyright (C) 2012  Jeremy Wootten
 *
  This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Author:
 *  Jeremy Wootten <jeremwootten@gmail.com>
 */
package gnonograms.utils;

import gnonograms.app.Resource;

public class Cell{
  public int row=-1;
  public int col=-1;
  public int state=Resource.CELLSTATE_UNDEFINED;

  public Cell(int row, int col, int state){
    this.row=row;
    this.col=col;
    this.state=state;
  }

  public boolean same_coords(Cell c){
    return (this.row==c.row && this.col==c.col);
  }

  public void clear(){
    this.row=-1;
    this.col=-1;
    this.state=Resource.CELLSTATE_UNDEFINED;
  }

  public void set(int r, int c, int state){
    this.row=r;
    this.col=c;
    this.state=state;
  }

  public int getRow(){return this.row;}
  public int getColumn(){return this.col;}
  public int getState(){return this.state;}

  public void copy(Cell b){
    this.row=b.row;
    this.col=b.col;
    this.state=b.state;
  }

  public Cell invert(){
    int newstate;
    if(this.state==Resource.CELLSTATE_EMPTY) newstate=Resource.CELLSTATE_FILLED;
    else newstate=Resource.CELLSTATE_EMPTY;
    return new Cell(this.row,this.col,newstate);
  }

}
