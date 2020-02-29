/*
 * ZipUtils.java
 * Copyright (C) Apache compress commons
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.bootstrapp.core;

import com.github.fracpete.resourceextractor4j.IOUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Methods for handling zip files.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ZipUtils {

  /** for logging. */
  protected static Logger LOGGER = Logger.getLogger(ZipUtils.class.getName());

  /**
   * Unzips the files in a ZIP file. Files can be filtered based on their
   * filename, using a regular expression (the matching sense can be inverted).
   * See also:
   * http://commons.apache.org/compress/examples.html
   *
   * @param input	the ZIP file to unzip
   * @param outputDir	the directory where to store the extracted files
   * @param createDirs	whether to re-create the directory structure from the
   * 			ZIP file
   * @param bufferSize	the buffer size to use
   * @param errors	for storing potential errors
   * @return		the successfully extracted files
   */
  public static List<File> decompress(File input, File outputDir, boolean createDirs, int bufferSize, List<String> errors) {
    List<File>				result;
    ZipFile 				archive;
    Enumeration<ZipArchiveEntry> 	enm;
    ZipArchiveEntry			entry;
    File				outFile;
    String				outName;
    byte[]				buffer;
    BufferedInputStream 		in;
    BufferedOutputStream 		out;
    FileOutputStream 			fos;
    int					len;
    String				msg;
    long				read;

    result  = new ArrayList<>();
    archive = null;
    try {
      // unzip archive
      buffer  = new byte[bufferSize];
      archive = new ZipFile(input.getAbsoluteFile());
      enm     = archive.getEntries();
      while (enm.hasMoreElements()) {
	entry = enm.nextElement();

	if (entry.isDirectory() && !createDirs)
	  continue;

	// extract
	if (entry.isDirectory() && createDirs) {
	  outFile = new File(outputDir.getAbsolutePath() + File.separator + entry.getName());
	  if (!outFile.mkdirs()) {
	    msg = "Failed to create directory '" + outFile.getAbsolutePath() + "'!";
	    LOGGER.log(Level.SEVERE, msg);
	    errors.add(msg);
	  }
	}
	else {
	  in      = null;
	  out     = null;
	  fos     = null;
	  outName = null;
	  try {
	    // assemble output name
	    outName = outputDir.getAbsolutePath() + File.separator;
	    if (createDirs)
	      outName += entry.getName();
	    else
	      outName += new File(entry.getName()).getName();

	    // create directory, if necessary
	    outFile = new File(outName).getParentFile();
	    if (!outFile.exists()) {
	      if (!outFile.mkdirs()) {
		msg =
		    "Failed to create directory '" + outFile.getAbsolutePath() + "', "
		    + "skipping extraction of '" + outName + "'!";
		LOGGER.log(Level.SEVERE, msg);
		errors.add(msg);
		continue;
	      }
	    }

	    // extract data
	    in   = new BufferedInputStream(archive.getInputStream(entry));
	    fos  = new FileOutputStream(outName);
	    out  = new BufferedOutputStream(fos, bufferSize);
	    read = 0;
	    while (read < entry.getSize()) {
	      len   = in.read(buffer);
	      read += len;
	      out.write(buffer, 0, len);
	    }
	    result.add(new File(outName));
	  }
	  catch (Exception e) {
	    msg = "Error extracting '" + entry.getName() + "' to '" + outName + "': ";
	    LOGGER.log(Level.SEVERE, msg, e);
	    errors.add(msg + "\n" + e);
	  }
	  finally {
	    IOUtils.closeQuietly(in);
	    IOUtils.closeQuietly(out);
	    IOUtils.closeQuietly(fos);
	  }
	}
      }
    }
    catch (Exception e) {
      msg = "Error occurred: ";
      LOGGER.log(Level.SEVERE, msg, e);
      errors.add(msg + "\n" + e);
    }
    finally {
      if (archive != null) {
	try {
	  archive.close();
	}
	catch (Exception e) {
	  // ignored
	}
      }
    }

    return result;
  }
}
