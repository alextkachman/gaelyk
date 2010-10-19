new AntBuilder().sequential {
	webinf = "war/WEB-INF"
    mkdir dir:"${webinf}/classes"

    gaeHome = System.getenv("APPENGINE_HOME")
    if(!gaeHome) {
      gaeHome = System.getProperty("APPENGINE_HOME")
      if(!gaeHome) {
          println "To build your file you have to set 'APPENGINE_HOME' env variable pointing to your GAE SDK."
          System.exit(1)
      }
    }

	taskdef name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc"

    java(classname: 'groovyx.gaelyk.tools.GaelykTemplate2GroovyGenerator') {
      classpath {
          fileset dir: "${webinf}/lib", {
              include name: "*.jar"
          }
          fileset dir: "${gaeHome}/lib/", {
              include name: "**/*.jar"
          }

          // TODO: hardcoded path to gaelyk
          pathelement path: "/Development/Dev.Git/groovypp/Test/out/production/gaelyk"

          // TODO: hardcoded path to gaelyk libs (particularly Groovy itself)
          fileset dir: "/Development/Dev.Git/gaelyk/core/lib/", {
              include name: "**/*.jar"
          }
      }
    }

	groovyc srcdir: "src", destdir: "${webinf}/classes", {
		classpath {
			fileset dir: "${webinf}/lib", {
		    	include name: "*.jar"
			}
            fileset dir: "${gaeHome}/lib/", {
                include name: "**/*.jar"
            }
            pathelement path: "${webinf}/classes"
		}
		javac source: "1.5", target: "1.5", debug: "on"
	}
}
