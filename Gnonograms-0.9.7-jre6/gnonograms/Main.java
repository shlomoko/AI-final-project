/* Main class for Gnonograms-java
 * Main entry point.
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

package gnonograms;

import javax.swing.UIManager;

import static java.lang.System.out;

import gnonograms.app.Controller;

public class Main {

  public static void main (String args[]) {
    try {
        // Set System L&F
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {
       out.println("Problem getting system look and feel - using default Java look and feel");
    }
    out.println("System encoding is "+System.getProperty("file.encoding"));
    out.println("OS architecture is "+System.getProperty("os.arch"));
    out.println("OS name is "+System.getProperty("os.name"));
    out.println("OS version is "+System.getProperty("os.version"));
    new Controller(); 
  }
}

