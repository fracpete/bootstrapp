/*
 * Maven.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.bootstrapp.core;

import com.github.fracpete.requests4j.Requests;
import com.github.fracpete.requests4j.response.FileResponse;
import com.github.fracpete.resourceextractor4j.Content;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.util.ArrayList;
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

  /** the file with the download URL. */
  public final static String URL_FILE = "apache-maven.url";

  /** the file with the directory in the zip file. */
  public final static String DIR_FILE = "apache-maven.dir";

  /** the file with the file names of the executables. */
  public final static String EXECUTABLES_FILE = "apache-maven.executables";

  /** the version of the maven installation. */
  protected static String VERSION = null;

  /** the home directory. */
  protected static String HOMEDIR = null;

  /** for logging. */
  protected static Logger LOGGER = Logger.getLogger(Maven.class.getName());

  /**
   * Returns the version of the downloaded version of maven.
   *
   * @return		the version
   */
  public static synchronized String version() {
    List<String> 	lines;

    if (VERSION == null) {
      lines = Content.readLines(Resources.LOCATION + "/" + DIR_FILE);
      if ((lines != null) && (lines.size() > 0))
        VERSION = lines.get(0);
      else
        VERSION = "unknown";
    }

    return VERSION;
  }

  /**
   * Returns the home directory for the downloaded maven version.
   *
   * @return		the directory
   */
  public static synchronized String homeDir() {
    String 	dir;

    if (HOMEDIR == null) {
      if (System.getenv(HOME_DIR_ENV) != null) {
	dir = System.getenv(HOME_DIR_ENV);
      }
      else {
	dir = System.getProperty("user.home");

	if (!SystemUtils.IS_OS_WINDOWS)
	  dir += "/.local/share";

	dir += "/bootstrapp/" + version();
      }

      HOMEDIR = dir;
    }

    return HOMEDIR;
  }

  /**
   * Downloads Maven from the web, if necessary.
   *
   * @return		null if successful, otherwise error message
   */
  public static String initRemoteMaven() {
    File			dir;
    String			res;
    String			dirname;
    String			url;
    String 			tmpFile;
    FileResponse 		r;
    List<String> 		lines;
    StringBuilder		error;
    File			file;

    dir = new File(homeDir());

    // exists already?
    if (dir.exists() && dir.isDirectory())
      return null;

    // get URL
    res = Resources.LOCATION + "/" + URL_FILE;
    url = Content.readString(res);
    if (url == null)
      return "Failed to read URL from resource: " + res;
    url = url.trim();

    // get directory
    res     = Resources.LOCATION + "/" + DIR_FILE;
    dirname = Content.readString(res);
    if (dirname == null)
      return "Failed to read directory from resource: " + res;
    dirname = dirname.trim();

    // download file
    tmpFile = System.getProperty("java.io.tmpdir") + File.separator + "maven.zip";
    try {
      LOGGER.info("Downloading Maven from: " + url);
      r = Requests.get(url)
	.allowRedirects(true)
	.execute(new FileResponse(tmpFile, 1024 * 1024));
      if (r.ok())
	LOGGER.info("Finished downloading Maven.");
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Failed to download Maven from:\n" + url + "\nto:\n" + tmpFile, e);
      return "Failed to download Maven from:\n" + url + "\nto:\n" + tmpFile;
    }

    // decompress
    lines = new ArrayList<>();
    ZipUtils.decompress(new File(tmpFile), dir.getParentFile(), true, 1024 * 1024, lines);
    if (!lines.isEmpty()) {
      error = new StringBuilder();
      for (String e: lines) {
        if (error.length() > 0)
          error.append("\n");
        error.append(e);
      }
      return error.toString();
    }

    // set executable flags
    if (!SystemUtils.IS_OS_WINDOWS) {
      res = Resources.LOCATION + "/" + EXECUTABLES_FILE;
      lines = Content.readLines(res);
      if (lines == null)
        return "Failed to read executable files from: " + res;
      for (String line : lines) {
	file = new File(homeDir() + "/" + line);
	file.setExecutable(true);
      }
    }

    return null;
  }
}
