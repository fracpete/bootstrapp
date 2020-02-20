/*
 * Template.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.bootstrapp.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the POM template.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Template {

  /** the resource location of the POM template. */
  public final static String TEMPLATE_FILE = "template.xml";

  /** the placeholder in the template POM for the dependencies (full dependencies tag). */
  public final static String PH_DEPENDENCIES = "<!-- dependencies -->";

  /** the placeholder in the template POM for the output directory (directory string). */
  public final static String PH_OUTPUTDIR = "<!-- outputdir -->";

  /** the placeholder in the template POM to skip downloading the sources (boolean). */
  public final static String PH_NOSOURCES = "<!-- nosources -->";

  /** the placeholder in the template POM to skip spring-boot jar generation (boolean). */
  public final static String PH_NOSPRINGBOOT = "<!-- nospringboot -->";

  /** the placeholder in the template POM for the main class (string). */
  public final static String PH_MAINCLASS = "<!-- mainclass -->";

  /** the placeholder in the template POM for the packaging (string, pom/jar). */
  public final static String PH_PACKAGING = "<!-- packaging -->";

  /** for logging. */
  protected static Logger LOGGER = Logger.getLogger(Template.class.getName());

  /**
   * Configures the bundled template.
   *
   * @param outputDir		the directory to copy the template to (as pom.xml)
   * @param outputDirMaven 	the output directory to use by the maven build
   * @param dependencies	the list of artifacts to bootstrap with
   * @param noSources		whether to skip downloading sources
   * @param noSpringBoot 	whether to skip generating a spring-boot jar
   * @param mainClass 		the main class to use
   * @return			null if successful, otherwise error message
   */
  public static String configureBundledTemplate(File outputDir, File outputDirMaven, List<String> dependencies, boolean noSources, boolean noSpringBoot, String mainClass) {
    String	result;
    String	path;
    File	file;

    try {
      path = Resources.copyResourceTo(Resources.LOCATION, TEMPLATE_FILE, System.getProperty("java.io.tmpdir"));
      result = configureTemplate(new File(path), outputDir, outputDirMaven, dependencies, noSources, noSpringBoot, mainClass);
      file = new File(path);
      if (file.exists())
        file.delete();
      return result;
    }
    catch (Exception e) {
      return "Failed to configure bundled template: " + e;
    }
  }

  /**
   * Configures the specified template.
   *
   * @param template 		the template file to configure
   * @param outputDir		the directory to copy the template to (as pom.xml)
   * @param outputDirMaven 	the output directory to use by the maven build
   * @param dependencies	the list of artifacts to bootstrap with
   * @param noSources		whether to skip downloading sources
   * @param noSpringBoot 	whether to skip generating a spring-boot jar
   * @param mainClass 		the main class to use
   * @return			null if successful, otherwise error message
   */
  public static String configureTemplate(File template, File outputDir, File outputDirMaven, List<String> dependencies, boolean noSources, boolean noSpringBoot, String mainClass) {
    List<String>	lines;
    int			i;
    String		line;
    StringBuilder	deps;
    String		depsStr;
    String[]		parts;
    boolean		modified;

    // build dependency string
    deps = new StringBuilder();
    deps.append("  <dependencies>\n");
    for (String dependency: dependencies) {
      parts = dependency.split(":");
      if (parts.length == 3) {
	deps.append("    <dependency>\n");
	deps.append("      <groupId>").append(parts[0]).append("</groupId>\n");
	deps.append("      <artifactId>").append(parts[1]).append("</artifactId>\n");
	deps.append("      <version>").append(parts[2]).append("</version>\n");
	deps.append("    </dependency>\n");
      }
      else {
        LOGGER.warning("Skipping dependency as it does not conform to format 'group:artifact:version': " + dependency);
      }
    }
    deps.append("  </dependencies>\n");
    depsStr = deps.toString();
    if (dependencies.size() == 0)
      LOGGER.warning("No dependencies supplied!");

    try {
      modified = false;
      lines = Files.readAllLines(template.toPath());
      for (i = 0; i < lines.size(); i++) {
        if (lines.get(i).contains("<!-- ")) {
          line = lines.get(i);
          line = line.replace(PH_DEPENDENCIES, depsStr);
          line = line.replace(PH_OUTPUTDIR, outputDirMaven.getAbsolutePath());
          line = line.replace(PH_NOSOURCES, "" + noSources);
          line = line.replace(PH_NOSPRINGBOOT, "" + noSpringBoot);
          line = line.replace(PH_PACKAGING, noSpringBoot ? "pom" : "jar");
          if (mainClass != null)
	    line = line.replace(PH_MAINCLASS, "" + mainClass);
          if (!lines.get(i).equals(line)) {
	    lines.set(i, line);
	    modified = true;
	  }
	}
      }

      if (!modified)
        LOGGER.warning("Template file did not contain any placeholders, not modified!");

      Files.write(new File(outputDir.getAbsolutePath() + "/pom.xml").toPath(), lines, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    catch (Exception e) {
      return "Failed to configure template file '" + template + "': " + e;
    }

    return null;
  }
}
