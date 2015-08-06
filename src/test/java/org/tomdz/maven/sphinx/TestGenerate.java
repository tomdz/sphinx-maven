package org.tomdz.maven.sphinx;

import java.io.File;
import java.util.List;
import com.google.common.io.Files;
import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.base.Charsets.UTF_8;
import static org.junit.Assert.assertTrue;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.0.5", "3.3.3"})
@SuppressWarnings("JUnitTestNG")
public class TestGenerate
{
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public TestGenerate(MavenRuntimeBuilder mavenBuilder) throws Exception
    {
        this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
    }

    @Test
    public void testBasic() throws Exception
    {
        File basedir = resources.getBasedir("generate");
        maven.forProject(basedir)
                .execute("package")
                .assertErrorFreeLog();

        File output = new File(basedir, "target/html");

        File index = new File(output, "index.html");
        assertTrue(index.isFile());
        assertTrue(Files.toString(index, UTF_8).contains("<title>"));
    }
}
