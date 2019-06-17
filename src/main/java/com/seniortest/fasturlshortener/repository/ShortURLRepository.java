package com.seniortest.fasturlshortener.repository;

import com.seniortest.fasturlshortener.Application;
import com.seniortest.fasturlshortener.model.ShortURL;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Optional;

public class ShortURLRepository {

    private final DataSource dataSource;

    public ShortURLRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init(){

        try(Connection connection = new Sql2o(dataSource).beginTransaction()){

            Table table = connection.createQuery("SHOW TABLES").executeAndFetchTable();

            boolean hasTable = false;

            for(Row row : table.rows()){
                if("ShortURL".equalsIgnoreCase(row.getString("TABLE_NAME"))){
                    hasTable = true;
                    break;
                }
            }

            if(! hasTable){
                final String ddl = "CREATE TABLE ShortURL(id bigint primary key auto_increment, url varchar, createdAt bigint, expiration bigint)";
                connection.createQuery(ddl).executeUpdate();
                connection.commit();
            }

        }

    }

    public Optional<ShortURL> findById(long id){

        try(Connection connection = new Sql2o(dataSource).open()){

            final String sql = "SELECT id, url, createdAt, expiration FROM ShortURL WHERE id=:id LIMIT 1";

            Table table = connection.createQuery(sql).addParameter("id", id).executeAndFetchTable();

            if(table.rows().isEmpty()){
                return Optional.empty();
            }

            Row row = table.rows().get(0);

            ShortURL entity = new ShortURL();
            entity.setId(row.getLong("id"));
            entity.setUrl(row.getString("url"));
            entity.setCreatedAt(Instant.ofEpochMilli(row.getLong("createdAt")));
            entity.setExpireSeconds(row.getLong("expiration"));

            return Optional.of(entity);
        }
    }

    public void save(ShortURL entity){

        try(Connection connection = new Sql2o(dataSource).beginTransaction()){

            final String insertSql = "INSERT INTO ShortURL (url, createdAt, expiration) VALUES(:url, :createdAt, :expiration)";

            long insertedId = connection.createQuery(insertSql, true)
                    .addParameter("url", entity.getUrl())
                    .addParameter("createdAt", entity.getCreatedAt().toEpochMilli())
                    .addParameter("expiration", entity.getExpireSeconds())
                    .executeUpdate().getKey(Long.class);

            entity.setId(insertedId);
            connection.commit();
        }

    }

}
