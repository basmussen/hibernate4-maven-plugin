package com.benasmussen.maven.hibernate4;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import junit.framework.TestCase;
import static org.mockito.Mockito.*;

/**
 * SchemaExportMojo Unit Test
 * 
 * @author Ben Asmussen
 *
 */
public class SchemaExportMojoTest extends TestCase
{

    public void testExecute() throws MojoExecutionException
    {
        SchemaExportMojo mojo = new SchemaExportMojo();

        mojo.packageToScan = "hibernate.test.entity";
        mojo.dialect = "org.hibernate.dialect.HSQLDialect";
        mojo.outputDirectory = new File("target/junit");
        mojo.sqlCreateFile = "create.sql";
        mojo.sqlDropFile = "drop.sql";

        MavenProject mavenProject = mock(MavenProject.class);
        mojo.project = mavenProject;

        mojo.execute();

        assertTrue("create file exists", new File(mojo.outputDirectory, mojo.sqlCreateFile).exists());
        assertTrue("drop file exists", new File(mojo.outputDirectory, mojo.sqlDropFile).exists());

        assertTrue("create file not empty", new File(mojo.outputDirectory, mojo.sqlCreateFile).length() > 0);
        assertTrue("drop file not empty", new File(mojo.outputDirectory, mojo.sqlDropFile).length() > 0);

    }

}
