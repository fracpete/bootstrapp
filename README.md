# bootstrapp
Command-line tool for bootstrapping Java applications just by using Maven 
dependencies and/or external jar files. It allows you to generate cross-platform 
applications, by using shell scripts for Linux/Mac and batch files for Windows, 
or Linux packages for Debian (`.deb`) and/or Redhat (`.rpm`).


## Command-line options

```
Bootstrapping Java applications with Maven dependencies and/or jar files.


Usage: [--help] [-m DIR] [-u FILE] [-j DIR] [-n NAME] [-V VERSION]
       [-d DEPENDENCY...] [-D DEPENDENCY_FILE...]
       [-J JAR_OR_DIR...] [-C] [-s] [-S JAR_OR_DIR...]
       [-p FILE] -o DIR [-c CLASSNAME] [-v JVM...] [-e] [-b] [--deb]
       [--deb-snippet FILE] [--rpm] [--rpm-snippet FILE] [-l]

Options:
-m, --maven_home DIR
	The directory with a local Maven installation to use instead of the
	downloaded one.

-u, --maven_user_settings FILE
	The file with the maven user settings to use other than
	$HOME/.m2/settings.xml.

-j, --java_home DIR
	The Java home to use for the Maven execution.

-n, --name NAME
	The name to use for the project in the pom.xml. Also used as library
	directory and executable name when generating Debian/Redhat packages.

-V, --version VERSION
	The version to use for the project in the pom.xml

-d, --dependency DEPENDENCY
	The maven dependencies to use for bootstrapping the application
	(group:artifact:version), e.g.: nz.ac.waikato.cms.weka:weka-dev:3.9.4

-D, --dependency-file DEPENDENCY_FILE
	The file(s) with maven dependencies to use for bootstrapping the
	application (group:artifact:version), one dependency per line.

-J, --external-jar JAR_OR_DIR
	The external jar or directory with jar files to also include in the
	application.

-C, --clean
	If enabled, the 'clean' goals gets executed.

-s, --sources
	If enabled, source jars of the Maven artifacts will get downloaded as
	well and stored in a separated directory.

-S, --external-source JAR_OR_DIR
	The external source jar or directory with source jar files to also
	include in the application.

-p, --pom_template FILE
	The alternative template for the pom.xml to use.

-o, --output_dir DIR
	The directory to output the bootstrapped application in.

-c, --main_class CLASSNAME
	The main class to execute after bootstrapping the application.

-v, --jvm JVM
	The parameters to pass to the JVM before launching the application.

-e, --scripts
	If enabled, shell/batch scripts get generated to launch the main class.

-b, --spring-boot
	If enabled, a spring-boot jar is generated utilizing the main class
	(single jar with all dependencies contained).

--deb
	If enabled, a Debian .deb package is generated. Required tools: fakeroot,
	dpkg-deb

--deb-snippet FILE
	The custom Maven pom.xml snippet for generating a Debian package.

--rpm
	If enabled, a Redhat .rpm package is generated.

--rpm-snippet FILE
	The custom Maven pom.xml snippet for generating a Redhat package.

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

**Note:** The *name* is used as package name and executable for launching the
application, in this case you will get `/usr/bin/weka`.


## Releases

Below are executable spring-boot jars for download that can be executed
via `java -jar XYZ.jar [options]`:

* [0.1.2](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.2/bootstrapp-0.1.2-spring-boot.jar)
* [0.1.1](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.1/bootstrapp-0.1.1-spring-boot.jar)
* [0.1.0](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.0/bootstrapp-0.1.0-spring-boot.jar)
* [0.0.3](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.3/bootstrapp-0.0.3-spring-boot.jar)
* [0.0.2](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.2/bootstrapp-0.0.2-spring-boot.jar)
* [0.0.1](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.1/bootstrapp-0.0.1-spring-boot.jar)


## Maven

```xml
    <dependency>
      <groupId>com.github.fracpete</groupId>
      <artifactId>bootstrapp</artifactId>
      <version>0.1.2</version>
    </dependency>
```


