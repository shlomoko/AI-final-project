/* Img2gno class
 * Main UI window
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
 *  Jeremy Wootten <jeremwootten@gmail.com>
 */

//======================================================================
package gnonograms.app.gui;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;
import java.awt.Cursor;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

import java.util.ResourceBundle;

import gnonograms.utils.Utils;
import gnonograms.app.Resource;

import static java.lang.System.out;

enum SelectionMode{
  NONE,
  RESIZE,
  MOVE
};

enum MovePoint{
  NONE,
  TOP,
  BOTTOM,
  LEFT,
  RIGHT,
  TOP_LEFT,
  BOTTOM_LEFT,
  TOP_RIGHT,
  BOTTOM_RIGHT
};

  public class ImageImporter extends JDialog implements ActionListener{
	//private static final long serialVersionUID = 1;
    public boolean hasImage, wasCancelled;
    private int origWidth, origHeight, rows, cols, offsetX, offsetY, selectionWidth, selectionHeight;
    private File imageFile;
    private BufferedImage originalImage, finalImage, originalMonoImage;
    private JPanel contentPane, controlPanel, finalImagePanel;
    private SelectableImagePane originalImagePanel;
    private JLabel originalImageLabel, finalImageLabel;
    private JButton okButton;
    private ImageIcon scaledImage;
    private int[] rgb;
    private int alphaThreshold=128, redThreshold=128, greenThreshold=128, blueThreshold=128;
    private int overallThreshold=510;
    private int background=0xFFFFFF; //white background by default
    private JLabel rowsLabel, colsLabel;
    private JPanel propertiesPanel;
    private Rectangle origBounds;
    private ResourceBundle rb;
    protected boolean invertColor=false;
    protected boolean lockAspectRatio=true;
    protected double imageAspectRatio;
    
    static final int IMAGE_DISPLAY_HEIGHT=380;
    static final int MINIMUM_SELECTABLE_PIXELS=5;
    
  public ImageImporter(JFrame parent, ResourceBundle rb, String imageDirectory, int rows, int cols){
    super(parent,rb.getString("Convert Image"), true);
    this.rb=rb;
    wasCancelled=true; hasImage=false;
      ImageFileLoader ifl=new ImageFileLoader(parent, imageDirectory);
      if (ifl.result==0){
        imageFile=ifl.getSelectedFile();
        if(openImageFile(imageFile)){
          hasImage=true;
          this.rows=rows; this.cols=cols;
          imageAspectRatio=(double)rows/(double)cols;
          createInterface(originalImage);
          updateFinalImage(rows, cols);
          setLocationRelativeTo((Component)parent);
        }
      }
  }
  
  private boolean openImageFile(File imgFl){
    try{
      originalImage=ImageIO.read(imgFl);
    }
    catch (FileNotFoundException e){Utils.showErrorDialog(rb.getString("Image File not found"));return false;}
    catch (IOException e){Utils.showErrorDialog(rb.getString("An error occurred opening the image file"));return false;}
    origWidth=originalImage.getWidth(); origHeight=originalImage.getHeight();
    selectionWidth=origWidth; selectionHeight=origHeight;
    offsetX=0;offsetY=0;
    return true;
  }
  public String getImagePath(){return imageFile.getPath();}
  public String getImageName(){return imageFile.getName();}
  
  private void createInterface(BufferedImage img){
    contentPane=new JPanel(new BorderLayout());
    originalImagePanel = new SelectableImagePane(img, this);
    finalImagePanel=new JPanel(new BorderLayout());
    finalImageLabel= new JLabel();
    finalImagePanel.add(finalImageLabel,BorderLayout.PAGE_START);
    controlPanel=fillControlPanel();
    contentPane.add(controlPanel,BorderLayout.CENTER);
    contentPane.add(originalImagePanel,BorderLayout.LINE_START);
    contentPane.add(finalImagePanel,BorderLayout.LINE_END);

    JPanel temp=Utils.okCancelPanelFactory(this,"INFO_OK");
    okButton=(JButton)(temp.getComponent(0));
    this.add(contentPane,BorderLayout.PAGE_START);
    this.add(temp,BorderLayout.PAGE_END);
    this.pack();
    originalImagePanel.selectAll();
  }
  
  
  private JPanel fillControlPanel(){
    
    ButtonGroup backgroundColourButtons= new ButtonGroup();
    final JRadioButton blackButton = new JRadioButton();

    blackButton.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        if (blackButton.isSelected())background=0x000000;
        else background=0xFFFFFF;
        updateFinalImage(-1,-1);
      }
    });
    JLabel blackLabel = new JLabel(Utils.createImageIcon("black_background36.png","Black Background"));
    blackLabel.setToolTipText(rb.getString("Black Background"));
    JRadioButton whiteButton = new JRadioButton();
    JLabel whiteLabel = new JLabel(Utils.createImageIcon("white_background36.png","White Background"));
    whiteLabel.setToolTipText(rb.getString("White Background"));
    backgroundColourButtons.add(blackButton);
    backgroundColourButtons.add(whiteButton);
    blackButton.setSelected(false);
    whiteButton.setSelected(true);
    blackButton.setHorizontalAlignment(SwingConstants.CENTER);
    whiteButton.setHorizontalAlignment(SwingConstants.CENTER);
    JPanel backgroundPanel=new JPanel(new GridLayout(1,0));
    JPanel wp=new JPanel(new BorderLayout()); wp.add(whiteLabel,BorderLayout.LINE_START); wp.add(whiteButton,BorderLayout.CENTER);
    JPanel bp=new JPanel(new BorderLayout()); bp.add(blackLabel,BorderLayout.LINE_START); bp.add(blackButton,BorderLayout.CENTER);
    backgroundPanel.add(wp); backgroundPanel.add(bp);
    backgroundPanel.setBorder(BorderFactory.createEtchedBorder());


    JRadioButton noInvertRadioButton= new JRadioButton();
    noInvertRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
    JLabel noInvertLabel=new JLabel(Utils.createImageIcon("NoInvertion36.png","No invertion"));
    noInvertLabel.setToolTipText(rb.getString("Do not invert Black and White"));
    JRadioButton invertRadioButton= new JRadioButton();
    invertRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
    JLabel invertLabel=new JLabel(Utils.createImageIcon("Invertion36.png","Invert"));
    invertLabel.setToolTipText(rb.getString("Invert Black and White"));
    ButtonGroup invertButtons= new ButtonGroup();
    invertButtons.add(noInvertRadioButton);
    invertButtons.add(invertRadioButton);
    noInvertRadioButton.setSelected(true);
    invertButtons.add(invertRadioButton);
    noInvertRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
    invertRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
    invertRadioButton.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        invertColor=((JRadioButton)(e.getSource())).isSelected();
        updateFinalImage(-1,-1);
      }
        });
    JPanel invertPanel=new JPanel(new GridLayout(1,0));
    JPanel nip=new JPanel(new BorderLayout()); nip.add(noInvertLabel,BorderLayout.LINE_START);nip.add(noInvertRadioButton,BorderLayout.CENTER);
    JPanel ip=new JPanel(new BorderLayout()); ip.add(invertLabel,BorderLayout.LINE_START);ip.add(invertRadioButton,BorderLayout.CENTER);
    invertPanel.add(nip); invertPanel.add(ip);
    invertPanel.setBorder(BorderFactory.createEtchedBorder());
    
    JPanel redThresholdPanel=makeThresholdSliderAndLabel(
      "red_threshold36.png",
      new ChangeListener(){
        public void stateChanged(ChangeEvent e){
          redThreshold=(int)(2.55*((JSlider)(e.getSource())).getValue());
          updateFinalImage(-1,-1);
        }
      },
      rb.getString("Red Threshold")
      );
    JPanel blueThresholdPanel=makeThresholdSliderAndLabel(
      "blue_threshold36.png",
      new ChangeListener(){
        public void stateChanged(ChangeEvent e){
          blueThreshold=(int)(2.55*((JSlider)(e.getSource())).getValue());
          updateFinalImage(-1,-1);
        }
      },
       rb.getString("Blue Threshold")
      );
    JPanel greenThresholdPanel=makeThresholdSliderAndLabel(
      "green_threshold36.png",
      new ChangeListener(){
        public void stateChanged(ChangeEvent e){
          greenThreshold=(int)(2.55*((JSlider)(e.getSource())).getValue());
          updateFinalImage(-1,-1);
        }
      },
       rb.getString("Green Threshold")
      );
    JPanel alphaThresholdPanel=makeThresholdSliderAndLabel(
      "alpha_threshold36.png",
      new ChangeListener(){
        public void stateChanged(ChangeEvent e){
          alphaThreshold=(int)(2.55*((JSlider)(e.getSource())).getValue());
          updateFinalImage(-1,-1);
        }
      },
       rb.getString("Alpha Threshold")
      );

    
    rowsLabel = new JLabel();
    rowsLabel.setToolTipText(rb.getString("Set number of rows"));
    colsLabel = new JLabel();
    colsLabel.setToolTipText(rb.getString("Set number of columns"));
    rowsLabel.setIcon(Utils.createImageIcon("resize-rows36.png","Rows"));
    colsLabel.setIcon(Utils.createImageIcon("resize-columns36.png","Columns"));
    final JSpinner rowSpinner=new JSpinner(new SpinnerNumberModel(rows,MINIMUM_SELECTABLE_PIXELS,Resource.MAXIMUM_GRID_SIZE,1));
    final JSpinner columnSpinner=new JSpinner(new SpinnerNumberModel(cols,MINIMUM_SELECTABLE_PIXELS,Resource.MAXIMUM_GRID_SIZE,1));
    
    rowSpinner.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        int r=((Integer)(rowSpinner.getValue())).intValue();
        if(lockAspectRatio){
          columnSpinner.setValue(new Integer(Math.max(MINIMUM_SELECTABLE_PIXELS,(int)((double)r/imageAspectRatio))));
        }
        int c=((Integer)(columnSpinner.getValue())).intValue();
        if(!lockAspectRatio)imageAspectRatio=(double)r/(double)c;
        updateFinalImage(r,c);
      }
    });
    columnSpinner.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        if(!lockAspectRatio){
          int c=((Integer)(columnSpinner.getValue())).intValue();
          int r=((Integer)(rowSpinner.getValue())).intValue();
          imageAspectRatio=(double)r/(double)c;
          updateFinalImage(r,c);
        }
      }
    });
    JPanel rowColPanel=new JPanel(new GridLayout(1,0));
    JPanel rp=new JPanel(new BorderLayout()); rp.add(rowsLabel,BorderLayout.LINE_START); rp.add(rowSpinner,BorderLayout.LINE_END);
    JPanel cp=new JPanel(new BorderLayout()); cp.add(colsLabel,BorderLayout.LINE_START); cp.add(columnSpinner,BorderLayout.LINE_END);
    rowColPanel.add(rp); rowColPanel.add(cp);
    rowColPanel.setBorder(BorderFactory.createEtchedBorder());

    JPanel upperPanel=new JPanel(new GridLayout(0,1));
    upperPanel.add(rowColPanel);
    upperPanel.add(backgroundPanel);
    upperPanel.add(invertPanel);

    JPanel thresholdPanel=new JPanel(new GridLayout(0,1));
    thresholdPanel.add(redThresholdPanel);
    thresholdPanel.add(greenThresholdPanel);
    thresholdPanel.add(blueThresholdPanel);
    thresholdPanel.add(alphaThresholdPanel);

    JCheckBox lockCheckBox = new JCheckBox();
    lockCheckBox.setIcon(Utils.createImageIcon("unlocked36.png","Unlocked"));
    lockCheckBox.setSelectedIcon(Utils.createImageIcon("locked36.png","Locked"));
    lockCheckBox.setRolloverEnabled(false);
    lockCheckBox.setContentAreaFilled(false);
    lockCheckBox.setSelected(false);
    lockAspectRatio=false;
    lockCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
    lockCheckBox.setToolTipText(rb.getString("Lock Aspect Ratio"));
    lockCheckBox.addChangeListener(new ChangeListener(){
        public void stateChanged(ChangeEvent e){
					//ignore irrelevant events
          if(lockAspectRatio=((JCheckBox)(e.getSource())).isSelected())return;
          lockAspectRatio=!lockAspectRatio;
          columnSpinner.setEnabled(!lockAspectRatio);
          if(lockAspectRatio)originalImagePanel.selectAll();
        }
      });
      
    JPanel controlPanel=new JPanel(new BorderLayout());
    controlPanel.add(lockCheckBox, BorderLayout.PAGE_START);
    controlPanel.add(upperPanel,BorderLayout.CENTER);
    controlPanel.add(thresholdPanel,BorderLayout.PAGE_END);
    return controlPanel;
   
  }

  private JPanel makeThresholdSliderAndLabel(String iconPath, ChangeListener cl, String tooltip){
    JSlider ts=new JSlider(5, 95, 50 );
    ts.setOrientation(SwingConstants.HORIZONTAL);
    ts.setPaintTrack(true);
    ts.setPaintLabels(false);
    ts.setBorder(BorderFactory.createEtchedBorder());
    JLabel tsl=new JLabel(Utils.createImageIcon(iconPath,""));
    tsl.setToolTipText(tooltip);
    tsl.setVerticalAlignment(JLabel.CENTER);
    tsl.setHorizontalAlignment(JLabel.CENTER);
    ts.addChangeListener(cl);
    JPanel jp=new JPanel(new BorderLayout());
    jp.add(tsl,BorderLayout.LINE_START); jp.add(ts,BorderLayout.CENTER);
    jp.setBorder(BorderFactory.createEtchedBorder());
    return jp;
  }
  private void updateFinalImage(int r, int c){
    if (r>0)rows=r; 
    if (c>0)cols=c;
    if(originalImage==null) return;
    finalImage=toMono(selectAndScaleOriginal(originalImage,offsetX,offsetY,selectionWidth,selectionHeight,cols,rows));
    if(finalImageLabel==null) return;
		int finalWidth=(int)(IMAGE_DISPLAY_HEIGHT*((double)cols/(double)rows));
		int finalHeight=IMAGE_DISPLAY_HEIGHT;
		if (finalWidth>IMAGE_DISPLAY_HEIGHT){
			finalWidth=IMAGE_DISPLAY_HEIGHT;
			finalHeight=(int)(finalWidth*((double)rows/(double)cols));
		}
		finalImageLabel.setIcon(new ImageIcon(finalImage.getScaledInstance(finalWidth,finalHeight,BufferedImage.SCALE_SMOOTH)));
		this.pack();
  }
  
  public void updateSelection(Rectangle2D selection){
    offsetX =(int)(origWidth*selection.getX());
    offsetY=(int)(origHeight*selection.getY());
    selectionWidth=(int)(origWidth*selection.getWidth());
    selectionHeight=(int)(origHeight*selection.getHeight());
    updateFinalImage(-1,-1);
  }
  
  private BufferedImage selectAndScaleOriginal(BufferedImage img,int offsetX, int offsetY, int width, int height, int cols, int rows){
    if (offsetX<0||offsetY<0||width<5||height<5||offsetX+width>img.getWidth()||offsetY+height>img.getHeight()) return null;
    BufferedImage selection=img.getSubimage(offsetX,offsetY,width,height);
    Image scaledSelection=selection.getScaledInstance(cols,rows,BufferedImage.SCALE_FAST);
    BufferedImage scaledBufferedImage = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_ARGB);
    Graphics g=scaledBufferedImage.createGraphics();
    g.drawImage(scaledSelection,0,0,null);
    g.dispose();
    return scaledBufferedImage;
  }
  
  private BufferedImage toMono(BufferedImage img){
    if (img==null) return finalImage;
    int width=img.getWidth(), height=img.getHeight();
    rgb=new int[width*height];
    int red,green,blue,alpha,pixel;
    img.getRGB(0,0,width,height,rgb,0,width);
    for(int ptr=0;ptr<width*height;ptr++){
      pixel=rgb[ptr];
      alpha = (pixel >>> 24) & 0xFF;
      red = (pixel >>> 16) & 0xFF;
      green = (pixel >>> 8) & 0xFF;
      blue = (pixel >>> 0) & 0xFF;
      rgb[ptr]=toMonoPixel(alpha,red,green,blue);
    }
    BufferedImage resultImage=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
    resultImage.setRGB(0,0,width,height,rgb,0,width);
    return resultImage;
  }
  private int toMonoPixel(int a,int r, int g, int b){
    a=(a>alphaThreshold ? 1 : 0);
    r=(r>redThreshold ? r : 0)*a;
    g=(g>greenThreshold ? g : 0)*a;
    b=(b>blueThreshold ? b : 0)*a;
    int black=invertColor ? 0xFFFFFFFF : 0xFF000000;
    int white=invertColor ? 0xFF000000 : 0xFFFFFFFF;
    return (r>0||g>0||b>0||background*(1-a)>0) ? white : black;
  }
  public int getRows(){return rows;}
  public int getCols(){return cols;}
  public int[] getRow(int r){
    int[] rowPixels= new int[cols];
    int offset=r*cols;
    for (int i=0;i<cols;i++) {
      if(rgb[offset+i]<0xFFFFFFFF) rowPixels[i]=Resource.CELLSTATE_FILLED;
      else rowPixels[i]=Resource.CELLSTATE_EMPTY;
    }
    return rowPixels;
  }
  public void actionPerformed(ActionEvent a){
    String command=a.getActionCommand();
    wasCancelled=!(command.equals("INFO_OK"));
    this.setVisible(false);
  }

  private class ImageFileLoader extends JFileChooser {
    protected int result;
    public ImageFileLoader(Component parent, String imageDirectoryPath){
    super(imageDirectoryPath);
    this.setFileSelectionMode(FILES_ONLY);
    this.setFileFilter(new FileNameExtensionFilter("Images","png","gif","bmp","jpg"));
    this.setAcceptAllFileFilterUsed(false);
    this.setDialogTitle(rb.getString("Choose a simple image to convert"));
    result=this.showOpenDialog(parent);
    }
  }
  
  private class SelectableImagePane extends JLayeredPane{
    JLabel originalImageLabel;
    public DrawingPanel drawingPane;
    protected ImageImporter parent;
    int height;
    
    public SelectableImagePane(BufferedImage img, ImageImporter parent){
      super();
      this.height=height;
      this.parent=parent;
      originalImageLabel = new JLabel();
      ImageIcon icon;
      if (origHeight>=origWidth){
        icon =new ImageIcon(img.getScaledInstance(-1,IMAGE_DISPLAY_HEIGHT,Image.SCALE_SMOOTH));
      }
      else{
        icon =new ImageIcon(img.getScaledInstance(IMAGE_DISPLAY_HEIGHT,-1,Image.SCALE_SMOOTH));
      }
      originalImageLabel.setIcon(icon);
      int h=icon.getIconHeight(), w=icon.getIconWidth();
      origBounds=new Rectangle(0,0,w,h);
      originalImageLabel.setBounds(origBounds);
      this.add(originalImageLabel,DEFAULT_LAYER);
      
      drawingPane=new DrawingPanel();
      drawingPane.setBounds(origBounds);
      drawingPane.setMinSelectionWidth((int)((double)(w*MINIMUM_SELECTABLE_PIXELS)/(double)(img.getWidth())));
      drawingPane.setMinSelectionHeight((int)((double)(h*MINIMUM_SELECTABLE_PIXELS)/(double)(img.getHeight())));
      this.add(drawingPane,PALETTE_LAYER);
      drawingPane.setVisible(true);
      
      this.addMouseListener(new MouseAdapter(){

        public void mouseExited(MouseEvent e) {
          drawingPane.setUnselected();
        }
        public void mouseEntered(MouseEvent e) {
          drawingPane.setReady();
        }
        public void mousePressed(MouseEvent e) {;
          if (e.getClickCount()>1) selectAll();
          else{
            int y= e.getY(), x= e.getX();
            drawingPane.setSelected(x,y);
          }
        }
        public void mouseReleased(MouseEvent e){
          drawingPane.setUnselected();
        }
      });
      
      this.addMouseMotionListener(new MouseMotionAdapter(){
        public void mouseDragged(MouseEvent e) {
          int y= e.getY(), x= e.getX();
           drawingPane.draggedTo(x,y);
           repaint();
           updateParent();
        }
      });
      
      this.setPreferredSize(new Dimension(origBounds.width, origBounds.height));
      this.setVisible(true);
      
    }
    
    protected void updateParent(){
      this.parent.updateSelection(getSelection());
    }
    
    public void selectAll(){
      drawingPane.setMaxSelection(origBounds);
      updateParent();
    }
    
    public Rectangle2D.Double getSelection(){
      return drawingPane.getSelectionRatios();
    }
    
    private class DrawingPanel extends JPanel  {
      public Rectangle selection;
      protected Color inactiveColor, activeColor, selectionColor, strokeColor;
      protected BasicStroke myStroke;
      int selectionOrigX=-1,selectionOrigY=-1;
      int maxWidth,maxHeight;
      SelectionMode selectionMode=SelectionMode.NONE;
      MovePoint movePoint=MovePoint.NONE;
      static final int SMALLEST_VISIBLE_SELECTION=16;
      private int minSelectionWidth, minSelectionHeight;

      public DrawingPanel(){
        super();
        this.setOpaque(false);
        selection=new Rectangle(20,20,0,0);
        inactiveColor=new Color(255,255,255,64);
        activeColor=new Color(255,0,0,128);
        strokeColor=new Color(30,30,30,255);
        float[]dash=new float[2]; dash[0]=3;dash[1]=3;
        myStroke=new BasicStroke(3,0,0,3,dash,0);
        selectionColor=inactiveColor;
        minSelectionHeight=SMALLEST_VISIBLE_SELECTION;
        minSelectionWidth=SMALLEST_VISIBLE_SELECTION;
      }
      
      @Override
      public void paintComponent(Graphics g){
        Graphics2D myGraphics =(Graphics2D)(g.create());
        myGraphics.setColor(selectionColor);
        myGraphics.fill(selection);
        myGraphics.setStroke(myStroke);
        myGraphics.setColor(strokeColor);
        myGraphics.draw(selection);
        myGraphics.dispose();
      }
      
      public void setMaxSelection(Rectangle r){
        maxWidth=r.width;
        maxHeight=r.height;
        double maxratio=maxHeight/maxWidth;
        out.println("maxratio "+maxratio+"imageAspectRatio "+imageAspectRatio);
        if (lockAspectRatio && imageAspectRatio!=maxratio){
          if (imageAspectRatio>maxratio)maxWidth=(int)((double)maxHeight/imageAspectRatio);
          else maxHeight=(int)((double)maxWidth*imageAspectRatio);
        }
        selection=new Rectangle(0,0,maxWidth,maxHeight);
        repaint();
      }
			
      protected boolean selectionContains(int x, int y){
				return selection.contains(x,y);
			}
      
      protected void setSelected(int x, int y){
        setMode(x,y); //whether to resize or move and which edge(s) to move
        if(!(selectionMode==SelectionMode.NONE)){
          selectionColor=activeColor;
          selectionOrigX=x; selectionOrigY=y;
          repaint(selection);
        }
        else setUnselected();
      }
      protected void setReady(){
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
      protected void setUnselected(){
        selectionColor=inactiveColor;
        selectionOrigX=0; selectionOrigY=0;
        selectionMode=SelectionMode.NONE;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        repaint(selection);
      }
      
      //dont let selection get too small to see easily
      protected void setMinSelectionHeight(int h){minSelectionHeight=Math.max(h,16);}
      protected void setMinSelectionWidth(int w){minSelectionWidth=Math.max(w,16);}
      
      private void setMode(int x, int y){
        boolean nearLeft=(Math.abs(8*(x-selection.x))<selection.width);
        boolean nearRight=(Math.abs(8*(selection.x+selection.width-x))<selection.width);
        boolean nearTop=(Math.abs(8*(y-selection.y))<selection.height);
        boolean nearBottom=(Math.abs(8*(selection.y+selection.height-y))<selection.height);
        Cursor cursor=Cursor.getDefaultCursor();
        selectionMode=SelectionMode.RESIZE;
        if (nearLeft){
          if(nearTop){movePoint=MovePoint.TOP_LEFT;cursor=Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);}
          else if(nearBottom){movePoint=MovePoint.BOTTOM_LEFT;cursor=Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);}
          else if(!lockAspectRatio){movePoint=MovePoint.LEFT; cursor=Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);}
          else selectionMode=SelectionMode.NONE;
        }else if(nearRight){
          if(nearTop){movePoint=MovePoint.TOP_RIGHT;cursor=Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);}
          else if(nearBottom){movePoint=MovePoint.BOTTOM_RIGHT;cursor=Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);}
          else if(!lockAspectRatio){movePoint=MovePoint.RIGHT;cursor=Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);}
          else selectionMode=SelectionMode.NONE;
        }
        else if (nearBottom && !lockAspectRatio) {movePoint=MovePoint.BOTTOM;cursor=Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);}
        else if (nearTop && !lockAspectRatio) {movePoint=MovePoint.TOP;cursor=Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);}
        else if(selection.contains(x,y)){selectionMode=SelectionMode.MOVE; movePoint=MovePoint.NONE;cursor=Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);}
        else selectionMode=SelectionMode.NONE;
        setCursor(cursor);
      }
      
      protected void draggedTo(int x, int y){
        if (selectionMode==SelectionMode.NONE) return;
        if(!(origBounds.contains(x,y))) {setUnselected();return;}
        int deltaX=x-selectionOrigX, deltaY=y-selectionOrigY;
        if(lockAspectRatio && selectionMode==SelectionMode.RESIZE){
          if (movePoint==MovePoint.TOP_LEFT||movePoint==MovePoint.BOTTOM_RIGHT)deltaX=(int)((double)deltaY/imageAspectRatio);
          else deltaX=-(int)((double)deltaY/imageAspectRatio);
        }
        selectionOrigX=x; selectionOrigY=y;
        if (selectionMode==SelectionMode.RESIZE) resizeSelection(deltaX,deltaY);
        else moveSelection(deltaX,deltaY);
      }
      
      private void resizeSelection(int dx, int dy){
        Cursor cursor=getCursor();
        switch (cursor.getType()){
          case Cursor.E_RESIZE_CURSOR:
            if(!lockAspectRatio)updateSelection(dx,false,0,false);
            break;
          case Cursor.W_RESIZE_CURSOR:
            if(!lockAspectRatio)updateSelection(dx,true,0,false);
            break;
          case Cursor.N_RESIZE_CURSOR:
            if(!lockAspectRatio)updateSelection(0,false,dy,true);
            break;
          case Cursor.S_RESIZE_CURSOR:
            if(!lockAspectRatio)updateSelection(0,false,dy,false);
            break;
          case Cursor.NE_RESIZE_CURSOR:
            updateSelection(dx,false,dy,true);
            break;
          case Cursor.NW_RESIZE_CURSOR:
            updateSelection(dx,true,dy,true);
            break;
          case Cursor.SE_RESIZE_CURSOR:
            updateSelection(dx,false,dy,false);
            break;
          case Cursor.SW_RESIZE_CURSOR:
            updateSelection(dx,true,dy,false);
            break;
          default:
            break;
        }
      }
      
      private void updateSelection(int dx, boolean moveOriginX, int dy, boolean moveOriginY){
        Rectangle oldSelection = new Rectangle(selection);
        if (!(updateSelectionWidth(dx,moveOriginX) && updateSelectionHeight(dy, moveOriginY)))selection=new Rectangle(oldSelection);
      }
      
      private boolean updateSelectionWidth(int delta, boolean moveOrigin){
        if (delta==0) return true;
        int deltaW=moveOrigin ? -delta : delta;
        if (selection.width+deltaW>minSelectionWidth){
          selection.width+=deltaW;
          selection.x+=moveOrigin ? delta : 0;
          return (selection.x>=0 && (selection.x+selection.width)<=origBounds.width);
        }
        return false;
      }
      private boolean updateSelectionHeight(int delta, boolean moveOrigin){
        if (delta==0) return true;
        int deltaH=moveOrigin ? -delta : delta;
        if (selection.height+deltaH>minSelectionHeight){
          selection.height+=deltaH;
          selection.y+=moveOrigin ? delta : 0;
          return (selection.y>=0 && (selection.y+selection.height)<=origBounds.height);
        }
        return false;
      }
      private void moveSelection(int x, int y){
        int newX=selection.x+x;
        int newY=selection.y+y;
        if (newX>=0 & newX+selection.width<=origBounds.width)selection.x+=x;
        if (newY>=0 & newY+selection.height<=origBounds.height)selection.y+=y;
      }
            
      protected Rectangle2D.Double getSelectionRatios(){
        double mw=(double)origBounds.width;
        double mh=(double)origBounds.height;
        
        return new Rectangle2D.Double(((double)selection.x)/mw,((double)selection.y)/mh, ((double)selection.width)/mw, ((double)selection.height)/mh);
      }
    }
  }
}
