package com.dining.fluxo;

import com.sun.net.httpserver.HttpServer;

import com.dining.fluxo.resolvers.TableResolver;
import com.dining.fluxo.resolvers.WaiterResolver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class App {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tables", new RequestHandler<>(TableResolver.class));
        httpServer.createContext("/waiters", new RequestHandler<>(WaiterResolver.class));
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
        System.out.println("Server started on port " + PORT);
    }
}
