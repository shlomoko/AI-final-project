/* Utils class for gnonograms-java
 * Various helper methods and dialogues
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

import static java.lang.System.out;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import java.util.Date;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.ResourceBundle;

import gnonograms.app.Resource;

public class Utils{
  static public String clueFromIntArray(int[] cs){
     StringBuilder sb= new StringBuilder("");
    int count=0, blocks=0;
    boolean counting=false;
    for (int i=0; i<cs.length; i++){
      if (cs[i]==Resource.CELLSTATE_EMPTY){
        if (counting){
          sb.append(count);
          sb.append(Resource.BLOCKSEPARATOR);
          counting=false;
          count=0;
          blocks++;
        }
      }
      else if(cs[i]==Resource.CELLSTATE_FILLED){
        counting=true; count++;
      }
    }
    if (counting) {
      sb.append(count);
      sb.append(Resource.BLOCKSEPARATOR);
      blocks++;
    }
    if (blocks==0) sb.append("0");
    else sb.setLength(sb.length() -1);
    return sb.toString();
  }

  public static int freedomFromClue(int regionSize, String clue){
    int[] blocks=blockArrayFromClue(clue);
    int count=0;
    for(int e : blocks) count+=e;
    return regionSize-count-blocks.length+1;
  }

  public static String stringFromIntArray(int[] cs){
    if (cs==null) return "";
    StringBuilder sb= new StringBuilder();
    for (int i=0; i<cs.length; i++){
      sb.append(cs[i]);
      sb.append(" ");
    }
    return sb.toString();
  }

  static public int[] cellStateArrayFromString(String s) throws NumberFormatException {
    String[] data=removeBlankLines(s.split("[\\D\\n]",110));
    int[] csa=new int[data.length];
    for (int i=0; i<data.length; i++) {
      csa[i]=new Integer(data[i]);
    }
    return csa;
  }

  public static String[] removeBlankLines(String[] sa){
    ArrayList<String> al = new ArrayList<String>();
    int count=0;
    for (String s : sa) {
      if (s.length()>0) {al.add(s);count++;}
    }
    String[] result=new String[count];
    for(int i=0; i<count; i++){
      result[i]=al.get(i);
    }
    return result;
  }

  public static int[] blockArrayFromClue(String s){
    String[] clues=removeBlankLines(s.split("[\\D\\n]",50));
    if(clues.length==0) {
      clues=new String[1]; clues[0]="0";
    }
    int[] blocks=new int[clues.length];

    for (int i=0;i<clues.length;i++) {
      blocks[i]=Integer.parseInt(clues[i]);
    }
    return blocks;
  }

  public static boolean showConfirmDialog(String s){
    int result=JOptionPane.showConfirmDialog(null,s,"",JOptionPane.YES_NO_OPTION);
    return (result==JOptionPane.YES_OPTION);
  }
  public static void showWarningDialog(String s){
    JOptionPane.showMessageDialog(null,s,"",JOptionPane.WARNING_MESSAGE);
  }
  public static void showErrorDialog(String s){
     JOptionPane.showMessageDialog(null,s,"",JOptionPane.ERROR_MESSAGE);
  }
  public static boolean showInfoDialog(String s){
    JOptionPane.showMessageDialog(null,s,"",JOptionPane.INFORMATION_MESSAGE);
    return true;
  }
  public static boolean showHelpDialog(ResourceBundle rb){
      JPanel messagePane=new JPanel(new GridBagLayout());
      GridBagConstraints c=new GridBagConstraints();
      c.gridx=0; c.gridy=0; c.weighty=2;
      c.gridwidth=2;c.gridheight=2;
      c.fill=GridBagConstraints.BOTH;
      c.anchor=GridBagConstraints.LINE_START;
      c.insets=new Insets(6,6,6,6);
      messagePane.add(new JLabel(rb.getString("Gnonograms for Java")+" "+rb.getString("Version")+" "+Resource.VERSION_STRING),c);
      c.gridy=2;c.gridwidth=2;c.gridheight=2;
      messagePane.add(new JLabel("Jeremy Wootten <jeremywootten@gmail.com"),c);
      c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weighty=1;
      c.insets=new Insets(6,6,2,6);
      messagePane.add(new JLabel(rb.getString("KEY CONTROLS")),c);
      c.gridy=5; c.insets=new Insets(2,6,2,6);
      messagePane.add(new JLabel(rb.getString("Arrows")),c);
      c.gridy=GridBagConstraints.RELATIVE;
      messagePane.add(new JLabel(rb.getString("Ctrl+Arrows")),c);
      messagePane.add(new JLabel(rb.getString("F or f")),c);
      messagePane.add(new JLabel(rb.getString("E or e")),c);
      messagePane.add(new JLabel(rb.getString("X or x")),c);
      messagePane.add(new JLabel(rb.getString("+ or =")),c);
      messagePane.add(new JLabel(rb.getString("- or _")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl S")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl O")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl N")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl R")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl U")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl Y")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl H")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl M")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl L")),c);
      messagePane.add(new JLabel(rb.getString("Ctrl Q")),c);
      c.gridy=5;c.gridx=1;
      messagePane.add(new JLabel(rb.getString("move cell selection cursor")),c);
      c.gridy=GridBagConstraints.RELATIVE;
      messagePane.add(new JLabel(rb.getString("drag cell selection cursor")),c);
      messagePane.add(new JLabel(rb.getString("mark selected cell full")),c);
      messagePane.add(new JLabel(rb.getString("mark selected cell empty")),c);
      messagePane.add(new JLabel(rb.getString("mark selected cell unknown")),c);
      messagePane.add(new JLabel(rb.getString("increase size of cells")),c);
      messagePane.add(new JLabel(rb.getString("decrease size of cells")),c);
      messagePane.add(new JLabel(rb.getString("Save the puzzle")),c);
      messagePane.add(new JLabel(rb.getString("Open a puzzle")),c);
      messagePane.add(new JLabel(rb.getString("Design a puzzle")),c);
      messagePane.add(new JLabel(rb.getString("generate a Random puzzle")),c);
      messagePane.add(new JLabel(rb.getString("Undo last move")),c);
      messagePane.add(new JLabel(rb.getString("Redo undone move")),c);
      messagePane.add(new JLabel(rb.getString("get Hint when solving")),c);
      messagePane.add(new JLabel(rb.getString("Mark cell under cursor")),c);
      messagePane.add(new JLabel(rb.getString("backtrack to Last marked cell")),c);
      messagePane.add(new JLabel(rb.getString("Quit")),c);
      JOptionPane.showMessageDialog(null,messagePane,"",JOptionPane.INFORMATION_MESSAGE);
      return true;
  }
  public static JPanel okCancelPanelFactory(ActionListener listener, String okCommand){
    JButton okButton = new JButton(createImageIcon("dialog-ok-apply48.png","okButtonIcon"));
    okButton.setActionCommand(okCommand);
    okButton.addActionListener(listener);
    JButton cancelButton=new JButton(createImageIcon("dialog-cancel48.png","cancelButtonIcon"));
    cancelButton.setActionCommand("");
    cancelButton.addActionListener(listener);
    JPanel buttonPanel=new JPanel();
    buttonPanel.setLayout(new BorderLayout());
    buttonPanel.add(okButton, BorderLayout.LINE_START);
    buttonPanel.add(cancelButton, BorderLayout.LINE_END);
    return buttonPanel;
  }
  
  public static String calculateTimeTaken(Date startDate, Date endDate){
    long msec=endDate.getTime()-startDate.getTime();
    long seconds=msec/1000;
    msec=msec-seconds*1000;
    long minutes=seconds/60;
    seconds=seconds-minutes*60;
    return minutes+" : "+seconds+((msec<100)? ".0":".")+msec;
  }
  
  public static ImageIcon createImageIcon(String name,String description){
    java.net.URL imgURL = Utils.class.getResource(Resource.IMAGE_PATH+name);
    if (imgURL != null) return new ImageIcon(imgURL, description);
    else {
        System.out.println("Couldn't find file: " + Resource.IMAGE_PATH+name);
        return null;
    }
  }
}
