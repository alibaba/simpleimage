/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.simpleimage.jai.scale;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderableOp;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderableRegistryMode;
import javax.media.jai.registry.RenderedRegistryMode;
import javax.media.jai.util.Range;

/**
 * TODO Comment of LanczosDescriptor
 * @author wendell
 *
 */
@SuppressWarnings("unchecked")
public class LanczosDescriptor extends OperationDescriptorImpl {

    /**
     * 
     */
    private static final long serialVersionUID = 4737776871201566774L;

    /**
     * The resource strings that provide the general documentation
     * and specify the parameter list for this operation.
     */
    private static final String[][] resources = {
        {"GlobalName",  "Lanczos"},
        {"LocalName",   "Lanczos"},
        {"Vendor",      "www.alibaba.com"},
        {"Description", "scale algthrom"},
        {"DocURL",      ""},
        {"Version",     "1.0"},
        {"arg0Desc",    "the scale factor of X"},
        {"arg1Desc",    "the scale factor of Y"}
    };

    /** The parameter class list for this operation. */
    private static final Class[] paramClasses = {
        java.lang.Double.class, java.lang.Double.class
    };

    /** The parameter name list for this operation. */
    private static final String[] paramNames = {
        "scaleX", "scaleY"
    };

    /** The parameter default value list for this operation. */
    private static final Object[] paramDefaults = {
        new Double(0.5), new Double(0.5)
    };

    /** The allowable <code>Range</code>s of parameter values. */
    private static final Object[] validParamValues = {
        new Range(Double.class, new Double(Double.MIN_VALUE), new Double(Double.MAX_VALUE)),
        new Range(Double.class, new Double(Double.MIN_VALUE), new Double(Double.MAX_VALUE))
    };

    public LanczosDescriptor() {
        super(resources,
              new String[] {RenderedRegistryMode.MODE_NAME,
                            RenderableRegistryMode.MODE_NAME},
              1,
              paramNames,
              paramClasses,
              paramDefaults,
              validParamValues);
    }
    
    protected boolean validateParameters(String modeName,
                                         ParameterBlock args,
                                         StringBuffer msg) {
        if (!super.validateParameters(modeName, args, msg)) {
            return false;
        }

        if(args.getNumParameters() < 2 || args.getObjectParameter(1) == null) {
            args.set(args.getObjectParameter(0), 1);
        }

        return true;
    }
    
    public static RenderedOp create(RenderedImage source0,
                                    Double scaleX,
                                    Double scaleY,
                                    RenderingHints hints)  {
        ParameterBlockJAI pb =
            new ParameterBlockJAI("Lanczos",
                                  RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);

        pb.setParameter("scaleX", scaleX);
        pb.setParameter("scaleY", scaleY);

        return JAI.create("Lanczos", pb, hints);
    }
    
    public static RenderableOp createRenderable(RenderableImage source0,
                                                Double scaleX,
                                                Double scaleY,
                                                RenderingHints hints)  {
        ParameterBlockJAI pb =
            new ParameterBlockJAI("Lanczos",
                                  RenderableRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);

        pb.setParameter("scaleX", scaleX);
        pb.setParameter("scaleY", scaleY);

        return JAI.createRenderable("Lanczos", pb, hints);
    }
}
