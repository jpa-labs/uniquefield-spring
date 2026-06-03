package io.github.jpa_labs.jpafieldconstraints;

import feign.Feign;
import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FeignHeadRemoteLookupConfiguration {

  /** Resource id that returns HTTP 200 for {@code HEAD /resources/{id}}. */
  static final String EXISTING_RESOURCE_ID = "exists-id";

  @Bean
  MockWebServer headExistsMockWebServer() throws IOException {
    MockWebServer server = new MockWebServer();
    server.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            if (!"HEAD".equals(request.getMethod())) {
              return new MockResponse().setResponseCode(405);
            }
            String path = request.getPath();
            if (!path.startsWith("/resources/")) {
              return new MockResponse().setResponseCode(404);
            }
            String id = path.substring("/resources/".length());
            if (EXISTING_RESOURCE_ID.equals(id)) {
              return new MockResponse().setResponseCode(204);
            }
            return new MockResponse().setResponseCode(404);
          }
        });
    server.start();
    return server;
  }

  @Bean
  HeadResourceClient headResourceClient(MockWebServer headExistsMockWebServer) {
    return Feign.builder()
        .target(HeadResourceClient.class, headExistsMockWebServer.url("/").toString());
  }

  @Bean
  TestExistsRemoteLookup testExistsRemoteLookup(HeadResourceClient headResourceClient) {
    return new TestExistsRemoteLookup(headResourceClient);
  }

  @Bean
  TestAllExistsRemoteLookup testAllExistsRemoteLookup(HeadResourceClient headResourceClient) {
    return new TestAllExistsRemoteLookup(headResourceClient);
  }
}
