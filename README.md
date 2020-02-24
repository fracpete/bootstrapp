# bootstrapp
Command-line tool for bootstrapping Maven applications just by using the dependencies.


## Command-line options

```
Usage: [--help] [-m MAVEN_HOME]
       [-u MAVEN_USER_SETTINGS] [-j JAVA_HOME] [-d DEPENDENCIES...] [-D DEPENDENCY_FILES...] [-s]
       [-p POM_TEMPLATE] -o OUTPUT_DIR [-c MAIN_CLASS] [-v JVM...] [-e] [-b] [-l]

Options:
-m, --maven_home MAVEN_HOME
	The directory with a local Maven installation to use instead of the bundled one.

-u, --maven_user_settings MAVEN_USER_SETTINGS
	The file with the maven user settings to use other than $HOME/.m2/settings.xml.

-j, --java_home JAVA_HOME
	The Java home to use for the Maven execution.

-d, --dependency DEPENDENCIES
	The maven dependencies to use for bootstrapping the application (group:artifact:version), e.g.: nz.ac.waikato.cms.weka:weka-dev:3.9.4

-D, --dependency-file DEPENDENCY_FILES
	The file(s) with maven dependencies to use for bootstrapping the application (group:artifact:version), one dependency per line.

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

-l, --launch
	If enabled, the supplied main class will get launched.
```

## Example

The following example will bootstrap Weka 3.9.4 (incl. sources)
in directory `./out` and create start scripts (shell/batch) for
Weka's GUIChooser, using 1GB of heap size:

```
java com.github.fracpete.bootstrapp.Main \
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

## Releases

Below are executable spring-boot jars for download that can be executed
via `java -jar XYZ.jar [options]`:

* [0.0.3](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.3/bootstrapp-0.0.3-spring-boot.jar)
* [0.0.2](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.2/bootstrapp-0.0.2-spring-boot.jar)
* [0.0.1](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.0.1/bootstrapp-0.0.1-spring-boot.jar)


## Maven

```xml
    <dependency>
      <groupId>com.github.fracpete</groupId>
      <artifactId>bootstrapp</artifactId>
      <version>0.0.3</version>
    </dependency>
```


