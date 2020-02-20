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

import com.github.fracpete.bootstrapp.core.Maven;
import com.github.fracpete.bootstrapp.core.Template;
import com.github.fracpete.processoutput4j.core.impl.SimpleStreamingProcessOwner;
import com.github.fracpete.processoutput4j.output.StreamingProcessOutput;
import com.github.fracpete.simpleargparse4j.ArgumentParser;
import com.github.fracpete.simpleargparse4j.ArgumentParserException;
import com.github.fracpete.simpleargparse4j.Namespace;
import com.github.fracpete.simpleargparse4j.Option.Type;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command-line application for bootstrapping a Maven appplication.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Main {

  /** the alternative maven installation. */
  protected File m_MavenHome;

  /** the actual maven home to use. */
  protected transient File m_ActMavenHome;

  /** the alternative java installation. */
  protected File m_JavaHome;

  /** the actual Java home to use. */
  protected transient File m_ActJavaHome;

  /** the output directory. */
  protected File m_OutputDir;

  /** the output directory for maven. */
  protected File m_OutputDirMaven;

  /** the JVM options. */
  protected List<String> m_JVM;

  /** the dependencies. */
  protected List<String> m_Dependencies;

  /** the pom template. */
  protected File m_PomTemplate;

  /** the actual POM template to use. */
  protected transient File m_ActPomTemplate;

  /** the main class to launch. */
  protected String m_MainClass;

  /** whether to retrieve source jars or not. */
  protected boolean m_Sources;

  /** whether to generate start scripts (when supplying a main class). */
  protected boolean m_Scripts;

  /** whether to launch the main class. */
  protected boolean m_Launch;

  /** for logging. */
  protected Logger m_Logger;

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
    m_MavenHome      = null;
    m_JavaHome       = null;
    m_OutputDir      = null;
    m_OutputDirMaven = null;
    m_JVM            = null;
    m_Dependencies   = null;
    m_MavenHome      = null;
    m_PomTemplate    = null;
    m_Sources        = false;
    m_Scripts        = false;
    m_Launch         = false;
    m_Logger         = null;
  }

  /**
   * Returns the logger instance to use.
   *
   * @return		the logger
   */
  protected Logger getLogger() {
    if (m_Logger == null)
      m_Logger = Logger.getLogger(getClass().getName());
    return m_Logger;
  }

  /**
   * Sets the alternative maven installation to use.
   *
   * @param dir		the top-level directory (above "bin")
   * @return		itself
   */
  public Main mavenHome(File dir) {
    m_MavenHome = dir;
    return this;
  }

  /**
   * Returns the alternative maven installation to use.
   *
   * @return		the directory, null to use bundled one
   */
  public File getMavenHome() {
    return m_MavenHome;
  }

  /**
   * Sets the alternative java installation to use.
   *
   * @param dir		the top-level directory (above "bin")
   * @return		itself
   */
  public Main javaHome(File dir) {
    m_JavaHome = dir;
    return this;
  }

  /**
   * Returns the alternative java installation to use.
   *
   * @return		the directory, null if using one that class was started with
   */
  public File getJavaHome() {
    return m_JavaHome;
  }

  /**
   * Sets the output directory for the bootstrapped application.
   *
   * @param dir		the directory
   * @return		itself
   */
  public Main outputDir(File dir) {
    m_OutputDir = dir;
    return this;
  }

  /**
   * Returns the output directory for the bootstrapped application.
   *
   * @return		the directory, null if none set
   */
  public File getOutputDir() {
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
   * Sets whether to retrieve the source jars as well.
   *
   * @param sources	true if to get sources
   * @return		itself
   */
  public Main sources(boolean sources) {
    m_Sources = sources;
    return this;
  }

  /**
   * Returns whether to download source jars as well.
   *
   * @return		true if to get sources
   */
  public boolean getSources() {
    return m_Sources;
  }

  /**
   * Sets the template for the POM to use.
   *
   * @param template	the template
   * @return		itself
   */
  public Main pomTemplate(File template) {
    m_PomTemplate = template;
    return this;
  }

  /**
   * Returns the template for the pom.xml to use.
   *
   * @return		the POM template, null if using the default
   */
  public File getPomTemplate() {
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
   * Sets whether to create scripts for launching the main class.
   *
   * @param scripts	true if to create scripts
   * @return		itself
   */
  public Main scripts(boolean scripts) {
    m_Scripts = scripts;
    return this;
  }

  /**
   * Returns whether to create scripts for launching the main class.
   *
   * @return		true if to create scripts
   */
  public boolean getScripts() {
    return m_Scripts;
  }

  /**
   * Sets whether to launch the main class.
   *
   * @param launch	true if to launch
   * @return		itself
   */
  public Main launch(boolean launch) {
    m_Launch = launch;
    return this;
  }

  /**
   * Returns whether to launch the main class.
   *
   * @return		true if to launch
   */
  public boolean getLaunch() {
    return m_Launch;
  }

  /**
   * Configures and returns the commandline parser.
   *
   * @return		the parser
   */
  protected ArgumentParser getParser() {
    ArgumentParser 		parser;

    parser = new ArgumentParser("");
    parser.addOption("-m", "--maven_home")
      .required(false)
      .type(Type.EXISTING_DIR)
      .dest("maven_home")
      .help("The directory with a local Maven installation to use instead of the bundled one.");
    parser.addOption("-j", "--java_home")
      .required(false)
      .type(Type.EXISTING_DIR)
      .dest("java_home")
      .help("The Java home to use for the Maven execution.");
    parser.addOption("-d", "--dependency")
      .required(true)
      .multiple(true)
      .dest("dependencies")
      .help("The maven dependencies to use for bootstrapping the application (group:artifact:version), e.g.: nz.ac.waikato.cms.weka:weka-dev:3.9.4");
    parser.addOption("-s", "--sources")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("sources")
      .help("If enabled, source jars of the Maven artifacts will get downloaded as well and stored in a separated directory.");
    parser.addOption("-p", "--pom_template")
      .required(false)
      .type(Type.EXISTING_FILE)
      .dest("pom_template")
      .help("The alternative template for the pom.xml to use.");
    parser.addOption("-o", "--output_dir")
      .required(true)
      .type(Type.DIRECTORY)
      .dest("output_dir")
      .help("The directory to output the bootstrapped application in.");
    parser.addOption("-c", "--main_class")
      .required(false)
      .dest("main_class")
      .help("The main class to execute after bootstrapping the application.");
    parser.addOption("-v", "--jvm")
      .required(false)
      .multiple(true)
      .dest("jvm")
      .help("The parameters to pass to the JVM before launching the application.");
    parser.addOption("-e", "--scripts")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("scripts")
      .help("If enabled, shell/batch scripts get generated to launch the main class.");
    parser.addOption("-l", "--launch")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("launch")
      .help("If enabled, the supplied main class will get launched.");

    return parser;
  }

  /**
   * Sets the parsed options.
   *
   * @param ns		the parsed options
   * @return		if successfully set
   */
  protected boolean setOptions(Namespace ns) {
    mavenHome(ns.getFile("maven_home"));
    javaHome(ns.getFile("java_home"));
    outputDir(ns.getFile("output_dir"));
    jvm(ns.getList("jvm"));
    dependencies(ns.getList("dependencies"));
    sources(ns.getBoolean("sources"));
    pomTemplate(ns.getFile("pom_template"));
    mainClass(ns.getString("main_class"));
    scripts(ns.getBoolean("scripts"));
    launch(ns.getBoolean("launch"));
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
   * Initializes Maven support.
   *
   * @return		null if successful, otherwise error message
   * @see		#m_ActMavenHome
   */
  protected String initMavenHome() {
    String	result;

    if (m_MavenHome == null) {
      if ((result = Maven.initBundledMaven()) != null)
        return result;
      m_ActMavenHome = new File(Maven.homeDir());
    }
    else {
      m_ActMavenHome = m_MavenHome;
    }
    if (!m_ActMavenHome.exists())
      return "Maven home does not exist: " + m_ActMavenHome;
    if (!m_ActMavenHome.isDirectory())
      return "Maven home is not a directory: " + m_ActMavenHome;
    System.setProperty("maven.home", m_ActMavenHome.getAbsolutePath());

    return null;
  }

  /**
   * Initializes Java (if a main class has been supplied).
   *
   * @return		null if successful, otherwise error message
   * @see		#m_JavaHome
   */
  protected String initJavaHome() {
    if (m_MainClass != null) {
      if (m_JavaHome == null)
	m_ActJavaHome = new File(System.getProperty("java.home"));
      else
	m_ActJavaHome = m_JavaHome;
      if (!m_ActJavaHome.exists())
	return "Java home does not exist: " + m_ActJavaHome;
      if (!m_ActJavaHome.isDirectory())
	return "Java home is not a directory: " + m_ActJavaHome;
    }
    return null;
  }

  /**
   * Initializes the output directory.
   *
   * @return		null if successful, otherwise error message
   */
  protected String initOutputDir() {
    if (!m_OutputDir.exists()) {
      if (!m_OutputDir.mkdirs())
	return "Failed to create output directory: " + m_OutputDir;
    }
    if (!m_OutputDir.isDirectory())
      return "Output directory is not a directory: " + m_OutputDir;

    m_OutputDirMaven = new File(m_OutputDir.getAbsolutePath() + "/output");

    return null;
  }

  /**
   * Initializes the POM template.
   *
   * @return		null if successful, otherwise error message
   */
  protected String initPomTemplate() {
    String	result;

    if (m_PomTemplate == null) {
      result = Template.configureBundledTemplate(m_OutputDir, m_OutputDirMaven, m_Dependencies, !m_Sources);
    }
    else {
      if (!m_PomTemplate.exists())
	return "pom.xml template does not exist: " + m_PomTemplate;
      if (m_PomTemplate.isDirectory())
	return "pom.xml template points to a directory: " + m_PomTemplate;
      result = Template.configureTemplate(m_PomTemplate, m_OutputDir, m_OutputDir, m_Dependencies, !m_Sources);
    }

    if (result == null)
      m_ActPomTemplate = new File(m_OutputDir.getAbsolutePath() + "/pom.xml");

    return result;
  }

  /**
   * Executes maven to pull in the artifacts.
   *
   * @return		null if successful, otherwise error message
   */
  protected String executeMaven() {
    InvocationRequest 	request;
    Invoker 		invoker;

    request = new DefaultInvocationRequest();
    request.setPomFile(m_ActPomTemplate);
    request.setGoals(Arrays.asList("clean", "package"));
    invoker = new DefaultInvoker();
    try {
      invoker.execute(request);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to bootstrap the application!", e);
      return "Failed to bootstrap the application: " + e;
    }

    return null;
  }

  /**
   * Builds and returns the launch command for the main class.
   *
   * @return		the command
   */
  protected List<String> buildLaunchCommand(String javaBinary, String libDir) {
    List<String> 	result;

    result = new ArrayList<>();
    result.add(javaBinary);
    if (m_JVM != null)
      result.addAll(m_JVM);
    result.add("-cp");
    result.add(libDir);
    result.add(m_MainClass);

    return result;
  }

  /**
   * Generates startup scripts if a main class was supplied.
   *
   * @return		null if successful, otherwise error message
   */
  protected String createScripts() {
    List<String>	cmd;
    StringBuilder	script;
    File		dir;
    File		file;

    if (m_MainClass != null) {
      dir = new File(m_OutputDirMaven.getAbsolutePath() + "/bin");
      if (!dir.mkdirs())
        return "Failed to create directory for scripts: " + dir;

      // shell
      cmd = buildLaunchCommand("java", "\"$CP\"");
      file = new File(dir.getAbsolutePath() + "/start.sh");
      script = new StringBuilder();
      script.append("#!/bin/bash\n");
      script.append("#\n");
      script.append("BASEDIR=`dirname $0`/..\n");
      script.append("BASEDIR=`(cd \"$BASEDIR\"; pwd)`\n");
      script.append("LIB=\"$BASEDIR\"/lib\n");
      script.append("CP=\"$LIB/*\"\n");
      for (String c: cmd)
        script.append(c).append(" ");
      script.append("\n");
      try {
	Files.write(file.toPath(), script.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	file.setExecutable(true);
      }
      catch (Exception e) {
        getLogger().log(Level.SEVERE, "Failed to write shell script to: " + file, e);
        return "Failed to write shell script to '" + file + "': " + e;
      }

      // batch
      cmd = buildLaunchCommand("java", "\"%CP%\"");
      file = new File(dir.getAbsolutePath() + "/start.bat");
      script = new StringBuilder();
      script.append("@echo off\n");
      script.append("\n");
      script.append("set BASEDIR=%~dp0\\..\n");
      script.append("set LIB=%BASEDIR%\\lib\n");
      script.append("set CP=%LIB%\\*\n");
      for (String c: cmd)
        script.append(c).append(" ");
      script.append("\n");
      try {
	Files.write(file.toPath(), script.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      }
      catch (Exception e) {
        getLogger().log(Level.SEVERE, "Failed to write batch script to: " + file, e);
        return "Failed to write batch script to '" + file + "': " + e;
      }
    }

    return null;
  }

  /**
   * Launches the main class, if provided.
   *
   * @return		null if successful, otherwise error message
   */
  protected String launchMainClass() {
    List<String>	cmd;
    ProcessBuilder 	builder;
    int			exitCode;

    if (m_MainClass != null) {
      cmd = buildLaunchCommand(m_ActJavaHome.getAbsolutePath() + "/bin/java", m_OutputDirMaven.getAbsolutePath() + "/lib/*");
      builder = new ProcessBuilder(cmd);
      try {
        StreamingProcessOutput output = new StreamingProcessOutput(new SimpleStreamingProcessOwner());
	output.monitor(builder);
	exitCode = output.getExitCode();
	if (exitCode != 0)
	  return "Failed to launch class (" + builder.command() + "): " + exitCode;
      }
      catch (Exception e) {
        getLogger().log(Level.SEVERE, "Failed to launch class!", e);
        return "Failed to launch class: " + e;
      }
    }

    return null;
  }

  /**
   * Performs the bootstrapping.
   *
   * @return		null if successful, otherwise error message
   */
  protected String doExecute() {
    String		result;

    // initialize
    m_ActMavenHome   = null;
    m_ActJavaHome    = null;
    m_ActPomTemplate = null;
    if ((result = initMavenHome()) != null)
      return result;
    if ((result = initJavaHome()) != null)
      return result;
    if ((result = initOutputDir()) != null)
      return result;
    if ((result = initPomTemplate()) != null)
      return result;

    // bootstrap
    if ((result = executeMaven()) != null)
      return result;

    // main class
    if (getScripts() && (result = createScripts()) != null)
      return result;
    if (getLaunch() && (result = launchMainClass()) != null)
      return result;

    return null;
  }

  /**
   * Performs the bootstrapping.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String		result;

    result = doExecute();
    if (result != null)
      getLogger().severe(result);

    return result;
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
