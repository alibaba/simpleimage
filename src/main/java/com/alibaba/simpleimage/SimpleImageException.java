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
package com.alibaba.simpleimage;

public class SimpleImageException extends Exception {

    private static final long serialVersionUID = 745614279229259820L;

    public SimpleImageException(){
        super();
    }

    public SimpleImageException(String message){
        super(message);
    }

    public SimpleImageException(String message, Throwable cause){
        super(message, cause);
    }

    public SimpleImageException(Throwable cause){
        super(cause);
    }
}
