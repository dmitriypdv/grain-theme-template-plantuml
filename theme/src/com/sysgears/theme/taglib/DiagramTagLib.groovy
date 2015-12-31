package com.sysgears.theme.taglib

import com.sysgears.grain.taglib.GrainTagLib
import com.sysgears.grain.taglib.GrainUtils

class DiagramTagLib {

    private GrainTagLib taglib

    public DiagramTagLib(GrainTagLib taglib) {
        this.taglib = taglib
    }

    /**
     * Returns the diagram location by the image name.
     *
     * @param name the diagram name
     * @return image resource location
     */
    def getDiagramLocation = { String name ->

        "/images/${name}-${GrainUtils.hash(taglib.page.diagrams."$name".bytes)}.png"
    }
}
