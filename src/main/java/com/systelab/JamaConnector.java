package com.systelab;

import com.google.gson.internal.LinkedTreeMap;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.TestplansApi;
import io.swagger.client.api.TestrunsApi;
import io.swagger.client.auth.HttpBasicAuth;
import io.swagger.client.model.*;

import java.text.SimpleDateFormat;
import java.util.*;

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

        Integer[] testGroupToIncude = new Integer[1];
        testGroupToIncude[0] = parameters.getTestgroup();

        createTestCycle(parameters.getProject(), parameters.getTestplan(), parameters.getCycleName(), new Date(), new Date(), testGroupToIncude);

        setAllTestRunInTheLastCycleOfTheTestPlan(parameters.getTestplan(), parameters.getTestcasesPassed(), parameters.getTestcasesFailed());
    }

    private String getDateAsAFieldValue(Date date) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    private Integer getLastTestCycleByTestPlanId(Integer testPlanId) {
        TestplansApi api = new TestplansApi();

        try {
            List<TestCycle> list = api.getTestCycles(testPlanId, null, null, null).getData();
            if (list.size() > 0)
                return list.get(list.size() - 1).getId();
            else
                return null;

        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createTestCycle(Integer project, Integer testPlanId, String testCycleName, Date startDate, Date endDate, Integer[] testGroupToIncude) {
        TestplansApi api = new TestplansApi();

        RequestTestCycle requestTestCycle = new RequestTestCycle();

        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("name", testCycleName);
        fields.put("startDate", getDateAsAFieldValue(startDate));
        fields.put("endDate", getDateAsAFieldValue(endDate));
        fields.put("project", project);
        requestTestCycle.setFields(fields);

        TestRunGenerationConfig config = new TestRunGenerationConfig();
        for (int i = 0; i < testGroupToIncude.length; i++)
            config.addTestGroupsToIncludeItem(testGroupToIncude[i]);

        List<TestRunGenerationConfig.TestRunStatusesToIncludeEnum> statuses = new ArrayList<TestRunGenerationConfig.TestRunStatusesToIncludeEnum>();
        statuses.add(TestRunGenerationConfig.TestRunStatusesToIncludeEnum.BLOCKED);
        statuses.add(TestRunGenerationConfig.TestRunStatusesToIncludeEnum.FAILED);
        statuses.add(TestRunGenerationConfig.TestRunStatusesToIncludeEnum.INPROGRESS);
        statuses.add(TestRunGenerationConfig.TestRunStatusesToIncludeEnum.NOT_RUN);
        statuses.add(TestRunGenerationConfig.TestRunStatusesToIncludeEnum.PASSED);
        config.setTestRunStatusesToInclude(statuses);

        requestTestCycle.setTestRunGenerationConfig(config);

        try {
            api.createTestCycle(requestTestCycle, testPlanId);

        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private List<TestRun> getTestRuns(Integer testCycle) {
        TestrunsApi api = new TestrunsApi();
        List<Integer> testCyclesList = new ArrayList<Integer>();
        testCyclesList.add(testCycle);

        try {
            TestRunDataListWrapper list = api.getTestRuns(testCyclesList, null, null, null, null, null, null);
            return list.getData();

        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setTestRunStatus(TestRun testRun, TestRunGenerationConfig.TestRunStatusesToIncludeEnum status) {

        Map<String, Object> fields = new HashMap<String, Object>();

        ArrayList<LinkedTreeMap> steps = (ArrayList<LinkedTreeMap>) testRun.getFields().get("testRunSteps");

        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).put("status", status);
        }

        fields.put("testRunSteps", steps);
        TestrunsApi api = new TestrunsApi();

        RequestTestRun body = new RequestTestRun();
        body.setFields(fields);
        try {
            api.updateTestRun(body, testRun.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private void setAllTestRunInTheLastCycleOfTheTestPlan(Integer testPlanId, List<String> passedTestCase, List<String> failedTestCase) {
        Integer testCycleId = getLastTestCycleByTestPlanId(testPlanId);
        List<TestRun> runs = getTestRuns(testCycleId);
        for (int i = 0; i < runs.size(); i++) {
            System.out.println("Setting Test Case " + runs.get(i).getFields().get("name"));
            if (passedTestCase.contains(runs.get(i).getFields().get("name")))
                setTestRunStatus(runs.get(i), TestRunGenerationConfig.TestRunStatusesToIncludeEnum.PASSED);
            if (failedTestCase.contains(runs.get(i).getFields().get("name")))
                setTestRunStatus(runs.get(i), TestRunGenerationConfig.TestRunStatusesToIncludeEnum.FAILED);
        }
    }

    public static void main(String[] args) {
        new JamaConnector(args);
    }
}
