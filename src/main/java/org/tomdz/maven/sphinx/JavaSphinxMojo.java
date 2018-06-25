package org.tomdz.maven.sphinx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * This Mojo gives access to the javasphinx-apidoc command to analyze Java source code
 * and generates Javadoc at the Sphinx format.
 * 
 * <p>The next paragraphs are <i>samples</i> to <b>demonstrate</b> how javasphinx-apidoc analyzes comments.
 * 
 * Note that closing tag are not mandatory.
 * 
 * <table><tr ><td rowspan="1">Cell1</td><td>Cell2</td>
 * <tr ><td>Cell3<td>Cell4</td></table>
 * 
 * Some links in a list:
 * 
 * <ul>
 * <li><a href="#generate-apidoc">Goal</a>
 * <li><a href="http://bronto.github.io/javasphinx/">Javasphinx documentation</a>
 * </ul>
 * 
 * @author tomdz & heurtier
 * @goal generate-apidoc
 * @phase pre-site
 */
public class JavaSphinxMojo extends AbstractMojo
{
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
     * @parameter expression="${basedir}/src/main/java"
     * @required
     */
    private File sourceDirectory;

    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.build.directory}/src/site/sphinx"
     * @required
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
     * Whether generation is in force mode
     *
     * @parameter alias="force" default-value="false"
     */
    private boolean force;

    /**
     * Whether generation is in update mode
     *
     * @parameter alias="update" default-value="true"
     */
    private boolean update;

    /**
     * Don't create a table of contents file
     * 
     * @parameter alias="no_toc" default-value="false"
     */
    private boolean no_toc;
    
    /**
     *  file suffix (default: rst)
     *  
     *  @parameter alias="suffix" default-value="rst"
     */
    private String suffix;
    
    /**
     * Additional input paths to scan.
     *
     * @parameter
     */
    private List<String> includes;

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
     * Whether it should be run in a forked jvm instance.
     *
     * @parameter alias="fork" default-value="false"
     */
    private boolean fork;

    /**
     * Kill the forked sphinx process after a certain number of seconds. If set to 0, wait forever for the process, never
     * timing out.
     *
     * @parameter alias="forkTimeoutSec"
     */
    private int forkTimeoutSec;

    @Override
    public void execute() throws MojoExecutionException
    {
        unpackSphinx();
        if (fork) {
            runForkedSphinx();
        }
        else {
            runSphinx();
        }
    }

    private void unpackSphinx() throws MojoExecutionException
    {
        if (sphinxSourceDirectory.exists()) {
            return;
        }
        if (!sphinxSourceDirectory.exists() && !sphinxSourceDirectory.mkdirs()) {
            throw new MojoExecutionException("Could not generate the temporary directory " + sphinxSourceDirectory.getAbsolutePath() + " for the sphinx sources"); 
        }

        if (verbose) {
            getLog().info("Unpacking sphinx to " + sphinxSourceDirectory.getAbsolutePath());
        }
        try {
            ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream("jar", JavaSphinxMojo.class.getResourceAsStream("/sphinx.jar"));
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
            throw new MojoExecutionException("Could not unpack the sphinx source", ex);
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
        if (force) {
            args.add("-f");
        }
        if (update) {
            args.add("-u");
        }
        if (no_toc) {
            args.add("-T");
        }
        if (suffix!=null && !suffix.isEmpty()) {
            args.add("-s");
            args.add(suffix);
        }
        if (includes!=null && includes.size()>0) {
            for(String s:includes) {
                args.add("-I");
                args.add(s);
            }
        }
        
        args.add("-o");
        args.add(outputDirectory.getAbsolutePath());
        args.add(sourceDirectory.getAbsolutePath());
        return args.toArray(new String[args.size()]);
    }

    private void runSphinx() throws MojoExecutionException
    {
        if (verbose) {
            getLog().info("Running javasphinx-apidoc on " + sourceDirectory.getAbsolutePath() + ", output will be placed in " + outputDirectory.getAbsolutePath());
        }

        String[] args = getSphinxRunnerCmdLine();
        int result;
        try {
            result = JavaSphinxRunner.run(args);
        }
        catch (Exception ex) {
            throw new MojoExecutionException("Could not generate documentation", ex);
        }
        if (result != 0) {
            throw new MojoExecutionException("Javasphinx-apidoc generation failed");
        }
    }

    private void runForkedSphinx() throws MojoExecutionException
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
            throw new MojoExecutionException("Could not determine the classpath of the plugin", ex);
        }

        String className = JavaSphinxRunner.class.getName();
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
            throw new MojoExecutionException("Could not execute sphinx in a forked jvm", ex);
        }
        if (result != 0) {
            throw new MojoExecutionException("Sphinx report generation failed");
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
