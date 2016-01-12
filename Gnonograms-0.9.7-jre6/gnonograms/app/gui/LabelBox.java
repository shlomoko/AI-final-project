/* LabelBox class for gnonograms-java
 * Holds and manages clue labels
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

package gnonograms.app.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;

import static java.lang.System.out;

public class LabelBox extends JPanel{
  //private static final long serialVersionUID = 1;
  private int maxClueLength=3;
  private GnonogramLabel[] labels;
  private int no_labels;
  private int logoSize=48;
  private int clueWidthMargin=4;
  private int clueLengthMargin=12;
  private int fontHeight, fontWidth;
  private boolean isColumn;
  private boolean sizeOutOfDate=true;
  private String freedomString;

  public LabelBox(int no_labels, boolean isColumn, String freedomString){
    if (isColumn)this.setLayout(new GridLayout(1,no_labels));
    else this.setLayout(new GridLayout(no_labels,1));
    this.no_labels=no_labels;
    this.isColumn=isColumn;
    this.freedomString=freedomString;
    this.setBorder(BorderFactory.createLineBorder(Color.black));

    labels=new GnonogramLabel[no_labels];
    for (int i=0; i<no_labels; i++) {
      GnonogramLabel l=new GnonogramLabel("0",isColumn);
      if (isColumn){
        l.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
      }
      else{
        l.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l.setAlignmentY(Component.CENTER_ALIGNMENT);
      }
      this.add(l);
      labels[i]=l;
      
  } }

  public void setFontAndSize(Font f, int fontWidth, int fontHeight){
    setLabelFont(f);
    this.fontWidth=fontWidth;
    this.fontHeight=fontHeight;
    //out.println("FontWidth: "+fontWidth+" Font height: "+fontHeight);
    sizeOutOfDate=true;
  }
  
  public boolean setLabelSize(){
    int labelWidth, labelHeight;
    //Trial and error functions giving reasonable appearance.
    if (!sizeOutOfDate) return false;
    if (isColumn){
      labelWidth=this.fontWidth*2+clueWidthMargin;
      labelHeight=(maxClueLength*fontHeight)/2+clueLengthMargin; //maxClueLength inludes commas
      logoSize=labelHeight;
    }
    else{
      labelWidth=maxClueLength*fontWidth;
      labelHeight=fontWidth*2+clueWidthMargin;
      logoSize=labelWidth;
    }
    Dimension d = new Dimension(labelWidth,labelHeight);
    for (GnonogramLabel l :labels){
      l.setPreferredSize(d);
    }
    sizeOutOfDate=false;
		//out.println((isColumn ? "ColumnBox" : "RowBox")+" Label Width: "+labelWidth+" Label Height: "+labelHeight);
		return true;
  }
  
  public void setMargins(int cwm, int clm){clueWidthMargin=cwm; clueLengthMargin=clm;}
  
  private void setLabelFont(Font f){
    for (GnonogramLabel l :labels){
      l.setFont(f);
  } }
  
  public void highlightLabel(int label, boolean on){
    if (label>=0 && label<no_labels)(labels[label]).highlightLabel(on);
  }

  public void setClueText(int l, String text){
    if (l>=no_labels || l<0) return;
    if (text==null) text="?";
    if(text.length()>maxClueLength){
      resetMaximumClueLength(text.length());
      //out.println("New longest clue: "+text);
		}
		labels[l].setText(text);
  }
  

  public void resetMaximumClueLength(int maxLength){
	  maxClueLength=maxLength;
	  //out.println((isColumn ? "ColumnBox" : "RowBox")+"Max clue length: "+maxLength);
	  sizeOutOfDate=true;
	}
  
  public void setLabelToolTip(int l, int freedom){
    labels[l].setToolTipText(freedomString+" "+freedom);
  }

  public String getClueText(int l){
    if (l>=no_labels || l<0) return "";
    else return labels[l].getOriginalText();
  }
  
  public int getLogoSize(){return logoSize;}

  public String getClues(){
    StringBuilder sb=new StringBuilder("");
    for (GnonogramLabel l : labels){
      sb.append(l.getOriginalText());
      sb.append("\n");
    }
    return sb.toString();
  }
  
}
