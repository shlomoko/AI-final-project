/* Viewer class for gnonograms-java
 * Constructs and Manages the GUI
 * Copyright 2012 Jeremy Paul Wootten <jeremywootten@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 *
 */

package gnonograms.app;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.JComponent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.BoxLayout;
import javax.swing.Box;

import javax.imageio.ImageIO;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Container;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;

import java.util.ResourceBundle;

import gnonograms.app.Resource;
import gnonograms.utils.*;
import gnonograms.app.gui.*;

import static java.lang.System.out;

public class Viewer extends JFrame {
  //private static final long serialVersionUID = 1;
  private CellGrid drawing;
  private LabelBox rowBox, columnBox;
  private Controller control;
  private InfoLabel nameLabel,authorLabel,licenseLabel,scoreLabel,sizeLabel,dateLabel,timeLabel;
  private JLabel [] rowlabels, collabels;
  private Container contentPane;
  private GridBagConstraints c;
  
  private ImageIcon myLogo;
  private ImageIcon scaledLogo;
  protected ImageIcon hiddenIcon, revealedIcon;
  private JLabel logoLabel;
  private JPanel puzzlePane, toolbarPane, infoPane;
  private JButton hiderevealButton, undoButton, redoButton, checkButton, restartButton;
  private ResourceBundle rb;

  private int rows, cols, cluePointSize=20;
  
