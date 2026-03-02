package com.dining.fluxo;

import com.sun.net.httpserver.HttpServer;

import com.dining.fluxo.resources.TableResource;
import com.dining.fluxo.resources.WaiterResource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class App {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tables", new RequestHandler<>(TableResource.class));
        httpServer.createContext("/waiters", new RequestHandler<>(WaiterResource.class));
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
        System.out.println("Server started on port " + PORT);
    }
}
