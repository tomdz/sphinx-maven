package org.tomdz.maven.sphinx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author tomdz
 * @goal generate
 * @phase site
 */
public class SphinxMojo extends AbstractMavenReport
{
    /**
     * The maven project object.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The plugin's descriptor.
     *
     * @parameter default-value="${plugin}"
     * @required
     * @readonly
     */
    private PluginDescriptor pluginDesc;

    /**
     * The artifact factory.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * Needed to resolve dependencies.
     *
     * @component
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * Needed to resolve dependencies.
     *
     * @component
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * The local repo for the project if defined;
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Remote repositories.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;

    /**
     * The base directory of the project.
     *
     * @parameter default-value="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

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
     */
    private File outputDirectory;
    
    /**
     * Name of the report.
     *
     * @parameter expression="Documentation via sphinx"
     * @required
     */
    private String name;
    
    /**
     * Description of the report.
     *
     * @parameter expression="Documentation via sphinx"
     * @required
     */
    private String description;
    
    /**
     * The base name used to create report's output file(s).
     *
     * @parameter expression="index"
     * @required
     */
    private String outputName;

    /**
     * The directory for sphinx' source.
     *
     * @parameter expression="${project.build.directory}/sphinx"
     * @required
     * @readonly
     */
    private File sphinxSourceDirectory;

    /**
     * The builder to use. See <a href="http://sphinx.pocoo.org/man/sphinx-build.html?highlight=command%20line">sphinx-build</a>
     * for a list of supported builders.
     *
     * @parameter alias="builder" default-value="html"
     */
    private String builder;

    /**
     * The <a href="http://sphinx.pocoo.org/markup/misc.html#tags">tags</a> to pass to the sphinx build.
     *
     * @parameter alias="tags"
     */
    private List<String> tags;

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

    /**
     * Whether Sphinx should generate output for all files instead of only the changed ones.
     *
     * @parameter alias="force" default-value="false"
     */
    private boolean force;

    /**
     * Whether sphinx should be run in a forked jvm instance.
     *
     * @parameter alias="fork" default-value="false"
     */
    private boolean fork;

    /**
     * Option to specify the jvm (or path to the java executable) to use with the forking options. For the default, the
     * jvm will be a new instance of the same VM as the one used to run Maven. JVM settings are not inherited from
     * MAVEN_OPTS.
     *
     * @parameter alias="jvm"
     */
    private String jvm;

    /**
     * Arbitrary JVM options for the forked sphinx process to set on the command line.
     *
     * @parameter alias="argLine"
     */
    private String argLine;

    /**
     * Kill the forked sphinx process after a certain number of seconds. If set to 0, wait forever for the process, never
     * timing out.
     *
     * @parameter alias="forkTimeoutSec"
     */
    private int forkTimeoutSec;

    @Override
    public String getDescription(Locale defaultLocale)
    {
        return description;
    }

    @Override
    public String getName(Locale defaultLocale)
    {
        return name;
    }

