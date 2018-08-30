package com.regan.api.jboot.api.util;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

public class JavaDocReader {
    private static RootDoc root;
    // 一个简单Doclet,收到 RootDoc对象保存起来供后续使用
    public static  class Doclet {
        public Doclet() {
        }
        public static boolean start(RootDoc root) {
            JavaDocReader.root = root;
            return true;
        }
    }
    public static ClassDoc[] show(String path){
        com.sun.tools.javadoc.Main.execute(new String[] {"-doclet",
                JavaDocReader.Doclet.class.getName(),
                path, "-encoding","utf-8"});
         return root.classes();
    }
    public static RootDoc getRoot() {
        return root;
    }
    public JavaDocReader() {

    }
}