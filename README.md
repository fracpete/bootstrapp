# bootstrapp
Command-line tool for bootstrapping Maven applications just by using the 
dependencies. It allows you to generate cross-platform applications, by
using shell scripts for Linux/Mac and batch files for Windows, or Linux
packages for Debian (`.deb`) and/or Redhat (`.rpm`).


## Command-line options

```
Bootstrapping Maven applications by supplying only dependencies.

Usage: [--help] [-m MAVEN_HOME]
       [-u MAVEN_USER_SETTINGS]
       [-j JAVA_HOME] [-n NAME] [-V VERSION] [-d DEPENDENCIES...]
       [-D DEPENDENCY_FILES...] [-C] [-s] [-p POM_TEMPLATE] -o OUTPUT_DIR [-c MAIN_CLASS] [-v JVM...]
       [-e] [-b] [--deb] [--rpm] [-l]

Options:
-m, --maven_home MAVEN_HOME
	The directory with a local Maven installation to use instead of the downloaded one.

-u, --maven_user_settings MAVEN_USER_SETTINGS
	The file with the maven user settings to use other than $HOME/.m2/settings.xml.

-j, --java_home JAVA_HOME
	The Java home to use for the Maven execution.

-n, --name NAME
	The name to use for the project in the pom.xml

-V, --version VERSION
	The version to use for the project in the pom.xml

-d, --dependency DEPENDENCIES
	The maven dependencies to use for bootstrapping the application (group:artifact:version), e.g.: nz.ac.waikato.cms.weka:weka-dev:3.9.4

-D, --dependency-file DEPENDENCY_FILES
	The file(s) with maven dependencies to use for bootstrapping the application (group:artifact:version), one dependency per line.

-C, --clean
	If enabled, the 'clean' goals gets executed.

-s, --sources
	If enabled, source jars of the Maven artifacts will get downloaded as well and stored in a separated directory.

-p, --pom_template POM_TEMPLATE
	The alternative template for the pom.xml to use.

-o, --output_dir OUTPUT_DIR
	The directory to output the bootstrapped application in.

-c, --main_class MAIN_CLASS
	The main class to execute after bootstrapping the application.

-v, --jvm JVM
	The parameters to pass to the JVM before launching the application.

-e, --scripts
	If enabled, shell/batch scripts get generated to launch the main class.

-b, --spring-boot
	If enabled, a spring-boot jar is generated utilizing the main class (single jar with all dependencies contained).

--deb
	If enabled, a Debian .deb package is generated. Required tools: fakeroot, dpkg-deb

--rpm
	If enabled, a Redhat .rpm package is generated.

-l, --launch
	If enabled, the supplied main class will get launched.
```

## Examples

### Cross-platform

The following example will bootstrap Weka 3.9.4 (incl. sources)
in directory `./out` and create start scripts (shell/batch) for
Weka's GUIChooser, using 1GB of heap size:

```
java com.github.fracpete.bootstrapp.Main \
  -C \
  -d nz.ac.waikato.cms.weka:weka-dev:3.9.4 \
  -s \ 
  -o ./out \
  -v -Xmx1g \
  -c weka.gui.GUIChooser \
  -e
```

The same example from within Java:

```java
import com.github.fracpete.bootstrapp.Main;
import java.io.File;

public static class TestBootstrapp {
  
  public static void main(String[] args) throws Exception {
    Main main = new Main();
    String result = main
        .clean(true)
        .dependencies("nz.ac.waikato.cms.weka:weka-dev:3.9.4")
        .sources(true)
        .outputDir(new File("./out"))
        .jvm("-Xmx1g")
        .mainClass("weka.gui.GUIChooser")
        .scripts(true)
        .execute();
    if (result != null)
      System.err.println(result);
  }
}
```

### Debian package

Instead of generating a cross-platform application, the following example
will generated a .deb package for Debian-based Linux distributions:

```
java com.github.fracpete.bootstrapp.Main \
  -C \
  -d nz.ac.waikato.cms.weka:weka-dev:3.9.4 \
  -o ./out \
  -v -Xmx1g \
  -c weka.gui.GUIChooser \
  -n weka
  -V 3.9.4
  --deb
```

And again, from within Java:

```java
import com.github.fracpete.bootstrapp.Main;
import java.io.File;

public static class TestBootstrapp {
  
  public static void main(String[] args) throws Exception {
    Main main = new Main();
    String result = main
        .clean(true)
        .dependencies("nz.ac.waikato.cms.weka:weka-dev:3.9.4")
        .outputDir(new File("./out"))
        .jvm("-Xmx1g")
        .mainClass("weka.gui.GUIChooser")
        .name("weka")
        .version("3.9.4")
        .debian(true)
        .execute();
    if (result != null)
      System.err.println(result);
  }
}
```

## Releases

Below are executable spring-boot jars for download that can be executed
via `java -jar XYZ.jar [options]`:

* [0.1.0](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.0/bootstrapp-0.1.0-spring-boot.jar)
* [0.0.3](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.3/bootstrapp-0.0.3-spring-boot.jar)
* [0.0.2](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.2/bootstrapp-0.0.2-spring-boot.jar)
* [0.0.1](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.1/bootstrapp-0.0.1-spring-boot.jar)


## Maven

```xml
    <dependency>
      <groupId>com.github.fracpete</groupId>
      <artifactId>bootstrapp</artifactId>
      <version>0.1.0</version>
    </dependency>
```


