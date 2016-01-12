/* Region class for gnonograms-java
 * Represents and solves one row or column of grid; used by solver
 * Contains some superfluous debugging code.  TODO: remove dubugging code and optimise
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

package gnonograms.utils;

import java.lang.Math;
import java.util.Arrays;
import java.util.ListIterator;
import static java.lang.System.out;

import gnonograms.app.Resource;

public class Region {

/** A region consists of a one dimensional array of cells, corresponding
 *  to a row or column of the puzzle. Associated with this are:
 *
  1) A list of block lengths (clues)
  2) A 'tag' boolean array for each cell with
    *   a flag for each block indicating whether that block is still a possible owner
    *   two extra flags - ' is completed' and ' can be empty'
  3) A 'completed blocks' boolean array with
    *   a flag for each block indicating whether it is completed.
  4) A status array, one per cell indicating the status of that cell as either
    * UNKNOWN,
    * FILLED (but not necessarily assigned to a completed block),
    * EMPTY, or
    * COMPLETED (assigned to a completed block).
    *
  Can save and restore its state - used to implement (one level)
  * back tracking during trial and error solution ('advanced solver').
  *
  Can generate all possible permutations of block positions in one region
  * Used for ('ultimate solver').
**/
  public boolean isColumn;
  public boolean inError;
  private boolean debug;
  public int index;
  public String message;

  public boolean isCompleted=false;
  private boolean isCompletedStore;
  private boolean[] completedBlocks;
  private boolean[] completedBlocksStore;
  private boolean[][] tags;
  private boolean[][] tagsStore;
  private int unknown;
  private int unknownStore;
  private int filled;
  private int filledStore;
  private int completedStore;
  public int[] status;
  private int[] statusStore;

  private int[][] ranges; //format: start,length,unfilled?,complete?
  private int[][] rangesStore;

  private int unchangedCount=0;
  public int nCells;
  private String clue;
  private int nBlocks;
  public int blockTotal; //total cells to be filled
  private int blockExtent; //int.minimum span of blocks, including gaps of 1 cell.
  private int canBeEmptyPointer;
  private int isFinishedPointer;
  private int[] myBlocks;
  private int currentIdx, currentBlockNum; //needed because Java cannot pass parameters by reference.
  public My2DCellArray grid;
  private int[] tempStatus, tempStatus2;


  private static int MAXCYCLES=20;
  private static int FORWARDS=1;
  private static int BACKWARDS=-1;

  public Region (My2DCellArray grid){
    this.grid=grid;
    //this.index=index;   this.isColumn=isColumn;  this.nCells=nCells;
    int maxlen=Math.max(grid.getRows(), grid.getCols());
    status=new int[maxlen]; statusStore=new int[maxlen];
    int maxblks=maxlen/2+2;
    ranges=new int[maxblks][4+maxblks];
    rangesStore=new int[maxblks][4+maxblks];
    myBlocks=new int[maxblks];
    completedBlocks=new boolean[maxblks];
    completedBlocksStore=new boolean[maxblks];
    tags=new boolean[maxlen][maxblks+2];
    tagsStore=new boolean[maxlen][maxblks+2];
    //two extra flags for "can be empty" and "is finished".
  }

  public void initialize(int index, boolean isColumn, int nCells, String clue){
    this.index=index;   this.isColumn=isColumn;  this.nCells=nCells; this.clue=clue;
    if (nCells==1) {this.isCompleted=true; return;}
    tempStatus=new int[nCells];
    tempStatus2=new int[nCells];
    int[] tmpblcks=Utils.blockArrayFromClue(clue);
    nBlocks=tmpblcks.length;
    canBeEmptyPointer=nBlocks; //flag for cell that may be empty
    isFinishedPointer=nBlocks+1; //flag for finished cell (filled or empty?)
    blockTotal=0;

    for (int i=0;i<nBlocks;i++){
      myBlocks[i]=tmpblcks[i];
      blockTotal=blockTotal+myBlocks[i];
    }
    blockExtent=blockTotal+nBlocks-1; //minimum space needed for blocks
    initialstate();
  }

  public void initialstate(){
    for (int i=0;i<nBlocks;i++){
      completedBlocks[i]=false;
      completedBlocksStore[i]=false;
    }
    for (int i=0;i<nCells;i++){ //Start with no possible owners and can be empty.
      for (int j=0; j<nBlocks; j++){
        tags[i][j]=false;tagsStore[i][j]=false;
      }
      tags[i][canBeEmptyPointer]=true;
      tags[i][isFinishedPointer]=false;
      status[i]=Resource.CELLSTATE_UNKNOWN;
      tempStatus[i]=Resource.CELLSTATE_UNKNOWN;
      tempStatus2[i]=Resource.CELLSTATE_UNKNOWN;
    }
    inError=false;
    isCompleted=(nCells==1); //allows debugging of single row
    if (isCompleted) return;
    this.unknown=99;
    this.filled=99;
    getstatus();
    if (myBlocks[0]==0){ //trivial solution - complete now
      for (int i=0;i<nCells;i++){
        for (int j=0; j<nBlocks; j++) tags[i][j]=false;
        //Start with no possible owners and empty.
        tags[i][canBeEmptyPointer]=false;
        tags[i][isFinishedPointer]=true;
        status[i]=Resource.CELLSTATE_EMPTY;
        tempStatus[i]=Resource.CELLSTATE_EMPTY;
        tempStatus2[i]=Resource.CELLSTATE_EMPTY;
      }
      isCompleted=true;
    }
    else  initialfix();
    tagsToStatus(); putStatus();
  }

  private void initialfix(){
    //finds cells that can be identified as FILLED from the start.
    //out.println("initialfix\n");
    int freedom=nCells-blockExtent;
    int start=0, length=0;
    for (int i=0; i<nBlocks; i++){
      length=myBlocks[i]+freedom;
      for (int j=start; j<start+length; j++) tags[j][i]=true;
      if (freedom<myBlocks[i]) setRangeOwner(i,start+freedom, myBlocks[i]-freedom, true, false);
      start=start+myBlocks[i]+1; //leave a gap between blocks
    }
    if (freedom==0) isCompleted=true;
  }

  public boolean solve(boolean debug, boolean hint){
    /**if change has occurred since last visit (due to change in an intersecting
     * region), runs full-fix() to see whether any inferences possible.
     * as soon as any change is made by fullfix(), updates status of
     * all cells from the tags, checks for errors or completion.
     * Repeats until no further inferences made or MAXCYCLES exceeded.
     *
     * The advanced solver relies on the error signals to implement a "trial
     * and error" method of solution when straight logic fails. An error
     * is produced when an intersecting region makes a change incompatible
     * with this region.
     *
      Ignores single cell regions for testing purposes ...
      *
      * In hint mode, return minimal change
    * */

    message=""; inError=false;
    this.debug=debug;
    if (isCompleted) return false;
    getstatus();
    //has a (valid) change been made by another region
    if (inError) return false;
    if (!totalsChanged()) {
      unchangedCount++;
      if (unchangedCount>1) return false; //allow an unchanged visit to ensure all possible changes are made.
    }
    else unchangedCount=0;
    if(isCompleted) {return false;}
    int count=0;  boolean madechanges=false;
    while (!isCompleted && count<MAXCYCLES){
      count++;
      fullfix();
      if (inError) break;
      tagsToStatus();
      if (totalsChanged()){
        madechanges=true;
        if(inError) break;
      }
      else break; // no further changes made
    }
    if ((madechanges && !inError)||debug) {putStatus();}
    if (count==MAXCYCLES){inError=true; out.println("Excessive looping in region "+index);}
    return madechanges;
  }

  private boolean fullfix() {
    //out.println("Fullfix");
    // Tries each ploy in turn, returns as soon as a change is made
    // or an error detected.
    if (filledSubregionAudit()||inError)return true;
    if (freecellaudit()||inError) return true;
    if (cappedrangeaudit()||inError)return true;
    if (possibilitiesAudit()||inError)return true;
    if (fillGaps()||inError)return true;
    if (onlypossibility()||inError)return true;
    if (doedge(1)||inError)return true;
    if (doedge(-1)||inError)return true;
    if (availableFilledSubRegionAudit()||inError)return true;
    if (fixblocksinranges()||inError)return true;
    return false;
  }

  private boolean filledSubregionAudit(){
    //find a range of filled cells not completed and see if can be associated
    // with a unique block.
    boolean changed=false, startcapped, endcapped;
    currentIdx=0;
    int length;
    while (currentIdx<nCells){ //find a filled sub-region
      startcapped=false; endcapped=false;
      currentIdx=skipWhileNotStatus(Resource.CELLSTATE_FILLED, currentIdx, nCells, 1);
      if (currentIdx==nCells) break;
      //found first FILLED cell; currentIdx points to it
      if (tags[currentIdx][isFinishedPointer]) {currentIdx++;continue;}//ignore if completed already
      if(currentIdx==0 || status[currentIdx-1]==Resource.CELLSTATE_EMPTY) startcapped=true; //edge cell
      length=countNextState(Resource.CELLSTATE_FILLED, currentIdx,true);//currentIdx not changed
      int lastcell=currentIdx+length-1;//last filled cell in this (partial) block
      if (lastcell==nCells-1 || status[lastcell+1]==Resource.CELLSTATE_EMPTY) endcapped=true; //last cell is at edge
      //is this region fully capped?
      if (startcapped && endcapped){  // assigned block must fit exactly
        assignAndCapRange(currentIdx,length);
        currentIdx+=length+1;
        continue;
      }
      else{ //find largest possible owner of this (partial) block
        int largest=findlargestpossibleincell(currentIdx);
        if (largest==length){//there is **at least one** largest block that fits exactly.
        // this region must therefore be complete
          assignAndCapRange(currentIdx,length);
          currentIdx+=length+1;
          changed=true;
          continue;
        }
        // remove blocks that are smaller than length from this region
        int start = currentIdx;
        int end = currentIdx+length-1; // last filled cell
        for (int bl=0;bl<nBlocks;bl++)  {
          for(int i=start;i<=end;i++){
            if (tags[i][bl] && myBlocks[bl]<length) tags[i][bl]=false;
        } }
        // For the adjacent cells (if not at edge) the minimum length
        // of the owner is one higher.
        if (start>0){
          start--;
          for (int bl=0;bl<nBlocks;bl++){
            if (tags[start][bl] && myBlocks[bl]<length+1) tags[start][bl]=false;
        } }
        if (end<nCells-1){
          end++;
          for (int bl=0;bl<nBlocks;bl++){
            if (tags[end][bl] && myBlocks[bl]<length+1) tags[end][bl]=false;
        } }
        if(startcapped || endcapped){//semi-capped - can we extend it?
          int smallest=findsmallestpossibleincell(currentIdx);
          if(smallest>length){//can extend by smallest-length away from cap
            int ptr;
            if(startcapped){
              ptr=currentIdx+length;
              for(int i=0;i<smallest-length;i++){
                if(ptr<nCells){tags[ptr][canBeEmptyPointer]=false;ptr++;}
              }
            }
            else{
              ptr=currentIdx-1;
              for(int i=0;i<smallest-length;i++){
                if(ptr>=0){tags[ptr][canBeEmptyPointer]=false;ptr--;}
              }
            }
            tagsToStatus();changed=true;
          }
        }
      }
      currentIdx+=length;//move past block - if reaches here no operations have been performed on block
    }
    return changed;
  }

  private boolean fillGaps(){
    // Find unknown gap between filled cells and complete accordingly.
    boolean changed=false;
    for (int idx=0; idx<nCells-2; idx++){
      if (status[idx]!=Resource.CELLSTATE_FILLED) continue; //find a FILLED cell
      if (status[idx+1]!=Resource.CELLSTATE_UNKNOWN) continue; //is following cell empty?
      if (!oneowner(idx)){  // if owner ambiguous, can only deal with single cell gap
        // see if single cell gap which can be marked empty because
        // to fill it would create a block larger than any permissible.
        if(status[idx+2]!=Resource.CELLSTATE_FILLED) continue; //cell after that filled?
        // we have found a one cell gap
        // calculate total length if gap were to be FILLED.
        int blength = countNextState(Resource.CELLSTATE_FILLED,idx+2,true)
                    + countNextState(Resource.CELLSTATE_FILLED,idx,false)
                    + 1;
        boolean mustbeempty=true;
        //look for a possible owner at least as long as combined length
        for (int bl=0; bl<nBlocks; bl++){
          if(tags[idx][bl] && myBlocks[bl]>=blength){ //possible owner found - gap could be filled
            mustbeempty=false;
            break;
        } }
        //no permissible blocks large enough
        if (mustbeempty){ setcellempty(idx+1); changed= true; }
        else{
        // see if setting empty would create two regions one or more of which is too small for the available blocks
          //out.println("Can be empty?\n");
          boolean mustNotBeEmpty=false;
          int lengthLeft=0, lengthRight=0;
          int ptr=idx;
          //left-hand region
          while (ptr>=0 && !tags[ptr][isFinishedPointer]){ptr--;lengthLeft++;}
          ptr=idx+2;
          //right-hand region
          while (ptr<nCells && !tags[ptr][isFinishedPointer]){ptr++;lengthRight++;}
          //find largest,earliest possible block fitting in first range
          //out.println("Left "+lengthLeft+" Right "+lengthRight+"\n");
          int firstBlock=-1;
          int tmpLength=0;
          int countLeft=0, countRight=0, totalCount;
          ptr=idx; //cell before gap
          for(int i=0;i<nBlocks;i++){
            if(tags[ptr][i] && myBlocks[i]<=lengthLeft) {countLeft++;}
          }
          ptr=idx+2; //cell after gap
          for(int i=0;i<nBlocks;i++){
            if(tags[ptr][i] && myBlocks[i]<=lengthRight) {countRight++;}
          }
          totalCount=countLeft+countRight;
          if (totalCount==2){
            for(int i=0;i<nBlocks;i++){
              if(tags[ptr][i] && myBlocks[i]<=lengthRight) {
                if(tags[idx][i] && myBlocks[i]<=lengthLeft) mustNotBeEmpty=true; // only one block fits in both sides
          } } }
          else if (totalCount<2) mustNotBeEmpty=true;
          //out.println("must not be empty "+mustNotBeEmpty+"\n");
          if(mustNotBeEmpty){tags[idx+1][canBeEmptyPointer]=false; changed=true;}
        }
        idx+=2; //skip gap
      }
      else{ //only one possible owner of first FILLED cell
        int cell1=idx; //start of gap
        idx++;
        //skip to end of gap
        while (idx<nCells-1 && status[idx]==Resource.CELLSTATE_UNKNOWN) idx++;

        if (status[idx]!=Resource.CELLSTATE_FILLED) continue; //gap ends with empty cell - abandon this gap
        else{ //if start and end of gap have same owner, fill in the gap.
          int owner=sameOwner(cell1,idx);
          if (owner>=0)changed=setRangeOwner(owner,cell1,idx-cell1+1,true,false)||changed;
          idx--;
    } } }
    return changed;
  }

  private boolean possibilitiesAudit(){
    //find a unique possible range for block if there is one.
    //eliminates ranges that are too small
    int start,length,count;
    boolean changed=false;
    for (int i=0;i<nBlocks;i++){
      if (completedBlocks[i]) continue; //skip completed block
      start=0;length=0;
      count=0; //how many possible ranges for this block
      for (int idx=0;idx<nCells;idx++){
        if (count>1) break; //no unique range - try next block
        if (!tags[idx][i]||tags[idx][isFinishedPointer]) continue; //cell not possible for this block or already completed

        int s=idx; //first cell with block i as possible owner
        int l=countnextowner(i,idx); //length of contiguous cells having this block (i) as a possible owner.

        if (l<myBlocks[i]) removeBlockFromRange(i,s,l,1);//block cannot be here
        else{
          length=l; start=s; count++;
        }
        idx+=l-1; //allow for incrementing on next loop
      }
      if (count!=1) continue; //no unique range found
      else{ //perhaps some cells can be assigned but
        //this range not proved exclusive to this block;
        changed=fixBlockInRange(i,start,length)||changed;
    } }
    return changed;
  }

  private void assignAndCapRange(int start, int length){
    //make list of possible blocks with right length in maxblks[]
    //record which is first and which last (in order).
    //always changes at least on cell status
    //out.println(@"Assign and cap start $start length $length\n");

    int count=0;
    int[] maxblks=new int[nBlocks];
    int first=nBlocks;
    int last=0;
    int end=start+length-1;

    for (int i=0;i<nBlocks;i++){
      if (completedBlocks[i]) continue;
      if (myBlocks[i]!=length) continue;
      if (!tags[start][i]||!tags[end][i]) continue;
      maxblks[count]=i; count++;
      if (i<first) first=i;
      if (i>last) last=i;
    }
    if(count==0) return; //no matching block - range is not complete
    if (count==1){  //unique owner
      setBlockCompleteAndCap(maxblks[0],start,1);
    }
    else{ //ambiguous owner
      //delete out of sequence blocks before end of range
      for (int i=last+1;i<nBlocks;i++)removeBlockFromCellToEnd(i,start+length-1,-1);
      //delete out of sequence blocks after start of range
      for (int i=0;i<first;i++)removeBlockFromCellToEnd(i,start,1);
      //remove as possible owner blocks between first and last that are wrong length
      for (int i=first+1;i<last;i++){
        if (myBlocks[i]==length) continue;
        removeBlockFromRange(i,start,length,1);
      }
      //for each possible mark as possible owner of subregion (not exclusive)
      for (int i=0;i<count;i++)setRangeOwner(maxblks[i],start,length,false, false);
      // cap range
      if (start>0) setcellempty(start-1);
      if (start+length<nCells) setcellempty(start+length);
    }
  }

  private boolean onlypossibility(){
    //find an unfinished cell with only one possibility
    //remove this block from cells out of range
    int owner;
    int length;
    int start;

    for (int i=0;i<nCells;i++){
      if (tags[i][isFinishedPointer]) continue;
      // unfinsihed cell found
      if (status[i]==Resource.CELLSTATE_FILLED && oneowner(i)){ //cell is FILLED and has only one owner
        //find the owner
        for (owner=0;owner<nBlocks;owner++) {
          if (tags[i][owner]) break;
        }
        length=myBlocks[owner];
        //remove this block from earlier cells our of range
        start=i-length;
        if (start>=0) removeBlockFromCellToEnd(owner,start,-1);
        //remove this block from later cells our of range
        start=i+length;
        if (start<nCells) removeBlockFromCellToEnd(owner,start,+1);
    } }
    return false; //always false - only changes tags
  }

  private boolean freecellaudit(){
    // Compare  number of UNKNOWN cells with the number of unassigned
    // block cells.
    // If they are the same then mark all UNKNOWN cells as
    // FILLED and mark all blocks COMPLETE.
    // If there are no unassigned block cells then mark all UNKNOWN
    // cells as EMPTY.
    int freecells=countcellstate(Resource.CELLSTATE_UNKNOWN);
    if (freecells==0) return false;

    int filledcells=countcellstate(Resource.CELLSTATE_FILLED);
    int completedcells=countcellstate(Resource.CELLSTATE_COMPLETED);
    int tolocate=blockTotal-filledcells-completedcells;

    if (freecells==tolocate){ // Set all UNKNOWN as COMPLETE
      for (int i=0;i<nCells;i++)  {
        if (status[i]==Resource.CELLSTATE_UNKNOWN) setcellcomplete(i);
      }
      for (int i=0;i<nBlocks;i++) completedBlocks[i]=true;
      return true;
    }
    else if (tolocate==0) {
      for (int i=0;i<nCells;i++) {
        if (status[i]==Resource.CELLSTATE_UNKNOWN) setcellempty(i);
        isCompleted=true;
      }
      return true;
    }
    return false;
  }

  private boolean doedge(int direction){
    // Scan forward (or backward) from an edge searching for filled cells
    // that are nearer than length of first (or last) block.
    // FILL cells after that to length of first (or last) block.
    // Look for FILLED cell just out of range - edge can be moved forward
    //  direction: 1=FORWARDS, -1=BACKWARDS
    //pointer to current cell
    //int blocknum; //current block
    int limit; //first out of range value of idx depending on direction
    boolean dir=(direction==FORWARDS);

    if (dir){currentIdx=0; currentBlockNum=0; limit=nCells;}
    else{currentIdx=nCells-1; currentBlockNum=nBlocks-1; limit=-1;}
    //Find first edge - skipping completed cells
    if (!findEdge(limit,direction)) return false;
    //currentIdx points to cell on the edge
    if (status[currentIdx]==Resource.CELLSTATE_FILLED){
    //first cell is FILLED. Can complete whole block
      return setBlockCompleteAndCap(currentBlockNum,currentIdx,direction);
    } else {  // see if filled cell in range of first block and complete after that
      int edgestart=currentIdx;
      int fillstart=-1;
      int blength = myBlocks[currentBlockNum];
      int blocklimit=(dir? currentIdx+blength : currentIdx-blength);
      if (blocklimit<-1 || blocklimit>nCells){inError=true; message="Invalid blocklimit";return false;}
      currentIdx=skipWhileNotStatus(Resource.CELLSTATE_FILLED,currentIdx,blocklimit,direction);
      if (currentIdx!=blocklimit) {
        fillstart=currentIdx;
        boolean changed=false;
        while (currentIdx!=blocklimit){
          if (status[currentIdx]==Resource.CELLSTATE_UNKNOWN){
            setcellowner(currentIdx,currentBlockNum,true,false);
            //changed=true;
          }
          if (dir) currentIdx++;
          else currentIdx--;
        }
        // currentIdx now points to cell after earliest possible end of block
        // if this is a filled cell then first cell in range must be empty
        // continue setting cells at beginning of range empty until
        // an unfilled cell found. FILL cells beyond first FILLED cells.
        // remove block from out of range of first filled cell.
        while (currentIdx!=blocklimit && status[currentIdx]==Resource.CELLSTATE_FILLED){
          setcellowner(currentIdx,currentBlockNum,true,false);
          setcellempty(edgestart);
          out.println("Set cell " + edgestart+" empty \n");
          changed=true;
          if (dir) {currentIdx++; edgestart++;}
          else {currentIdx--; edgestart--;}
        }
        //if a fillable cell was found then fillstart>0
        if (fillstart>0){
        //delete block more than block length from where filling started
          currentIdx= dir ? fillstart+blength : fillstart-blength;
          if (currentIdx>=0 && currentIdx<nCells) removeBlockFromCellToEnd(currentBlockNum,currentIdx,direction);
        }
        return changed;
    } }
    return false;
  }


  private boolean findEdge(int limit, int direction){
    // Edge is first FILLED or UNKNOWN cell from limit of region.
    //starting point is set in currentIdx and currentBlockNum before calling.
    //out.println(this.toString());
    boolean dir=(direction==FORWARDS);
    boolean found=false;
    int loopstep=dir?1:-1;
    for (int i=currentIdx; (i>=0 && i<nCells); i+=loopstep){
      if (status[i]==Resource.CELLSTATE_EMPTY) continue;
      //now pointing at first cell of filled or unknown block after edge
      if (tags[i][isFinishedPointer]){  //skip to end of finished block
        i = (dir ? i+myBlocks[currentBlockNum]-1 : i-myBlocks[currentBlockNum]+1); //could skip over limit
        //now pointing at last cell of filled block
        currentBlockNum+=loopstep; //Increment or decrement current block as appropriate
        if (currentBlockNum<0 || currentBlockNum==nBlocks) {recordError("FindEdge", "Invalid BlockNum"); return false;}
        //out.println("i is now "+i+ " block is now " + currentBlockNum + "\n");
      }
      else {currentIdx=i; return true;}
    }
    return false;
  }

  private boolean fixblocksinranges(){
    //out.println("Fix blocks in ranges\n");
    //int e=countcellstate(Resource.CELLSTATE_EMPTY);
    // blocks may have been marked completed - thereby reducing available ranges
    int[] availableBlocks=countBlocksAvailable();
    int bl=availableBlocks.length;
     // TODO check whether no of available blocks has changed
    int[][] blockstart = new int[bl][2]; //range number and offset of earliest start point
    int[][] blockend = new int[bl][2]; //range number and offset of latest end point
    //update ranges with currently available ranges (can contain only unknown  and incomplete cells)
    int numberOfAvailableRanges=countAvailableRanges(false);
    //out.println("Empty count "+e+" Available blocks "+bl+" available ranges "+numberOfAvailableRanges+"\n");

    //find earliest start point of each block (treating ranges as all unknown cells)
    int rng=0, offset=0, length=0, ptr; //start at beginning of first available range
    for (int b=0; b<bl; b++) {//for each available block
      length=myBlocks[availableBlocks[b]]; //get its length
       if (ranges[rng][1]<(length+offset)) {//cannot fit in current range
        rng++; offset=0;//skip to start of next range
        while (rng<numberOfAvailableRanges && ranges[rng][1]<length) rng++;//keep skipping if too small
        if (rng>=numberOfAvailableRanges)return false;
      }
      //look for collision with filled cell
      ptr=ranges[rng][0]+offset+length; //cell after end of block
      while(ptr<nCells && !tags[ptr][canBeEmptyPointer]){ptr++;offset++;}
      blockstart[b][0]=rng; //set start range number
      blockstart[b][1]= offset; //and start point
      offset+=(length+1); //move offset allowing for one cell gap between blocks
    }
    //carry out same process in reverse to get latest end points
    rng=numberOfAvailableRanges-1; offset=0; //start at end of last range NB offset now counts from end
    for (int b=bl-1; b>=0; b--) {//start at last block
      length=myBlocks[availableBlocks[b]]; //get length
       if (ranges[rng][1]<(length+offset)){//doesn't fit
        rng--; offset=0; //skip to end of previous block
        while (rng>=0 && ranges[rng][1]<length) rng--; //keep skipping if too small
        if (rng<0)return false;
      }
      //look for collision with filled cell
      ptr=ranges[rng][0]+ranges[rng][1]-(offset+length)-1; //cell before beginning of block
      while(ptr>=0 && !tags[ptr][canBeEmptyPointer]){ptr--;offset++;}
      blockend[b][0]=rng; //set end range number
      blockend[b][1]= ranges[rng][1]-offset;  //and end point
      //NB end point is index of cell AFTER last possible cell so that
      //subtracting start from end gives length of range.
      offset+=(length+1); //shift offset allowing for one cell gap
    }
    int start;
    for (int b=0; b<bl; b++){ //for each available block
      rng=blockstart[b][0];offset=blockstart[b][1];
      start=ranges[rng][0];
      if (rng==blockend[b][0]){ //if starts and ends in same range
        length=blockend[b][1]-blockstart[b][1];
        //'length' now used for total length of possible range for this block
        fixBlockInRange(availableBlocks[b],start+offset,length);
      }
      //remove block from outside possible range
      if(offset>1)removeBlockFromRange(availableBlocks[b],start,offset-1,1);
      for (int r=0; r<blockstart[b][0];r++) {//ranges before possible
        removeBlockFromRange(availableBlocks[b],ranges[r][0],ranges[r][1],1);
      }
      rng=blockend[b][0];
      start=ranges[rng][0]+blockend[b][1];
      length=ranges[rng][1]-blockend[b][1];
      if(length>0)removeBlockFromRange(availableBlocks[b],start,length,1);
      for (int r=numberOfAvailableRanges-1; r>blockend[b][0]; r--) {//ranges after possible
        removeBlockFromRange(availableBlocks[b],ranges[r][0],ranges[r][1],1);
    } }
    return false;
  }

  private boolean cappedrangeaudit(){
    // For each capped range (contiguous filled cells bounded on both
    // ends by an edge or an empty cell), remove as owner all blocks
    // of the wrong size. Check there is at least one possible owner
    // else return an error.
    // only changes tags so returns false
    int start=0, length=0, idx=0;
    int nranges=countcappedranges();
    if (nranges==0) return false;
    for (int rng=0; rng<nranges; rng++){
      start=ranges[rng][0];
      length=ranges[rng][1];
      for (idx=start;idx<start+length;idx++){
        int count=0;
        for(int b=0;b<nBlocks;b++){
          if(tags[idx][b]){
            count++;
            if(myBlocks[b]!=length){
              tags[idx][b]=false;
              count--;
        } } }
        if (count==0){
          recordError("capped range audit","filled cell with no owners",false);
          return false;
    } } }
    return false;
  }

  private boolean availableFilledSubRegionAudit(){
    //test whether there is an unambiguous distribution of available blocks amongs available filled subregions.
    //out.println("Available subregion audit\n");
    int idx=0, start=0, end=nCells, countRegions=0;
    Range[]availableSubRegions=new Range[nCells/2]; //start and end of each subregion
    while(idx<nCells){
      if (status[idx]!=Resource.CELLSTATE_FILLED) {idx++; continue;}
      countRegions++;
      if(countRegions<=nBlocks)start=idx;
      else return false;
      while(idx<nCells && status[idx]==Resource.CELLSTATE_FILLED){idx++;}
      end=idx-1; //last filled cell
      availableSubRegions[countRegions-1]=new Range(start,end,-1,-1);
    }
     if (countRegions<2||countRegions>nBlocks) return false;
    //now see how many blocks could fit here;
    int[] availableBlocks = countBlocksAvailable();
    int nAvailableBlocks=availableBlocks.length;
    //out.println("Available block count is "+nAvailableBlocks+"\n");
    if (countRegions>nAvailableBlocks) return false;
    int firstStart=availableSubRegions[0].start;
    int length=availableSubRegions[0].length();
    int lastEnd=availableSubRegions[countRegions-1].end;
    //delete available blocks up to first in first subregion
    int countBlocks=nAvailableBlocks;
    int bl;
    for (int i=0;i<nAvailableBlocks;i++){
      bl=availableBlocks[i];
      if (!tags[firstStart][bl]){
        availableBlocks[i]=-1; countBlocks--;
      }
      else break;
    }
    for (int i=nAvailableBlocks-1;i>=0;i--){
      bl=availableBlocks[i];
      if (bl>=0 && !tags[lastEnd][bl]){
        availableBlocks[i]=-1; countBlocks--;
      }
      else break;
    }
    if (countBlocks!=countRegions) return false;
    int[] candidates= new int[countBlocks];
    int countCandidates=0;
    int combinedLength=0;
    for (int i=0;i<nAvailableBlocks;i++){
      if (availableBlocks[i]<0)continue;
      else {
        candidates[countCandidates]=availableBlocks[i];
        combinedLength+=myBlocks[availableBlocks[i]];
        countCandidates++;
      }
    }
     combinedLength+=(countCandidates-1); //allow for gap of at least 1 between blocks
    //for unambiguous assignment all sub regions must be separated by more than the combined length of the candidate blocks and gaps
    int overallLength=lastEnd-firstStart+1;
     if (overallLength<combinedLength) return false;
    //consecutive regions must be separated so one block cannot cover both
    //either by  finished cell or by distance
    for (int ar=0; ar<countRegions-1;ar++){
      boolean separate=false;
      for (int i=availableSubRegions[ar].end; i<availableSubRegions[ar+1].start; i++){
        if(tags[i][isFinishedPointer]) {separate=true; break;}
      }
      if (separate) continue; //separated by empty or complete cell
      else{
        start=availableSubRegions[ar].start;
        end=availableSubRegions[ar+1].end;
        length=end-start+1;
        if (length<=myBlocks[candidates[ar]]||length<=myBlocks[candidates[ar+1]]) return false; //too close
      }
    }
    //Unambiguous assignment possible
    if (countRegions>countCandidates){inError=true; out.println("too many regions\n");return false;}
    for (int ar=0; ar<countRegions;ar++){
      bl=candidates[ar]; length=myBlocks[bl];
      setRangeOwner(bl,availableSubRegions[ar].start,availableSubRegions[ar].length(),true, false);
    }
    return false;  //only changes tags
  }
