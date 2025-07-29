package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        String webhookRegistrationUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Tushar Goyal");
        requestBody.put("regNo", "2210992469");
        requestBody.put("email", "tushar2469.be22@chitkara.edu.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> registrationRequest = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(webhookRegistrationUrl, registrationRequest, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String webhook = (String) response.getBody().get("webhook");
                String accessToken = (String) response.getBody().get("accessToken");

                System.out.println("Webhook URL: " + webhook);
                System.out.println("Access Token: " + accessToken);

                String finalSQLQuery =
                        "SELECT p.AMOUNT AS SALARY, " +
                        "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                        "FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365.25) AS AGE, " +
                        "d.DEPARTMENT_NAME " +
                        "FROM PAYMENTS p " +
                        "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                        "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                        "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                        "ORDER BY p.AMOUNT DESC LIMIT 1";

                Map<String, String> submissionBody = new HashMap<>();
                submissionBody.put("finalQuery", finalSQLQuery);

                HttpHeaders submissionHeaders = new HttpHeaders();
                submissionHeaders.setContentType(MediaType.APPLICATION_JSON);
                submissionHeaders.setBearerAuth(accessToken);

                HttpEntity<Map<String, String>> submissionRequest = new HttpEntity<>(submissionBody, submissionHeaders);

                ResponseEntity<String> submissionResponse = restTemplate.postForEntity(webhook, submissionRequest, String.class);
                System.out.println("Submission Response: " + submissionResponse.getBody());

            } else {
                System.out.println("Failed to generate webhook: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.out.println("Error during execution: " + e.getMessage());
        }
    }
}
