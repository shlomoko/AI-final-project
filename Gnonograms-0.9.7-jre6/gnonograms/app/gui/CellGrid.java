/* CellGrid class for Gnonograms-java
 * Displays the puzzle pattern and responds to mouse and keyboard
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

package gnonograms.app.gui;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.BorderFactory;

import java.lang.Math;

import gnonograms.utils.Cell;
import gnonograms.app.Controller;
import gnonograms.app.Resource;


import static java.lang.System.out;

public class CellGrid extends JPanel{

  //private static final long serialVersionUID = 1;
  private int rows, cols, currentRow, currentCol;
  private double rowHeight, columnWidth;
  private Cell currentCell, previousCell;
  private Color[] solvingColors;
  private Color[] settingColors;
  private Color[] displayColors;

  public Controller control;

  public CellGrid(int rows, int cols, Controller control) {
  this.control=control;
  this.rows=rows;
  this.cols=cols;
  this.addMouseListener(new GridMouseAdapter());
  this.addMouseMotionListener(new GridMouseMotionAdapter());
  this.addKeyListener(new GridKeyAdapter());
  this.setBorder(BorderFactory.createLineBorder(Color.black));

  currentCell=new Cell(-1,-1,Resource.CELLSTATE_UNDEFINED);
  previousCell=new Cell(-1,-1,Resource.CELLSTATE_UNDEFINED);
  currentRow=-1;
  currentCol=-1;
  
  solvingColors=new Color[8];
  settingColors=new Color[8];
  
  for(int i=0;i<8;i++) {
	  solvingColors[i]=Color.orange;
	  settingColors[i]=Color.red;
  }
  solvingColors[Resource.CELLSTATE_FILLED]=Color.blue;
  solvingColors[Resource.CELLSTATE_EMPTY]=Color.yellow;
  solvingColors[Resource.CELLSTATE_UNKNOWN]=(new Color(240,240,240,255));

  settingColors[Resource.CELLSTATE_FILLED]=Color.black;
  settingColors[Resource.CELLSTATE_EMPTY]=Color.white;
  settingColors[Resource.CELLSTATE_UNKNOWN]=Color.red;
  displayColors=settingColors;
  }

@Override
  public void paintComponent(Graphics g) {
    Graphics myGraphics=g.create();
    int gridWidth=this.getWidth();
    int gridHeight=this.getHeight();
    rowHeight=((double)gridHeight)/((double)rows);
    columnWidth=(double)(gridWidth)/((double)cols);

    //Draw cell bodies
    for (int r=0;r<rows;r++){
     for(int c=0;c<cols;c++){
       myGraphics.setColor(displayColors[control.getDataFromRC(r,c)]);
       myGraphics.fillRect((int)(c*columnWidth+1),(int)(r*rowHeight+1),(int)(columnWidth),(int)(rowHeight));
     }
    }
    // Draw minor gridlines
    myGraphics.setColor(Color.gray);
    for (int r=0;r<rows;r++){
      int h=(int)(r*rowHeight);
      myGraphics.drawLine(0,h,gridWidth,h);
    }
    for (int c=0;c<cols;c++){
      int w=(int)(c*columnWidth);
      myGraphics.drawLine(w, 0, w, gridHeight);
    }
    // Draw major gridlines
    myGraphics.setColor(Color.black);
    for (int r=0;r<=rows;r+=5){
      int h=(int)(r*rowHeight);
      myGraphics.drawLine(0,h,gridWidth,h);
      myGraphics.drawLine(0,h+1,gridWidth,h+1);
    }
    for (int c=0;c<=cols;c+=5){
      int w=(int)(c*columnWidth);
      myGraphics.drawLine(w, 0, w, gridHeight);
      myGraphics.drawLine(w+1, 0, w+1, gridHeight);
    }
    //highlight current cell
    highlightCell(myGraphics, currentRow, currentCol,true);
    markCell(myGraphics,control.markedCell);
  }
  
  protected void moveHighlight(int r, int c){
    if (r==currentRow && c==currentCol) return;
    if (currentRow>=0 && currentCol>=0 && currentRow<rows && currentCol<cols){
    highlightCell(this.getGraphics(), currentRow,currentCol,false);
    control.highlightLabels(currentRow,currentCol,false);
    }
    currentRow=r; currentCol=c;
    if (r<0||r>rows||c<0||c>=cols) return;
    highlightCell(this.getGraphics(),r,c,true);
    control.highlightLabels(r,c,true);
  }
  protected void highlightCell(Graphics g, int r, int c, boolean on){
    if (r<0||r>rows||c<0||c>=cols) return;
    if (on) g.setColor(Resource.HIGHLIGHT_COLOR);
    else g.setColor(displayColors[control.getDataFromRC(r,c)]);
    drawHighlight(g,r,c);
   }
  
  protected void markCell(Graphics g,Cell c){
    if (c.row<0) return;
    g.setColor(Resource.MARKED_CELL_COLOR);
    drawMark(g,c.row,c.col);
  }
    
  protected void drawMark(Graphics g, int r, int c){

    int x=(int)(c*columnWidth+columnWidth/4);
    int y=(int)(r*rowHeight+rowHeight/4);
    int w=(int)(columnWidth/2);
    int h=(int)(rowHeight/2);
    g.fillRect(x,y,w,h);
  }
  
  protected void drawHighlight(Graphics g,int r, int c){
    int x=(int)(c*columnWidth+2.5);
    int y=(int)(r*rowHeight+2.5);
    int w=(int)(columnWidth-4.5);
    int h=(int)(rowHeight-4.5);
    
    g.drawRect(x,y,w,h);
    g.drawRect(x+1,y+1,w-2,h-2);
  }

  public void updateCurrentCell(int state){
    updateCell(currentRow,currentCol,state);
  }
  
  protected void updateCell(int r,int c,int cs){
    if (cs==Resource.CELLSTATE_UNDEFINED) return;
    currentCell.set(r,c,cs);
    control.setDataFromCell(currentCell);//takes care of updating labels and checking if solved if necessary
  }

  public void setSolving(boolean isSolving){
    if(isSolving) displayColors=solvingColors;
    else displayColors=settingColors;
  }

  private class GridMouseAdapter extends MouseAdapter{

    public void mousePressed(MouseEvent e) {
     int r= (int)((double)(e.getY())/rowHeight);
     int c= (int)((double)(e.getX())/columnWidth);
     int x= (int)(c*columnWidth)+1;
     int y= (int)(r*rowHeight)+1;
     int b= e.getButton();
     int cs=Resource.CELLSTATE_UNDEFINED;

     if (e.getClickCount()>1) b=MouseEvent.BUTTON2;

     switch (b){
      case MouseEvent.BUTTON1:
      cs=Resource.CELLSTATE_FILLED;
      break;
       case MouseEvent.BUTTON2:
      if(control.isSolving) cs=Resource.CELLSTATE_UNKNOWN;
      break;
       case MouseEvent.BUTTON3:
      cs=Resource.CELLSTATE_EMPTY;
      break;
       default :
      break;
     }
      updateCell(r,c,cs);
    }
    public void mouseExited(MouseEvent e) {
      moveHighlight(-1,-1);
      currentRow=-1;currentCol=-1;
      currentCell.clear();
      previousCell.clear();
    }
    public void mouseEntered(MouseEvent e) {
     requestFocus();
    }
  }

  private class GridMouseMotionAdapter extends MouseMotionAdapter{

    public void mouseDragged(MouseEvent e) {
      int r= (int)((double)(e.getY())/rowHeight);
      int c= (int)((double)(e.getX())/columnWidth);
      if (r== currentRow && c== currentCol) return;
      if (r<0||r>rows||c<0||c>=cols) return;
      updateCell(r,c,currentCell.getState());
      moveHighlight(r,c);
    }
    public void mouseMoved(MouseEvent e){
      int r= (int)((double)(e.getY())/rowHeight);
      int c= (int)((double)(e.getX())/columnWidth);
      if(r==currentRow&&c==currentCol) return;
      moveHighlight(r,c);
    }
  }

  private class GridKeyAdapter extends KeyAdapter{
    public void keyPressed(KeyEvent e){
      int keyCode =e.getKeyCode();
      boolean controlDown=e.isControlDown();
      switch (keyCode){
      case KeyEvent.VK_MINUS:
          control.zoomFont(-1);
          break;
      case KeyEvent.VK_PLUS:
      case KeyEvent.VK_EQUALS:
          control.zoomFont(1);
          break;
      case KeyEvent.VK_KP_LEFT:
      case KeyEvent.VK_LEFT:
          if (currentCol>0) {
          if (controlDown&&currentRow==currentCell.row&&currentCol==currentCell.col){
            updateCell(currentRow,currentCol-1,currentCell.getState());
          }
          moveHighlight(currentRow,currentCol-1);
          }
          break;
      case KeyEvent.VK_KP_RIGHT:
      case KeyEvent.VK_RIGHT:
          if (currentCol<cols-1) {
          if (controlDown&&currentRow==currentCell.row&&currentCol==currentCell.col){
            updateCell(currentRow,currentCol+1,currentCell.getState());
          }
          moveHighlight(currentRow,currentCol+1);
          }
          break;
      case KeyEvent.VK_KP_UP:
      case KeyEvent.VK_UP:
          if (currentRow>0){
          if (controlDown&&currentRow==currentCell.row&&currentCol==currentCell.col){
            updateCell(currentRow-1,currentCol,currentCell.getState());
          }
           moveHighlight(currentRow-1,currentCol);
          }
          break;
      case KeyEvent.VK_KP_DOWN:
      case KeyEvent.VK_DOWN:
          if (currentRow<rows-1) {
          if (controlDown&&currentRow==currentCell.row&&currentCol==currentCell.col){
            updateCell(currentRow+1,currentCol,currentCell.getState());
          }
          moveHighlight(currentRow+1,currentCol);
          }
          break;
      case Resource.KEY_FILLED:
          updateCell(currentRow,currentCol,Resource.CELLSTATE_FILLED);
          break;
      case Resource.KEY_EMPTY:
          updateCell(currentRow,currentCol,Resource.CELLSTATE_EMPTY);
          break;
      case Resource.KEY_UNKNOWN:
          if(control.isSolving)updateCell(currentRow,currentCol,Resource.CELLSTATE_UNKNOWN);
          break;
      case KeyEvent.VK_S:
          if (e.isControlDown()) control.saveGame();
          break;
      case KeyEvent.VK_O:
          if (e.isControlDown()) control.loadGame();
          break;
      case KeyEvent.VK_N:
          if (e.isControlDown()) control.createGame();
          break;
      case KeyEvent.VK_R:
          if (e.isControlDown()) control.randomGame();
          break;
      case KeyEvent.VK_U:
          if (e.isControlDown()) control.undoMove();
          break;
      case KeyEvent.VK_Y:
          if (e.isControlDown()) control.redoMove();
          break;
      case KeyEvent.VK_Q:
          if (e.isControlDown()) {control.quit(); System.exit(0);}
          break;
      case KeyEvent.VK_H:
          if (e.isControlDown()) {control.hint();}
          break;
      case KeyEvent.VK_M:
          if (e.isControlDown()) {control.markCell(currentRow, currentCol);}
          break;
      case KeyEvent.VK_L:
          if (e.isControlDown()) {
            control.rewindToMarkedCell();
          }
          break;
      default:
        break;
      }
    }
  }
}
