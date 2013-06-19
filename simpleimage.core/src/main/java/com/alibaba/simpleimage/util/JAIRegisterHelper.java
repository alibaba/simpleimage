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
package com.alibaba.simpleimage.util;

import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;

import com.alibaba.simpleimage.jai.scale.LanczosCRIF;
import com.alibaba.simpleimage.jai.scale.LanczosDescriptor;

public class JAIRegisterHelper {

    public static void register() {
        //noting to do
    }

    /**
     * 由于LancsosOpImage暂时不用，该方法也暂时不用
     */
    @SuppressWarnings("unused")
    private synchronized static void registerLanczosOperator() {
        LanczosDescriptor lanczosDescriptor = new LanczosDescriptor();
        RenderedImageFactory rif = new LanczosCRIF();
        
        registerRIFOperator(lanczosDescriptor, rif, "Lanczos", "com.alibaba.platform", "rendered");
    }

    private static void registerRIFOperator(OperationDescriptor descriptor, RenderedImageFactory rif,
                                            String operationName, String productName, String model) {
        OperationRegistry op = JAI.getDefaultInstance().getOperationRegistry();
        String[] p = op.getDescriptorNames(model);
        boolean registed = false;

        if (p != null) {
            for (int i = 0; i < p.length; i++) {
                if (p[i].equalsIgnoreCase(operationName)) {
                    registed = true;
                    break;
                }
            }
        }

        if (!registed) {
            op.registerDescriptor(descriptor);
        }

        RIFRegistry.register(op, operationName, productName, rif);
    }
}
