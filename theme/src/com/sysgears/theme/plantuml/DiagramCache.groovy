package com.sysgears.theme.plantuml

import com.sysgears.grain.taglib.GrainUtils
import com.sysgears.grain.taglib.Site
import groovy.io.FileType
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

import java.nio.charset.Charset

class DiagramCache implements GroovyInterceptable {

    /**
     * Site reference, provides access to site configuration.
     */
    private final Site site

    /**
     * Default charset for JVM.
     */
    private final String charset = Charset.defaultCharset().displayName()

    /**
     * Registry of the diagram resources that are currently in use.
     */
    private final Set mappedResources = []

    public DiagramCache(Site site) {
        this.site = site
    }

    /**
     * Retrieves a diagram image from the cache.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return the image file
     */
    File get(Map model) {
        new File(getDiagramLocation(model))
    }

    /**
     * Generates a new diagram image from the diagram model, and puts the image to the cache.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return the image file
     */
    File put(Map model) {
        generate(getDiagramLocation(model), model.content)
    }

    /**
     * Checks whether the cache contains the image.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return true if the image is found in the cache, false otherwise
     */
    boolean contains(Map model) {
        get(model).exists()
    }

    /**
     * Drops diagrams that are not longer used.
     * <br />
     * All the resources that were not accessed since the last method call are considered as unused.
     */
    void dropUnused() {
        new File("${cacheDir}/images/").eachFileRecurse (FileType.FILES) { file ->
            if(!mappedResources.contains(file.name)) { file.delete() }
        }
        mappedResources.clear()
    }

    /**
     * Returns the path to the diagram cache directory.
     *
     * @return path to the cache directory
     */
    String getCacheDir() {
        "${site.base_dir}${site.plantuml_dir}"
    }

    /**
     * Builds the diagram name basing on the model.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return the diagram resource name
     */
    private String getDiagramName(model) {
        "${model.name}-${GrainUtils.hash(model.content.bytes)}.png"
    }

    /**
     * Returns the diagram location basing on the model.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return the diagram resource location
     */
    private String getDiagramLocation(model) {
        "${cacheDir}/images/${getDiagramName(model)}"
    }

    /**
     * Generates a new diagram image.
     *
     * @param location image file location
     * @param content diagram content to generate the image resource
     * @return the diagram image file
     */
    private File generate(String location, String content) {
        File file = new File(location)
        file.parentFile.mkdirs()
        file.createNewFile()
        def reader = new SourceStringReader("@startuml\n${content}@enduml\n", charset)
        reader.generateImage(new FileOutputStream(file), new FileFormatOption(FileFormat.PNG))

        return file
    }

    def invokeMethod(String name, args) {
        if (name in ['get', 'put', 'contains']) {
            mappedResources << getDiagramName(args[0]) // adds the resource to the registry
        }
        metaClass.getMetaMethod(name, args).invoke(this, args)
    }
}
