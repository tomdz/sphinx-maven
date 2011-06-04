package org.tomdz.maven.sphinx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.python.core.Py;
import org.python.core.PySystemState;

/**
 * @author tomdz
 * @goal generate
 * @phase site
 */
public class SphinxMojo extends AbstractMavenReport
{
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The directory containing the sphinx doc source.
     *
     * @parameter expression="${basedir}/src/site/sphinx"
     * @required
     */
    private File sourceDirectory;

    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * The directory for sphinx' source.
     *
     * @parameter expression="${project.build.directory}/sphinx"
     * @required
     * @readonly
     */
    private File sphinxSourceDirectory;

    /**
     * Whether Sphinx should generate verbose output.
     *
     * @parameter alias="verbose" default-value="true"
     */
    private boolean verbose;

    /**
     * Whether Sphinx should treat warnings as errors.
     *
     * @parameter alias="warningsAsErrors" default-value="false"
     */
    private boolean warningsAsErrors;

    @Override
    public String getDescription(Locale defaultLocale)
    {
        return "Documentation via sphinx";
    }

    @Override
    public String getName(Locale defaultLocale)
    {
        return "Documentation via sphinx";
    }

    @Override
    public String getOutputName()
    {
        return "index";
    }

    @Override
    public boolean isExternalReport()
    {
        return true;
    }

    @Override
    protected Renderer getSiteRenderer()
    {
        return null;
    }

    @Override
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    @Override
    protected MavenProject getProject()
    {
        return project;
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException
    {
        unpackSphinx();
        runSphinx();
    }

    private void unpackSphinx() throws MavenReportException
    {
        if (!sphinxSourceDirectory.exists() && !sphinxSourceDirectory.mkdirs())
        {
            throw new MavenReportException("Could not generate the temporary directory " + sphinxSourceDirectory.getAbsolutePath() + " for the sphinx sources"); 
        }

        if (verbose) {
            getLog().info("Unpacking sphinx to " + sphinxSourceDirectory.getAbsolutePath());
        }
        try {
            ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream("jar", SphinxMojo.class.getResourceAsStream("/sphinx.jar"));
            ArchiveEntry entry = input.getNextEntry();
    
            while (entry != null) {
                File archiveEntry = new File(sphinxSourceDirectory, entry.getName());
                archiveEntry.getParentFile().mkdirs();
                if (entry.isDirectory()){
                    archiveEntry.mkdir();
                    entry = input.getNextEntry();
                    continue;
                }
                OutputStream out = new FileOutputStream(archiveEntry);
                IOUtils.copy(input, out);
                out.close();
                entry = input.getNextEntry();
            }
            input.close();
        }
        catch (Exception ex) {
            throw new MavenReportException("Could not unpack the sphinx source", ex);
        }
    }

    private void runSphinx() throws MavenReportException
    {
        PySystemState engineSys = new PySystemState();

        engineSys.path.append(Py.newString(sphinxSourceDirectory.getAbsolutePath()));
        Py.setSystemState(engineSys);

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");

        if (verbose) {
            getLog().info("Running sphinx on " + sourceDirectory.getAbsolutePath() + ", output will be placed in " + outputDirectory.getAbsolutePath());
        }

        List<String> args = new ArrayList<String>();

        if (verbose) {
            args.add("-v");
        }
        else {
            args.add("-q");
        }
        if (warningsAsErrors) {
            args.add("-W");
        }
        args.add("-n");
        args.add("-b");
        args.add("html");
        args.add(sourceDirectory.getAbsolutePath());
        args.add(outputDirectory.getAbsolutePath());

        engine.put("args", args.toArray(new String[args.size()]));
        try {
            engine.eval("import sphinx; sphinx.main(args)");
        }
        catch (ScriptException ex) {
            throw new MavenReportException("Could not generate documentation", ex);
        }
    }
}
