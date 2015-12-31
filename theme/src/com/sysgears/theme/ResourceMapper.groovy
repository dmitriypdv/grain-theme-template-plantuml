package com.sysgears.theme

import com.sysgears.grain.taglib.Site
import com.sysgears.theme.plantuml.DiagramCache

/**
 * Change pages urls and extend models.
 */
class ResourceMapper {

    /**
     * Site reference, provides access to site configuration.
     */
    private final Site site

    /**
     * Diagram cache.
     */
    private final DiagramCache diagramCache

    public ResourceMapper(Site site) {
        this.site = site
        diagramCache = new DiagramCache(site)
    }

    /**
     * This closure is used to transform page URLs and page data models.
     */
    def map = { resources ->

        def refinedResources = generateResources(resources.findResults(filterPublished)).collect { Map resource ->
            fillDates << resource
        }

        refinedResources
    }

    /**
     * Excludes resources with published property set to false,
     * unless it is allowed to show unpublished resources in SiteConfig.
     */
    private def filterPublished = { Map it ->
        (it.published != false || site.show_unpublished) ? it : null
    }

    /**
     * Fills in page `date` and `updated` fields 
     */
    private def fillDates = { Map it ->
        def update = [date: it.date ? Date.parse(site.datetime_format, it.date) : new Date(it.dateCreated as Long),
                updated: it.updated ? Date.parse(site.datetime_format, it.updated) : new Date(it.lastUpdated as Long)]
        it + update
    }

    /*
     * Generates additional resources on the fly.
     */
    private def generateResources = { List resources ->

        // generates diagram images
        resources.findAll { it.diagrams }.each { page -> // looks up only the resources that have the 'diagrams' key specified in the header
            page.diagrams.each { name, content ->        // iterates the list of diagrams provided in the page header
                def model = [name: name, content: content]
                if (!diagramCache.contains(model)) {     // checks if the diagram image is already in the cache
                    def file = diagramCache.put(model)   // generates the new image and puts it to the cache
                    def location = file.path - diagramCache.cacheDir   // calculates the image location
                    resources << [                       // inserts the new image resource to the resource list
                            location: location,
                            url: location,
                            markup: 'binary',
                            type: 'asset',
                            bytes: file.bytes,
                            dateCreated: file.dateCreated(),
                            lastUpdated: file.lastModified()
                    ]
                }
            }
        }

        resources
    }
}