  public Viewer(Controller control, ResourceBundle rb){
    this.rb=rb;
    this.control=control;
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setTitle(rb.getString("Gnonograms for Java"));
    this.setResizable(false);
    this.setLocationByPlatform(true);
    myLogo=Utils.createImageIcon("gnonograms3-256.png","Logo");
    logoLabel=new JLabel();
    this.setIconImage(myLogo.getImage().getScaledInstance(32,32,Image.SCALE_SMOOTH));
    
    hiddenIcon=Utils.createImageIcon("eyes-closed.png","Hidden icon");
    revealedIcon=Utils.createImageIcon("eyes-open.png","Reveal icon");

    puzzlePane=new JPanel();
    puzzlePane.setLayout(new GridBagLayout());
    toolbarPane=new JPanel();
    toolbarPane.setLayout(new BorderLayout());
    contentPane=this.getContentPane();
    createInfoPane();
    toolbarPane.add(createCommonToolBar(),BorderLayout.LINE_START);

    contentPane.setLayout(new BorderLayout());
    contentPane.add(toolbarPane,BorderLayout.PAGE_START);
    contentPane.add(puzzlePane,BorderLayout.LINE_START);
    contentPane.add(infoPane,BorderLayout.PAGE_END);
    this.pack();
    
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            quit();
        }
    });
  }
  
  public String getClueText(int idx, boolean isColumn){
    if (isColumn) return columnBox.getClueText(idx);
    else return rowBox.getClueText(idx);
  }
  
  public int getPointSize(){return cluePointSize;}
  public String getScore() {return scoreLabel.getInfo();}
  public void setScore(String score){scoreLabel.setInfo(score);}
  public String getName() {return nameLabel.getInfo();}
  public void setName(String name){nameLabel.setInfo(name);}
  public String getAuthor() {return authorLabel.getInfo();}
  public void setAuthor(String author){authorLabel.setInfo(author);}
  public String getCreationDate() {return dateLabel.getInfo();}
  public void setCreationDate(String date){dateLabel.setInfo(date);}
  public String getLicense() {return licenseLabel.getInfo();}
  public void setLicense(String license){licenseLabel.setInfo(license);}
  public String getTime(){return timeLabel.getInfo();}
  public void setTime(String time){timeLabel.setInfo(time);}
  
  public void clearInfoBar(){
    setName("");
    setAuthor("");
    setCreationDate("");
    setLicense("");
    setScore("");
    setTime("");
  }

  public void setSolving(boolean isSolving){
    drawing.setSolving(isSolving);
    hiderevealButton.setIcon(isSolving ? hiddenIcon : revealedIcon);
    hiderevealButton.setToolTipText(isSolving ? rb.getString("Reveal the solution") : rb.getString("Hide the solution"));
	restartButton.setEnabled(isSolving);
	undoButton.setEnabled(isSolving);
	redoButton.setEnabled(isSolving);
	checkButton.setEnabled(isSolving);
  
  }

  protected void quit(){control.quit();}

  private void editGame(){
    GameEditor ge=new GameEditor(this,rows, cols,rb);
    ge.setGameName(getName());
    ge.setCreationDate(getCreationDate());
    ge.setAuthor(getAuthor());
    ge.setLicense(getLicense());
    for (int r=0; r<rows; r++) ge.setClue(r,rowBox.getClueText(r),false);
    for (int c=0; c<cols; c++) ge.setClue(c,columnBox.getClueText(c),true);
    ge.setLocationRelativeTo(this);
    ge.setVisible(true);

    if (!ge.wasCancelled){
      setName(ge.getGameName());
      setCreationDate(ge.getCreationDate());
      setAuthor(ge.getAuthor());
      setLicense(ge.getLicense());
      boolean clueChanged=false;
      String originalText, currentText;
      for (int r=0; r<rows; r++) {
		  currentText=ge.getClue(r,false);
				originalText=rowBox.getClueText(r);
				if (!originalText.equals(currentText)) {
					clueChanged=true;
					rowBox.setClueText(r,currentText);
				}
			}
      for (int c=0; c<cols; c++) {
				currentText=ge.getClue(c,true);
				originalText=columnBox.getClueText(c);
				if (!originalText.equals(currentText)) {
					clueChanged=true;
					columnBox.setClueText(c,currentText);	
				}	  
			}
      setClueFontAndSize(cluePointSize);
      if (clueChanged){
				control.checkCluesValid(); 
			}
    }
    ge.dispose();
  }

  public void setDimensions(int rows, int cols){
    puzzlePane.removeAll();
    this.rows=rows; this.cols=cols;
    sizeLabel.setInfo(rows+"X"+cols);
    String freedomString=rb.getString("Freedom");
    rowBox=new LabelBox(rows, false, freedomString);
    columnBox=new LabelBox(cols, true, freedomString);
    drawing=new CellGrid(rows, cols, control);
    c = new GridBagConstraints();
    
    c.gridx=1; c.gridy=0;   
    c.weightx=0;c.weighty=1;
    c.fill=GridBagConstraints.BOTH;
    c.anchor=GridBagConstraints.CENTER;
    puzzlePane.add(columnBox,c);

    c.gridx=0; c.gridy=1;    
    c.weightx=1;c.weighty=0;
    c.fill=GridBagConstraints.VERTICAL;
    c.anchor=GridBagConstraints.CENTER;
    puzzlePane.add(rowBox,c);

    c.gridx=0; c.gridy=0;
    c.weightx=0;c.weighty=0;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.LINE_END;
    puzzlePane.add(logoLabel,c);
    
    c.gridx=1; c.gridy=1;
    c.weightx=0;c.weighty=0;
    c.fill=GridBagConstraints.BOTH;
    c.anchor=GridBagConstraints.CENTER;
    puzzlePane.add(drawing,c);
    
    resetMaximumClueLength(false, cols/2+1);
    resetMaximumClueLength(true, rows/2+1);
  }

  public void setClueFontAndSize(int pointSize){
    cluePointSize=pointSize;
    if (this.getGraphics()==null) return;
    Point location = this.getLocation();
    Font f=new Font("",Font.BOLD,cluePointSize);
    FontMetrics fm= this.getGraphics().getFontMetrics(f);
    int fontWidth=fm.stringWidth("0");
    int fontHeight=fm.getHeight();
    rowBox.setFontAndSize(f, fontWidth, fontHeight);
    columnBox.setFontAndSize(f, fontWidth, fontHeight);
    repack();
  }
  
  public void setMargins(int cwm, int clm){
	  rowBox.setMargins(cwm,clm);
	  columnBox.setMargins(cwm,clm);
  }

  public void zoomFont(int changeInPointSize){
    if (changeInPointSize>0)cluePointSize++;
    else if (changeInPointSize<0) cluePointSize--;
    if(cluePointSize<Resource.MINIMUM_CLUE_POINTSIZE) cluePointSize=Resource.MINIMUM_CLUE_POINTSIZE;
    if(cluePointSize>Resource.MAXIMUM_CLUE_POINTSIZE) cluePointSize=Resource.MAXIMUM_CLUE_POINTSIZE;
    setClueFontAndSize(cluePointSize);
  }

  public void setClueText(int idx, String text, boolean isColumn, boolean repackAfter){
    Point location = this.getLocation();
    LabelBox lb= isColumn ? columnBox : rowBox;
    lb.setClueText(idx,text);
    setLabelToolTip(idx, Utils.freedomFromClue((isColumn ? rows : cols),text),isColumn);
    if (repackAfter) repack();
  }
  
  public void repack(){  
		//if row or column boxes change size, repack. 
		boolean b1=rowBox.setLabelSize();
		boolean b2=columnBox.setLabelSize(); 
    if (b1 || b2){
			//out.println("Repack");
			resizeLogoLabelImage(); //resize logo label accordingly;
			this.pack();
		}
  }

  private void resizeLogoLabelImage(){ 
      int width=rowBox.getLogoSize();
      int height=columnBox.getLogoSize();
      if (width==0||height==0) return;
      if (scaledLogo==null||scaledLogo.getIconHeight()!=height || scaledLogo.getIconWidth()!=width){
        scaledLogo=new ImageIcon(myLogo.getImage().getScaledInstance(width,height,Image.SCALE_SMOOTH));
        logoLabel.setIcon(scaledLogo);
      }
  }
  
  public void resetMaximumClueLength(boolean isColumn, int maxLength){
    if (isColumn)  columnBox.resetMaximumClueLength(maxLength);
    else  rowBox.resetMaximumClueLength(maxLength);
  }
  
  public String getClues(boolean isColumn){
    if (isColumn) return columnBox.getClues();
    else return rowBox.getClues();
  }

  public void setLabelToolTip(int idx, int freedom, boolean isColumn){
    if (isColumn) columnBox.setLabelToolTip(idx, freedom);
    else rowBox.setLabelToolTip(idx, freedom);
  }
  
  public void highlightLabels(int r, int c, boolean on){
      rowBox.highlightLabel(r,on);
      columnBox.highlightLabel(c,on);
  }

  public void redrawGrid(){
    drawing.repaint();
  }
  
  public void enableCheckButton(boolean b){
	  checkButton.setEnabled(b);
  }
  
  private JToolBar createCommonToolBar(){
    JToolBar tb=new JToolBar();
    //get odd effects if toolbar is added back to a resized puzzle
    tb.setFloatable(false);

    int position=0;

    tb.add(new MyAction("Create",Utils.createImageIcon("New24.gif","Create icon"),"CREATE_GAME"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Draw your own puzzle grid"));
    position++;
    
    tb.add(new MyAction("Import image",Utils.createImageIcon("Import24.gif","Import icon"),"IMPORT_IMAGE"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Convert an image to a puzzle"));
    position++;


    tb.add(new MyAction("Edit",Utils.createImageIcon("Edit24.gif","Edit icon"),"EDIT_GAME"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Edit the description and clues"));
    position++;

    tb.addSeparator();
    position++;
    
    tb.add(new MyAction("Load game",Utils.createImageIcon("Open24.gif","Load icon"),"LOAD_GAME"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Load a puzzle from file"));
    position++;

    tb.add(new MyAction("Save game",Utils.createImageIcon("Save24.gif","Save icon"),"SAVE_GAME"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Save the puzzle to file"));
    position++;
    
    tb.addSeparator();
    position++;
    
    undoButton=tb.add(new MyAction("Undo",Utils.createImageIcon("Undo24.gif","Undo icon"),"UNDO_MOVE"));
    undoButton.setToolTipText(rb.getString("Undo move"));
     position++;
     
    redoButton=tb.add(new MyAction("Redo",Utils.createImageIcon("Redo24.gif","Redo icon"),"REDO_MOVE"));
    redoButton.setToolTipText(rb.getString("Redo move"));
     position++;

    restartButton=tb.add(new MyAction("Restart",Utils.createImageIcon("Refresh24.gif","Restart icon"),"RESTART_GAME"));
    restartButton.setToolTipText(rb.getString("Start solving this puzzle again"));
    position++;
     
    tb.addSeparator();
    position++;

    tb.add(new MyAction("Solve game",Utils.createImageIcon("computer.png","Solve icon"),"SOLVE_GAME"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Let the computer try to solve the puzzle"));
     position++;
     
    checkButton=tb.add(new MyAction("Check",Utils.createImageIcon("errorcheck.png","Check icon"),"CHECK_GAME"));
    checkButton.setToolTipText(rb.getString("Check for mistakes"));
    position++;
    
    tb.addSeparator();
    position++;

    tb.add(new MyAction("Random game",Utils.createImageIcon("dice.png","Random icon"),"RANDOM_GAME"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Generate a random puzzle"));
    position++;
    
    tb.add(new MyAction("Hide game",revealedIcon,"HIDE_REVEAL_GAME"));
    hiderevealButton=((JButton)(tb.getComponentAtIndex(position)));
    hiderevealButton.setToolTipText(rb.getString("Hide the solution"));
    position++;
    
    tb.addSeparator();
    position++;
    
    tb.add(new MyAction("Preferences",Utils.createImageIcon("Preferences24.gif","Preferences icon"),"EDIT_PREFERENCES"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Edit preferences"));
    position++;

    tb.add(new MyAction("Smaller",Utils.createImageIcon("ZoomOut24.gif","Soom Out icon"),"ZOOM_OUT"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Make the font smaller"));
    position++;

    tb.add(new MyAction("Larger",Utils.createImageIcon("ZoomIn24.gif","Zoom In icon"),"ZOOM_IN"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Make the font larger"));
    position++;
    
    tb.add(new MyAction("About",Utils.createImageIcon("About24.gif","About icon"),"ABOUT_GAME"));
    ((JComponent)(tb.getComponentAtIndex(position))).setToolTipText(rb.getString("Credits and keyboard shortcuts"));
    position++;
    
    return tb;
  }
  
  private class MyAction extends AbstractAction{
    //private static final long serialVersionUID = 1;
    public MyAction(String text, ImageIcon icon, String command){
      super(text, icon);
      putValue(ACTION_COMMAND_KEY, command);
    }
    //@Override
    public void actionPerformed(ActionEvent a){
      String command=a.getActionCommand();
      if (command.equals("CREATE_GAME")) control.createGame();
      if (command.equals("IMPORT_IMAGE")) control.importImage();
      if (command.equals("LOAD_GAME")) {
        control.loadGame();
        setClueFontAndSize(cluePointSize);//resize label boxes if necessary
      }
      if (command.equals("SAVE_GAME")) control.saveGame();
      if (command.equals("RANDOM_GAME")) {
        control.randomGame();
        setClueFontAndSize(cluePointSize);//resize label boxes if necessary
      }
      if (command.equals("HIDE_REVEAL_GAME")) {
        JButton source=(JButton)(a.getSource());
        String description=((ImageIcon)(source.getIcon())).getDescription();
        if (description.contains("Hidden")) control.setSolving(false);
        else control.setSolving(true);
      }
      if (command.equals("SOLVE_GAME")) control.userSolveGame();
      if (command.equals("RESTART_GAME")) control.restartGame();
      if (command.equals("ZOOM_IN")) control.zoomFont(2);
      if (command.equals("ZOOM_OUT")) control.zoomFont(-2);
      if (command.equals("EDIT_GAME")) editGame();
      if (command.equals("CHECK_GAME")) control.checkGame();
      if (command.equals("UNDO_MOVE")) control.undoMove();
      if (command.equals("REDO_MOVE")) control.redoMove();
      if (command.equals("ABOUT_GAME")) Utils.showHelpDialog(rb);
      if (command.equals("EDIT_PREFERENCES")) control.editPreferences();
    }
  }

  private void createInfoPane(){
    infoPane=new JPanel();
    infoPane.setLayout(new BoxLayout(infoPane,BoxLayout.LINE_AXIS));
    nameLabel=new InfoLabel(rb.getString("Name"));
    authorLabel=new InfoLabel(rb.getString("Source"));
    licenseLabel=new InfoLabel(rb.getString("(C)"));
    dateLabel=new InfoLabel(rb.getString("Created"));
    sizeLabel=new InfoLabel(rb.getString("Size"));
    scoreLabel=new InfoLabel(rb.getString("Score"));
    timeLabel=new InfoLabel(rb.getString("Time"));
    infoPane.add(nameLabel);
    infoPane.add(authorLabel);
    infoPane.add(licenseLabel);
    infoPane.add(dateLabel);
    infoPane.add(Box.createHorizontalGlue());
    infoPane.add(timeLabel);
    infoPane.add(sizeLabel);
    infoPane.add(scoreLabel);
    infoPane.addMouseListener(new MouseAdapter(){
        public void mousePressed(MouseEvent e) {;
          if (e.getClickCount()>1) editGame();
        }
      });
      
  }

  private class InfoLabel extends JLabel {
    private String info;
    private String heading;
    //private static final long serialVersionUID = 1;

    public InfoLabel(String heading){
      this.info=".....";
      this.heading=heading;
      this.setBorder(BorderFactory.createEtchedBorder());
      this.setFont(new Font("",Font.PLAIN,10));
      this.setInfo(this.info);
    }

    protected String getInfo(){return this.info;}

    protected void setInfo(String info){
      if (info.length()<1) this.info=".....";
      else this.info=info;
      this.setText(this.heading+": "+this.info+" ");
    }
  }
}

