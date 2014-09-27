package com.benasmussen.maven.hibernate4;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.reflections.Reflections;

import com.google.common.collect.Sets;

/**
 * Goal export hibernate 4 ddl
 * 
 * @author Ben Asmussen
 */
@Mojo(name = "export", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SchemaExportMojo extends AbstractMojo
{

    /**
     * Package to scan
     */
    @Parameter(property = "packageToScan", required = true)
    String packageToScan;

    /**
     * Hibernate dialect
     */
    @Parameter(required = true)
    String dialect;

    /**
     * Default export directory
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/sql/", required = true)
    File outputDirectory;

    /**
     * Filename create sql
     */
    @Parameter(property = "sqlCreateFile", defaultValue = "create.sql")
    String sqlCreateFile;

    /**
     * Filename drop sql
     */
    @Parameter(property = "sqlDropFile", defaultValue = "drop.sql")
    String sqlDropFile;

    /**
     * Maven project
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    public void execute() throws MojoExecutionException
    {
        getLog().info("Package to scan: " + packageToScan);
        getLog().info("Hibernate dialect: " + dialect);

        // create
        File createFile = null;
        if (sqlCreateFile != null)
        {
            createFile = new File(outputDirectory, sqlCreateFile);
            getLog().info("Sql create file:  " + createFile.getAbsolutePath());
        }

        // drop
        File dropFile = null;
        if (sqlDropFile != null)
        {
            dropFile = new File(outputDirectory, sqlDropFile);
            getLog().info("Sql drop file:  " + dropFile.getAbsolutePath());
        }

        Writer dropWriter = null;
        Writer createWriter = null;
        try
        {
            initialise(outputDirectory);

            // classpath elements
            URL[] urls = getCompileClasspathElements();
            URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);

            // hibernate configuration
            Configuration hibernateConfiguration = buildHibernateConfig();
            Dialect hibernateDialect = Dialect.getDialect(hibernateConfiguration.getProperties());

            // generate create sql file
            if (createFile != null)
            {
                createWriter = new FileWriter(createFile);
                String[] createSQL = hibernateConfiguration.generateSchemaCreationScript(hibernateDialect);
                if (createSQL != null && createSQL.length > 0)
                {
                    write(createWriter, createSQL);
                }
                else
                {
                    throw new MojoExecutionException("No create sql generated");
                }
            }

            // generate drop sql file
            if (dropFile != null)
            {
                dropWriter = new FileWriter(dropFile);
                String[] dropSQL = hibernateConfiguration.generateDropSchemaScript(hibernateDialect);
                if (dropSQL != null && dropSQL.length > 0)
                {
                    write(dropWriter, dropSQL);
                }
                else
                {
                    throw new MojoExecutionException("No drop sql generated");
                }
            }

        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Unable to create export file.", e);
        }
        finally
        {
            IOUtils.closeQuietly(dropWriter);
            IOUtils.closeQuietly(createWriter);
        }
    }

    /**
     * Initialise directories
     * 
     * @param exportDirectory
     */
    private void initialise(File exportDirectory)
    {
        if (!exportDirectory.exists())
        {
            exportDirectory.mkdirs();
        }
    }

    /**
     * Retrieve all compile classpath elements
     * 
     * @return URL[]
     * @throws MojoExecutionException
     * @throws IOException
     */
    private URL[] getCompileClasspathElements() throws MojoExecutionException, IOException
    {
        try
        {
            List<File> files = new ArrayList<File>();
            List<String> compileClasspathElements = project.getCompileClasspathElements();
            for (String element : compileClasspathElements)
            {
                files.add(new File(element));
            }
            return FileUtils.toURLs(files.toArray(new File[0]));
        }
        catch (DependencyResolutionRequiredException e)
        {
            throw new MojoExecutionException("Unable to resolve dependencies", e);
        }
        catch (MalformedURLException e)
        {
            throw new MojoExecutionException("Invalid URL", e);
        }
    }

    /**
     * Package scan, configure hibernate configuration
     * 
     * @return {@link Configuration}
     * @throws MojoExecutionException
     */
    private Configuration buildHibernateConfig() throws MojoExecutionException
    {
        Reflections reflections = new Reflections(packageToScan);

        Configuration hibernateConfiguration = new Configuration();

        Set<Class<?>> annotatedClasses = Sets.newHashSet();
        annotatedClasses.addAll(reflections.getTypesAnnotatedWith(MappedSuperclass.class));
        annotatedClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class));
        for (Class<?> cl : annotatedClasses)
        {
            hibernateConfiguration.addAnnotatedClass(cl);
            getLog().info("Entity: " + cl.getName());
        }

        if (annotatedClasses.size() <= 0)
        {
            throw new MojoExecutionException("No annotated class found in package: " + packageToScan);
        }

        hibernateConfiguration.setProperty(AvailableSettings.DIALECT, dialect);

        return hibernateConfiguration;
    }

    /**
     * Write to output
     * 
     * @param writer
     * @param lines
     * @throws IOException
     */
    private void write(Writer writer, String[] lines) throws IOException
    {
        Formatter formatter = FormatStyle.DDL.getFormatter();
        for (String line : lines)
        {
            writer.write(formatter.format(line));
        }
        writer.flush();
    }
}