    @Override
    public String getOutputName()
    {
        return outputName;
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
    public void execute() throws MojoExecutionException
    {
        try {
            executeReport(Locale.getDefault());
        }
        catch (MavenReportException ex) {
            throw new MojoExecutionException("Failed to run the report", ex);
        }
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException
    {
        unpackSphinx();
        if (fork) {
            runForkedSphinx();
        }
        else {
            runSphinx();
        }
    }

    private void unpackSphinx() throws MavenReportException
    {
        if (!sphinxSourceDirectory.exists() && !sphinxSourceDirectory.mkdirs()) {
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
                if (entry.isDirectory()) {
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

    private String[] getSphinxRunnerCmdLine()
    {
        List<String> args = new ArrayList<String>();

        args.add("--sphinxSourceDirectory");
        args.add(sphinxSourceDirectory.getAbsolutePath());
        if (verbose) {
            args.add("-v");
        }
        else {
            args.add("-Q");
        }
        if (warningsAsErrors) {
            args.add("-W");
        }
        if (force) {
            args.add("-a");
            args.add("-E");
        }
        if (builder != null) {
            args.add("-b");
            args.add(builder);
        }
        if ((tags != null) && !tags.isEmpty()) {
            for (String tag : tags) {
                args.add("-t");
                args.add(tag);
            }
        }
        args.add("-n");
        args.add(sourceDirectory.getAbsolutePath());
        args.add(outputDirectory.getAbsolutePath());
        return args.toArray(new String[args.size()]);
    }

    private void runSphinx() throws MavenReportException
    {
        if (verbose) {
            getLog().info("Running sphinx on " + sourceDirectory.getAbsolutePath() + ", output will be placed in " + outputDirectory.getAbsolutePath());
        }

        String[] args = getSphinxRunnerCmdLine();

        try {
            SphinxRunner.main(args);
        }
        catch (Exception ex) {
            throw new MavenReportException("Could not generate documentation", ex);
        }
    }

    private void runForkedSphinx() throws MavenReportException
    {
        String jvmBinary = jvm;

        if ((jvmBinary == null) || "".equals(jvmBinary)) {
            jvmBinary = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        }
        getLog().debug("Using JVM: " + jvmBinary);

        Commandline cmdLine = new Commandline();

        cmdLine.setWorkingDirectory(basedir);
        cmdLine.setExecutable(jvmBinary);
        if (argLine != null) {
            cmdLine.createArg().setLine(argLine.replace("\n", " ").replace("\r", " "));
        }
        try {
            cmdLine.addEnvironment("CLASSPATH", getPluginClasspath());
        }
        catch (Exception ex) {
            throw new MavenReportException("Could not determine the classpath of the plugin", ex);
        }

        String className = SphinxRunner.class.getName();
        String[] args = getSphinxRunnerCmdLine();

        cmdLine.createArg().setValue(className);
        for (String arg: args) {
            cmdLine.createArg().setValue(arg);
        }

        StreamConsumer infoStreamConsumer = new StreamConsumer() {
            @Override
            public void consumeLine(String line)
            {
                getLog().info(line);
            }
        };
        StreamConsumer errorStreamConsumer = new StreamConsumer() {
            @Override
            public void consumeLine(String line)
            {
                getLog().error(line);
            }
        };

        getLog().debug(cmdLine.toString());

        int result = 0;

        try
        {
            int timeout = forkTimeoutSec > 0 ? forkTimeoutSec : 0;

            result = CommandLineUtils.executeCommandLine(cmdLine,
                                                         infoStreamConsumer,
                                                         errorStreamConsumer,
                                                         timeout);
        }
        catch (Exception ex)
        {
            throw new MavenReportException("Could not execute sphinx in a forked jvm", ex);
        }
        if (result != 0) {
            throw new MavenReportException("Sphinx report generation failed");
        }
    }

    private String getPluginClasspath() throws ArtifactResolutionException, ArtifactNotFoundException
    {
        Set<Artifact> artifacts = new HashSet<Artifact>(1);

        artifacts.add(pluginDesc.getPluginArtifact());

        // we need the dummy artifact here so that maven actually resolves the
        // plugin's dependencies for us and returns them in the result object
        Artifact originatingArtifact = artifactFactory.createBuildArtifact( "dummy", "dummy", "1.0", "jar" );
        ArtifactResolutionResult result = resolver.resolveTransitively(artifacts,
                                                                       originatingArtifact,
                                                                       remoteRepositories,
                                                                       localRepository,
                                                                       artifactMetadataSource);
        StringBuilder classpath = new StringBuilder();

        for (Object obj : result.getArtifacts()) {
            Artifact curArtifact = (Artifact)obj;

            if (classpath.length() > 0) {
                classpath.append(File.pathSeparatorChar);
            }
            classpath.append(curArtifact.getFile().getAbsolutePath());
        }
        return classpath.toString();
    }
}
