/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Maven.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.bootstrapp.core;

import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maven related tasks.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Maven {

  /** the environment variable for the bootstrapp home directory. */
  public final static String HOME_DIR_ENV = "BOOTSTRAPP_HOME";

  /** the file with the version of the bundled maven installation. */
  public final static String VERSION_FILE = "apache-maven.version";

  /** the file with the file names of the bundled maven installation. */
  public final static String BUNDLEDFILES_FILE = "apache-maven.files";

  /** the file with the file names of the executables. */
  public final static String EXECUTABLES_FILE = "apache-maven.executables";

  /** the location of the bundled maven installation. */
  public final static String LOCATION = Resources.LOCATION + "/apache-maven/";

  /** the version of the bundled maven installation. */
  protected static String VERSION = null;

  /** for logging. */
  protected static Logger LOGGER = Logger.getLogger(Maven.class.getName());

  /**
   * Returns the version of the bundled version of maven.
   *
   * @return		the version
   */
  public static synchronized String version() {
    List<String> 	lines;

    if (VERSION == null) {
      lines = Resources.readLines(Resources.LOCATION + "/" + VERSION_FILE);
      if (lines.size() > 0)
        VERSION = lines.get(0);
      else
        VERSION = "unknown";
    }

    return VERSION;
  }

  /**
   * Returns the home directory for the bundled maven version.
   *
   * @return		the directory
   */
  public static String homeDir() {
    String	result;

    if (System.getenv(HOME_DIR_ENV) != null) {
      result = System.getenv(HOME_DIR_ENV);
    }
    else {
      result = System.getProperty("user.home");

      if (!SystemUtils.IS_OS_WINDOWS)
	result += "/.local/share";

      result += "/bootstrapp/mvn-" + version();
    }

    return result;
  }

  /**
   * Initializes the bundled maven installation.
   *
   * @return		null if successful, otherwise error message
   */
  public static String initBundledMaven() {
    File 		dir;
    List<String>	lines;
    String		inDir;
    String		name;
    File		file;
    String		home;
    File		outDir;

    dir = new File(homeDir());

    // exists already?
    if (dir.exists() && dir.isDirectory())
      return null;

    if (!dir.mkdirs())
      return "Failed to create directory for bundled Maven installation: " + dir;

    home = homeDir();

    // copy files
    lines = Resources.readLines(Resources.LOCATION + "/" + BUNDLEDFILES_FILE);
    for (String line: lines) {
      file  = new File(line);
      inDir = file.getParent();
      name  = file.getName();
      try {
        outDir = new File(home + "/" + (inDir == null ? "" : inDir));
        if (!outDir.exists())
          outDir.mkdirs();
	Resources.copyResourceTo(LOCATION + "/" + (inDir == null ? "" : inDir), name, outDir.getAbsolutePath());
      }
      catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Failed to copy '" + line + "' to '" + home + "'!", e);
        return "Failed to copy '" + line + "' to '" + home + "': " + e;
      }
    }

    // set executable flags
    if (!SystemUtils.IS_OS_WINDOWS) {
      lines = Resources.readLines(Resources.LOCATION + "/" + EXECUTABLES_FILE);
      for (String line : lines) {
	file = new File(home + "/" + line);
	file.setExecutable(true);
      }
    }

    return null;
  }
}
