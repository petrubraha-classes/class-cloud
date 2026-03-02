package com.dining.fluxo.resources;

public interface RestResource {
    byte[] resolvePost(String payload) throws Exception;

    byte[] resolveGet(Integer id) throws Exception;

    byte[] resolvePut(Integer id, String payload) throws Exception;

    byte[] resolveDelete(Integer id) throws Exception;
}
