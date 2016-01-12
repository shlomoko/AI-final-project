/* GnonogramLabel class for Gnonograms-java
 * Displays a clue in correct orientation
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

import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.LineBorder;
import java.awt.Color;

import gnonograms.app.Resource;

public class GnonogramLabel extends JLabel{
  //private static final long serialVersionUID = 1;
  private boolean isColumn;
  private String text;
  private LineBorder labelBorder;

  public GnonogramLabel(String text, boolean isColumn){
    this.isColumn=isColumn;
    this.text=text;

    if (isColumn) {
      this.setHorizontalAlignment(CENTER);
      this.setVerticalAlignment(BOTTOM);
    }else{
      this.setHorizontalAlignment(RIGHT);
      this.setVerticalAlignment(CENTER);
    }
    labelBorder=(LineBorder)BorderFactory.createLineBorder(Resource.HIGHLIGHT_COLOR,2);
    setText(text);
  }
  
  public void highlightLabel(boolean on){
    if (on)this.setBorder(labelBorder);
    else this.setBorder(null);
    
  }

  public String getOriginalText(){ return text;}

  public void setText(String text){
    this.text=text;
    if (isColumn) super.setText(verticalString(text));
    else super.setText(horizontalString(text));
  }
  
  private String verticalString (String s){
    String[] sa=s.split(Resource.BLOCKSEPARATOR);
    StringBuilder sb=new StringBuilder("<html><P align=right><b>");
    for (String ss : sa){
      sb.append(ss); sb.append("<br>");
    }
    sb.append("</b></P></html>");
    return sb.toString();
  }
  private String horizontalString (String s){
    return "<html><b>"+s+"</b></html>";
  }
}
