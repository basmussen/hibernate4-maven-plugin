Hibernate 4 Maven Plugin
=======================

Hibernate 4 Maven Plugin

Generate DDL from annotated `@Entity` and `@MappedSuperclass` classes.


## Usage

```
<plugin>
	<groupId>com.benasmussen.maven</groupId>
	<artifactId>hibernate4-maven-plugin</artifactId>
	<version>0.0.2-SNAPSHOT</version>
	<executions>
		<execution>
			<id>export-ddl</id>
			<phase>prepare-package</phase>
			<goals>
				<goal>export</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<dialect>org.hibernate.dialect.H2Dialect</dialect>
		<packageToScan>hibernate.demo.entity</packageToScan>
		<sqlCreateFile>create.sql</sqlCreateFile>
		<sqlDropFile>drop.sql</sqlDropFile>
	</configuration>
</plugin>
```




## Bug tracker

Have a bug or a feature request? Please create an issue here on GitHub.

http://github.com/basmussen/hibernate4-maven-plugin/issues


## Contributing

Fork the repository and submit pull requests.


## Author

**Ben Asmussen**

+ http://github.com/basmussen
