/* Model class for gnonograms-java
 * Manages working and solution grid data, generates new puzzles
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
import java.util.Random;

import gnonograms.app.Resource;

public class Model {
  private int rows;
  private int cols;
  private My2DCellArray displayData;  //points to grid being displayed
  private My2DCellArray solutionData; //display when setting
  private My2DCellArray workingData; //display when solving
  private RandomPatternGenerator generator;
  private int[] temp;
  private static Random rand = new Random(System.nanoTime());
  private double minRowFreedomFactor=30.0;
  private double minColumnFreedomFactor=30.0;
  private double maxBlockSizeFactor1=2.0;
  private double maxBlockSizeFactor2=3.0;
  

  public Model(){
    rows = 10; cols = 10; //Must call set dimensions before use
    solutionData=new My2DCellArray(Resource.MAXIMUM_GRID_SIZE,Resource.MAXIMUM_GRID_SIZE,Resource.CELLSTATE_EMPTY);
    workingData=new My2DCellArray(Resource.MAXIMUM_GRID_SIZE,Resource.MAXIMUM_GRID_SIZE,Resource.CELLSTATE_UNKNOWN);
    displayData = solutionData;
  }

  public void clear(){
    blankSolution();  
    blankWorking();
  }

  public void blankSolution() {solutionData.setAll(Resource.CELLSTATE_EMPTY);}
  public void blankWorking(){workingData.setAll(Resource.CELLSTATE_UNKNOWN);}
  public void errorSolution(){solutionData.setAll(Resource.CELLSTATE_ERROR);}
  
  public void setDimensions(int r, int c){
    this.rows=r;this.cols=c;
    solutionData.resize(r,c); workingData.resize(r,c);
    generator=new RandomPatternGenerator(r,c);
  }
	public void setFactors(
										double mrff,
										double mcff,
										double mblf1,
										double mblf2
										){
		minRowFreedomFactor=mrff;
		minColumnFreedomFactor=mcff;
		maxBlockSizeFactor1=mblf1;
		maxBlockSizeFactor2=mblf2;
	}

  public void useWorking(){displayData=workingData;}
  public void useSolution() {displayData=solutionData;}

  public int[] getRow(int idx){return displayData.getRow(idx);}
  public int[] getRow(int idx, boolean fromSolution){
    if (fromSolution) return solutionData.getRow(idx);
    else return workingData.getRow(idx);
  }
  public int[] getColumn(int idx){return displayData.getColumn(idx);}
  public int[] getColumn(int idx, boolean fromSolution){
    if (fromSolution) return solutionData.getColumn(idx);
    else return workingData.getColumn(idx);
  }
  public int getDataFromRC(int r, int c){return displayData.getDataFromRC(r,c);}
  public Cell getCell(int r, int c){return displayData.getCell(r,c);}

  public void setDataFromCell(Cell c){displayData.setDataFromCell(c);}
  public void setRowDataFromString(int r, String s){displayData.setRowDataFromString(r,s);}
  public void setRowDataFromArray(int r, int[] cs){displayData.setRowDataFromArray(r,cs);}
  public void setGrade(double grade){generator.setGrade(grade);}

  public int countUnknownCells(){return displayData.countState(Resource.CELLSTATE_UNKNOWN);}
  public int countErrors(){return workingData.countDifferences(solutionData);}

  public void generateRandomPattern(){
    solutionData.setAll(Resource.CELLSTATE_EMPTY);
    generator.generateBasicPattern();
    generator.adjustPattern();
  }

  private class RandomPatternGenerator {
    int[] temp;
    double rows2D, cols2D, middleOfColumn, middleOfRow;
    int maximumColumnBlockSize, maximumRowBlockSize;
    int minimumColumnFreedom, minimumRowFreedom;
       
    public RandomPatternGenerator(int r, int c){
      rows2D=(double)(r);
      cols2D=(double)(c);
      middleOfColumn = rows2D/2.0;
      middleOfRow = cols2D/2.0;
    }

    protected void setMaximumBlockSizes(double grade){
      maximumColumnBlockSize=calculatemaximumBlockSize(rows2D,grade);
      maximumRowBlockSize=calculatemaximumBlockSize(cols2D,grade);
    }
    
    private int calculatemaximumBlockSize(double lengthOfRegion, double grade){
      //Empirical formula giving reasonable results
      double max=1+(int)((lengthOfRegion/maxBlockSizeFactor1)*(1.0-(grade/(Resource.MAXIMUM_GRADE*maxBlockSizeFactor2))));
      if(max<lengthOfRegion/5)max=lengthOfRegion/5;
      if (max>lengthOfRegion)max=lengthOfRegion-1;
      return (int)max;
    }
    
    protected void setMinimumFreedoms(double grade){

      if (grade>=Resource.GRADE_FOR_ONE_GUESS) {
        minimumColumnFreedom=4;minimumRowFreedom=4; //helps generate solvable advanced puzzles
        return;
      }
      if (grade<Resource.MAXIMUM_GRADE/4) {
        minimumColumnFreedom=0;minimumRowFreedom=1; //elementary levels
        return;
      }
      minimumColumnFreedom = (int)((rows*grade)/minColumnFreedomFactor);
      minimumRowFreedom = (int)((cols*grade)/minRowFreedomFactor);      
    }

    protected void setGrade(double grade){
      setMaximumBlockSizes(grade);
      setMinimumFreedoms(grade);
    }

    protected void generateBasicPattern(){
      for (int c=0;c<cols;c++){
        temp =solutionData.getColumn(c);
        temp=fillRegion(rows, temp, (int)(Math.abs((c-middleOfRow)/middleOfRow)), maximumColumnBlockSize);
        solutionData.setColumn (c, temp);
      }
      for (int r=0;r<rows;r++){
        temp=solutionData.getRow(r);
        temp=fillRegion(cols, temp, Math.abs((r-middleOfColumn)/middleOfColumn), maximumRowBlockSize);
        solutionData.setRow(r, temp);
	  } 
    }

    private int[] fillRegion (int sizeOfRegion, int[] region, double distanceFromCenter, int maximumBlockSize){
	//distanceFromCenter range 0 -> 1
      int p=0, blockSize, max;
      int cellState;
      boolean fill;
      assert sizeOfRegion<=region.length;
      while (p<sizeOfRegion-1){
        fill=(rand.nextInt(100)>50+25*distanceFromCenter); //whether next block will be filled or not (50% chance)
        //set max sixe of filled or unfilled block
        max=fill ? maximumBlockSize : (sizeOfRegion-maximumBlockSize);
        // random length up to remaining space but not larger than max
        blockSize=Math.min(1+rand.nextInt(max),sizeOfRegion-p);
        // fill in block (default is empty
        for (int i=0; i<blockSize; i++){
          if (fill) region[p]=Resource.CELLSTATE_FILLED;
          p++;
        }
        if (fill && p<sizeOfRegion-1) p++;
      }
      return region;
    }

    protected void adjustPattern(){
      if (rows>4){
        for (int c=0;c<cols;c++){
          temp =solutionData.getColumn(c);
          temp=adjustRegion(rows, temp, minimumColumnFreedom);
          solutionData.setColumn (c, temp);
        }
      }
      if (cols>4){
        for (int r=0;r<rows;r++){
          temp=solutionData.getRow(r);
          temp=adjustRegion(cols, temp, minimumRowFreedom);
          solutionData.setRow(r, temp);
    } } }
    
    private int[] adjustRegion(int sizeOfRegion, int [] region, int mindf)  {
      // mindf = minimum degrees of freedom
      int filledCells=0, filledBlocks=0, degreesOfFreedom=0;
      for (int i=0; i<sizeOfRegion; i++){
        if (region[i]==Resource.CELLSTATE_FILLED) {
          filledCells++;
          if (i==0 || region[i-1]==Resource.CELLSTATE_EMPTY) filledBlocks++;
      } }
      degreesOfFreedom=sizeOfRegion-filledCells-filledBlocks+1;

      if (degreesOfFreedom>sizeOfRegion){//completely empty - fill one cell
        region[rand.nextInt(sizeOfRegion)]=Resource.CELLSTATE_FILLED;
      }
      else{ // empty cells until reach min freedom
        int count=0;
        while (degreesOfFreedom<mindf && count<30){
          count++;
          int i=rand.nextInt(sizeOfRegion);
          if (region[i]==Resource.CELLSTATE_FILLED) {
            region[i]=Resource.CELLSTATE_EMPTY;
            degreesOfFreedom++;
      } } }
      return region;
    }
  }

  public String displayDataToString(){
    StringBuilder sb= new StringBuilder();
    for (int r=0; r<rows; r++){
      temp=displayData.getRow(r);
      sb.append(Utils.stringFromIntArray(temp));
      sb.append("\n");
    }
    return sb.toString();
  }

  public My2DCellArray getCellDataArray(){
    My2DCellArray ca =new My2DCellArray(rows,cols);
    ca.copyFrom(displayData);
    return ca;
  }
}
