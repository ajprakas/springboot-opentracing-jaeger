package com.ajay.example.userApp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class MongoServiceClient {

    @Value("${db.host}")
    private String dbHost;

    @Value("${db.port}")
    private String dbPort;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JaegerTracer jaegerTracer;

    public MongoServiceClient() {
        baseUrl = "http://"+dbHost+":"+dbPort+"/users";
    }

    private String baseUrl;

    public User getUser(String id){
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        String url = baseUrl+"/"+id;
        Span span = jaegerTracer.buildSpan("get user by id").start();
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
            Tags.HTTP_METHOD.set(span, HttpMethod.GET.name());
            Tags.HTTP_URL.set(span, url);

            HttpHeaders headers = getHeaders(span);
            ResponseEntity<User> response  = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), User.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e){
            span.log(String.format("Failed to get user with id : %s from mongo-service", id));
            Tags.HTTP_STATUS.set(span, HttpStatus.NOT_FOUND.value());
            Tags.ERROR.set(span, true);
            throw new UserNotFoundException();
        } catch (Exception e){
            span.log("Failed to get all users from mongo-service");
            Tags.HTTP_STATUS.set(span, HttpStatus.INTERNAL_SERVER_ERROR.value());
            Tags.ERROR.set(span, true);
            throw e;
        } finally {
            span.finish();
        }
    }

    public List<Object> getAllUsers() throws Exception {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        Span span = jaegerTracer.buildSpan("get all users from mongo client").start();
        try(Scope scope = jaegerTracer.scopeManager().activate(span)) {
            Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
            Tags.HTTP_METHOD.set(span, "GET");
            Tags.HTTP_URL.set(span, baseUrl);

            HttpHeaders headers = getHeaders(span);

            ResponseEntity<List> response = restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<List>(headers), List.class);

            return response.getBody();
        } catch (Exception e) {
            Tags.ERROR.set(jaegerTracer.activeSpan(), true);
            jaegerTracer.activeSpan().log("Failed to get all users from mongo-service");
            throw new Exception();
        } finally {
            span.finish();
        }
    }

    private HttpHeaders getHeaders(Span span) {
        HttpHeaders headers = new HttpHeaders();
        TextMapCarrrier carrier = new TextMapCarrrier(headers);
        jaegerTracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, carrier);
        return headers;
    }

    public User createUser(User user) {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        Span span = jaegerTracer.buildSpan("create user request to mongoServiceClient").start();
        try(Scope scope = jaegerTracer.activateSpan(span)){
            HttpHeaders headers = getHeaders(span);
            ResponseEntity<User> response = restTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<User>(user, headers), User.class);
            return response.getBody();
        } catch (Exception e){
            Tags.ERROR.set(jaegerTracer.activeSpan(), true);
            span.log(String.format("Failed to get all users from mongo-service with exception:%s",e.getMessage()));
            throw e;
        }
    }

    public User updateUser(User user, String id) throws URISyntaxException {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users"+"/"+id;
        Span span = jaegerTracer.buildSpan("update user request to mongoServiceClient").start();
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            Tags.HTTP_METHOD.set(span, HttpMethod.PUT.name());
            Tags.HTTP_URL.set(span, baseUrl);

            HttpHeaders header = getHeaders(span);
            ResponseEntity<User> response = restTemplate.exchange(baseUrl, HttpMethod.PUT, new HttpEntity<User>(user, header), User.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e){
            span.log(String.format("User with id %s not found",id));
            Tags.ERROR.set(span, true);
            throw new UserNotFoundException();
        } catch (Exception e) {
            span.log("Exception while updating user");
            Tags.ERROR.set(span, true);
            throw e;
        } finally {
            span.finish();
        }
    }

    public void deleteUser(String id){
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users"+"/"+id;
        Span span = jaegerTracer.buildSpan("delete user request to mongoServiceClient").start();
        try (Scope scope = jaegerTracer.scopeManager().activate(span)){
            Tags.HTTP_METHOD.set(span, HttpMethod.DELETE.name());
            Tags.HTTP_URL.set(span, baseUrl);

            HttpHeaders headers = getHeaders(span);
            restTemplate.exchange(baseUrl, HttpMethod.DELETE, new HttpEntity<String>(headers), String.class);
        } catch (HttpClientErrorException.NotFound e){
            span.log(String.format("User with id %s not found",id));
            Tags.ERROR.set(span, true);
            throw new UserNotFoundException();
        } catch(Exception e){
            span.log("Exception while updating user");
            Tags.ERROR.set(span, true);
            throw e;
        } finally {
            span.finish();
        }
    }



}
