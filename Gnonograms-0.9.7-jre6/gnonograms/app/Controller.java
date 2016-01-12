/* Controller class for Gnonograms-java
 * Overall coordination of view, model and solver
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

package gnonograms.app;

import static java.lang.System.out;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

import gnonograms.utils.*;
import gnonograms.app.Viewer;
import gnonograms.app.gui.ImageImporter;


public class Controller {

  protected Viewer view;
  protected Model model;
  private Solver solver;
  private MoveList history;
  private Config config;
  private int rows, cols;
  public Cell markedCell;
  public boolean isSolving;
  private boolean validSolution;
  private boolean debug;
  private Date startDate, endDate;
  private ResourceBundle rb;
  private String myLocale;

  public Controller() {
    debug=false;
    config=new Config();
    myLocale=config.getLocale();
    setLocale();
    model=new Model();
    solver=new Solver(false,debug,false,0,this,rb);

    history=new MoveList();
    markedCell=new Cell(-1,-1,Resource.CELLSTATE_UNKNOWN);
    init(config.getRows(),config.getCols());
    int startState=config.getStartState();
    switch (startState){
      case Resource.GAME_STATE_SETTING:
            setSolving(false);
            break;
      case Resource.GAME_STATE_SOLVING:
            randomGame();
            //setSolving(true);
            break;
      case Resource.GAME_STATE_LOADING:
            loadGame();
            break;
      default :
            setSolving(false);
    }
    this.setMarginsAndFactors();
    view.setClueFontAndSize(config.getPointSize());
    view.setVisible(true);
  }
  
  private void setLocale(){
    rb=config.getResourceBundle();
    view=new Viewer(this,rb);
  }

  public void init(int r, int c){
    this.rows=r; this.cols=c;
    model.setDimensions(r,c);
    solver.setDimensions(r,c);
    view.setDimensions(r,c);
    view.setClueFontAndSize(calculateCluePointSize(r,c));
    newGame();
  }

  public void resize(int r, int c){ init(r,c); }
  
  public void zoomFont(int change){
    view.zoomFont(change);
    config.setPointSize(view.getPointSize());
  }

  public int getDataFromRC(int r, int c){return model.getDataFromRC(r,c);}

  public void setDataFromCell(Cell c){
    history.recordMove(c,model.getDataFromRC(c.getRow(),c.getColumn()));
    model.setDataFromCell(c);
    view.redrawGrid();
    if(isSolving && model.countUnknownCells()==0 && checkSolved()){
      endDate=new Date();
      view.setTime(Utils.calculateTimeTaken(startDate, endDate));
      setSolving(false);
      view.redrawGrid();
    }
    else updateLabelsFromModel(c.row,c.col,true);
  }
  
  public void quit(){config.saveProperties();}  
  
  public void editPreferences(){
    if (config.editPreferences(view)){
      int r=config.getRows(),c=config.getCols();
      this.setMarginsAndFactors();
      String locale = config.getLocale();
      if (!locale.equals(myLocale)){
        if (!isSolving || 
          Utils.showConfirmDialog(rb.getString(
            "Changing the locale will start a new game - Continue?"
          ))){
          view.setVisible(false);
          setSolving(false);
          view.dispose();
          myLocale=locale;
          setLocale();
          init(r,c);
          view.setVisible(true);
          newGame();
        }
      }
      else if (rows!=r || cols!=c){
        if (!isSolving || 
          Utils.showConfirmDialog(rb.getString(
            "Changing the size will start a new game - Continue?"
          ))){
          setSolving(false); 
          resize(r,c);
          newGame();
        }
      }
      view.setClueFontAndSize(config.getPointSize());
		} 
  }

  private void setMarginsAndFactors(){
      view.setMargins(
                      config.getClueWidthMargin(),
                      config.getClueLengthMargin()
                      );
      model.setFactors(
                      config.getMinRowFreedomFactor(),
                      config.getMinColumnFreedomFactor(),
                      config.getMaxBlockSizeFactor1(),
                      config.getMaxBlockSizeFactor2()
                      );
  }

  public void newGame(){
    model.clear();
    history.initialize();
    validSolution=true; //solution grid corresponds to clues
    updateAllLabelText();
    view.clearInfoBar();
    view.redrawGrid();
    view.validate();
  }
  
  public void createGame(){
    setSolving(false);
    newGame();
  }
  public void restartGame(){
    model.blankWorking();
    setSolving(isSolving);
    if(isSolving)initialiseSolving();
  }
  
  public void checkGame(){
    if(!isSolving) return;
    int numberOfErrors=this.countErrors();
    if (numberOfErrors==0)
      Utils.showInfoDialog(rb.getString("There are no errors"));
    else if (Utils.showConfirmDialog(rb.getString(
                "Number of errors")+"  " +numberOfErrors+"\n\n"+
                rb.getString("Go back to last correct position?"
            )))rewindGame();
  }
  
  public int countErrors(){
      return model.countErrors()-model.countUnknownCells();
  }

  private void rewindGame(){
    if (!validSolution)return;
    while (model.countErrors()-model.countUnknownCells()>0){
      if(undoMove()==null) break;
    }
    view.redrawGrid();
  }
  
  public void importImage(){
    ImageImporter ii=new ImageImporter(view, rb, config.getImageDirectory(),this.rows, this.cols);
    
    if (ii.hasImage){
      config.setImageDirectory(ii.getImagePath());
      ii.setVisible(true);
      if(!ii.wasCancelled){
        setSolving(false); //ensure clues updated etc
        this.resize(ii.getRows(), ii.getCols());

        view.setName(ii.getImageName());
        view.setAuthor(rb.getString("Image"));
        view.setCreationDate(rb.getString("Today"));
        view.setScore("");
        view.setLicense("");
        
        model.useSolution();
        for (int i=0; i<this.rows; i++) model.setRowDataFromArray(i,ii.getRow(i));
        updateAllLabelsFromModel(); 
        this.validSolution=true;
      }
    }
    ii.dispose();
  }

  public void loadGame(){
    GameLoader gl=new GameLoader(view, config.getPuzzleDirectory());
    int result=gl.getResult();
    if (result>0) return; //User cancelled

    config.setPuzzleDirectory((gl.getCurrentDirectory()).getPath());

    try {gl.openDataInputStream();} //can chosen file be opened?
    catch (java.io.FileNotFoundException e){
      Utils.showErrorDialog(e.getMessage()); 
      return;
    }

    try{gl.parseGameFile();} //is it a valid gnonogram puzzle?
    catch (java.util.NoSuchElementException e) {
      Utils.showErrorDialog(e.getMessage()); 
      gl.close();
      return;
    }
    catch (Exception e) {
      Utils.showErrorDialog(rb.getString(rb.getString("Exception"))+" - "+e.getMessage());
      gl.close();
      return;
    }

    if (!gl.validGame) {
      Utils.showErrorDialog(rb.getString("Not a valid game file"));
      gl.close(); 
      return;
    }
    
    setSolving(false); //ensure clues updated etc
    this.resize(gl.rows,gl.cols);

    view.setName(gl.name);
    view.setAuthor(gl.author);
    view.setCreationDate(gl.date);
    view.setScore(gl.score);
    view.setLicense(gl.license);

    if (gl.hasSolution){
      model.useSolution();
      for (int i=0; i<this.rows; i++) model.setRowDataFromString(i,gl.solution[i]);
      updateAllLabelsFromModel(); 
      this.validSolution=true;
    }
    else{
      //Valid games either have Solution or Clues (or both)
      for (int i=0; i<this.rows; i++) view.setClueText(i,gl.rowClues[i],false,false);
      for (int i=0; i<this.cols; i++) view.setClueText(i,gl.colClues[i],true,false);
      view.repack();
      if (!checkCluesValid()) {
        setSolving(false);
        gl.close();
        return;
    } } 
    
    if (gl.hasWorking){
      model.useWorking();
      for (int i=0; i<this.rows; i++){
        model.setRowDataFromString(i,gl.working[i]);
    } }
    
    setSolving(true); //always start in solving mode to avoid displaying solution
    initialiseSolving();
    gl.close();
  }
  
  private int calculateCluePointSize(int r, int c){
    int pointSize=(4*Resource.MAXIMUM_CLUE_POINTSIZE)/(Math.max(r,c));
    if(pointSize<Resource.MINIMUM_CLUE_POINTSIZE)pointSize=Resource.MINIMUM_CLUE_POINTSIZE;
    if(pointSize>Resource.MAXIMUM_CLUE_POINTSIZE)pointSize=Resource.MAXIMUM_CLUE_POINTSIZE; 
    return pointSize;
  }
  
  public void saveGame(){
    GameSaver gs=new GameSaver(view, config.getPuzzleDirectory(), view.getName());
    if (gs.getResult()>0) return;

    config.setPuzzleDirectory((gs.getCurrentDirectory()).getPath());

    try {gs.openDataOutputStream();}
    catch (IOException e){out.println("Error while saving game file: "+e.getMessage());return;}

    try {
      gs.writeDescription(view.getName(), view.getAuthor(), view.getCreationDate(), view.getScore());
      gs.writeLicense(view.getLicense());
      gs.writeDimensions(rows,cols);
      gs.writeClues(view.getClues(false),false);
      gs.writeClues(view.getClues(true),true);

      if(validSolution){
        model.useSolution();
        gs.writeSolution(model.displayDataToString());
      }

      model.useWorking();
      gs.writeWorking(model.displayDataToString());
      gs.writeState(isSolving);
    }
    catch (IOException e) {out.println("Error while writing game file: "+e.getMessage());}
    try {gs.close();}
    catch (IOException e){out.println("Error closing file:"+e.getMessage());}
    setSolving(isSolving);
  }

  public void randomGame(){
    double grade=config.getGrade();
    setSolving(false); //avoid displaying trial clues while generating game
    newGame();
    int passes=generatePuzzle(grade);
    if (passes<0) Utils.showWarningDialog(rb.getString("Failed to generate puzzle - error in solver"));
    else{
      model.useSolution();
      updateAllLabelText(); //from solution
      setSolving(true);
      initialiseSolving();
      validSolution=true;
      view.setScore(passes+" ");
      view.setName(rb.getString("Random"));
      view.setAuthor(rb.getString("Computer"));
      view.setLicense(rb.getString("GPL"));
      view.setCreationDate(rb.getString("Today"));
    }
  }

  private int generatePuzzle(double startingGrade){
    //Try to generate a solvable pattern, reducing grade if necessary
    int passes=-1, count=0, limit;
    double grade=startingGrade+1;
    int maxGuesswork;
    maxGuesswork=0;
    if(startingGrade>Resource.GRADE_FOR_ONE_GUESS)maxGuesswork=1;   
    if(startingGrade>Resource.GRADE_FOR_TWO_GUESSES)maxGuesswork=2;
    while (passes<=grade-3||passes>9999){
      grade--;
      //out.println("Using grade "+grade+"\n");     
      count=0; limit=(int)(500+20*grade); 
      //for higher grades generate puzzles requiring advanced logic
      model.setGrade(grade);
      while (count<limit){
        count++;
        model.generateRandomPattern();
        prepareToSolve(false,false,false); //get clues straight from model, no startgrid, no solutiongrid
        passes=solver.solveIt(false,maxGuesswork,false, true); //not debug, not stepwise, unique solutions only
        if (passes>grade-3 && passes<99999) break;
      }
    }
    return passes;
  }

  public void userSolveGame(){
    //user asks computer to solve puzzle
    boolean useSolution=false;
    boolean useExistingGrid=true, debug=false, stepwise=false;
    if (isSolving==false){
      setSolving(true);
      restartGame();
    }
    prepareToSolve(true,useExistingGrid, rows==1 ? false : useSolution); //clues from labels, use existing working grid as start point
    startDate=new Date();
    int result=solveGame(debug, Resource.MAXGUESSWORK_FOR_SOLVER, stepwise);
    endDate=new Date();
    updateWorkingGridFromSolver();
    view.setTime(Utils.calculateTimeTaken(startDate,endDate));
    view.zoomFont(0);//may need to resize window to show full info bar.
    setSolving(true); //redisplay working grid
  }
  
  public int solveGame(boolean debug, int maxGuesswork, boolean stepwise){
    int passes=solver.solveIt(	debug, //debug
								maxGuesswork, //max number of critical guesses to solve
								stepwise, //single step mode
								false); //only unique solutions (not currently used)
    view.setScore("999999");
    String message="";
    switch (passes) {
      case -2://debug mode
	  case 999999: //user cancelled
        break;
      case -1:  //invalid clues;
        validSolution=false;
        message="Invalid or inconsistent clues - no solution";
        break;
      case 0: //solver failed
        break;
      default: //solver succeeded
        view.setScore(String.valueOf(passes));
        if(!validSolution){ //don't overwrite existing solution
			updateSolutionGridFromSolver();
			validSolution=true;
		}
        break;
    }
    if (message.length()>0) Utils.showInfoDialog(message);
    return passes;
  }
  
  public void hint(){
    if (!isSolving) return;
    //Hint disabled when using marked cell
    if (markedCell.getState()!=Resource.CELLSTATE_UNDEFINED) return;
    //Only hint of no errors have been made
    if (countErrors()>0){
        checkGame();
        return;
    }
    prepareToSolve(true,true,false);
    solver.getHint();
    view.redrawGrid();
  } 
  
  public void markCell(int r, int c){
    if (r<0||r>=rows||c<0||c>=cols) return;
    int cs=model.getDataFromRC(r,c);
    if(cs==Resource.CELLSTATE_UNKNOWN)return;
    if(markedCell.row==r && markedCell.col==c)markedCell.clear();
    else markedCell=new Cell(r,c,cs);
    view.enableCheckButton(false);
    view.redrawGrid();
  }  

  public void rewindToMarkedCell(){
    int r=markedCell.row, c=markedCell.col;
    Move lm=undoMove();
    markedCell.clear();
    view.enableCheckButton(true);
    if (r<0||lm==null) {view.redrawGrid();return;}
    while (!(lm.row==r && lm.col==c)){lm=undoMove();}
  }
  
  private boolean prepareToSolve(boolean useLabels, boolean useStartgrid, boolean useSolution){
    String[] rowClues= new String[this.rows], columnClues= new String[this.cols];
    My2DCellArray solution=null;
    if (useLabels){//get clues from labels
      for (int i =0; i<this.rows; i++) rowClues[i]=view.getClueText(i,false);
      for (int i =0; i<this.cols; i++) columnClues[i]=view.getClueText(i, true);
    }
    else{//get clues from model
      for (int i =0; i<this.rows; i++) rowClues[i]=Utils.clueFromIntArray(model.getRow(i));
      for (int i =0; i<this.cols; i++) columnClues[i]=Utils.clueFromIntArray(model.getColumn(i));
    }
    if (useSolution){ //used during development to locate point at which solver fails
      out.println("Solver using solution\n");
      model.useSolution();
      solution = model.getCellDataArray();
      model.useWorking();
    }
    solver.initialize(rowClues, columnClues, useStartgrid ? model.getCellDataArray() : null, useSolution ? solution : null);
	return solver.valid();
  }

  public boolean checkCluesValid(){
		//false if solver returns an error
		out.print("Check valid");
		validSolution=false;
		model.blankWorking();
		if(prepareToSolve(true,false,false)){// clues from labels, no start grid, do not use solution
			validSolution=(solveGame(false,0,false)>=0); //no debug, no guessing
		}
		else validSolution=false;
		if (!validSolution) {
			model.errorSolution();
			Utils.showErrorDialog("Clues are not valid");
		}
		setSolving(isSolving);
		return validSolution;
	}
  
  public void updateWorkingGridFromSolver(){
    model.useWorking();
    setDisplayGridFromSolver();
    }
  public void updateSolutionGridFromSolver(){
	  out.println("Update solution from solver");
    model.useSolution();
    setDisplayGridFromSolver();
    }
  private void setDisplayGridFromSolver() {
    for (int r=0; r<this.rows; r++) {
      for(int c=0; c<this.cols; c++) {
        model.setDataFromCell(solver.getCell(r,c));
  } } }

  public void highlightLabels(int row, int col, boolean on){
      view.highlightLabels(row, col, on);
  }
  
  private void updateAllLabelText(){
    updateAllLabelsFromModel();
  }
   public void updateLabelsFromModel(int r, int c, boolean repackAfter){
    if (isSolving) return;
    view.setClueText(r, Utils.clueFromIntArray(model.getRow(r)),false,true);
    view.setClueText(c, Utils.clueFromIntArray(model.getColumn(c)),true,true);
  }
  
  public void updateAllLabelsFromModel(){
    if (isSolving) return;
    for (int r=0;r<rows;r++) view.setClueText(r, Utils.clueFromIntArray(model.getRow(r)),false,false);
    for(int c=0;c<cols;c++) view.setClueText(c, Utils.clueFromIntArray(model.getColumn(c)),true,false);
    view.repack();
  }
 
  private boolean checkSolved(){
    if (checkRows() && checkColumns()){
      if (model.countErrors()>0){
        Utils.showInfoDialog(rb.getString("Congratulations")+" - "+rb.getString("you have found an alternative solution"));
      }
      else{
        Utils.showInfoDialog(rb.getString("Congratulations")+" - "+rb.getString("you have solved the puzzle"));
      }
      return true;
    }
    Utils.showInfoDialog(rb.getString("Sorry - you have made a mistake"));
    return false;
  }
  
  private boolean checkRows(){
    for (int r=0;r<rows;r++){
      if (!((view.getClueText(r,false)).equals(Utils.clueFromIntArray(model.getRow(r))))) return false;
    }
    return true;
  }
  private boolean checkColumns(){
    for (int c=0;c<cols;c++){
      if (!((view.getClueText(c,true)).equals(Utils.clueFromIntArray(model.getColumn(c))))) return false;
    }
    return true;
  }

  
  public Move undoMove(){
    if(!isSolving) return null;
    Move lm=history.getLastMove();
    if (!(lm==null)){
      model.setDataFromCell(new Cell(lm.row,lm.col,lm.previousState));
      updateLabelsFromModel(lm.row,lm.col,false);
      view.redrawGrid();
    }
    return lm;
  }
  public void redoMove(){
    Move lm=history.getNextMove();
    if (lm==null) return;
    model.setDataFromCell(new Cell(lm.row,lm.col,lm.replacementState));
    updateLabelsFromModel(lm.row,lm.col,false);
    view.redrawGrid();
  }
  
  public void initialiseSolving(){
      history.initialize();
      startDate=new Date();
      markedCell.clear();     
  }
  
  public void setSolving(boolean isSolving){
    if (isSolving){
      model.useWorking();
    }
    else {
			rewindToMarkedCell();
			model.useSolution();
		}
    
    view.setSolving(isSolving);
    view.redrawGrid();
    this.isSolving=isSolving;
  }
}
