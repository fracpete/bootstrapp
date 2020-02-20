/*
 * Resources.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.bootstrapp.core;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages resources.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Resources {

  /** the top-level resource location. */
  public final static String LOCATION = "com/github/fracpete/bootstrapp";

  /** for logging. */
  protected static Logger LOGGER = Logger.getLogger(Resources.class.getName());

  /**
   * Copies the specified resource to the output directory.
   *
   * @param inDir	the resource directory to use
   * @param name	the name of the resource
   * @param outDir	the output directory
   * @return		the full path
   */
  protected static String copyResourceTo(String inDir, String name, String outDir) throws Exception {
    String			result;
    String			resource;
    InputStream 		is;
    BufferedInputStream 	bis;
    String			outFull;
    File 			out;
    FileOutputStream 		fos;
    BufferedOutputStream 	bos;

    result = null;
    is     = null;
    bis    = null;
    fos    = null;
    bos    = null;

    try {
      resource = inDir;
      if (!resource.endsWith("/"))
	resource += "/";
      resource += name;
      outFull = outDir + File.separator + name;
      LOGGER.info("Copying resource '" + resource + "' to '" + outFull + "'");
      is  = Resources.class.getClassLoader().getResourceAsStream(resource);
      bis = new BufferedInputStream(is);
      out = new File(outFull);
      fos = new FileOutputStream(out);
      bos = new BufferedOutputStream(fos);
      IOUtils.copy(bis, bos);
      result = out.getAbsolutePath();
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Copying failed!", e);
      throw e;
    }
    finally {
      IOUtils.closeQuietly(bis);
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(bos);
      IOUtils.closeQuietly(fos);
    }

    return result;
  }

  /**
   * Returns all the lines from the resource file.
   *
   * @return		the lines
   */
  public static List<String> readLines(String resource) {
    List<String>    	result;
    InputStream		in;
    InputStreamReader 	isr;
    BufferedReader 	br;
    String		line;

    result = new ArrayList<>();
    in     = null;
    isr    = null;
    br     = null;

    try {
      in  = Maven.class.getClassLoader().getResourceAsStream(resource);
      isr = new InputStreamReader(in);
      br  = new BufferedReader(isr);
      while ((line = br.readLine()) != null)
	result.add(line);
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Failed to read resource file: " + resource, e);
    }
    finally {
      if (br != null) {
	try {
	  br.close();
	}
	catch (Exception e) {
	  // ignored
	}
      }
      if (isr != null) {
	try {
	  isr.close();
	}
	catch (Exception e) {
	  // ignored
	}
      }
      if (in != null) {
	try {
	  in.close();
	}
	catch (Exception e) {
	  // ignored
	}
      }
    }

    return result;
  }
}
