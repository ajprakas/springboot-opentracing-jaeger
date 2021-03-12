package com.ajay.example.userApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import java.net.URISyntaxException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private MongoServiceClient mongoServiceClient;
    @Autowired
    private ObjectMapper mapper;
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private JaegerTracer jaegerTracer;

    @GetMapping
    public ResponseEntity<List<Object>> getAllUsers() throws Exception {
        LOG.info("hitting user-service to find all users");
        Span span = jaegerTracer.buildSpan("get all users").start();
        try(Scope scope = jaegerTracer.scopeManager().activate(span)) {
            span.log("getting all users");
            return ResponseEntity.ok(mongoServiceClient.getAllUsers());
        } finally {
            span.finish();
        }
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") String id){
        LOG.info("hitting user-service to find user "+id);
        Span span = jaegerTracer.buildSpan("get user by id").start();
        try (Scope scope = jaegerTracer.scopeManager().activate(span)){
            span.setTag("user id", id);
            span.log("calling mongoServiceClient for userId : "+id);
            return ResponseEntity.ok(mongoServiceClient.getUser(id));
        } catch (UserNotFoundException e){
                span.log(String.format("user with id %s not found", id));
                throw e;
        } catch (Exception e){
                 span.log(String.format("user with id %s not found", id));
                 throw e;
        } finally {
            span.finish();
        }
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody User user) {
        LOG.info("hitting user-service to create user "+user.getName());
        Span span = jaegerTracer.buildSpan("create user").start();
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            span.log("create user "+user.getName());
            User user1 = mongoServiceClient.createUser(user);
            return ResponseEntity.ok(user1);
        } catch (Exception e){
            span.log(String.format("not found able to create user :%s due to exception", user.getName(), e.getMessage()));
            throw e;
        } finally {
            span.finish();
        }
    }

    @PutMapping(value = "{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user, HttpServletRequest request) throws URISyntaxException {
        Span span = jaegerTracer.buildSpan("update user").start();
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            span.setTag("user id", id);
            span.log(String.format("Request to update user with id :%s", id));
            User user1 = mongoServiceClient.updateUser(user, id);
            return ResponseEntity.ok(user1);
        } catch (UserNotFoundException e){
            span.log(String.format("User with id : %s not found", id));
            throw e;
        }
        catch (Exception e){
            span.log(String.format("Exception while updating user %s with exception :%s", id, e.getMessage()));
            throw e;
        } finally {
            span.finish();
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id){
        Span span = jaegerTracer.buildSpan("Delete user request at user-service").start();
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            span.setTag("user id", id);
            span.log(String.format("delete user requet for id :%s", id));
            mongoServiceClient.deleteUser(id);
            return ResponseEntity.ok("deleted successfully");
        } catch (UserNotFoundException e){
            span.log(String.format("User with id : %s not found", id));
            throw e;
        } catch (Exception e){
            span.log(String.format("Exception while updating user %s with exception :%s", id, e.getMessage()));
            throw e;
        } finally {
            span.finish();
        }
    }
}
