/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk

import groovy.servlet.ServletBinding
import groovy.servlet.TemplateServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovyx.gaelyk.plugins.PluginsHandler
import javax.servlet.ServletConfig
import groovyx.gaelyk.logging.GroovyLogger
import groovy.text.Template
import groovy.text.SimpleTemplateEngine.SimpleTemplate
import java.util.concurrent.ConcurrentHashMap
import java.lang.ref.SoftReference
import groovy.text.SimpleTemplateEngine

/**
 * The Gaelyk template servlet extends Groovy's own template servlet 
 * to inject Google App Engine dedicated services in the binding of the Groolets.
 *
 * @author Marcel Overdijk
 * @author Guillaume Laforge
 *
 * @see groovy.servlet.TemplateServlet
 */
class GaelykTemplateServlet extends TemplateServlet {

    private String rootPath

    @Override
    void init(ServletConfig config) {
        super.init(config)
        rootPath = servletContext.getRealPath("/")
        PluginsHandler.instance.initPlugins()
    }

    /**
     * Injects the default variables and GAE services in the binding of templates
     *
     * @param binding the binding to enhance
     */
    @Override
    protected void setVariables(ServletBinding binding) {
        GaelykBindingEnhancer.bind(binding)
        PluginsHandler.instance.enrich(binding)
        binding.setVariable("log", GroovyLogger.forTemplateUri(super.getScriptUri(binding.request)))
    }

    /**
     * Service incoming requests applying the <code>GaelykCategory</code>
     * and the other categories defined by the installed plugins.
     *
     * @param request the request
     * @param response the response
     * @throws IOException when anything goes wrong
     */
    @Override
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        use([GaelykCategory, * PluginsHandler.instance.categories]) {
            super.service(request, response)
        }
    }

  private final def classCache = new ConcurrentHashMap()

  protected Template getTemplate(File file) {
    def path = file.absolutePath
    String name = "__generated_templates__${path.substring(rootPath.length(),path.length()-5)}".replace('/', '.').replace('\\', '.').replace('-','_') // 5 == '.gtpl'.length ()

    def old = classCache.get(name)
    Class cls
    if(!old || !(cls = old.get())) {
      try {
          cls = getClass().getClassLoader().loadClass(name)
      }
      catch(t) { //
      }

      if (!cls) {
        def res = super.getTemplate(file)
        classCache.putIfAbsent(name, new SoftReference(res.script.class))
        return res
      }
      else {
        classCache.putIfAbsent(name, new SoftReference(cls))
      }
    }

    return new SimpleTemplate(script:cls.newInstance())
  }
}