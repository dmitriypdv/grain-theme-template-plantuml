package com.sysgears.theme.plantuml

import com.sysgears.grain.taglib.GrainUtils
import com.sysgears.grain.taglib.Site
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

import java.nio.charset.Charset

class DiagramCache {

    /**
     * Site reference, provides access to site configuration.
     */
    private final Site site

    /**
     * Default charset for JVM.
     */
    private final String charset = Charset.defaultCharset().displayName()

    public DiagramCache(Site site) {
        this.site = site
    }

    /**
     * Retrieves a diagram image from the cache.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return the image file
     */
    def get(Map model) {
        def location = "${model.name}-${GrainUtils.hash(model.content.bytes)}.png"
        new File("${cacheDir}/images/${location}")
    }

    /**
     * Generates a new diagram image from the diagram model, and puts the image to the cache.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return the image file
     */
    def put(Map model) {
        generate(get(model), model)
    }

    /**
     * Checks whether the cache contains the image.
     *
     * @param model diagram model, should contain 'name' and 'content' keys
     * @return true if the image is found in the cache, false otherwise
     */
    def contains(Map model) {
        get(model).exists()
    }

    /**
     * Returns the path to the diagram cache directory.
     *
     * @return path to the cache directory
     */
    def getCacheDir() {
        "${site.base_dir}${site.plantuml_dir}"
    }

    /**
     * Generates a new diagram image using the given model.
     *
     * @param file the image file to write to
     * @param model diagram model to generate the resource
     * @return the diagram image file
     */
    private def generate(File file, Map model) {
        file.parentFile.mkdirs()
        file.createNewFile()
        def reader = new SourceStringReader("@startuml\n${model.content}@enduml\n", charset)
        reader.generateImage(new FileOutputStream(file), new FileFormatOption(FileFormat.PNG))

        return file
    }
}
