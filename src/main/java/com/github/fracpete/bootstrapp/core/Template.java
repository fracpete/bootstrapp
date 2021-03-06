/*
 * Template.java
 * Copyright (C) 2020-2021 University of Waikato, Hamilton, NZ
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

  /** the debian build plugin file. */
  public final static String DEBIANBUILD_FILE = "debian.build";

  /** the debian build plugin file (incl sources). */
  public final static String DEBIANBUILDSRC_FILE = "debian.build_incl_sources";

  /** the redhat build plugin file. */
  public final static String REDHATBUILD_FILE = "redhat.build";

  /** the redhat build plugin file (incl sources). */
  public final static String REDHATBUILDSRC_FILE = "redhat.build_incl_sources";

  /** the placeholder in the template POM for the dependencies (full dependencies tag). */
  public final static String PH_DEPENDENCIES = "<!-- dependencies -->";

  /** the placeholder in the template POM for the repositories (full repositories tag). */
  public final static String PH_REPOSITORIES = "<!-- repositories -->";

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

  /** the placeholder in the template POM for the project name. */
  public final static String PH_NAME = "<!-- name -->";

  /** the placeholder in the template POM for the version. */
  public final static String PH_VERSION = "<!-- version -->";

  /** the placeholder in the template POM for additional build plugins. */
  public final static String PH_BUILDPLUGINS = "<!-- buildplugins -->";

  /** the default name. */
  public final static String DEFAULT_NAME = "bootstrapp-harness";

  /** the default version. */
  public final static String DEFAULT_VERSION = "0.0.1";

  /** for logging. */
  protected static Logger LOGGER = Logger.getLogger(Template.class.getName());

  /**
   * The configuration to use for customizing the template.
   */
  public static class Configuration {

    /** the maven output directory. */
    public File outputDirMaven;

    /** the dependencies to use (group:artifact:version). */
    public List<String> dependencies;

    /** the exclusions to use (group:artifact or group:artifact:WHATEVER). */
    public List<String> exclusions;

    /** the repositories to use (id;name;url). */
    public List<String> repositories;

    /** the external jars to use. */
    public List<File> externalJars;

    /** the external sources to use. */
    public List<File> externalSources;

    /** whether to skip downloading sources. */
    public boolean noSources;

    /** whether to skip generating spring boot jar. */
    public boolean noSpringBoot;

    /** the main class, can be null. */
    public String mainClass;

    /** the name of the project. */
    public String name;

    /** the version of the project. */
    public String version;

    /** additional build plugins to use. */
    public String buildPlugins;
  }

  /**
   * Configures the bundled template.
   *
   * @param outputDir		the directory to copy the template to (as pom.xml)
   * @param config 		the configuration
   * @return			null if successful, otherwise error message
   */
  public static String configureBundledTemplate(File outputDir, Configuration config) {
    String	result;
    String	path;
    File	file;

    try {
      path = com.github.fracpete.resourceextractor4j.Files.extractTo(Resources.LOCATION, TEMPLATE_FILE, System.getProperty("java.io.tmpdir"));
      result = configureTemplate(new File(path), outputDir, config);
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
   * @param config 		the configuration
   * @return			null if successful, otherwise error message
   */
  public static String configureTemplate(File template, File outputDir, Configuration config) {
    List<String>	lines;
    int			i;
    String		line;
    StringBuilder	excls;
    String		exclsStr;
    StringBuilder	deps;
    String		depsStr;
    StringBuilder	repos;
    String		reposStr;
    String[]		parts;
    boolean		modified;

    // exclusions
    excls = new StringBuilder();
    if (config.exclusions != null) {
      excls.append("      <exclusions>");
      for (String exclusion: config.exclusions) {
	parts = exclusion.split(":");
	if (parts.length == 2) {
	  excls.append("        <exclusion>\n");
	  excls.append("          <groupId>").append(parts[0]).append("</groupId>\n");
	  excls.append("          <artifactId>").append(parts[1]).append("</artifactId>\n");
	  excls.append("        </exclusion>\n");
	}
	else {
        LOGGER.warning("Skipping exclusion as it does not conform to format 'group:artifact': " + exclusion);
	}
      }
      excls.append("      </exclusions>");
    }
    exclsStr = excls.toString();

    // build dependency string
    deps = new StringBuilder();
    deps.append("  <dependencies>\n");

    // regular dependencies
    for (String dependency: config.dependencies) {
      parts = dependency.split(":");
      if (parts.length == 3) {
	deps.append("    <dependency>\n");
	deps.append("      <groupId>").append(parts[0]).append("</groupId>\n");
	deps.append("      <artifactId>").append(parts[1]).append("</artifactId>\n");
	deps.append("      <version>").append(parts[2]).append("</version>\n");
	if (!exclsStr.isEmpty())
	  deps.append(exclsStr);
	deps.append("    </dependency>\n");
      }
      else {
        LOGGER.warning("Skipping dependency as it does not conform to format 'group:artifact:version': " + dependency);
      }
    }

    // external jars
    if (config.externalJars != null) {
      for (File externalJar: config.externalJars) {
        deps.append("    <dependency>\n");
        deps.append("      <groupId>bootstrapp</groupId>\n");
        deps.append("      <artifactId>ext-" + externalJar.getName().toLowerCase().replace(".jar", "") + "</artifactId>\n");
        deps.append("      <version>0.0.0</version>\n");
        deps.append("      <scope>system</scope>\n");
        deps.append("      <systemPath>" + externalJar.getAbsolutePath() + "</systemPath>\n");
        deps.append("    </dependency>\n");
      }
    }

    // external sources
    if (config.externalSources != null) {
      for (File externalSource : config.externalSources) {
        deps.append("    <dependency>\n");
        deps.append("      <groupId>bootstrapp</groupId>\n");
        deps.append("      <artifactId>ext-" + externalSource.getName().toLowerCase().replace(".jar", "") + "</artifactId>\n");
        deps.append("      <version>0.0.0</version>\n");
        deps.append("      <scope>system</scope>\n");
        deps.append("      <classifier>sources</classifier>\n");
        deps.append("      <systemPath>" + externalSource.getAbsolutePath() + "</systemPath>\n");
        deps.append("    </dependency>\n");
      }
    }

    deps.append("  </dependencies>\n");
    depsStr = deps.toString();
    if (config.dependencies.size() == 0)
      LOGGER.warning("No dependencies supplied!");

    // build repository string
    repos = new StringBuilder();
    if ((config.repositories != null) && (config.repositories.size() > 0)) {
      repos.append("  <repositories>\n");
      for (String repo: config.repositories) {
        parts = repo.split(";");
        if (parts.length == 3) {
          repos.append("    <repository>\n");
          repos.append("      <id>").append(parts[0]).append("</id>\n");
          repos.append("      <name>").append(parts[1]).append("</name>\n");
          repos.append("      <url>").append(parts[2]).append("</url>\n");
          repos.append("    </repository>\n");
        }
      }
      repos.append("  </repositories>\n");
    }
    reposStr = repos.toString();

    try {
      modified = false;
      lines = Files.readAllLines(template.toPath());
      for (i = 0; i < lines.size(); i++) {
        if (lines.get(i).contains("<!-- ")) {
          line = lines.get(i);
          line = line.replace(PH_NAME, config.name);
          line = line.replace(PH_VERSION, config.version);
          line = line.replace(PH_DEPENDENCIES, depsStr);
          line = line.replace(PH_REPOSITORIES, reposStr);
          line = line.replace(PH_OUTPUTDIR, config.outputDirMaven.getAbsolutePath());
          line = line.replace(PH_NOSOURCES, "" + config.noSources);
          line = line.replace(PH_NOSPRINGBOOT, "" + config.noSpringBoot);
          line = line.replace(PH_PACKAGING, config.noSpringBoot ? "pom" : "jar");
          if (config.mainClass != null)
	    line = line.replace(PH_MAINCLASS, "" + config.mainClass);
          if (config.buildPlugins != null)
	    line = line.replace(PH_BUILDPLUGINS, "" + config.buildPlugins);
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
