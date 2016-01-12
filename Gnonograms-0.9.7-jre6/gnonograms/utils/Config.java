/* Config class for Gnonograms-java
 * Manages persistent user options
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import java.lang.NullPointerException;
import static java.lang.System.out;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;

import java.util.Hashtable;
import java.util.Properties;
import java.util.InvalidPropertiesFormatException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.List;
import java.util.Arrays;


import gnonograms.app.Resource;

public class Config extends Properties {
    //private static final long serialVersionUID = 1;
    private String configDirectoryPath=System.getProperty("user.home")+"/.jpw";
    private String languageFilePath= "/res/i18n/supported_languages.xml";
    private String propertiesFilePath;
    private boolean valid=false;
    private Properties properties, defaultProperties, languages, defaultLanguages;
    private ResourceBundle rb;
     
    public Config(){
        boolean alreadyExists=false;
        createDefaultProperties();  
        createDefaultLanguages();
        propertiesFilePath=configDirectoryPath+"/gnonograms-java.conf";
        loadLanguages();
        
        try{ 
            File configDirectory=new File(configDirectoryPath);
            configDirectory.mkdirs();
            File propertiesFile=new File(propertiesFilePath);
            alreadyExists=!propertiesFile.createNewFile();
            }
        catch(IOException e){out.println("Problem creating properties file "+e.getMessage());}

        if(!alreadyExists||!loadProperties()){
            this.properties=new Properties(defaultProperties);
            valid=saveProperties();
        }
        else valid=true;
        makeResourceBundle(getLocale());
    }
    
    private void makeResourceBundle(String locale){
         rb=ResourceBundle.getBundle("res.i18n.GnonogramsText",new Locale(locale),new ResourceBundle.Control() {
         public List<String> getFormats(String baseName) {
             if (baseName == null)
                 throw new NullPointerException();
             return Arrays.asList("properties");
         }
         public ResourceBundle newBundle(String baseName,
                                         Locale locale,
                                         String format,
                                         ClassLoader loader,
                                         boolean reload)
                          throws IllegalAccessException,
                                 InstantiationException,
                                 IOException {
             if (baseName == null || locale == null
                   || format == null || loader == null)
                 throw new NullPointerException();
             ResourceBundle bundle = null;
             if (format.equals("properties")) {
                 String bundleName = toBundleName(baseName, locale);
                 String resourceName = toResourceName(bundleName, format);
                 InputStream stream = null;
                 if (reload) {
                     URL url = loader.getResource(resourceName);
                     if (url != null) {
                         URLConnection connection = url.openConnection();
                         if (connection != null) {
                             // Disable caches to get fresh data for
                             // reloading.
                             connection.setUseCaches(false);
                             stream = connection.getInputStream();
                         }
                     }
                 } else {
                     stream = loader.getResourceAsStream(resourceName);
                 }
                 if (stream != null) {
                     BufferedReader br=new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                     bundle = new PropertyResourceBundle(br);
                     br.close();
                 }
             }
             return bundle;
         }
        });
      if(rb==null){out.println("RB is null");rb=ResourceBundle.getBundle("res.i18n.GnonogramsText",new Locale(""));}
    }
    
    public ResourceBundle getResourceBundle(){return rb;};
    
    private void createDefaultProperties(){
        defaultProperties= new Properties();
        defaultProperties.setProperty("model.rows",String.valueOf(Resource.DEFAULT_ROWS));
        defaultProperties.setProperty("model.cols",String.valueOf(Resource.DEFAULT_COLS));
        defaultProperties.setProperty("model.grade",String.valueOf(Resource.DEFAULT_GRADE));
        defaultProperties.setProperty("model.startstate",String.valueOf(Resource.DEFAULT_STARTSTATE));
        defaultProperties.setProperty("model.minrowfreedomfactor","30.0");
        defaultProperties.setProperty("model.mincolumnfreedomfactor","30.0");
        defaultProperties.setProperty("model.maxblocksizefactor1","2.0");
        defaultProperties.setProperty("model.maxblocksizefactor2","3.0");
        defaultProperties.setProperty("view.pointsize","20");
        defaultProperties.setProperty("view.cluewidthmargin","4");
        if (((System.getProperty("os.name")).toUpperCase()).contains("LINUX")){
			defaultProperties.setProperty("view.cluelengthmargin","20");
		}
		else defaultProperties.setProperty("view.cluelengthmargin","40");	    
        defaultProperties.setProperty("system.puzzledirectory",System.getProperty("user.home"));
        defaultProperties.setProperty("system.imagedirectory",System.getProperty("user.home"));
        out.println("Default locale:"+(Locale.getDefault()).toString()+":");
        defaultProperties.setProperty("system.locale",(Locale.getDefault()).toString());
    }
    
    private void createDefaultLanguages(){
        defaultLanguages=new Properties();
        defaultLanguages.setProperty("en_us","American English");
        defaultLanguages.setProperty("en_gb","British English");
    }
    
    private int getInteger(String key){
        try{
            return Integer.valueOf(properties.getProperty(key));
        }
        catch (Exception e){return Integer.valueOf(defaultProperties.getProperty(key));}
    }
    private void setInteger(String key, int value){
        properties.setProperty(key,String.valueOf(value));
    }
    private double getDouble(String key){
        try{
            return Double.valueOf(properties.getProperty(key));
        }
        catch (Exception e){
			return Double.valueOf(defaultProperties.getProperty(key));
		}
    }
    private void setDouble(String key, double value){
        properties.setProperty(key,String.valueOf(value));
    }
    private String getString(String key){
        try{
            return properties.getProperty(key);
        }
        catch (Exception e){return defaultProperties.getProperty(key);}
    }
    private void setString(String key, String value){
        properties.setProperty(key,value);
    }
    
    public String getLocale(){
        return getString("system.locale");
    }
    public void setLocale(String locale){
        setString("system.locale",locale); //locale validated elsewhere
        makeResourceBundle(locale);
    }
    
    public String[] getLanguageKeys(){
        return (languages.stringPropertyNames()).toArray(new String[1]);
    }
    
    public int getRows(){
        int rows = getInteger("model.rows");
        if (rows<1||rows>Resource.MAXIMUM_GRID_SIZE) return 10;
        else return rows;
    }
    public void setRows(int value){setInteger("model.rows",value);}
        
    public int getCols(){
        int cols = getInteger("model.cols");
        if (cols<1||cols>Resource.MAXIMUM_GRID_SIZE) return 10;
        else return cols;
    }
    public void setCols(int value){setInteger("model.cols",value);}
    
    public double getGrade(){
        double grade = getInteger("model.grade");
        if (grade<1.0||grade>Resource.MAXIMUM_GRADE) return 10.0;
        else return grade;
    }
    public void setGrade(double value){setInteger("model.grade",(int)value);}
    
    public int getPointSize(){
        int ps = getInteger("view.pointsize");
        if (ps<Resource.MINIMUM_CLUE_POINTSIZE||ps>Resource.MAXIMUM_CLUE_POINTSIZE) return 10;
        else return ps;
    }
    public void setPointSize(int value){setInteger("view.pointsize",value);}
    
    public int getClueWidthMargin(){return getInteger("view.cluewidthmargin");}
    public void setClueWidthMargin(int value){setInteger("view.cluewidthmargin",value);}
    
    public int getClueLengthMargin(){return getInteger("view.cluelengthmargin");}
    public void setClueLengthMargin(int value){setInteger("view.cluelengthmargin",value);}
    
    public double getMinColumnFreedomFactor(){return getDouble("model.mincolumnfreedomfactor");}
    public void setMinColumnFreedomFactor(double value){setDouble("model.mincolumnfreedomfactor",value);}
    
    public double getMinRowFreedomFactor(){return getDouble("model.minrowfreedomfactor");}
    public void setMinRowFreedomFactor(double value){setDouble("model.minrowfreedomfactor",value);}
    
    public double getMaxBlockSizeFactor1(){return getDouble("model.maxblocksizefactor1");}
    public void setMaxBlockSizeFactor1(double value){setDouble("model.maxblocksizefactor1",value);}
    
    public double getMaxBlockSizeFactor2(){return getDouble("model.maxblocksizefactor2");}
    public void setMaxBlockSizeFactor2(double value){setDouble("model.maxblocksizefactor2",value);}
    
    public String getPuzzleDirectory(){return getString("system.puzzledirectory");}
    public void setPuzzleDirectory(String value){setString("system.puzzledirectory",value);}
    
    public String getImageDirectory(){return getString("system.imagedirectory");}
    public void setImageDirectory(String value){setString("system.imagedirectory",value);}
    
    public int getStartState(){return getInteger("model.startstate");}
    public void setStartState(int state){setInteger("model.startstate",state);}
    
    public boolean saveProperties(){
        try{
            FileOutputStream fos = new FileOutputStream(propertiesFilePath);
            properties.storeToXML(fos, null);
        }
        catch(IOException e){out.println("Problem saving properties file "+e.getMessage()); return false;}
        return true;
    }
    
    public boolean loadProperties(){
        try{
            properties=new Properties(defaultProperties);
            properties.loadFromXML(new FileInputStream(propertiesFilePath));
        }
        catch(Exception e){
            String msg="";
            if (e instanceof NullPointerException) msg="Null properties file";
            if (e instanceof InvalidPropertiesFormatException) msg="Invalid format in properties file";
            if (e instanceof IOException) msg="Cannot load properties file";
            out.println(msg+"-creating default."); 
            return false;
            }
        return true;
    }
    public void loadLanguages(){
        try{
            languages=new Properties(defaultLanguages);
            languages.loadFromXML(Config.class.getResourceAsStream(languageFilePath));
        }
        catch(Exception e){
            String msg="";
            if (e instanceof NullPointerException) msg="Null properties file";
            if (e instanceof InvalidPropertiesFormatException) msg="Invalid format in properties file";
            if (e instanceof IOException) msg="Cannot load languages file"+languageFilePath;
            out.println(msg+" - using default."); 
            }
    }
    
    public boolean editPreferences(JFrame owner){
        ConfigDialog dialog = new ConfigDialog(
										owner,getRows(),
										getCols(), 
										(int)getGrade(), 
										getPointSize(), 
										getStartState(), 
										getLocale(),
										getClueWidthMargin(),
										getClueLengthMargin(),
										getMinRowFreedomFactor(),
										getMinColumnFreedomFactor(),
										getMaxBlockSizeFactor1(),
										getMaxBlockSizeFactor2()
										);
        dialog.setLocationRelativeTo((Component)owner);
        dialog.setVisible(true);
        boolean cancelled=dialog.wasCancelled;
        if (!cancelled){
            setRows(dialog.getRows());
            setCols(dialog.getCols());
            setGrade(dialog.getGrade());
            setPointSize(dialog.getPointSize());
            setStartState(dialog.getStartState());
            setLocale(dialog.getNewLocale());
            setClueWidthMargin(dialog.getClueWidthMargin());
            setClueLengthMargin(dialog.getClueLengthMargin());
            setMinRowFreedomFactor(dialog.getMinRowFreedomFactor());
            setMinColumnFreedomFactor(dialog.getMinColumnFreedomFactor());
            setMaxBlockSizeFactor1(dialog.getMaxBlockSizeFactor1());
            setMaxBlockSizeFactor2(dialog.getMaxBlockSizeFactor2());
            saveProperties();
        }
        dialog.dispose();
        return !cancelled;
    }
    
    private class ConfigDialog extends JDialog implements ActionListener {
        //private static final long serialVersionUID = 1;
        private JSpinner rowSpinner,columnSpinner, pointsizeSpinner;
        private JSpinner widthMarginSpinner,lengthMarginSpinner;
        private JSpinner minColumnFreedomFactorSpinner,minRowFreedomFactorSpinner;
        private JSpinner maxBlockSizeFactor1Spinner,maxBlockSizeFactor2Spinner;
        private JSlider gradeSlider;
        private JRadioButton settingButton, solvingButton, loadingButton;
        private JComboBox locales;
        private JButton advancedButton;
        private String currentlocale;
        private String[] localeKeys;
        private JPanel advancedPanel;

        public boolean wasCancelled=false;
        
        public ConfigDialog(
			JFrame owner, 
			int rows, 
			int cols, 
			int grade, 
			int pointsize, 
			int startstate, 
			String currentlocale,
			int clueWidthMargin,
			int clueLengthMargin,
			double minRowFreedomFactor,
			double minColumnFreedomFactor,
			double maxBlockSizeFactor1,
			double maxBlockSizeFactor2)
			{
            super(owner,"Preferences",true);
            this.currentlocale=currentlocale;
            this.setLayout(new BorderLayout());
            this.add(createInfoPane(
									rows,
									cols,
									grade,
									pointsize,
									startstate,
									currentlocale,
									clueWidthMargin,
									clueLengthMargin,
									minRowFreedomFactor,
									minColumnFreedomFactor,
									maxBlockSizeFactor1,
									maxBlockSizeFactor2),BorderLayout.PAGE_START);
            this.add(Utils.okCancelPanelFactory(this,"INFO_OK"),BorderLayout.PAGE_END);
            this.pack();
        }
        
        private JPanel createInfoPane(
								int rows,
								int cols, 
								int grade, 
								int pointsize, 
								int startstate, 
								String currentlocale,
								int clueWidthMargin,
								int clueLengthMargin,
								double minRowFreedomFactor,
								double minColumnFreedomFactor,
								double maxBlockSizeFactor1,
								double maxBlockSizeFactor2
								){
            JPanel infoPane=new JPanel(new GridBagLayout());
            GridBagConstraints c=new GridBagConstraints();
            c.gridx=0; c.gridy=0;
            c.gridwidth=1; c.gridheight=1;
            c.weightx=0; c.weighty=0;
            c.ipadx=48; c.ipady=12;
            c.fill=GridBagConstraints.NONE;
            c.anchor=GridBagConstraints.LINE_START;
            JLabel tmpLabel=new JLabel(Utils.createImageIcon("resize-rows48.png","resizeRowIcon"));
            tmpLabel.setToolTipText(rb.getString("Set number of rows"));
            infoPane.add(tmpLabel,c);

            c.gridy=1;
            tmpLabel=new JLabel(Utils.createImageIcon("resize-columns48.png","resizeColumnIcon"));
            tmpLabel.setToolTipText(rb.getString("Set number of columns"));
            infoPane.add(tmpLabel,c);

            c.gridy=2;
            tmpLabel=new JLabel(Utils.createImageIcon("resize-font48.png","resizeFontIcon"));
            tmpLabel.setToolTipText(rb.getString("Set size of font"));
            infoPane.add(tmpLabel,c);

            c.gridy=3;
            tmpLabel=new JLabel(Utils.createImageIcon("question48.png","difficultyIcon"));
            tmpLabel.setToolTipText(rb.getString("Set difficulty of puzzles"));
            infoPane.add(tmpLabel,c);
            
            c.gridy=4;
            tmpLabel=new JLabel(Utils.createImageIcon("start48.png","startIcon"));
            tmpLabel.setToolTipText(rb.getString("Set state on startup"));
            infoPane.add(tmpLabel,c);
            
            c.gridy=5;
            tmpLabel=new JLabel(Utils.createImageIcon("international48.png","WorldIcon"));
            tmpLabel.setToolTipText(rb.getString("Language"));
            infoPane.add(tmpLabel,c);
            
            c.gridy=6; 
            c.fill=GridBagConstraints.NONE;
            advancedButton=new JButton("....");
            advancedButton.setPreferredSize(new Dimension(36,18));
            advancedButton.setActionCommand("advanced");
            advancedButton.addActionListener(this);
            infoPane.add(advancedButton,c);
            
            c.gridy=7; c.gridwidth=2;
            advancedPanel=createAdvancedPanel(
											clueWidthMargin,
											clueLengthMargin,
											minRowFreedomFactor,
											minColumnFreedomFactor,
											maxBlockSizeFactor1,
											maxBlockSizeFactor2);
            advancedPanel.setVisible(false);
            infoPane.add(advancedPanel,c);
            
            
            rowSpinner=new JSpinner(new SpinnerNumberModel(rows,1,Resource.MAXIMUM_GRID_SIZE,1));
            columnSpinner=new JSpinner(new SpinnerNumberModel(cols,1,Resource.MAXIMUM_GRID_SIZE,1));
            pointsizeSpinner=new JSpinner(new SpinnerNumberModel(pointsize,Resource.MINIMUM_CLUE_POINTSIZE,Resource.MAXIMUM_CLUE_POINTSIZE,1));

            int max=(int)Resource.MAXIMUM_GRADE;
            gradeSlider=new JSlider(1, max, grade );
            Hashtable<Integer , JLabel> gradeSliderLabels = new Hashtable<Integer , JLabel>();
            gradeSliderLabels.put(1,new JLabel(Utils.createImageIcon("face-smile-big.png","easyIcon")));
            gradeSliderLabels.put(max/2,new JLabel(Utils.createImageIcon("face-plain.png","mediumIcon")));
            gradeSliderLabels.put(max,new JLabel(Utils.createImageIcon("face-uncertain.png","hardIcon")));
            
            gradeSlider.setLabelTable(gradeSliderLabels);
            gradeSlider.setPaintLabels(true);
            gradeSlider.setPaintTrack(true);
            gradeSlider.setPaintTicks(false);
            gradeSlider.setBorder(BorderFactory.createEtchedBorder());

            ButtonGroup stateButtons= new ButtonGroup();
            solvingButton = new JRadioButton();
            JLabel solvingLabel = new JLabel(Utils.createImageIcon("dice.png","randomIcon"));
            solvingLabel.setToolTipText(rb.getString("Start with random puzzle"));
            settingButton = new JRadioButton();
            JLabel settingLabel = new JLabel(Utils.createImageIcon("New24.gif","newIcon"));
            settingLabel.setToolTipText(rb.getString("Start in design mode"));
            loadingButton = new JRadioButton();
            JLabel loadingLabel = new JLabel(Utils.createImageIcon("Open24.gif","loadIcon"));
            loadingLabel.setToolTipText(rb.getString("Start in file chooser"));
            stateButtons.add(solvingButton);
            stateButtons.add(settingButton);
            stateButtons.add(loadingButton);
            switch (startstate) {
                case Resource.GAME_STATE_SOLVING:
                    solvingButton.setSelected(true);
                    break;
                case Resource.GAME_STATE_SETTING:
                    settingButton.setSelected(true);
                    break;
                case Resource.GAME_STATE_LOADING:
                    loadingButton.setSelected(true);
                    break;
                default:
                    solvingButton.setSelected(true);
                    break;
            }
            
            JPanel radioPane=new JPanel(new GridLayout(1,0));
            radioPane.add(solvingLabel);
            radioPane.add(solvingButton);
            radioPane.add(settingLabel);
            radioPane.add(settingButton);
            radioPane.add(loadingLabel);
            radioPane.add(loadingButton);
            radioPane.setBorder(BorderFactory.createEtchedBorder());
            
            localeKeys=getLanguageKeys();
            String[] localeNames=new String[localeKeys.length];
            for(int i=0;i<localeKeys.length;i++){
                localeNames[i]=languages.getProperty(localeKeys[i]);
            }
            locales=new JComboBox(localeNames);
            locales.setSelectedItem(languages.getProperty(currentlocale));
            
            c.weightx=1;
            c.anchor=GridBagConstraints.LINE_START;
            c.fill=GridBagConstraints.NONE;
            c.gridx=1; 
            c.gridy=0;
            c.ipadx=6; c.ipady=6;
            
            infoPane.add(rowSpinner,c);
            c.gridy=1;
            infoPane.add(columnSpinner,c);
            c.gridy=2;
            infoPane.add(pointsizeSpinner,c);
            c.gridy=3;
            c.fill=GridBagConstraints.HORIZONTAL;
            infoPane.add(gradeSlider,c);
            c.gridy=4;
            infoPane.add(radioPane,c);      
            c.gridy=5;
            infoPane.add(locales,c);
            
            infoPane.setBorder(BorderFactory.createEtchedBorder());
            return infoPane;
        }
        
        private JPanel createAdvancedPanel(
										int clueWidthMargin, 
										int clueLengthMargin,
										double minRowFreedomFactor,
										double minColumnFreedomFactor,
										double maxBlockSizeFactor1,
										double maxBlockSizeFactor2){
			JPanel ap=new JPanel(new GridBagLayout());
			GridBagConstraints c=new GridBagConstraints();
            c.gridx=0; c.gridy=0;
            c.gridwidth=1; c.gridheight=1;
            c.weightx=0; c.weighty=0;
            c.anchor=GridBagConstraints.LINE_START;
            
            JLabel tmpLabel=new JLabel("Clue width margin");
            ap.add(tmpLabel,c);
            c.gridy=1;
            tmpLabel=new JLabel("Clue height margin");
            ap.add(tmpLabel,c);
            c.gridy=2;
            tmpLabel=new JLabel("Min row freedom factor");
            ap.add(tmpLabel,c);
            c.gridy=3;
            tmpLabel=new JLabel("Min column freedom factor");
            ap.add(tmpLabel,c);
            c.gridy=4;
            tmpLabel=new JLabel("Max block length factor 1");
            ap.add(tmpLabel,c);
            c.gridy=5;
            tmpLabel=new JLabel("Max block length factor 1");
            ap.add(tmpLabel,c);
            
            widthMarginSpinner=new JSpinner(
									new SpinnerNumberModel(
										clueWidthMargin,
										1,
										Resource.MAXIMUM_CLUE_WIDTH_MARGIN,
										1));
            lengthMarginSpinner=new JSpinner(
										new SpinnerNumberModel(
										clueLengthMargin,
										1,
										Resource.MAXIMUM_CLUE_LENGTH_MARGIN,
										1));
            minRowFreedomFactorSpinner=new JSpinner(
										new SpinnerNumberModel(
										minRowFreedomFactor,
										1,
										Resource.MAXIMUM_ROW_FREEDOM_FACTOR,
										1));
            minColumnFreedomFactorSpinner=new JSpinner(
										new SpinnerNumberModel(
										minColumnFreedomFactor,
										1,
										Resource.MAXIMUM_COL_FREEDOM_FACTOR,
										1));
            maxBlockSizeFactor1Spinner=new JSpinner(
										new SpinnerNumberModel(
										maxBlockSizeFactor1,
										1,
										Resource.MAXIMUM_BLOCKSIZEFACTOR1,
										1));
            maxBlockSizeFactor2Spinner=new JSpinner(
										new SpinnerNumberModel(
										maxBlockSizeFactor2,
										1,
										Resource.MAXIMUM_BLOCKSIZEFACTOR2,
										1));
            
            c.weightx=1;
            c.anchor=GridBagConstraints.LINE_START;
            c.fill=GridBagConstraints.NONE;
            c.gridx=1; 
            c.gridy=0;
            c.ipadx=6; c.ipady=6;
            
            ap.add(widthMarginSpinner,c);
            c.gridy=1;
            ap.add(lengthMarginSpinner,c);
            c.gridy=2;
            ap.add(minRowFreedomFactorSpinner,c);
            c.gridy=3;
            ap.add(minColumnFreedomFactorSpinner,c);
            c.gridy=4;
            ap.add(maxBlockSizeFactor1Spinner,c);
            c.gridy=5;
            ap.add(maxBlockSizeFactor2Spinner,c);
            
            return ap;          
		}
        
        public void actionPerformed(ActionEvent a){
            String command=a.getActionCommand();
            if (command.equals("advanced")) {
				advancedPanel.setVisible(!advancedPanel.isVisible());
				this.pack();
			}
			else{
				wasCancelled=!(command.equals("INFO_OK"));
				this.setVisible(false);
			}
        }
        
        protected int getRows(){return spinnerValueToInt(rowSpinner.getValue());}
        protected int getCols(){return spinnerValueToInt(columnSpinner.getValue());}
        protected int getGrade(){return gradeSlider.getValue();}
        protected int getPointSize(){return spinnerValueToInt(pointsizeSpinner.getValue());}
        protected int getStartState(){
            if (loadingButton.isSelected()) return Resource.GAME_STATE_LOADING;
            if (settingButton.isSelected()) return Resource.GAME_STATE_SETTING;
            return Resource.GAME_STATE_SOLVING;
        }
        protected String getNewLocale(){
            int selectedIndex=locales.getSelectedIndex();
             if (selectedIndex<0) return currentlocale;
            return localeKeys[selectedIndex];
        }
        protected int getClueWidthMargin(){return spinnerValueToInt(widthMarginSpinner.getValue());}
        protected int getClueLengthMargin(){return spinnerValueToInt(lengthMarginSpinner.getValue());}
        protected double getMinRowFreedomFactor(){return spinnerValueToDouble(minRowFreedomFactorSpinner.getValue());}
        protected double getMinColumnFreedomFactor(){return spinnerValueToDouble(minColumnFreedomFactorSpinner.getValue());}
        protected double getMaxBlockSizeFactor1(){return spinnerValueToDouble(maxBlockSizeFactor1Spinner.getValue());}
        protected double getMaxBlockSizeFactor2(){return spinnerValueToDouble(maxBlockSizeFactor2Spinner.getValue());}
        
        private int spinnerValueToInt(Object o){
            return ((Integer)o).intValue();
        }
        private double spinnerValueToDouble(Object o){
            return ((Double)o).doubleValue();
        }
    }
}
