/* GameSaver class for gnonograms-java
 * Saves puzzles to text file.
 * Copyright (C) 2010-2011  Jeremy Wootten
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
 *  Jeremy Wootten <jeremywootten@gmail.com>
 */
package gnonograms.utils;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Component;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import static java.lang.System.out;

public class GameSaver extends JFileChooser {
    int result;
    BufferedWriter dataStream;

    //private static final long serialVersionUID = 1;
    public GameSaver(Component parent, String puzzleDirectoryPath, String filename){
    super(puzzleDirectoryPath);
	if (filename!=null)	this.setSelectedFile(new File(puzzleDirectoryPath,filename));
    this.setFileSelectionMode(FILES_ONLY);
    this.setFileFilter(new FileNameExtensionFilter("Gnonogram Puzzles","gno"));
    this.setDialogTitle("Save the puzzle");
    result=this.showSaveDialog(parent);
  }

  public void openDataOutputStream() throws IOException{
      File f=this.getSelectedFile();
      String filename=f.getName();
      if (filename.length()<5 || (filename.substring(filename.length()-4)).compareTo(".gno")!=0) {
        f=new File(f.getPath()+".gno");
      }
      dataStream=new BufferedWriter(new FileWriter(f));
  }

  public void writeDescription(String name, String author, String date, String score) throws IOException {
      dataStream.write("[Description]\n");
      if (name.length()>0) dataStream.write(name+"\n");
      if (author.length()>0) dataStream.write(author+"\n");
      if (date.length()>0) dataStream.write(date+"\n");
      if (score.length()>0) dataStream.write(score+"\n");
  }
  public void writeLicense(String license)throws IOException{
      dataStream.write("[License]\n");
      if (license.length()>0) dataStream.write(license+"\n");
  }
  public void writeDimensions(int rows, int cols)throws IOException{
      dataStream.write("[Dimensions]\n");
      dataStream.write(rows+"\n");
      dataStream.write(cols+"\n");
  }
  public void writeClues(String clues, boolean isColumn)throws IOException{
    dataStream.write(isColumn ? "[Column clues]\n" : "[Row clues]\n");
    dataStream.write(clues);
  }
  public void writeSolution(String solution)throws IOException{
      dataStream.write("[Solution]\n");
      dataStream.write(solution+"\n");
  }

  public void writeWorking(String working)throws IOException{
      dataStream.write("[Working grid]\n");
      dataStream.write(working+"\n");
  }
  public void writeState(boolean isSolving)throws IOException{
      dataStream.write("[State]\n");
      if(isSolving)dataStream.write("GAME_STATE_SOLVING\n");
      else dataStream.write("GAME_STATE_SETTING\n");
  }
  public void close()throws IOException{
      dataStream.close();
  }
  public int getResult(){return result;}

}
