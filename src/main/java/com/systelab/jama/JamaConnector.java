package com.systelab.jama;

import io.swagger.client.ApiClient;
import io.swagger.client.Configuration;
import io.swagger.client.auth.HttpBasicAuth;
public class JamaConnector {

    Parameters parameters;

    public JamaConnector(String args[]) {
        // Check https://blogs.oracle.com/gc/unable-to-find-valid-certification-path-to-requested-target
        // System.setProperty("javax.net.ssl.trustStore", "/Users/aserra/EclipseProjects/jama-client/jssecacerts");

        parameters = new Parameters(args);

        ApiClient client = Configuration.getDefaultApiClient();
        client.setBasePath(parameters.getServer());
        HttpBasicAuth basic = (HttpBasicAuth) client.getAuthentication("basic");
        basic.setUsername(parameters.getUsername());
        basic.setPassword(parameters.getPassword());

        TestRunUpdater testRunUpdater = new TestRunUpdater();
        testRunUpdater.run(parameters);

    }

    public static void main(String[] args) {
        new JamaConnector(args);
    }
}
