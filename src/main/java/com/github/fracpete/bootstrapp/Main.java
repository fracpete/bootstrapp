/*
 * Main.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.bootstrapp;

import com.github.fracpete.bootstrapp.core.Maven;
import com.github.fracpete.bootstrapp.core.Resources;
import com.github.fracpete.bootstrapp.core.Template;
import com.github.fracpete.bootstrapp.core.Template.Configuration;
import com.github.fracpete.processoutput4j.core.impl.SimpleStreamingProcessOwner;
import com.github.fracpete.processoutput4j.output.StreamingProcessOutput;
import com.github.fracpete.resourceextractor4j.Content;
import com.github.fracpete.simpleargparse4j.ArgumentParser;
import com.github.fracpete.simpleargparse4j.ArgumentParserException;
import com.github.fracpete.simpleargparse4j.Namespace;
import com.github.fracpete.simpleargparse4j.Option.Type;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

import java.io.File;
import java.io.FilenameFilter;
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

  /** the maven user settings to use. */
  protected File m_MavenUserSettings;

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

  /** the dependency files. */
  protected List<File> m_DependencyFiles;

  /** the external jar files/dirs. */
  protected List<File> m_ExternalJars;

  /** the pom template. */
  protected File m_PomTemplate;

  /** the actual POM template to use. */
  protected transient File m_ActPomTemplate;

  /** the name of the projet. */
  protected String m_Name;

  /** the version of the project. */
  protected String m_Version;

  /** whether to call the "clean" goal. */
  protected boolean m_Clean;

  /** the main class to launch. */
  protected String m_MainClass;

  /** whether to retrieve source jars or not. */
  protected boolean m_Sources;

  /** the external source jar files/dirs. */
  protected List<File> m_ExternalSources;

  /** whether to generate start scripts (when supplying a main class). */
  protected boolean m_Scripts;

  /** whether to create spring-boot jar. */
  protected boolean m_SpringBoot;

  /** whether to build .deb package. */
  protected boolean m_Debian;

  /** the custom debian maven snippet to use. */
  protected File m_DebianSnippet;

  /** whether to build .rpm package. */
  protected boolean m_Redhat;

  /** the custom redhat maven snippet to use. */
  protected File m_RedhatSnippet;

  /** whether to launch the main class. */
  protected boolean m_Launch;

  /** for logging. */
  protected Logger m_Logger;

  /** whether help got requested. */
  protected boolean m_HelpRequested;

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
    m_MavenHome         = null;
    m_MavenUserSettings = null;
    m_JavaHome          = null;
    m_OutputDir         = null;
    m_OutputDirMaven    = null;
    m_JVM               = null;
    m_Dependencies      = null;
    m_DependencyFiles   = null;
    m_ExternalJars      = null;
    m_PomTemplate       = null;
    m_Name              = Template.DEFAULT_NAME;
    m_Version           = Template.DEFAULT_VERSION;
    m_Clean             = false;
    m_Sources           = false;
    m_ExternalSources   = null;
    m_Scripts           = false;
    m_Launch            = false;
    m_SpringBoot        = false;
    m_Debian            = false;
    m_DebianSnippet     = null;
    m_Redhat            = false;
    m_RedhatSnippet     = null;
    m_Logger            = null;
    m_HelpRequested     = false;
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
   * @return		the directory, null to use downloaded one
   */
  public File getMavenHome() {
    return m_MavenHome;
  }

  /**
   * Sets the alternative maven user settings to use.
   *
   * @param dir		the XML file, null to use default ($HOME/.m2/settings.xml)
   * @return		itself
   */
  public Main mavenUserSettings(File dir) {
    m_MavenUserSettings = dir;
    return this;
  }

  /**
   * Returns the alternative maven user settings to use.
   *
   * @return		the file, null to use default ($HOME/.m2/settings.xml)
   */
  public File getMavenUserSettings() {
    return m_MavenUserSettings;
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
   * Sets the name for the project.
   *
   * @param name	the name
   * @return		itself
   */
  public Main name(String name) {
    m_Name = name;
    return this;
  }

  /**
   * Returns the name for the project.
   *
   * @return		the name
   */
  public String getName() {
    return m_Name;
  }

  /**
   * Sets the version of the project.
   *
   * @param version	the version
   * @return		itself
   */
  public Main version(String version) {
    m_Version = version;
    return this;
  }

  /**
   * Returns the version of the project.
   *
   * @return		the version
   */
  public String getVersion() {
    return m_Version;
  }

  /**
   * Sets the dependencies to use for bootstrapping.
   *
   * @param dependencies	the dependencies, can be null
   * @return		itself
   */
  public Main dependencies(List<String> dependencies) {
    m_Dependencies = dependencies;
    return this;
  }

  /**
   * Sets the dependencies to use for bootstrapping.
   *
   * @param dependencies	the dependencies, can be null
   * @return		itself
   */
  public Main dependencies(String... dependencies) {
    if (dependencies != null)
      m_Dependencies = new ArrayList<>(Arrays.asList(dependencies));
    else
      m_Dependencies = null;
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
   * Sets the dependency files to use for bootstrapping (one dependency per line).
   *
   * @param files	the dependencies, can be null
   * @return		itself
   */
  public Main dependencyFiles(List<File> files) {
    m_DependencyFiles = files;
    return this;
  }

  /**
   * Sets the dependency files to use for bootstrapping (one dependency per line).
   *
   * @param files	the dependency files, can be null
   * @return		itself
   */
  public Main dependencyFiles(File... files) {
    if (files != null)
      m_DependencyFiles = new ArrayList<>(Arrays.asList(files));
    else
      m_DependencyFiles = null;
    return this;
  }

  /**
   * Returns the dependency files.
   *
   * @return		the files, can be null
   */
  public List<File> getDependencyFiles() {
    return m_DependencyFiles;
  }

  /**
   * Combines manually set dependencies and ones from files into one list.
   *
   * @return		all dependencies
   */
  public List<String> getAllDependencies() {
    List<String>	result;
    List<String> 	lines;

    result = new ArrayList<>();
    if (m_Dependencies != null)
      result.addAll(m_Dependencies);

    if (m_DependencyFiles != null) {
      for (File depFile: m_DependencyFiles) {
        try {
          getLogger().info("Reading dependency file: " + depFile);
          lines = Files.readAllLines(depFile.toPath());
          for (String line: lines) {
            line = line.trim();
            if (line.isEmpty())
              continue;
            if (!line.contains(":"))
              continue;
            result.add(line);
	  }
	}
	catch (Exception e) {
          getLogger().log(Level.SEVERE, "Failed to read dependency file: " + depFile, e);
	}
      }
    }

    return result;
  }

  /**
   * Sets the external jar files/dirs to use.
   *
   * @param external	the files/dirs, null to unset
   * @return		itself
   */
  public Main externalJars(List<File> external) {
    m_ExternalJars = external;
    return this;
  }

  /**
   * Sets the external jar files/dirs to use.
   *
   * @param external	the files/dirs, null to unset
   * @return		itself
   */
  public Main externalJars(File... external) {
    if (external == null)
      m_ExternalJars = null;
    else
      externalJars(Arrays.asList(external));
    return this;
  }

  /**
   * Returns the currently set external jar files/dirs.
   * 
   * @return		the files/dirs, null if none set
   */
  public List<File> getExternalJars() {
    return m_ExternalJars;
  }
  
  /**
   * Sets whether to execute the "clean" goal.
   *
   * @param clean	true if to clean
   * @return		itself
   */
  public Main clean(boolean clean) {
    m_Clean = clean;
    return this;
  }

  /**
   * Returns whether to execute the "clean" goal.
   *
   * @return		true if to clean
   */
  public boolean getClean() {
    return m_Clean;
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
   * Sets the external source files/dirs to use.
   *
   * @param external	the files/dirs, null to unset
   * @return		itself
   */
  public Main externalSources(List<File> external) {
    m_ExternalSources = external;
    return this;
  }

  /**
   * Sets the external source files/dirs to use.
   *
   * @param external	the files/dirs, null to unset
   * @return		itself
   */
  public Main externalSources(File... external) {
    if (external == null)
      m_ExternalSources = null;
    else
      externalSources(Arrays.asList(external));
    return this;
  }

  /**
   * Returns the currently set external source files/dirs.
   * 
   * @return		the files/dirs, null if none set
   */
  public List<File> getExternalSources() {
    return m_ExternalSources;
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
   * Sets the JVM options to use for launching the main class.
   *
   * @param options	the options, can be null
   * @return		itself
   */
  public Main jvm(String... options) {
    if (options != null)
      m_JVM = new ArrayList<>(Arrays.asList(options));
    else
      m_JVM = null;
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
   * Sets whether to create spring-boot jar.
   *
   * @param springBoot	true if to generate jar
   * @return		itself
   */
  public Main springBoot(boolean springBoot) {
    m_SpringBoot = springBoot;
    return this;
  }

  /**
   * Returns whether to create spring-boot jar.
   *
   * @return		true if to generate jar
   */
  public boolean getSpringBoot() {
    return m_SpringBoot;
  }

  /**
   * Sets whether to generate .deb package.
   *
   * @param debian	true if to generate .deb
   * @return		itself
   */
  public Main debian(boolean debian) {
    m_Debian = debian;
    return this;
  }

  /**
   * Returns whether to generate .deb package.
   *
   * @return		true if to generate .deb
   */
  public boolean getDebian() {
    return m_Debian;
  }

  /**
   * Sets the file containing the custom maven snippet file for generating the debian package.
   *
   * @param snippet	the file
   * @return		itself
   */
  public Main debianSnippet(File snippet) {
    m_DebianSnippet = snippet;
    return this;
  }

  /**
   * Returns the file containing the custom maven snippet file for generating the debian package.
   *
   * @return		the file
   */
  public File getDebianSnippet() {
    return m_DebianSnippet;
  }

  /**
   * Sets whether to generate .rpm package.
   *
   * @param redhat	true if to generate .rpm
   * @return		itself
   */
  public Main redhat(boolean redhat) {
    m_Redhat = redhat;
    return this;
  }

  /**
   * Returns whether to generate .rpm package.
   *
   * @return		true if to generate .rpm
   */
  public boolean getRedhat() {
    return m_Redhat;
  }

  /**
   * Sets the file containing the custom maven snippet file for generating the redhat package.
   *
   * @param snippet	the file
   * @return		itself
   */
  public Main redhatSnippet(File snippet) {
    m_RedhatSnippet = snippet;
    return this;
  }

  /**
   * Returns the file containing the custom maven snippet file for generating the redhat package.
   *
   * @return		the file
   */
  public File getRedhatSnippet() {
    return m_RedhatSnippet;
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

    parser = new ArgumentParser("Bootstrapping Java applications with Maven dependencies and/or jar files.");
    parser.addOption("-m", "--maven_home")
      .required(false)
      .type(Type.EXISTING_DIR)
      .dest("maven_home")
      .metaVar("DIR")
      .help("The directory with a local Maven installation to use instead of the downloaded one.");
    parser.addOption("-u", "--maven_user_settings")
      .required(false)
      .type(Type.EXISTING_FILE)
      .dest("maven_user_settings")
      .metaVar("FILE")
      .help("The file with the maven user settings to use other than $HOME/.m2/settings.xml.");
    parser.addOption("-j", "--java_home")
      .required(false)
      .type(Type.EXISTING_DIR)
      .dest("java_home")
      .metaVar("DIR")
      .help("The Java home to use for the Maven execution.");
    parser.addOption("-n", "--name")
      .required(false)
      .setDefault(Template.DEFAULT_NAME)
      .dest("name")
      .help("The name to use for the project in the pom.xml. Also used as library directory and executable name when generating Debian/Redhat packages.");
    parser.addOption("-V", "--version")
      .required(false)
      .setDefault(Template.DEFAULT_VERSION)
      .dest("version")
      .help("The version to use for the project in the pom.xml");
    parser.addOption("-d", "--dependency")
      .required(false)
      .multiple(true)
      .dest("dependencies")
      .metaVar("DEPENDENCY")
      .help("The maven dependencies to use for bootstrapping the application (group:artifact:version), e.g.: nz.ac.waikato.cms.weka:weka-dev:3.9.4");
    parser.addOption("-D", "--dependency-file")
      .required(false)
      .multiple(true)
      .type(Type.EXISTING_FILE)
      .dest("dependency_files")
      .metaVar("DEPENDENCY_FILE")
      .help("The file(s) with maven dependencies to use for bootstrapping the application (group:artifact:version), one dependency per line.");
    parser.addOption("-J", "--external-jar")
      .required(false)
      .multiple(true)
      .type(Type.EXISTING_FILE_OR_DIRECTORY)
      .dest("external_jars")
      .metaVar("JAR_OR_DIR")
      .help("The external jar or directory with jar files to also include in the application.");
    parser.addOption("-C", "--clean")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("clean")
      .help("If enabled, the 'clean' goals gets executed.");
    parser.addOption("-s", "--sources")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("sources")
      .help("If enabled, source jars of the Maven artifacts will get downloaded as well and stored in a separated directory.");
    parser.addOption("-S", "--external-source")
      .required(false)
      .multiple(true)
      .type(Type.EXISTING_FILE_OR_DIRECTORY)
      .dest("external_sources")
      .metaVar("JAR_OR_DIR")
      .help("The external source jar or directory with source jar files to also include in the application.");
    parser.addOption("-p", "--pom_template")
      .required(false)
      .type(Type.EXISTING_FILE)
      .dest("pom_template")
      .metaVar("FILE")
      .help("The alternative template for the pom.xml to use.");
    parser.addOption("-o", "--output_dir")
      .required(true)
      .type(Type.DIRECTORY)
      .dest("output_dir")
      .metaVar("DIR")
      .help("The directory to output the bootstrapped application in.");
    parser.addOption("-c", "--main_class")
      .required(false)
      .dest("main_class")
      .metaVar("CLASSNAME")
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
    parser.addOption("-b", "--spring-boot")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("spring_boot")
      .help("If enabled, a spring-boot jar is generated utilizing the main class (single jar with all dependencies contained).");
    parser.addOption("--deb")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("debian")
      .help("If enabled, a Debian .deb package is generated. Required tools: fakeroot, dpkg-deb");
    parser.addOption("--deb-snippet")
      .type(Type.EXISTING_FILE)
      .required(false)
      .dest("debian_snippet")
      .metaVar("FILE")
      .help("The custom Maven pom.xml snippet for generating a Debian package.");
    parser.addOption("--rpm")
      .type(Type.BOOLEAN)
      .setDefault(false)
      .dest("redhat")
      .help("If enabled, a Redhat .rpm package is generated.");
    parser.addOption("--rpm-snippet")
      .type(Type.EXISTING_FILE)
      .required(false)
      .dest("rpm_snippet")
      .metaVar("FILE")
      .help("The custom Maven pom.xml snippet for generating a Redhat package.");
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
    mavenUserSettings(ns.getFile("maven_user_settings"));
    javaHome(ns.getFile("java_home"));
    outputDir(ns.getFile("output_dir"));
    jvm(ns.getList("jvm"));
    name(ns.getString("name"));
    version(ns.getString("version"));
    dependencies(ns.getList("dependencies"));
    dependencyFiles(ns.getList("dependency_files"));
    externalJars(ns.getList("external_jars"));
    clean(ns.getBoolean("clean"));
    sources(ns.getBoolean("sources"));
    externalSources(ns.getList("external_sources"));
    pomTemplate(ns.getFile("pom_template"));
    mainClass(ns.getString("main_class"));
    scripts(ns.getBoolean("scripts"));
    springBoot(ns.getBoolean("spring_boot"));
    debian(ns.getBoolean("debian"));
    debianSnippet(ns.getFile("debian_snippet"));
    redhat(ns.getBoolean("redhat"));
    redhatSnippet(ns.getFile("redhat_snippet"));
    launch(ns.getBoolean("launch"));
    return true;
  }

  /**
   * Returns whether help got requested when setting the options.
   *
   * @return		true if help got requested
   */
  public boolean getHelpRequested() {
    return m_HelpRequested;
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

    m_HelpRequested = false;
    parser          = getParser();
    try {
      ns = parser.parseArgs(options);
    }
    catch (ArgumentParserException e) {
      parser.handleError(e);
      m_HelpRequested = parser.getHelpRequested();
      return m_HelpRequested;
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
      if ((result = Maven.initRemoteMaven()) != null)
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
    if (m_JavaHome == null)
      m_ActJavaHome = new File(System.getProperty("java.home"));
    else
      m_ActJavaHome = m_JavaHome;
    if (!m_ActJavaHome.exists())
      return "Java home does not exist: " + m_ActJavaHome;
    if (!m_ActJavaHome.isDirectory())
      return "Java home is not a directory: " + m_ActJavaHome;
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

    m_OutputDirMaven = new File(m_OutputDir.getAbsolutePath() + "/target");

    return null;
  }

  /**
   * Expands the jars/dirs to just jars.
   *
   * @param list	the list of jars/dirs to expand
   * @return		the expanded list
   */
  protected List<File> toJars(List<File> list) {
    List<File>		result;
    File[]		files;

    result = new ArrayList<>();
    for (File ext: m_ExternalJars) {
      if (ext.isDirectory()) {
	files = ext.listFiles(new FilenameFilter() {
	  @Override
	  public boolean accept(File dir, String name) {
	    return name.toLowerCase().endsWith(".jar");
	  }
	});
	if (files != null)
	  result.addAll(Arrays.asList(files));
      }
      else {
	result.add(ext);
      }
    }

    return result;
  }

  /**
   * Initializes the POM template.
   *
   * @return		null if successful, otherwise error message
   */
  protected String initPomTemplate() {
    String		result;
    Configuration	config;
    StringBuilder	buildPlugins;
    String		buildPlugin;
    List<String>	lines;

    config = new Configuration();
    config.outputDirMaven = m_OutputDirMaven;
    config.dependencies   = getAllDependencies();
    config.noSources      = !m_Sources;
    config.noSpringBoot   = !m_SpringBoot;
    config.mainClass      = m_MainClass;
    config.name           = m_Name;
    config.version        = m_Version;
    if (m_ExternalJars != null)
      config.externalJars = toJars(m_ExternalJars);
    if (m_ExternalSources != null)
      config.externalSources = toJars(m_ExternalSources);

    buildPlugins = new StringBuilder();
    if (m_Debian) {
      if (m_DebianSnippet != null) {
        try {
	  lines = Files.readAllLines(m_DebianSnippet.toPath());
	  for (String line: lines)
	    buildPlugins.append(line).append("\n");
	}
	catch (Exception e) {
          getLogger().log(Level.SEVERE, "Failed to load Debian maven snippet: " + m_DebianSnippet, e);
          return "Failed to load Debian maven snippet: " + m_DebianSnippet;
	}
      }
      else {
        if (m_Sources)
	  buildPlugin = Content.readString(Resources.LOCATION + "/" + Template.DEBIANBUILDSRC_FILE);
        else
	  buildPlugin = Content.readString(Resources.LOCATION + "/" + Template.DEBIANBUILD_FILE);
	buildPlugins.append(buildPlugin);
      }
    }
    if (m_Redhat) {
      if (m_RedhatSnippet != null) {
        try {
	  lines = Files.readAllLines(m_RedhatSnippet.toPath());
	  for (String line: lines)
	    buildPlugins.append(line).append("\n");
	}
	catch (Exception e) {
          getLogger().log(Level.SEVERE, "Failed to load Redhat maven snippet: " + m_RedhatSnippet, e);
          return "Failed to load Redhat maven snippet: " + m_RedhatSnippet;
	}
      }
      else {
        if (m_Sources)
	  buildPlugin = Content.readString(Resources.LOCATION + "/" + Template.REDHATBUILDSRC_FILE);
        else
	  buildPlugin = Content.readString(Resources.LOCATION + "/" + Template.REDHATBUILD_FILE);
	buildPlugins.append(buildPlugin);
      }
    }
    if (buildPlugins.length() > 0)
      config.buildPlugins = buildPlugins.toString();

    if (m_PomTemplate == null) {
      result = Template.configureBundledTemplate(m_OutputDir, config);
    }
    else {
      if (!m_PomTemplate.exists())
	return "pom.xml template does not exist: " + m_PomTemplate;
      if (m_PomTemplate.isDirectory())
	return "pom.xml template points to a directory: " + m_PomTemplate;
      result = Template.configureTemplate(m_PomTemplate, m_OutputDir, config);
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
    List<String>	goals;

    goals = new ArrayList<>();
    if (m_Clean)
      goals.add("clean");
    goals.add("package");
    if (m_Debian)
      goals.add("deb:package");

    request = new DefaultInvocationRequest();
    request.setPomFile(m_ActPomTemplate);
    request.setGoals(goals);
    request.setJavaHome(m_ActJavaHome);
    if (m_MavenUserSettings != null)
      request.setUserSettingsFile(m_MavenUserSettings);
    invoker = new DefaultInvoker();
    invoker.setMavenHome(m_ActMavenHome);
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
   * Generates startup shell script if a main class was supplied.
   *
   * @return		null if successful, otherwise error message
   */
  protected String createShellScript() {
    List<String>	cmd;
    StringBuilder	script;
    File		dir;
    File		file;

    if (m_MainClass != null) {
      dir = new File(m_OutputDirMaven.getAbsolutePath() + "/bin");
      if (!dir.exists()) {
	if (!dir.mkdirs())
	  return "Failed to create directory for shell script: " + dir;
      }

      cmd = buildLaunchCommand("java", "\"$CP\"");
      file = new File(dir.getAbsolutePath() + "/start.sh");
      script = new StringBuilder();
      script.append("#!/bin/bash\n");
      script.append("#\n");
      script.append("# Start script for " + m_Name + "\n");
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
    }

    return null;
  }

  /**
   * Generates startup batch script if a main class was supplied.
   *
   * @return		null if successful, otherwise error message
   */
  protected String createBatchScript() {
    List<String>	cmd;
    StringBuilder	script;
    File		dir;
    File		file;

    if (m_MainClass != null) {
      dir = new File(m_OutputDirMaven.getAbsolutePath() + "/bin");
      if (!dir.exists()) {
	if (!dir.mkdirs())
	  return "Failed to create directory for batch script: " + dir;
      }

      cmd = buildLaunchCommand("java", "\"%CP%\"");
      file = new File(dir.getAbsolutePath() + "/start.bat");
      script = new StringBuilder();
      script.append("@echo off\n");
      script.append("\n");
      script.append("REM Start script for " + m_Name + "\n");
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
   * Generates startup scripts if a main class was supplied.
   *
   * @return		null if successful, otherwise error message
   * @see		#createShellScript()
   * @see		#createBatchScript()
   */
  protected String createScripts() {
    String	result;

    if ((result = createShellScript()) != null)
      return result;
    if ((result = createBatchScript()) != null)
      return result;

    return null;
  }

  /**
   * Generates startup shell script for debian/redhat packages.
   *
   * @return		null if successful, otherwise error message
   */
  protected String createLaunchScript() {
    List<String>	cmd;
    StringBuilder	script;
    File		dir;
    File		file;

    if (m_MainClass == null)
      return "Cannot create launch script for Debian/Redhat packages without a main class!";

    dir = new File(m_OutputDir.getAbsolutePath());
    if (!dir.exists()) {
      if (!dir.mkdirs())
	return "Failed to create directory for launch script: " + dir;
    }

    cmd = buildLaunchCommand("java", "\"$CP\"");
    file = new File(dir.getAbsolutePath() + "/launch");
    script = new StringBuilder();
    script.append("#!/bin/bash\n");
    script.append("#\n");
    script.append("# Start script for " + m_Name + "\n");
    script.append("#\n");
    script.append("CP=\"/usr/lib/" + m_Name + "/*\"\n");
    for (String c: cmd)
      script.append(c).append(" ");
    script.append("\n");
    try {
      Files.write(file.toPath(), script.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      file.setExecutable(true);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to write launch script to: " + file, e);
      return "Failed to write launch script to '" + file + "': " + e;
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
    if (m_Debian || m_Redhat) {
      if ((result = createLaunchScript()) != null)
	return result;
    }

    // bootstrap application
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
    else if (main.getHelpRequested()) {
      System.exit(0);
    }

    String result = main.execute();
    if (result != null) {
      System.err.println("Failed to perform bootstrapping:\n" + result);
      System.exit(2);
    }
  }
}
