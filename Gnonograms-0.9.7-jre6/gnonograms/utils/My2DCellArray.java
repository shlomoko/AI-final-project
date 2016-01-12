/* 2D array of Cells class for gnonograms-java
 * Represents the state of a cell grid
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

import java.lang.Math;
import static java.lang.System.out;

import gnonograms.app.Resource;

 public class My2DCellArray
{
  private int rows;
  private int cols;
  private int maxRows;
  private int maxCols;
  private int[][] myData;

  public My2DCellArray(int rows, int cols){
    init(rows,cols,Resource.CELLSTATE_EMPTY);
  }
  public My2DCellArray(int rows, int cols, int state){
    init(rows,cols,state);
  }

  private void init(int rows, int cols, int state){
    this.rows=rows;
    this.cols=cols;
    this.maxRows=rows;
    this.maxCols=cols;
    myData = new int[this.rows][this.cols];
    setAll(state);
    Math.random();
  }
  
  public void resize(int r, int c){
    init(r,c,Resource.CELLSTATE_UNKNOWN);
  }

  public int getRows() {return rows;}
  private void setRows(int r) {
    assert (r>=0 && r<=this.maxRows);
    this.rows=r;
  }
  public int getCols() {return cols;}
  private void setCols(int c) {
    assert (c>=0 && c<=this.maxCols);
    this.cols=c;
  }
  public int getDataFromRC(int r, int c) {return myData[r][c];}
  public Cell getCell(int r, int c){
    return new Cell(r,c,myData[r][c]);
  }
  public int[] getRow(int row){
    int[] sa=new int[cols];
    for (int c=0;c<cols;c++) sa[c]=myData[row][c];
    return sa;
  }
  public int[] getColumn(int col){
    int[] sa = new int[rows];
    for (int r=0;r<rows;r++) sa[r]=myData[r][col];
    return sa;
  }
  public int[] getArray(int idx, boolean isColumn){
    if (isColumn) return getColumn(idx);
    else return getRow(idx);
  }
  
  public void setDataFromCell(Cell c) {myData[c.getRow()][c.getColumn()]=c.getState();}
  public void setDataFromRC(int r, int c, int s) {myData[r][c]=s;}
  public void setRow(int row, int[] sa){
    for (int c=0;c<sa.length;c++) myData[row][c]=sa[c];
  }
  public void setRow(int row, int[] sa, int start){
    if (start+sa.length>cols) throw new IllegalArgumentException("Exceeds column bound");
    for (int c=0;c<sa.length;c++) myData[row][c+start]=sa[c];
  }
  public void setColumn(int col, int[] sa)  {
    for (int r=0;r<sa.length;r++) myData[r][col]=sa[r];
  }
  public void setColumn(int col, int[] sa, int start) {
    for (int r=0;r<sa.length;r++) myData[r+start][col]=sa[r];
  }
  public void setArray(int idx, boolean isColumn, int[] sa){
    if (isColumn) setColumn(idx, sa);
    else setRow(idx, sa);
  }
  public void setArray(int idx, boolean isColumn, int[] sa, int start){
    if (isColumn) setColumn(idx, sa, start);
    else setRow(idx, sa, start);
  }
  
  public void setAll(int s){
    for (int r=0; r<rows; r++){
      for (int c=0;c<cols;c++){
        myData[r][c]=s;
      }
    }
  }
  
  public boolean setRowDataFromString(int r, String s){
    int[] cs =Utils.cellStateArrayFromString(s);
    return setRowDataFromArray(r, cs);
  }

  public boolean setRowDataFromArray(int r, int[] cs) {
    if (cs.length>this.cols) return false;
    this.setRow(r, cs);
    return true;
  }
  
  public String data2text(int idx, int length, boolean isColumn){
    int[] arr;
    arr=getArray(idx, isColumn);
    return Utils.clueFromIntArray(arr);
  }


  public void copyFrom(My2DCellArray ca)  {
    //only copies intersection
    int rows = Math.min(ca.getRows(), this.rows);
    int cols  = Math.min(ca.getCols(), this.cols);

    for (int r=0; r<rows; r++){
      for (int c=0; c<cols; c++){
        myData[r][c]=ca.getDataFromRC(r,c);
  } } }

  public int countDifferences(My2DCellArray ca) {
    int rows = Math.min(ca.getRows(), this.rows);
    int cols  = Math.min(ca.getCols(), this.cols);
    int count=0;
    for (int r=0; r<rows; r++){
      for (int c=0; c<cols; c++){
        if(myData[r][c]!=ca.getDataFromRC(r,c)) count++;
    } }
    return count;
  }
  
  public int countState(int cs){
    int count=0;
    for (int r=0; r<rows; r++){
      for (int c=0;c<cols;c++){
        if (myData[r][c]==cs) count++;
    } }
    return count;
  }
  
  public String toString(){
	  StringBuilder sb=new StringBuilder("");
	  for (int r=0; r<rows; r++){
		  sb.append(Utils.stringFromIntArray(myData[r]));
		  sb.append("\n");
		}
		return sb.toString();
	}
}
