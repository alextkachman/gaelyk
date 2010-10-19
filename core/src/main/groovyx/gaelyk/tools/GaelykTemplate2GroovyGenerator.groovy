package groovyx.gaelyk.tools

import groovy.text.SimpleTemplateEngine.SimpleTemplate
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport
import org.codehaus.groovy.tools.FileSystemCompiler
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 *
 */
public class GaelykTemplate2GroovyGenerator {
    public static void main(String [] args) {
      def root = new File('war')

      def genDir = new File("src/__generated_templates__")
      genDir.deleteDir()

      genDir.mkdir()

      scan(root, "__generated_templates__", "src/__generated_templates__")
    }

    static void scan(final File self, String packagePrefix, String pathPrefix) {
        def files = self.listFiles()
        if (files == null)
            return

        for (file in files) {
            if (file.isDirectory()) {
              def name = file.name.replace('-', '_')
              String dir = "${pathPrefix}/$name"
              new File(dir).mkdir()
              scan(file, "$packagePrefix.$name", dir)
            }
            else {
              if(file.canonicalPath.endsWith('.gtpl')) {
                def template = new SimpleTemplate()
                Reader reader = new StringReader(file.text)
                  String script = """
package ${packagePrefix}
${template.parse(reader)}
"""
                def created = new File("$pathPrefix/${file.name.substring(0,file.name.length()-5)}.groovy")
                created.text = script
              }
            }
        }
    }
}
