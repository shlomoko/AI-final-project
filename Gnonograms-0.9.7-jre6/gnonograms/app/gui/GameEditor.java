/* GameEditor class for Gnonograms-java
 * Keyboard Input puzzle description and clues
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

import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.InputVerifier;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.UIManager;
import javax.swing.SwingConstants;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Font;

import java.lang.NumberFormatException;

import static java.lang.System.out;
import java.awt.GridLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gnonograms.utils.Utils;
import java.util.ResourceBundle;

public class GameEditor extends JDialog implements ActionListener{
  //private static final long serialVersionUID = 1;
  private JComponent infoPane,rowPane,columnPane;
  private JTextField nameField, authorField, dateField, licenseField;
  protected JButton okButton;
  private ClueEditor[] clues;
  private int rows, cols;
  private ResourceBundle rb;
  public boolean wasCancelled=false;

  public GameEditor(JFrame owner, int rows, int cols, ResourceBundle rb){
    super(owner,rb.getString("Edit Game"), true);
    this.rows=rows; this.cols=cols; this.rb=rb;
    this.setLayout(new BorderLayout());
    JTabbedPane tp=new JTabbedPane();
    clues=new ClueEditor[rows+cols];
    tp.add(rb.getString("Information"), createInfoPane());
    tp.add(rb.getString("Row Clues"),createCluePane(false));
    tp.add(rb.getString("Column Clues"), createCluePane(true));

    this.add(tp,BorderLayout.PAGE_START);
    JPanel temp=Utils.okCancelPanelFactory(this,"INFO_OK");
    okButton=(JButton)(temp.getComponent(0));
    this.add(temp,BorderLayout.PAGE_END);
    this.pack();
  }

  private JPanel createInfoPane(){
    JPanel infoPane=new JPanel(new GridBagLayout());
    GridBagConstraints c=new GridBagConstraints();
    c.gridx=0; c.gridy=0;
    c.gridwidth=1; c.gridheight=1;
    c.weightx=0; c.weighty=0;
    c.ipadx=6; c.ipady=6;
    c.fill=GridBagConstraints.NONE;
    c.insets=new Insets(10,10,20,20);
    c.anchor=GridBagConstraints.LINE_END;
    infoPane.add(new JLabel(rb.getString("Name of Puzzle")),c);
    c.gridy=1;
    infoPane.add(new JLabel(rb.getString("Author or Source")),c);
    c.gridy=2;
    infoPane.add(new JLabel(rb.getString("Date of Creation")),c);
    c.gridy=3;
    infoPane.add(new JLabel(rb.getString("License or copyright")),c);
    c.weightx=1;
    c.anchor=GridBagConstraints.LINE_START;
    c.fill=GridBagConstraints.HORIZONTAL;
    c.gridx=1; c.gridy=0;
    nameField=new JTextField(25);
    infoPane.add(nameField,c);
    c.gridy=1;
    authorField=new JTextField(25);
    infoPane.add(authorField,c);
    c.gridy=2;
    dateField=new JTextField(25);
    infoPane.add(dateField,c);
    c.gridy=3;
    licenseField=new JTextField(25);
    infoPane.add(licenseField,c);
    return infoPane;
  }

  private JScrollPane createCluePane(boolean isColumn){

    int offset=isColumn ? rows : 0;
    int size=isColumn ? cols : rows;
    JPanel CluePane=new JPanel(new GridLayout(0,1));
    for (int i=0; i<size; i++) {
      clues[offset+i]=new ClueEditor(i,size);
      CluePane.add(clues[offset+i]);
    }
    JScrollPane sp=new JScrollPane(CluePane);
    return sp;
  }

  public void setGameName(String name){nameField.setText(name);}
  public void setAuthor(String author){authorField.setText(author);}
  public void setCreationDate(String date){dateField.setText(date);}
  public void setLicense(String license){licenseField.setText(license);}
  public void setClue(int idx, String clue, boolean isColumn){
    if(isColumn) clues[idx+rows].setClueText(clue);
    else clues[idx].setClueText(clue);
  }

  public String getGameName(){return nameField.getText();}
  public String getAuthor(){return authorField.getText();}
  public String getCreationDate(){return dateField.getText();}
  public String getLicense(){return licenseField.getText();}
  public String getClue(int idx, boolean isColumn){
    if(isColumn) return clues[idx+rows].getClueText();
    else return clues[idx].getClueText();
  }

  public void actionPerformed(ActionEvent a){
    String command=a.getActionCommand();
    wasCancelled=!(command.equals("INFO_OK"));
    this.setVisible(false);
  }
  
  private class ClueEditor extends Box{
    private JTextField clueText;

    public ClueEditor(int idx, int size){
      super(BoxLayout.X_AXIS);
      JLabel l=new JLabel((idx<9 ? "0" : "")+String.valueOf(idx+1)+"   ");
      l.setFont(new Font("",Font.BOLD,14));
      this.add(l);
      clueText=new JTextField(25);
      clueText.setInputVerifier(new ClueVerifier(size));
      this.add(clueText);
    }
    
    public String getClueText(){return clueText.getText();}
    public void setClueText(String clue){clueText.setText(clue);}
    
  }
  
  private class ClueVerifier extends InputVerifier{
	private int size;
	public ClueVerifier(int size){
		this.size=size;		
	}
    @Override
    public boolean verify(JComponent input){
		boolean valid=false;
		JTextField tf=(JTextField)input;
		String[] sa=(tf.getText()).split(",");
		int count=0, tokens=sa.length, sum=0;
		int[] blocks=new int[tokens];
		for (String s: sa){
			//Check each token is a number
			try{
				blocks[count]=Integer.valueOf(s);
			}
			catch(NumberFormatException e){break;}
			//Only first token can be zero
			if ((blocks[count]==0) && (count>0 || tokens>1)) {
			  break;
			}
			count++;
		}
		if (count==tokens){  
			//All tokens are numbers
			//Check freedom>=0
			for (int i : blocks) sum+=i;
			sum+=tokens-1;
			if (sum<=size){
				//Rebuild string in standard format
				StringBuilder sb= new StringBuilder("");
				for(int i : blocks){
					sb.append(String.valueOf(i)+",");
				}
				tf.setText(sb.substring(0,sb.length()-1));
				tf.setForeground(UIManager.getColor("TextField.foreground"));
				valid=true;
			}
		}
		if (!valid){
			tf.setForeground(Color.red);
			tf.repaint();
		}
		okButton.setEnabled(valid);
		return valid;
    }
  }
}
