package org.helioviewer.jhv.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.annotation.Nonnull;

import org.helioviewer.jhv.base.Pair;
import org.helioviewer.jhv.log.Log;
import org.helioviewer.jhv.threads.JHVThread;

public class SourcesDatabase extends Thread {

    private static class NamedDbThreadFactory extends JHVThread.NamedThreadFactory {

        NamedDbThreadFactory(String _name) {
            super(_name);
        }

        @Override
        public SourcesDatabase newThread(@Nonnull Runnable r) {
            SourcesDatabase thread = new SourcesDatabase(r, name);
            thread.setDaemon(true);
            return thread;
        }
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedDbThreadFactory("SourcesDatabase"));

    private static Connection connection;
    private static PreparedStatement insert;
    private static PreparedStatement select;

    private SourcesDatabase(Runnable r, String name) {
        super(r, name);
        try {
            connection = DriverManager.getConnection("jdbc:sqlite::memory:");

            try (Statement create = connection.createStatement()) {
                create.setQueryTimeout(30);
                create.executeUpdate("drop table if exists Sources"); // debug
                create.executeUpdate("CREATE TABLE Sources(sourceId INTEGER, server STRING, observatory STRING, dataset STRING, start INTEGER, end INTEGER, UNIQUE(sourceId, server) ON CONFLICT REPLACE)");
            }

            insert = connection.prepareStatement("INSERT INTO Sources(sourceId, server, observatory, dataset, start, end) VALUES(?,?,?,?,?,?)");
            insert.setQueryTimeout(30);
            select = connection.prepareStatement("SELECT sourceId,server FROM Sources WHERE server=? AND observatory LIKE ? AND dataset LIKE ?");
            select.setQueryTimeout(30);
        } catch (SQLException e) {
            Log.error("Could not create database connection", e);
            try {
                connection.close();
            } catch (Exception ignore) {
            }
            connection = null;
        }
    }

    public static void insert(int sourceId, @Nonnull String server, @Nonnull String observatory, @Nonnull String dataset, long start, long end) {
        FutureTask<Void> ft = new FutureTask<>(new Insert(sourceId, server, observatory, dataset, start, end));
        executor.execute(ft);
        try {
            ft.get();
        } catch (InterruptedException | ExecutionException e) {
            e.getCause().printStackTrace();
        }
    }

    private static class Insert implements Callable<Void> {

        private final int sourceId;
        private final String server;
        private final String observatory;
        private final String dataset;
        private final long start;
        private final long end;

        Insert(int _sourceId, @Nonnull String _server, @Nonnull String _observatory, @Nonnull String _dataset, long _start, long _end) {
            sourceId = _sourceId;
            server = _server;
            observatory = _observatory;
            dataset = _dataset;
            start = _start;
            end = _end;
        }

        @Override
        public Void call() {
            if (connection == null)
                return null;

            try {
                insert.setInt(1, sourceId);
                insert.setString(2, server);
                insert.setString(3, observatory);
                insert.setString(4, dataset);
                insert.setLong(5, start);
                insert.setLong(6, end);
                insert.executeUpdate();
            } catch (SQLException e) {
                Log.error("Failed to insert", e);
            }
            return null;
        }

    }

    public static ArrayList<Pair<Integer, String>> select(@Nonnull String server, @Nonnull String observatory, @Nonnull String dataset) {
        FutureTask<ArrayList<Pair<Integer, String>>> ft = new FutureTask<>(new Select(server, observatory, dataset));
        executor.execute(ft);
        try {
            return ft.get();
        } catch (InterruptedException | ExecutionException e) {
            e.getCause().printStackTrace();
        }
        return new ArrayList<>();
    }

    private static class Select implements Callable<ArrayList<Pair<Integer, String>>> {

        private final String server;
        private final String observatory;
        private final String dataset;

        Select(@Nonnull String _server, @Nonnull String _observatory, @Nonnull String _dataset) {
            server = _server;
            observatory = _observatory;
            dataset = _dataset;
        }

        @Override
        public ArrayList<Pair<Integer, String>> call() {
            ArrayList<Pair<Integer, String>> res = new ArrayList<>();
            if (connection == null)
                return res;

            try {
                select.setString(1, server);
                select.setString(2, '%' + observatory + '%');
                select.setString(3, '%' + dataset + '%');

                try (ResultSet rs = select.executeQuery()) {
                    while (rs.next())
                        res.add(new Pair<>(rs.getInt(1), rs.getString(2)));
                }
            } catch (SQLException e) {
                Log.error("Failed to select", e);
            }
            return res;
        }

    }

}
