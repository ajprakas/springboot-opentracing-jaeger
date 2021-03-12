package com.ajay.example.mongo;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
@RequestMapping(value = "/users")
public class UserDBController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JaegerTracer jaegerTracer;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @GetMapping
    public List<User> getAllUsers(HttpServletRequest request) {
        Span span = startServerSpan(request, "get all users at mongo-service");
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            LOG.info("Finding all users from mongo-service");
            span.setTag("method", "get all users");
            Tags.HTTP_METHOD.set(span, "GET");
            Tags.HTTP_URL.set(span, request.getRequestURL().toString());
            span.log("calling userRepo findAll");
            return userRepo.findAll();
        } catch (Exception e){
            Tags.ERROR.set(span, true);
            span.log("Exception occurred while getting all users from db");
            Tags.HTTP_STATUS.set(span, 500);
            throw e;
        } finally {
            span.finish();
        }
    }

    @GetMapping(value = "{id}")
    public User getUser(@PathVariable String id, HttpServletRequest request) {
        Span span = startServerSpan(request, "get user by id at mongo-service");
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            Tags.HTTP_METHOD.set(span, "GET");
            Tags.HTTP_URL.set(span, request.getRequestURL().toString());
            span.log("calling userRepo findById");
            Optional<User> entity = userRepo.findById(id);
            if(!entity.isPresent())
                throw new UserNotFoundException();
            return entity.get();
        } catch (UserNotFoundException e){
            span.log(String.format("Not found user with id :%s in db", id));
            Tags.HTTP_STATUS.set(span, HttpStatus.NOT_FOUND.value());
            throw e;
        }
        catch (Exception e){
            Tags.ERROR.set(span, true);
            span.log(String.format("Exception occurred while getting user with id :%s from db", id));
            Tags.HTTP_STATUS.set(span, HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw e;
        } finally {
            span.finish();
        }
    }

    @PostMapping
    public ResponseEntity addUser(@RequestBody User user, HttpServletRequest request) throws Exception {
        Span span = startServerSpan(request, "add user at mongo-service");
        try (Scope scope = jaegerTracer.scopeManager().activate(span)){
            Tags.HTTP_METHOD.set(span, HttpMethod.POST.name());
            Tags.HTTP_URL.set(span, request.getRequestURL().toString());
            span.setTag("payload", user.getName());
            Optional<User> entity = userRepo.findByName(user.getName());
            if(entity.isPresent()){
                throw new Exception(String.format("user with name %s already present in DB", user.getName()));
            }
            userRepo.save(user);
        } catch (Exception e){
            Tags.ERROR.set(span, true);
            span.log(String.format("Exception occurred while creating user %s into DB with exception: %s", user.getName(), e.getMessage()));
            Tags.HTTP_STATUS.set(span, HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw e;
        } finally {
            span.finish();
        }
        User user1;
        try {
             user1 = userRepo.save(user);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(user1);
    }

    @PutMapping(value = "{id}")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody User userReq, HttpServletRequest request) throws Exception {
        Span span = startServerSpan(request, "update user at mongo-service");
        try (Scope scope = jaegerTracer.scopeManager().activate(span)){
            span.log(String.format("updatng user with id %s", id));
            Tags.HTTP_METHOD.set(span, HttpMethod.PUT.name());
            Tags.HTTP_URL.set(span, request.getRequestURL().toString());
            span.setTag("payload", String.format("User[name:%s]",userReq.getName()));

            Optional<User> entity = userRepo.findById(id);
            if(!entity.isPresent()){
                throw new UserNotFoundException();
            }
            Optional<User> entity1 = userRepo.findByName(userReq.getName());
            if(entity1.isPresent()){
                throw new Exception(String.format("user with name : %s already present in DB", userReq.getName()));
            }

            User user = userRepo.save(entity.get());
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e){
            span.log(String.format("Not found user with id :%s in db", id));
            Tags.HTTP_STATUS.set(span, HttpStatus.NOT_FOUND.value());
            throw e;
        } catch (Exception e){
            Tags.ERROR.set(span, true);
            span.log(String.format("Exception occurred updating user with id :%s due to exception: %s", id, e.getMessage()));
            Tags.HTTP_STATUS.set(span, HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw e;
        } finally {
            span.finish();
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity deleteUser(@PathVariable String id, HttpServletRequest request){
        Span span = startServerSpan(request, "delete user at mongo-service");
        try(Scope scope = jaegerTracer.scopeManager().activate(span)){
            Tags.HTTP_METHOD.set(span, HttpMethod.DELETE.name());
            Tags.HTTP_URL.set(span, request.getRequestURL().toString());

            Optional<User> entity = userRepo.findById(id);
            if(!entity.isPresent()){
                throw new UserNotFoundException();
            }
            userRepo.deleteById(id);
            return ResponseEntity.ok("Deleted user");
        } catch (UserNotFoundException e){
            span.log(String.format("Not found user with id :%s in db", id));
            Tags.HTTP_STATUS.set(span, HttpStatus.NOT_FOUND.value());
            throw e;
        } catch (Exception e){
            Tags.ERROR.set(span, true);
            span.log(String.format("Exception occurred updating user with id :%s due to exception: %s", id, e.getMessage()));
            Tags.HTTP_STATUS.set(span, HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw e;
        } finally {
            span.finish();
        }
    }

    private Span startServerSpan(HttpServletRequest request, String operationName){
        TextMapCarrier carrier  =  new TextMapCarrier(request);
        SpanContext parentSpanCtx = jaegerTracer.extract(Format.Builtin.HTTP_HEADERS, carrier);
        return parentSpanCtx == null ? jaegerTracer.buildSpan(operationName).withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER).start() :
                jaegerTracer.buildSpan(operationName).asChildOf(parentSpanCtx).withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER).start();
    }

}
