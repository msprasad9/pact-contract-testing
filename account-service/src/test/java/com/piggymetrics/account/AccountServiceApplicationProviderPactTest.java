package com.piggymetrics.account;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.repository.AccountRepository;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.net.URISyntaxException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AccountApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "server.port=8081")
@Provider("accountservice")
@PactBroker(host = "citi.pactflow.io", port="443", scheme = "https",
        authentication = @PactBrokerAuth(token = "663-sCQYf_VxQQ0nPew8sg"))
@ContextConfiguration(classes = {StatisticsServiceClient.class})
public class AccountServiceApplicationProviderPactTest {
    @MockBean
    AccountRepository accountRepository;

    @MockBean
    StatisticsServiceClient statisticsClient;


    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void testTemplate(PactVerificationContext context, HttpRequest request) {
        URI uri = ((HttpUriRequest) request).getURI();
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        try{
            ((HttpRequestBase) request).setURI(new URI("http", null, host, port, path.replaceFirst("/accounts", "/"), null, null));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        context.verifyInteraction();
    }

    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        System.setProperty("pact.verifier.publishResults", "true");
        System.setProperty("pact.provider.version", "4.0.0");
        context.setTarget(new HttpTestTarget("localhost", 8081, "/"));
        Account account = new Account();
        Mockito.when(accountRepository.save(Mockito.any())).thenReturn(account);
    }

    @State("provider accepts a new account")
    public void setupForNewAccount(){

    }

    @State("demo_acct exists")
    public void setupForUpdateAccount() {
        Account account = new Account();
        Mockito.when(accountRepository.save(Mockito.any())).thenReturn(account);
        Mockito.when(accountRepository.findByName(Mockito.anyString())).thenReturn(account);
        Mockito.when(statisticsClient.updateStatistics(Mockito.anyString(), Mockito.any())).thenReturn(new Object());
    }
}
