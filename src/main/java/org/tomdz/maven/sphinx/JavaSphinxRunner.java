package org.tomdz.maven.sphinx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.python.core.Py;
import org.python.core.PySystemState;

public class JavaSphinxRunner
{
    /**
     * For running javasphinx via a forked jvm (or standalone).
     *
     * @param args
     */
    public static void main(String[] args) throws ScriptException
    {
        // use headless mode for AWT (prevent "Launcher" app on Mac OS X)
        System.setProperty("java.awt.headless", "true");

        System.exit(run(args));
    }

    public static int run(String[] args) throws ScriptException
    {
        // this setting supposedly allows GCing of jython-generated classes but I'm
        // not sure if this setting has any effect on newer jython versions anymore
        System.setProperty("python.options.internalTablesImpl", "weak");

        PySystemState engineSys = new PySystemState();
        String sphinxSourceDirectory = null;
        List<String> sphinxArgs = new ArrayList<String>(Arrays.asList(args));

        for (Iterator<String> it = sphinxArgs.iterator(); it.hasNext();) {
            String arg = it.next();
            if ("--sphinxSourceDirectory".equals(arg) && it.hasNext()) {
                // we need to remove it from the argument list as sphinx wouldn't like it
                it.remove();
                sphinxSourceDirectory = it.next();
                it.remove();
                break;
            }
        }
        if (sphinxSourceDirectory == null) {
            throw new IllegalArgumentException("No --sphinxSourceDirectory argument given");
        }

        engineSys.path.append(Py.newString(sphinxSourceDirectory));
        Py.setSystemState(engineSys);

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");

        engine.put("args", Py.java2py(sphinxArgs));
        engine.eval("import javasphinx.apidoc");
        return (Integer) engine.eval("javasphinx.apidoc.main(args)");
    }
}
