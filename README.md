# bootstrapp
Command-line tool for bootstrapping Java applications by using Maven dependencies 
and/or external jar files. It allows you to generate cross-platform applications, 
by using shell scripts for Linux/Mac and batch files for Windows, or Linux 
packages for Debian (`.deb`) and/or Redhat (`.rpm`). [Docker](https://www.docker.com/) 
images can be generated as well by generating `Dockerfile` output.


## Command-line options

```
Bootstrapping Java applications with Maven dependencies and/or jar files.


Usage: [--help] [-m DIR] [-u FILE] [-j DIR] [-n NAME] [-V VERSION]
       [-d DEPENDENCY...] [-D DEPENDENCY_FILE...]
       [-J JAR_OR_DIR...] [-x EXCLUSION...] [-r REPOSITORY...] [-C]
       [-s] [-S JAR_OR_DIR...] [-p FILE] -o DIR [-c CLASSNAME]
       [-v JVM...] [-e] [-l] [-b] [--deb] [--deb_snippet FILE] [--rpm]
       [--rpm_snippet FILE] [--docker] [--docker_base_image IMAGE]
       [--docker_snippet FILE] [-z]

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

-D, --dependency_file DEPENDENCY_FILE
	The file(s) with maven dependencies to use for bootstrapping the
	application (group:artifact:version), one dependency per line.

-J, --external-jar JAR_OR_DIR
	The external jar or directory with jar files to also include in the
	application.

-x, --exclusion EXCLUSION
	The maven artifacts to exclude from all the dependencies
	('group:artifact').

-r, --repository REPOSITORY
	The maven repository to use for bootstrapping the application (id;name;
	url), e.g.: bedatadriven;bedatadriven public repo;
	https://nexus.bedatadriven.com/content/groups/public/

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

-l, --launch
	If enabled, the supplied main class will get launched.

-b, --spring_boot
	If enabled, a spring-boot jar is generated utilizing the main class
	(single jar with all dependencies contained).

--deb
	If enabled, a Debian .deb package is generated. Required tools: fakeroot,
	dpkg-deb

--deb_snippet FILE
	The custom Maven pom.xml snippet for generating a Debian package.

--rpm
	If enabled, a Redhat .rpm package is generated.

--rpm_snippet FILE
	The custom Maven pom.xml snippet for generating a Redhat package.

--docker
	If enabled, a Dockerfile is generated.

--docker_base_image IMAGE
	The base image to use for the docker image, e.g.,
	'openjdk:11-jdk-slim-buster'.

--docker_snippet FILE
	The file with custom docker instructions.

-z, --compress_dir_structure
	If enabled, the directory structure gets compressed (ie 'target' left
	out). However, side-effect in combination with '--clean' is that the
	'pom.xml' disappears.
```

## Examples

**Note:**  The following examples use [Weka](https://www.cs.waikato.ac.nz/ml/weka/)
as the application that we bootstrap. Since Weka is released under the
[GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) license, which requires you to
provide the sources alongside the binaries, we always set the flag to include
the *sources* as well. For other licenses, like [MIT](https://opensource.org/licenses/MIT) 
or [Apache 2.0](https://opensource.org/licenses/Apache-2.0), you do not need
to do that. Also, for projects that are only used in-house and not publicly
available, then the requirement to bundle the source code as well, does not
apply either. However, I consider it good practice to always bundle the source 
code with the binaries. ;-) 


### Cross-platform

The following example will bootstrap Weka 3.9.4 (incl. sources)
in directory `./out` and create start scripts (shell/batch) for
Weka's GUIChooser, using 1GB of heap size:

```
java -jar bootstrapp-X.Y.Z-spring-boot.jar \
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
java -jar bootstrapp-X.Y.Z-spring-boot.jar \
  -C \
  -d nz.ac.waikato.cms.weka:weka-dev:3.9.4 \
  -s \
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
        .sources(true)
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


### Docker

The following example generates a `Dockerfile` for Weka 3.9.4, which allows you
to launch Weka from within a Docker container using `/bootstrapp/weka-3.9.4.sh` 
(`name-version.sh`). In order to get the Java Swing user interface working, 
some additional libraries need to get installed on top of the `openjdk:11-jdk-slim-buster` base image 
([docker.additional](src/main/resources/com/github/fracpete/bootstrapp/docker.additional)). 

```
java -jar bootstrapp-X.Y.Z-spring-boot.jar \
  --dependency nz.ac.waikato.cms.weka:weka-dev:3.9.4 \
  --sources
  --output_dir ./out \
  --clean \
  --jvm -Xmx1g \
  --main_class weka.gui.GUIChooser \
  --name weka \
  --version 3.9.4 \
  --docker \
  --docker_base_image openjdk:11-jdk-slim-buster \
  --docker_snippet ./docker.additional
```

The command-line in Java code:

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
        .clean(true)
        .jvm("-Xmx1g")
        .mainClass("weka.gui.GUIChooser")
        .name("weka")
        .version("3.9.4")
        .docker(true)
        .dockerBaseImage("openjdk:11-jdk-slim-buster")
        .dockerSnippet(new File("./docker.additional"))
        .execute();
    if (result != null)
      System.err.println(result);
  }
}
```

Build the docker image as follows (tagging it as `weka394`):

```
cd ./out
[sudo] docker build -t weka394 .
```

If not already done in the current session, you need to expose your xhost in 
order to allow the Docker container to display the Weka user interface using 
your local X-Server:

```
xhost +local:root
```

Launch the `weka394` image:

```
[sudo] docker run -it --env="DISPLAY" \
  -v "/tmp/.X11-unix:/tmp/.X11-unix:rw" \
  weka394:latest \
  /bootstrapp/weka-3.9.4.sh
```

**NB:** Windows and Mac users can refer to [this MOA blog](https://moa.cms.waikato.ac.nz/how-to-use-moa-in-docker/)
for details on how to get it working on their platforms.


## Releases

Below are executable spring-boot jars for download that can be executed
via `java -jar XYZ.jar [options]`:

* [0.1.9](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.9/bootstrapp-0.1.9-spring-boot.jar)
* [0.1.8](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.8/bootstrapp-0.1.8-spring-boot.jar)
* [0.1.7](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.7/bootstrapp-0.1.7-spring-boot.jar)
* [0.1.6](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.6/bootstrapp-0.1.6-spring-boot.jar)
* [0.1.5](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.5/bootstrapp-0.1.5-spring-boot.jar)
* [0.1.4](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.4/bootstrapp-0.1.4-spring-boot.jar)
* [0.1.3](https://github.com/fracpete/bootstrapp/releases/download/bootstrapp-0.1.3/bootstrapp-0.1.3-spring-boot.jar)
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
      <version>0.1.9</version>
    </dependency>
```


