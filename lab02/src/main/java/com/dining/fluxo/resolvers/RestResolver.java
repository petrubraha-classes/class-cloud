package com.dining.fluxo.resolvers;

public interface RestResolver {
    byte[] resolvePost(String payload) throws Exception;

    byte[] resolveGet(Integer id) throws Exception;

    byte[] resolvePut(Integer id, String payload) throws Exception;

    byte[] resolveDelete(Integer id) throws Exception;
}
