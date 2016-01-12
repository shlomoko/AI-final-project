/* Resource class for gnonograms-java
 * Defines various values in one place
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
 
package gnonograms.app;

import java.awt.event.KeyEvent;
import java.awt.Color;

public class Resource
{
  public static final String BLOCKSEPARATOR=",";
  public  static final String VERSION_STRING="0.9.7";
  public static final String IMAGE_PATH="/res/images/";
  public static final double MAXIMUM_GRADE=20;
  public static final double GRADE_FOR_ONE_GUESS=15;
  public static final double GRADE_FOR_TWO_GUESSES=18;
  public static final int MAXGUESSWORK_FOR_SOLVER=9999;
  public static final int MAXIMUM_GRID_SIZE=100;
  public static final int MAXIMUM_CLUE_POINTSIZE=72;
  public static final int MAXIMUM_CLUE_WIDTH_MARGIN=30;
  public static final int MAXIMUM_CLUE_LENGTH_MARGIN=50;
  public static final double MAXIMUM_COL_FREEDOM_FACTOR=60.0;
  public static final double MAXIMUM_ROW_FREEDOM_FACTOR=60.0;
  public static final double MAXIMUM_BLOCKSIZEFACTOR1=4.0;
  public static final double MAXIMUM_BLOCKSIZEFACTOR2=6.0;
  
  public static final int MINIMUM_CLUE_POINTSIZE=4;
  public static final int DEFAULT_ROWS=10;
  public static final int DEFAULT_COLS=15;
  public static final int DEFAULT_GRADE=10;

  public static final int CELLSTATE_UNKNOWN=0;
  public static final int CELLSTATE_EMPTY=1;
  public static final int CELLSTATE_FILLED=2;
  public static final int CELLSTATE_ERROR=3;
  public  static final int CELLSTATE_COMPLETED=4;
  public  static final int CELLSTATE_ERROR_EMPTY=5;
  public static final int CELLSTATE_ERROR_FILLED=6;
  public static final int CELLSTATE_UNDEFINED=7;
  
  public static final int GAME_STATE_SETTING=0;
  public static final int GAME_STATE_SOLVING=1;
  public static final int GAME_STATE_LOADING=2;
  public static final int DEFAULT_STARTSTATE= GAME_STATE_SETTING;
  

  public static final int KEY_FILLED=KeyEvent.VK_F;
  public static final int KEY_EMPTY=KeyEvent.VK_E;
  public static final int KEY_UNKNOWN=KeyEvent.VK_X;
  
  public static final Color HIGHLIGHT_COLOR=Color.gray;
  public static final Color MARKED_CELL_COLOR=Color.white;

}
