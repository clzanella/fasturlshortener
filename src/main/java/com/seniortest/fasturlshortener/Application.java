package com.seniortest.fasturlshortener;

import com.google.gson.JsonObject;
import com.seniortest.fasturlshortener.common.InstantProvider;
import com.seniortest.fasturlshortener.controller.ShortURLController;
import com.seniortest.fasturlshortener.repository.ShortURLRepository;
import com.seniortest.fasturlshortener.service.ShortURLService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.json.Gzon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.DateTimeException;
import java.time.Duration;
import java.util.concurrent.Executors;

public class Application extends Jooby {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static final String HTTP_PORT = "HTTP_PORT";
    private static final String URL_EXPIRIRATION_DURATION = "URL_EXPIRATION";

    private final DataSource dataSource;

    {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("URL", "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "sa");

        dataSource = new HikariDataSource(config);
        new ShortURLRepository(dataSource).init();

        {
            Runtime runtime = Runtime.getRuntime();

            int processors = runtime.availableProcessors();
            long maxMemory = runtime.maxMemory();

            LOGGER.info("Number of processors: {}", processors);
            LOGGER.info("Max memory: {} bytes", maxMemory);

            executor(Executors.newFixedThreadPool(processors));
        }

        port(getIntVar(HTTP_PORT, 8082));
        Duration expiration = getUrlExpirationPeriodVar("P60DT1M");

        use(new Gzon());

        get(req -> Results.redirect("/index.html"));

        post("/shortener", getController(dataSource, expiration)::shortenUrl);
        get("/{id:[a-z0-9]+}", getController(dataSource, expiration)::redirectUrl);

        assets("/**", "/static/{0}");

    }

    public static ShortURLController getController(DataSource dataSource, Duration expiration){

        return new ShortURLController(
                expiration,
                new ShortURLService(
                        new ShortURLRepository(dataSource)
                ),
                new InstantProvider.Impl()
        );
    }

    public static void main(String[] args) {
        run(Application::new, args);
    }

    public static String getVar(String varName, String defaultValue) {

        if(System.getenv().containsKey(varName)){
            String value = System.getenv(varName);
            return value;
        }

        if(System.getProperty(varName) != null) {
            String value = System.getProperty(varName);
            return value;
        }

        return defaultValue;
    }

    static int getIntVar(String varName, int defaultValue) {
        return Integer.parseInt(getVar(varName, Integer.toString(defaultValue)));
    }

    public static Duration getUrlExpirationPeriodVar(String defaultValue){
        return getUrlExpirationPeriod(getVar(URL_EXPIRIRATION_DURATION, defaultValue));
    }

    public static Duration getUrlExpirationPeriod(String propertyValue){

        if(propertyValue == null){
            throw new IllegalArgumentException();
        }

        try {
            return Duration.parse(propertyValue);
        } catch (DateTimeException exc){
            throw new RuntimeException(String.format("Invalid ISO-8601 Duration format %s for property %s.", propertyValue, URL_EXPIRIRATION_DURATION), exc);
        }
    }

}