//======================================================================
// END OF PLOYS
// HELPER FUNCTIONS FOLLOW
//======================================================================
  private int skipWhileNotStatus(int cs, int idx, int limit, int direction){
    // increments/decrements idx until cell of required state
    // or end of range found.
    //returns idx of cell with status cs if found else limit
    //  boolean dir=(direction>0);
    boolean test1= idx>=limit;
    if (limit<-1 || limit>nCells) {inError=true; message="Invalid limit"; out.println(message); return 0;}
    if ((direction==Region.FORWARDS) && idx>=limit)  return limit;
    else if ((direction==Region.BACKWARDS) && (idx<=limit)) return limit;

    for (int i=idx; i!=limit;i+=direction){
      if (status[i]==cs)  return i;
    }
    return limit; //false;
  }

  private int countNextState(int cs, int idx, boolean forwards){
    // count how may consecutive cells of state cs starting at given
    // index idx (inclusive of starting cell)
    int count=0;
    if (forwards && idx>=0) {
      while (idx<nCells && status[idx]==cs) {
        count++; idx++;
      }
    }
    else if (!forwards && idx<nCells){
      while (idx>=0 && status[idx]==cs) {
        count++; idx--;
      }
    }
    else {inError=true; message="countNextState idx invalid\n";}
    return count;
  }

  private int countnextowner(int owner, int idx){
    // count how may consecutive cells with owner possible starting
    // at given index idx?
    int count=0;
    if (idx>=0) {
      while (idx<nCells && tags[idx][owner] && !tags[idx][isFinishedPointer]) {
        count++; idx++;
      }
    }
    else {inError=true;message="countnextowner idx negative\n";}

    return count;
  }

  private int countAvailableRanges(boolean notempty){
    // determine location of ranges of unknown or unfinished filled cells
    // and store in ranges[][]
    // ranges[ ,0] indicates start point,
    // ranges[ ,1] indicates length
    // ranges[ ,2] indicates number of filled,
    // ranges[ ,3] indicates number of unknown
    int range=0, start=0, length=0, idx=0;
    //skip to start of first range;
    while (idx<nCells && tags[idx][isFinishedPointer]) idx++;

    while (idx<nCells){
      length=0; start=idx;
      ranges[range][0]=start;
      ranges[range][2]=0;
      ranges[range][3]=0;

      while (idx<nCells && !tags[idx][isFinishedPointer]){
        if (!tags[idx][canBeEmptyPointer]) ranges[range][2]++; //FILLED
        else ranges[range][3]++; //UNKNOWN
        idx++; length++;
      }
      if(notempty && ranges[range][2]==0) {} //dont include completely empty ranges
      else {ranges[range][1]=length; range++;}
      //skip to beginning of next range
      idx++;
      while (idx<nCells && tags[idx][isFinishedPointer]) idx++;
    }
    return range; //number of ranges - not last index!
  }

  private boolean checkNumberOfBlocks(){
    //only called when region is completed. Checks whether number of blocks is correct
    int count=0, idx=0;
    while (idx<nCells){
      while (idx<nCells && status[idx]==Resource.CELLSTATE_EMPTY) idx++;
      if (idx<nCells) count++;
      else break;
      while (idx<nCells && status[idx]!=Resource.CELLSTATE_EMPTY) idx++;
    }
    if (count!=nBlocks){
      recordError("Check nBlocks","Wrong number of blocks found "+count+" should be "+nBlocks);
      return false;
    }
    else return true;
  }

  private int countcappedranges(){
    // determine location of capped ranges of filled cells (not marked complete) and store in ranges[][]
    int range=0, start=0, length=0, idx=0;
    while (idx<nCells && status[idx]!=Resource.CELLSTATE_FILLED) idx++; //skip to beginning of first range
    while (idx<nCells){
      length=0; start=idx;
      ranges[range][0]=start;
      ranges[range][2]=0; //not used
      ranges[range][3]=0; //not used

      while (idx<nCells && status[idx]==Resource.CELLSTATE_FILLED){
        idx++; length++;
      }

      if ((start==0||status[start-1]==Resource.CELLSTATE_EMPTY) && (idx==nCells||status[idx]==Resource.CELLSTATE_EMPTY)) {//capped
        ranges[range][1]=length;
        range++;
      }
      idx++;
      while (idx<nCells && status[idx]!=Resource.CELLSTATE_FILLED) idx++; //skip to beginning of next range
    }
    return range;
  }

  private int countownersandempty(int cell) {
    // how many possible owners?  Does include can be empty tag!
    int count=0;
    if (invalidData(cell)) {inError=true;message="countownersandempty invalid data\n";}
    else  {
      for (int j=0;j<nBlocks; j++) {
        if (tags[cell][j]) count++;
      }
      if (tags[cell][canBeEmptyPointer]) count++;
    }
    if (count==0){inError=true; message="count owners and empty - count is zero\n";}
    return count;
  }

  private int countcellstate(int cs){
    //how many times does state cs occur in range.
    int count=0;
    for (int i=0;i<nCells; i++)   {
      if (status[i]==cs) count++;
    }
    return count;
  }

  private int[] countBlocksAvailable(){
    //array of incomplete block indexes
    int[] tempInt=new int[50];
    int count=0;
    for (int i=0; i<nBlocks; i++){
      if (!completedBlocks[i]) tempInt[count++]=i;
    }
    return Arrays.copyOf(tempInt,count);
  }

  private int sameOwner(int cell1, int cell2){
    //checks if both the same single possible owner.
    //return owner if same owner else -1
    int count=0, owner=-1;
    boolean tmp;
    if (cell1<0||cell1>=nCells||cell2<0||cell2>=nCells) {
      inError=true; message="sameOwner cell - out of range\n";
    }
    else  {
      for (int i=0; i<nBlocks; i++){
        tmp=tags[cell1][i];
        if (count>1||(tmp!=tags[cell2][i]) )  {
          owner=-1;
          break;
        }
        else if (tmp){
          count++;
          owner=i;
        }
      }
    }
    return owner;
  }

  private boolean oneowner(int cell){
    // if only one possible owner (if not empty) then return true
    int count=0;
    for (int i=0; i<nBlocks; i++){
      if (tags[cell][i]) count++;
      if (count>1) break;
    }
    return count==1;
  }

  private boolean fixBlockInRange(int block, int start, int length){
    //out.println("Fix Block in Range");
    // block must be limited to range
    boolean changed=false;

    if (invalidData(start,block,length))  {
      inError=true; message="fix block in range - invalid data\n";
    }else {
      int blocklength=myBlocks[block];
      int freedom = length-blocklength;
      if (freedom<0){
        recordError("Fix block in range", "block longer than range",false);
        return false;
      }

      if (freedom<blocklength){
        if (freedom==0){setBlockCompleteAndCap(block,start,1);changed=true;}
        else setRangeOwner(block,start+freedom,blocklength-freedom,true,false);
      }
    }
    return changed;
  }

  private int findlargestpossibleincell(int cell){
    // find the largest incomplete block possible for given cell
    int maxsize=-1;
    for (int i=0;i<nBlocks;i++){
      if (!tags[cell][i]) continue; // not possible
      if (myBlocks[i]<=maxsize) continue; // not largest
      maxsize=myBlocks[i]; //update largest
    }
    return maxsize;
  }

  private int findsmallestpossibleincell(int cell){
    // find the largest incomplete block possible for given cell
    int minsize=9999;
    for (int i=0;i<nBlocks;i++){
      if (!tags[cell][i]) continue; // not possible
      if (myBlocks[i]>=minsize) continue; // not largest
      minsize=myBlocks[i]; //update largest
    }
    if (minsize==9999){
      recordError("findsmallest possible in cell","No block possible in "+cell);
      return 0;
    }
    return minsize;
  }

  private void removeBlockFromCellToEnd(int block, int start,int direction){
    //remove block as possibility after/before start
    //bi-directional forward=1 backward =-1
    //if reverse direction then equivalent forward range is used
    //only changes tags

    int length=direction>0 ? nCells-start : start+1;
    start=direction>0 ? start : 0;
    if (length>0)removeBlockFromRange(block,start,length,1);
  }

  private void removeBlockFromRange(int block, int start, int length, int direction){
    //remove block as possibility in given range
    //bi-directional forward=1 backward =-1
    //if reverse direction then equivalent forward range is used
    //only changes tags

    if (direction<0) start=start-length+1;
    if (invalidData(start,block, length)) {
      inError=true; message="remove block from range: invalid data: start "+start+" block "+block+" length "+length+"nCells "+nCells+" nBlocks "+nBlocks+"\n";
    }
    else  {
      for (int i=start; i<start+length; i++) tags[i][block]=false;
    }
  }

  private boolean setBlockCompleteAndCap(int block, int start, int direction){
    //returns true - always changes a cell status if not in error
    boolean changed=false;
    int length=myBlocks[block];
    if (direction<0) start=start-length+1;
    if (invalidData(start,block, length)) {
      inError=true; message="setBlockCompleteAndCap - invalid data\n"; return false;
    }
    if (completedBlocks[block]==true && tags[start][block]==false){
      inError=true; message="setBlockCompleteAndCap - contradiction - block not in this range\n"; return false;
    }
    completedBlocks[block]=true;
    setRangeOwner(block,start,length,true,false);

    if (start>0 && !tags[start-1][isFinishedPointer]) {changed=true;setcellempty(start-1);}
    if (start+length<nCells && !tags[start+length][isFinishedPointer]) {changed=true; setcellempty(start+length);}

    for (int cell=start; cell<start+length; cell++) setcellcomplete(cell);
    //taking into account minimum distance between blocks.
    // constrain the preceding blocks if this are at least two
    int l;
    if (block>1){ //at least third block
      l=0;
      for (int bl=block-2;bl>=0;bl--){
        l=l+myBlocks[bl+1]+1;// length of exclusion zone for this block
        removeBlockFromRange(bl,start-2,l,-1);
      }
    }
    // constrain the following blocks if there are at least two
    if (block<nBlocks-2){
      l=0;
      for (int bl=block+2;bl<=nBlocks-1;bl++){
        l=l+myBlocks[bl-1]+1;// length of exclusion zone for this block
        removeBlockFromRange(bl,start+length+1,l,1);
      }
    }
    return changed;  //if block was not already capped
  }


  private boolean setRangeOwner(int owner, int start, int length, boolean exclusive, boolean canbeempty){
    boolean changed=false;
    if (invalidData(start,owner,length)){
      inError=true; message="setRangeOwner - invalid data\n";
      return false;
    }else{
      int blocklength=myBlocks[owner];
      for (int cell=start; cell<start+length; cell++){
        setcellowner(cell,owner,exclusive,canbeempty);
      }

      if (exclusive){
        //remove block and out of sequence from regions out of reach if exclusive
        if (blocklength<length && !canbeempty){
          inError=true; message="setRangeOwner - contradiction - range too big for owner\n";
          return false;
        }

        int bstart=Math.min(start-1,start+length-blocklength);
        if(bstart>=0)removeBlockFromCellToEnd(owner,bstart-1,-1);

        int bend=Math.max(start+length,start+blocklength);
        if (bend<nCells)removeBlockFromCellToEnd(owner,bend,1);

        int earliestend=start+length;
        for (int bl=nBlocks-1;bl>owner;bl--){ //following blocks cannot be earlier
          removeBlockFromCellToEnd(bl,earliestend,-1);
        }

        int lateststart=start-1;
        for (int bl=0;bl<owner;bl++){ //preceding blocks cannot be later
          removeBlockFromCellToEnd(bl,lateststart,1);
        }
      }
    }
    return changed;
  }

  private boolean setcellowner(int cell, int owner, boolean exclusive, boolean canbeempty){
    //exclusive - cant be any other block here
    //can be empty - self evident
    boolean changed=false;
    if (invalidData(cell,owner)){
      inError=true; message="setcellowner - cell "+cell+" invalid data\n";
    }
    else if (status[cell]==Resource.CELLSTATE_EMPTY) {}// do nothing - not necessarily an error
    else if (status[cell]==Resource.CELLSTATE_COMPLETED && tags[cell][owner]==false){
      recordError("setcellowner","contradiction cell "+cell+" filled but cannot be owner");
    }else{
      if (exclusive){
        for (int i=0; i<nBlocks; i++) tags[cell][i]=false;
      }
      if(!canbeempty) {
        status[cell]=Resource.CELLSTATE_FILLED; changed=true;
        tags[cell][canBeEmptyPointer]=false;
      }
      tags[cell][owner]=true;
    }
    return changed;
  }

  private void setcellempty(int cell){
    if (invalidData(cell)){
      recordError("setcellempty","cell "+cell+" invalid data");
    }
    else if (tags[cell][canBeEmptyPointer]==false)  {
      recordError("setcellempty","cell "+cell+" cannot be empty");
    }
    else if (cellFilled(cell)){
      recordError("setcellempty","cell "+cell+" is filled");
    }else{
      for (int i=0; i<nBlocks; i++) tags[cell][i]=false;

      tags[cell][canBeEmptyPointer]=true;
      tags[cell][isFinishedPointer]=true;
      status[cell]=Resource.CELLSTATE_EMPTY;
    }
  }

  private void setcellcomplete(int cell)
  {
    if (status[cell]==Resource.CELLSTATE_EMPTY){
      recordError("setcellcomplete","cell "+cell+" already set empty");
    }

    tags[cell][isFinishedPointer]=true;
    tags[cell][canBeEmptyPointer]=false;
    status[cell]=Resource.CELLSTATE_COMPLETED;
  }

  private boolean invalidData(int start){
    return invalidData(start,0,1);
  }
  private boolean invalidData(int start, int block){
    return invalidData(start,block,1);
  }
  private boolean invalidData(int start, int block, int length){
    return (start<0||start>=nCells||length<0||start+length>nCells||block<0||block>=nBlocks);
  }

  private boolean cellFilled(int cell){
    return (status[cell]==Resource.CELLSTATE_FILLED||status[cell]==Resource.CELLSTATE_COMPLETED);
  }

  private boolean totalsChanged(){
    //has number of filled or unknown cells changed?
    boolean changed=false;
    int unknown=countcellstate(Resource.CELLSTATE_UNKNOWN);
    int filled=countcellstate(Resource.CELLSTATE_FILLED);
    int completed=countcellstate(Resource.CELLSTATE_COMPLETED);
    if (unknown!=this.unknown){
      changed=true;
      this.unknown=unknown;
      this.filled=filled;
      if (filled+completed>blockTotal) recordError("totals changed","too many filled cells");
      else if (this.unknown==0){
         this.isCompleted=true;
         if (filled+completed<blockTotal) recordError("totals changed","too few filled cells - "+filled);
         else checkNumberOfBlocks(); //generates its own error
    } }
    return changed;
  }

  private void getstatus(){
    //transfers cell statuses from grid to internal range status array
    tempStatus=grid.getArray(index,isColumn);
    for (int i=0; i<nCells; i++){
      switch (tempStatus[i]){
        case Resource.CELLSTATE_EMPTY :
          if (!tags[i][canBeEmptyPointer])recordError("getstatus", "cell "+i+" cannot be empty");
          else  status[i]=Resource.CELLSTATE_EMPTY;
          break;
        case Resource.CELLSTATE_FILLED :
          //dont overwrite COMPLETE status
          if (status[i]==Resource.CELLSTATE_EMPTY)recordError("getstatus", "cell "+i+" cannot be filled");
          if (status[i]==Resource.CELLSTATE_UNKNOWN)status[i]=Resource.CELLSTATE_FILLED;
          break;
        default : break;
      }
      statusToTags();
  } }

  public void putStatus(){
    //use tempStatus2 to ovoid overwriting original input - needed for debugging
    for (int i=0;i<nCells; i++){
      tempStatus2[i]=(status[i]==Resource.CELLSTATE_COMPLETED ? Resource.CELLSTATE_FILLED : status[i]);
    }
    grid.setArray(index, isColumn, tempStatus2);
  }

  private void statusToTags(){
    for(int i=0;i<nCells;i++){
      switch (status[i]){
        case Resource.CELLSTATE_COMPLETED :
          tags[i][isFinishedPointer]=true;
          tags[i][canBeEmptyPointer]=false;
          break;

        case Resource.CELLSTATE_FILLED :
          tags[i][canBeEmptyPointer]=false;
          break;

        case Resource.CELLSTATE_EMPTY :
          for (int j=0;j<nBlocks;j++) tags[i][j]=false;
          tags[i][canBeEmptyPointer]=true;
          tags[i][isFinishedPointer]=true;
          break;

        default : break;
      }
    }
  }

  private void tagsToStatus(){
    for (int i=0;i<nCells; i++){
      if(status[i]==Resource.CELLSTATE_FILLED && tags[i][isFinishedPointer])status[i]=Resource.CELLSTATE_COMPLETED;
      if(status[i]!=Resource.CELLSTATE_UNKNOWN) continue;
      if(!tags[i][canBeEmptyPointer]){//cannot be EMPTY
        status[i]=(tags[i][isFinishedPointer] ? Resource.CELLSTATE_COMPLETED : Resource.CELLSTATE_FILLED);
        continue;
      }
      //Can be empty (but not necessarily is empty)
      if(countownersandempty(i)<=1){
        status[i]=Resource.CELLSTATE_EMPTY; tags[i][isFinishedPointer]=true;
      }
    }
  }

  private void recordError(String method, String errmessage){
    recordError(method,errmessage,true);
  }

  private void recordError(String method, String errmessage, boolean debug){
    if (debug){
      StringBuilder sb =new StringBuilder("");
      sb.append(":  ");
      sb.append(isColumn ? "column" : "row");
      sb.append(index);
      sb.append(" in method ");
      sb.append(method);
      sb.append("\n");
      sb.append(errmessage);
      sb.append(this.toString());
      message=  message+sb.toString();
    }
    else{
      inError=true; message="Record error in "+method+": "+errmessage+"\n";
    }
  }

  public void savestate(){
    for (int i=0;i<nCells;i++){
      statusStore[i]=status[i];
      for (int j=0; j<nBlocks+2; j++) tagsStore[i][j]=tags[i][j];
    }
    for (int j=0; j<nBlocks; j++) completedBlocksStore[j]=completedBlocks[j];
    isCompletedStore=this.isCompleted;
    filledStore=this.filled;
    unknownStore=this.unknown;
  }

  public void restorestate(){
    for (int i=0;i<nCells;i++){
      status[i]=statusStore[i];
      for (int j=0; j<nBlocks+2; j++) tags[i][j]=tagsStore[i][j];
    }
    for (int j=0; j<nBlocks; j++) completedBlocks[j]=completedBlocksStore[j];
    isCompleted=isCompletedStore;
    filled=filledStore;
    unknown=unknownStore;
    inError=false; message="";
    unchangedCount=0;
  }

  public String getID(){
    String colrow;
    StringBuilder sb= new StringBuilder("");
    if (isColumn) colrow="Column";
    else colrow="Row";
    sb.append(" ["+colrow+" "+index+" Clue: "+clue + " Is Completed " +isCompleted+"] ") ;
    return sb.toString();
  }
  public String toString()  {
    StringBuilder sb= new StringBuilder("");
    sb.append(this.getID()) ;
    sb.append("\n status before:\n");
    for (int i=0; i<nCells; i++) sb.append((tempStatus[i]));
    sb.append("\n status now:\n");
    for (int i=0; i<nCells; i++) sb.append((status[i]));
    sb.append("\nCell Status and Tags:\n");
    for (int i=0; i<nCells; i++){
      sb.append("Cell "+i+" Status: ");
      sb.append(status[i]+" ");
      for (int j=0; j<nBlocks; j++) sb.append(tags[i][j] ? "t" :"f");
      sb.append(" : ");
      for (int j=canBeEmptyPointer; j<canBeEmptyPointer+2; j++) sb.append(tags[i][j] ? "t" :"f");
      sb.append("\n");
    }
    return sb.toString();
  }

  private class Range {  //can use for filled subregions or ranges of filled and unknown cells
    public int start,end,filled,unknown;
    public Range(int start, int end, int filled, int unknown){
      this.start=start; //first cell in range
      this.end=end; // last cell in range
      this.filled=filled;
      this.unknown=unknown;
    }
    public int length (){return end-start+1;}
  }
}
