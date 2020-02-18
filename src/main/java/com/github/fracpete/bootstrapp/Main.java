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
 * Main.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.bootstrapp;

import com.github.fracpete.simpleargparse4j.ArgumentParser;
import com.github.fracpete.simpleargparse4j.ArgumentParserException;
import com.github.fracpete.simpleargparse4j.Namespace;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line application for bootstrapping a Maven appplication.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Main {

  /** the alternative maven installation. */
  protected String m_MavenHome;

  /** the alternative java installation. */
  protected String m_JavaHome;

  /** the output directory. */
  protected String m_OutputDir;

  /** the JVM options. */
  protected List<String> m_JVM;

  /** the dependencies. */
  protected List<String> m_Dependencies;

  /** the pom template. */
  protected String m_PomTemplate;

  /** the main class to launch. */
  protected String m_MainClass;

  /**
   * Initializes the object.
   */
  public Main() {
    initialize();
  }

  /**
   * Initializes the members.
   */
  protected void initialize() {
    m_MavenHome    = null;
    m_JavaHome     = null;
    m_OutputDir    = null;
    m_JVM          = null;
    m_Dependencies = null;
    m_MavenHome    = null;
    m_PomTemplate  = null;
  }

  /**
   * Sets the alternative maven installation to use.
   *
   * @param dir		the top-level directory (above "bin")
   * @return		itself
   */
  public Main mavenHome(String dir) {
    m_MavenHome = dir;
    return this;
  }

  /**
   * Returns the alternative maven installation to use.
   *
   * @return		the directory, null to use bundled one
   */
  public String getMavenHome() {
    return m_MavenHome;
  }

  /**
   * Sets the alternative java installation to use.
   *
   * @param dir		the top-level directory (above "bin")
   * @return		itself
   */
  public Main javaHome(String dir) {
    m_JavaHome = dir;
    return this;
  }

  /**
   * Returns the alternative java installation to use.
   *
   * @return		the directory, null if using one that class was started with
   */
  public String getJavaHome() {
    return m_JavaHome;
  }

  /**
   * Sets the output directory for the bootstrapped application.
   *
   * @param dir		the directory
   * @return		itself
   */
  public Main outputDir(String dir) {
    m_OutputDir = dir;
    return this;
  }

  /**
   * Returns the output directory for the bootstrapped application.
   *
   * @return		the directory, null if none set
   */
  public String getOutputDir() {
    return m_OutputDir;
  }

  /**
   * Sets the dependencies to use for bootstrapping.
   *
   * @param options	the dependencies, can be null
   * @return		itself
   */
  public Main dependencies(List<String> options) {
    m_Dependencies = options;
    return this;
  }

  /**
   * Returns the dependencies.
   *
   * @return		the dependencies, can be null
   */
  public List<String> getDependencies() {
    return m_Dependencies;
  }

  /**
   * Sets the template for the POM to use.
   *
   * @param template	the template
   * @return		itself
   */
  public Main pomTemplate(String template) {
    m_PomTemplate = template;
    return this;
  }

  /**
   * Returns the template for the pom.xml to use.
   *
   * @return		the POM template, null if using the default
   */
  public String getPomTemplate() {
    return m_PomTemplate;
  }

  /**
   * Sets the JVM options to use for launching the main class.
   *
   * @param options	the options, can be null
   * @return		itself
   */
  public Main jvm(List<String> options) {
    m_JVM = options;
    return this;
  }

  /**
   * Returns the JVM options.
   *
   * @return		the options, can be null
   */
  public List<String> getJvm() {
    return m_JVM;
  }

  /**
   * Sets the main class to launch after bootstrapping.
   *
   * @param dir		the main class
   * @return		itself
   */
  public Main mainClass(String dir) {
    m_MainClass = dir;
    return this;
  }

  /**
   * Returns the main class to launch after bootstrapping.
   *
   * @return		the main class, null if none set
   */
  public String getMainClass() {
    return m_MainClass;
  }

  /**
   * Configures and returns the commandline parser.
   *
   * @return		the parser
   */
  protected ArgumentParser getParser() {
    ArgumentParser 		parser;

    parser = new ArgumentParser("");
    parser.addOption("-m")
      .required(false)
      .dest("maven_home")
      .help("The directory with a local Maven installation to use instead of the bundled one.");
    parser.addOption("-j")
      .required(false)
      .dest("java_home")
      .help("The Java home to use for the Maven execution.");
    parser.addOption("-d")
      .required(true)
      .multiple(true)
      .dest("dependencies")
      .help("The maven dependencies to use for bootstrapping the application (group:artifact:version), e.g.: nz.ac.waikato.cms.weka:weka-dev:3.9.4");
    parser.addOption("-p")
      .required(false)
      .dest("pom_template")
      .help("The alternative template for the pom.xml to use.");
    parser.addOption("-o")
      .required(true)
      .dest("output_dir")
      .help("The directory to output the bootstrapped application in.");
    parser.addOption("-c")
      .required(false)
      .dest("main_class")
      .help("The main class to execute after bootstrapping the application.");
    parser.addOption("-v")
      .required(false)
      .multiple(true)
      .dest("jvm")
      .help("The parameters to pass to the JVM before launching the application.");

    return parser;
  }

  /**
   * Sets the parsed options.
   *
   * @param ns		the parsed options
   * @return		if successfully set
   */
  protected boolean setOptions(Namespace ns) {
    mavenHome(ns.getString("maven_home"));
    javaHome(ns.getString("java_home"));
    outputDir(ns.getString("output_dir"));
    jvm(ns.getList("jvm"));
    dependencies(ns.getList("dependencies"));
    pomTemplate(ns.getString("pom_template"));
    mainClass(ns.getString("main_class"));
    return true;
  }

  /**
   * Parses the options and configures the object.
   *
   * @param options	the command-line options
   * @return		true if successfully set (or help requested)
   */
  public boolean setOptions(String[] options) {
    ArgumentParser 	parser;
    Namespace 		ns;

    parser = getParser();
    try {
      ns = parser.parseArgs(options);
    }
    catch (ArgumentParserException e) {
      parser.handleError(e);
      return parser.getHelpRequested();
    }

    return setOptions(ns);
  }

  /**
   * Performs the bootstrapping.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String		actMavenHome;
    File		actMavenHomeDir;
    String		actPomTemplate;
    File		actPomTemplateFile;
    String		actJavaHome;
    File		actJavaHomeDir;
    File		outputDir;
    InvocationRequest 	request;
    Invoker 		invoker;
    List<String>	cmd;
    ProcessBuilder 	builder;
    Process		process;
    int			exitCode;

    // maven
    if ((m_MavenHome == null) || m_MavenHome.isEmpty())
      actMavenHome = "TODO";  // TODO extract and use bundled Maven
    else
      actMavenHome = m_MavenHome;
    actMavenHomeDir = new File(actMavenHome);
    if (!actMavenHomeDir.exists())
      return "Maven home does not exist: " + actMavenHomeDir;
    if (!actMavenHomeDir.isDirectory())
      return "Maven home is not a directory: " + actMavenHomeDir;
    System.setProperty("maven.home", actMavenHome);

    // template
    if ((m_PomTemplate == null) || m_PomTemplate.isEmpty())
      actPomTemplate = "TODO"; // TODO extract from classpath
    else
      actPomTemplate = m_PomTemplate;
    actPomTemplateFile = new File(actPomTemplate);
    if (!actPomTemplateFile.exists())
      return "pom.xml template does not exist: " + actPomTemplateFile;
    if (actPomTemplateFile.isDirectory())
      return "pom.xml template points to a directory: " + actPomTemplateFile;
    // TODO replace placeholders in template

    // output directory
    outputDir = new File(m_OutputDir);
    if (!outputDir.exists())
      return "Output directory does not exist: " + outputDir;
    if (!outputDir.isDirectory())
      return "Output directory is not a directory: " + outputDir;

    // bootstrap
    request = new DefaultInvocationRequest();
    request.setPomFile(new File(actPomTemplate));
    request.setGoals(Arrays.asList("clean", "package"));
    invoker = new DefaultInvoker();
    try {
      invoker.execute(request);
    }
    catch (Exception e) {
      e.printStackTrace();
      return "Failed to bootstrap the application: " + e;
    }

    // launch class
    if (m_MainClass != null) {
      if ((m_JavaHome == null) || m_JavaHome.isEmpty())
	actJavaHome = System.getProperty("java.home");
      else
	actJavaHome = m_JavaHome;
      actJavaHomeDir = new File(actJavaHome);
      if (!actJavaHomeDir.exists())
	return "Java home does not exist: " + actJavaHomeDir;
      if (!actJavaHomeDir.isDirectory())
	return "Java home is not a directory: " + actJavaHomeDir;

      cmd = new ArrayList<>();
      cmd.add(actJavaHomeDir + "/bin/java");
      if (m_JVM != null)
        cmd.addAll(m_JVM);
      cmd.add("-cp");
      cmd.add(outputDir.getAbsolutePath() + "/lib/*");
      cmd.add(m_MainClass);
      builder = new ProcessBuilder(cmd);
      try {
	process = builder.start();
	exitCode = process.waitFor();
	if (exitCode != 0)
	  return "Failed to launch class (" + builder.command() + "): " + exitCode;
      }
      catch (Exception e) {
        e.printStackTrace();
        return "Failed to launch class: " + e;
      }
    }

    return null;
  }

  /**
   * Executes the bootstrapping with the specified command-line arguments.
   *
   * @param args	the options to use
   */
  public static void main(String[] args) {
    Main main = new Main();
    if (!main.setOptions(args)) {
      System.err.println("Failed to parse options!");
      System.exit(1);
    }
    String result = main.execute();
    if (result != null) {
      System.err.println("Failed to perform bootstrapping:\n" + result);
      System.exit(2);
    }
  }
}
