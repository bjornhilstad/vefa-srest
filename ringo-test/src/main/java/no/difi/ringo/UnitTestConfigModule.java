package no.difi.ringo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static no.sr.ringo.config.RingoConfigProperty.*;

/**
 * Provides the configuration parameters for Unit testing.
 *
 * @author steinar
 *         Date: 29.01.2017
 *         Time: 09.36
 */
public class UnitTestConfigModule extends AbstractModule {

    private final Optional<String> jdbcPathString = Optional.empty();

    public UnitTestConfigModule() {

    }

    /**
     * Provides the Path of the RINGO_HOME directory
     * @return
     */
    @Provides
    @Singleton
    @Named(HOME_DIR_PATH)
    protected Path provideRingoHomeDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }


    String createJdbcUri() {
        return "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    }

    /**
     * Creates class path of H2 JDBC driver by referencing the Maven repository.
     * <p>
     * This method is meant as an example of how to specify the class path of a .jar holding the JDBC driver
     *
     * @return full path of .jar file holding the H2 JDBC driver
     */
    String getJdbcPath() {
        String homeDir = System.getProperty("user.home");
        Path jdbcPath = Paths.get(homeDir, ".m2/repository/com/h2database/h2/1.4.192/h2-1.4.192.jar");
        if (!Files.exists(jdbcPath)) {
            throw new IllegalStateException("JDBC driver not found at " + jdbcPath);
        }
        return "file://" + jdbcPath.toString();
    }


    @Override
    protected void configure() {

        bind(Path.class).annotatedWith(Names.named(PAYLOAD_BASE_PATH)).toInstance(getPathToPayloadDirectory());

        bind(String.class).annotatedWith(Names.named(JDBC_CONNECTION_URI)).toInstance("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        bind(String.class).annotatedWith(Names.named(JDBC_CLASS_PATH)).toInstance("");
        bind(String.class).annotatedWith(Names.named(JDBC_DRIVER_CLASS)).toInstance("org.h2.Driver");
        bind(String.class).annotatedWith(Names.named(JDBC_USER)).toInstance("SA");
        bind(String.class).annotatedWith(Names.named(JDBC_PASSWORD)).toInstance("");

        bind(String.class).annotatedWith(Names.named(JDBC_VALIDATION_QUERY)).toInstance("select current_date()");
    }

    @Provides
    Config provideConfig() {
        ConfigFactory.invalidateCaches();
        final Config defaultReference = ConfigFactory.defaultReference();
        final Config config = ConfigFactory.systemProperties()
                .withFallback(defaultReference)
                .withFallback(defaultReference.getConfig("default"));
        final Config resolved = config.resolve();

        return resolved;
    }

    private Path getPathToPayloadDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path baseDirPath = Paths.get(tmpDir, "/peppol");
        try {
            if (!Files.exists(baseDirPath) && !Files.isDirectory(baseDirPath)) {
                Path path = Files.createDirectories(baseDirPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to of directory " + baseDirPath + "; " + e.getMessage(), e);
        }
        return baseDirPath;
    }
}
