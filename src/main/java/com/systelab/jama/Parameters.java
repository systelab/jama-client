package com.systelab.jama;

import java.util.*;

public class Parameters {

    private String server;
    private String username;
    private String password;
    private String project;
    private String testplan;
    private String cycleName;
    private String testgroup;
    private String testcasespassed;
    private String testcasesfailed;

    private HashMap optsList = new HashMap<String, String>();
    private List<String> doubleOptsList = new ArrayList<String>();

    public Parameters(String args[]) {
        parse(args);
        if (isHelp()) {
            showHelp(true);
            System.exit(0);
        }
        check();
    }

    private void parse(String args[]) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].length() < 2)
                        throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                    if (args[i].charAt(1) == '-') {
                        if (args[i].length() < 3)
                            throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                        // --opt
                        doubleOptsList.add(args[i].substring(2, args[i].length()));
                    } else {
                        if (args.length - 1 == i)
                            throw new IllegalArgumentException("Expected arg after: " + args[i]);
                        // -opt
                        optsList.put(args[i].substring(1, args[i].length()), args[i + 1]);
                        i++;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void check() {
        try {
            server = checkParameter("server");
            username = checkParameter("username");
            password = checkParameter("password");
            project = checkParameter("project");
            testplan = checkParameter("testplan");
            testgroup = checkParameter("testgroup");
            cycleName = checkParameter("cycleName");
            testcasespassed = checkParameter("testcasespassed");
            testcasesfailed = checkParameter("testcasesfailed");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            showHelp(false);
            System.exit(0);
        }
    }

    private String checkParameter(String parameter) throws MandatoryException {
        try {
            return optsList.get(parameter).toString();
        } catch (Exception ex) {
            throw new MandatoryException(parameter + " parameter is mandatory");
        }
    }

    private boolean isHelp() {
        return doubleOptsList.contains("help");
    }

    private void showHelp(boolean fromHelp) {
        if (fromHelp)
            System.out.println("Welcome to Jama Client.");
        System.out.println("");
        System.out.println("Main Parameters:");
        System.out.println("    " + "-server for Jama Countour (mandatory)");
        System.out.println("    " + "-username for the username (mandatory)");
        System.out.println("    " + "-password for the password (mandatory)");
        System.out.println("    " + "-project for the project id (mandatory)");
        System.out.println("    " + "-testplan for the test plan id (mandatory)");
        System.out.println("    " + "-testgroup for the test group id (mandatory)");
        System.out.println("    " + "-cycleName for the Cycle Name (mandatory)");
        System.out.println("    " + "-testcasespassed for the comma separated test case list (mandatory)");
        System.out.println("    " + "-testcasesfailed for the comma separated test case list (mandatory)");
        System.out.println("    " + "-help to show this output.");
        System.out.println("");
        System.out.println("Example:");
        System.out.println("    " + "jama-client -server https://jama.systelab.net/contour/rest/latest -username peter -password peter -project 30 -testplan 50965 -testgroup 676 -cycleName CycleX -testcasespassed TC_12,TC-34,TC-25");
        System.out.println("");
    }

    public String getServer() {
        return server;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCycleName() {
        return cycleName;
    }

    public Integer getProject() {
        return new Integer(project);
    }

    public Integer getTestplan() {
        return new Integer(testplan);
    }

    public Integer getTestgroup() {
        return new Integer(testgroup);
    }

    public List<String> getTestcasesPassed() {
        return getList(testcasespassed);
    }

    public List<String> getTestcasesFailed() {
        return getList(testcasesfailed);
    }

    private List<String> getList(String commaSeparated) {
        return Arrays.asList(commaSeparated.split("\\s*,\\s*"));
    }
}
