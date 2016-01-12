/* GameLoader class for gnonograms-java
 * Loads puzzles from file.
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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Scanner;
import java.util.NoSuchElementException;

import static java.lang.System.out;

import gnonograms.app.Resource;

public class GameLoader extends JFileChooser {
  //private static final long serialVersionUID = 1;
  public int rows=0;
  public int cols=0;
  public String[] rowClues;
  public String[] colClues;
  public String state="";
  public String name="";
  public String author="";
  public String date="";
  public String score="";
  public String license="";
  public boolean validGame=false;
  public boolean hasDimensions=false;
  public boolean hasRowClues=false;
  public boolean hasColumnClues=false;
  public boolean hasSolution=false;
  public boolean hasWorking=false;
  public boolean hasState=false;
  public String[] solution;
  public String[] working;
  private String[] headings;
  private String[] bodies;
  private Scanner dataStream;
  private int result, headingCount;

  public GameLoader(Component parent, String puzzleDirectoryPath) {
    super(puzzleDirectoryPath);
    this.setFileSelectionMode(FILES_ONLY);
    this.setFileFilter(new FileNameExtensionFilter("Gnonogram Puzzles","gno"));
    this.setDialogTitle("Choose a puzzle");
    result=this.showOpenDialog(parent);
  }

  public int getResult(){return this.result;}
  public String getFileName(){return getName(getSelectedFile());}

  public void openDataInputStream() throws FileNotFoundException{
    dataStream= new Scanner(new FileReader(getSelectedFile()));
  }

  public void close(){dataStream.close();}

  public void parseGameFile() throws IOException, NoSuchElementException, Exception {
    state="SOLVING";//by default, the game enters solving mode after loading puzzle
    headingCount=-1;
    String nextToken="";
    headings=new String[10];
    bodies=new String[10];
    dataStream.useDelimiter("\\]");
    while (nextToken!=null){
      try {nextToken=dataStream.next();}
      catch (Exception e){break;}
      if(nextToken.startsWith("[")){
        headingCount++;
        headings[headingCount]=nextToken.substring(1);
        dataStream.useDelimiter("\\[");
      } else {
        bodies[headingCount]=nextToken.substring(2);
        dataStream.useDelimiter("\\]");
      }
    }
    parseGnonogramHeadingsAndBodies();
    validGame=(hasDimensions && ((hasColumnClues && hasRowClues) || hasSolution));
  }

  private boolean parseGnonogramHeadingsAndBodies() throws Exception {
    int headingID;
    for (int i=0;i<=headingCount;i++){
      headingID=headingToInt(headings[i]);
      switch (headingID)  {
        case 1:
          getGnonogramDimensions(bodies[i]); break;
        case 2 :
          getGnonogramClues(bodies[i],false); break;
        case 3 :
          getGnonogramClues(bodies[i],true); break;
        case 4 :
          getGnonogramCellstateArray(bodies[i],true); break;
        case 5:
          getGnonogramCellstateArray(bodies[i],false); break;
        case 6:
          getGnonogramState(bodies[i]); break;
        case 7:
          getGameDescription(bodies[i]); break;
        case 8:
          get_game_license(bodies[i]); break;
        default :
          out.println("Unrecognised heading");
          break;
      }
    }
    return false;
  }

  //required older Java version
  private int headingToInt(String heading){
      if (heading==null||heading.length()<3) return -1;
      if (heading.length()>3) heading=heading.substring(0,3).toUpperCase();
      if (heading.compareTo("DIM")==0) return 1;
      if (heading.compareTo("ROW")==0) return 2;
      if (heading.compareTo("COL")==0) return 3;
      if (heading.compareTo("SOL")==0) return 4;
      if (heading.compareTo("WOR")==0) return 5;
      if (heading.compareTo("STA")==0) return 6;
      if (heading.compareTo("DES")==0) return 7;
      if (heading.compareTo("LIC")==0) return 8;
      return 0;
  }

  private void getGnonogramDimensions(String body) throws NumberFormatException, Exception{
    String[] s=splitString(body,"\n",2,3);
    rows=new Integer(s[0]);
    cols=new Integer(s[1]);
    if (rows<1 || cols<1 || rows>Resource.MAXIMUM_GRID_SIZE || cols> Resource.MAXIMUM_GRID_SIZE) {
      throw new Exception("Dimensions out of range:"+rows+","+cols);
    }
    else hasDimensions=true;
  }

  private void getGnonogramClues(String body, boolean isColumn) throws Exception{
    String[] s=splitString(body,"\n",isColumn?cols:rows,isColumn?cols:rows);
    String[] arr=new String[s.length];
    for (int i=0; i< s.length; i++){
      arr[i]=parseGnonogramClue(s[i],isColumn);
    }
    if (isColumn){
        colClues=arr;
        hasColumnClues=true;
    }
    else{
        rowClues=arr;
        hasRowClues=true;
    }
  }
  private String parseGnonogramClue(String line, boolean isColumn) throws NumberFormatException, Exception {
    String[] sa=splitString(line,"[\\D\\n]",1,isColumn?rows:cols); //split on non-digit or EOL
    int b, zero_count=0;
    int maxblock=isColumn?rows:cols;
    StringBuilder sb=new StringBuilder(200);
    for (int i=0; i<sa.length; i++){
      // ignore extraneous non-digits (allow one zero)
      try{b=new Integer(sa[i]);}
      catch (NumberFormatException e){continue;}
      if (b<0||b>maxblock) throw new Exception("Invalid block size in clue");
      if (b==0 && zero_count>0) continue;
      else zero_count++;
      if(i>0)sb.append(Resource.BLOCKSEPARATOR);
      sb.append(sa[i]);
    }
    return sb.toString();
  }

  private void getGnonogramCellstateArray(String body, boolean is_solution) throws Exception{
    String[] s=splitString(body,"\n",rows,110);
    if (s.length!=rows) throw new Exception("Wrong number of rows in solution or working grid");
    for (int i=0; i<s.length;i++){
      int[] arr = Utils.cellStateArrayFromString(s[i]);
      if (arr.length!=cols) throw new Exception("Too few columns in grid");
      if (is_solution){
        for (int c=0;c<cols;c++){
          if(arr[c]!=Resource.CELLSTATE_EMPTY && arr[c]!=Resource.CELLSTATE_FILLED) throw new Exception("Invalid cell state"+arr[c]);
    } } }
    if (is_solution){ solution=s; hasSolution=true;}
    else{working=s;hasWorking=true;}
  }

  private void getGnonogramState(String body)throws Exception{
    String[] s = splitString(body,"\n",1,3);
    state=s[0];
    if (state.contains("SETTING")) state="SETTING";
    else state="SOLVING";
  }

  private void getGameDescription(String body) throws Exception{
    String[] s = splitString(body,"\n",1,10);
    if (s.length>=1) this.name=convertHtml(s[0]);
    if (s.length>=2) this.author=convertHtml(s[1]);
    if (s.length>=3) this.date=s[2];
    if (s.length>=4) this.score=s[3];
  }
  
  //This is needed to cope with some characters
  private String convertHtml(String s){return s;} //TODO complete stub

  private void get_game_license(String body) throws Exception{
    String[] s = splitString(body,"\n",1,2);
      if (s[0].length()>50)license=s[0].substring(0,50);
      else license=s[0];
  }

  private String[] splitString(String body, String delimiter, int minTokens, int maxTokens) throws Exception {
    if (body==null) throw new Exception("Null string to split");
    String[] s = Utils.removeBlankLines(body.split(delimiter,maxTokens));
    if (s.length<minTokens) throw new Exception("Too few tokens");
    return s;
  }

}

