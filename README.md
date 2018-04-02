# shurly

CI346 demo of using Spark with Velocity templates. The app is a very
simple URL-shortening service. The UI is a single template with a 
form to submit URLs.

Fetch the code and compile it. You need to set up the database before
starting the app. It is expecting a MySQL database server running on 
`localhost` and to which you have admin rights.

```
$ git clone https://github.com/jimburton/shurly
$ cd shurly
$ mvn compile
$ mysql -h localhost -u root -p < src/main/resources/db.sql
$ mvn exec:java
```

## Things to look out for when reading the code

+ The use of the config file, `src/main/resources/application.conf`,
and the way it is loaded in the `Application` class.
+ The use of the lightweight data access framework `Sql2o` -- read
the `Application` class to see how it is set up, then the `Sql2oModel` class
to see it being used.
+ The use of the template, `src/main/resources/templates/index.vm`,
and the way it is setup in the `main` method of `Application`.

## Exercise

At the moment our UI (a web page) is strictly for human consumption. It would be a more useful
tool, however, if we allowed other applications that need shortened URLs to use this application 
as a [microservice](https://en.wikipedia.org/wiki/Microservices). A microservice architecture is 
one in which the components of a system are separate mini-applications in their own right, 
communicating with each other to achieve their goal. This can be an effective way to build 
scalable systems, as the component parts are very loosely coupled and it is easy to
dedicate more resources to the components that need it. Another advantage is that each component
can be written using the most appropriate programming language and tools, without affecting any
other component.

We want the ability to submit a URL to be shortened and to receive the response as a JSON object.
We can do that by differentiating between requests based on the `Accept` header. If a `POST` request
is made with `Accept` set to `text/html` we will serve the response as a web page, as we currently
do. If a `POST` request arrives with `Accept` set to `application/json`, we should return the 
response as a JSON object. We can use `curl` to set the header and pass the form data:

```
$ curl -H "Accept: text/html" -d "the_url=http://brighton.ac.uk" http://localhost:4567/
<!DOCTYPE html>
<html lang="en">
<head>
... etc
```

After you have added this feature you should be able to `POST` to the same address but ask for a
JSON response, making it possible for other applications to consume and use this data:

```
$ curl -H "Accept: application/json" -d "the_url=http://brighton.ac.uk" http://localhost:4567/
{"status":"SUCCESS","url":"http://brighton.ac.uk","enc":"be6ce4cd"}
```

The `status` field should contain the error message if something went wrong:

```
curl -H "Accept: application/json" -d "the_url=bananas" http://localhost:4567/
{"status": "INVALID_URL"}
```

There are overloaded versions of the Spark methods to define routes (`get`, `post` and so on) that
take an extra argument specifying the content type that was requested:

```
get("/hello", "application/json", (request, response) -> {
    response.type("application/json");
    return "{\"message\": \"Hello World\"}";
});
```

Constructing the JSON ourselves, as in the example above, would quickly become fiddly for complex
objects. Our JSON objects will be based on instances of `ShurlyURL`, and we can use the 
[Gson](https://github.com/google/gson) library to transform objects for us. Add the Gson 
dependency to `pom.xml` (inside the `<dependencies>` tag with the other libraries that we're 
using):

```
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.8.2</version>
</dependency>  
``` 

Now we can transform our POJOs to JSON objects like so: `new Gson().toJson(pojo)`.

Change the existing `post` route in the `Application` class so that it is specialised for requests
with `Accept` set to `text/html`. Check that it still works as expected by encoding a few URLs
using `curl` or your browser. 

Now you can add a `post` route that is specialised for JSON. In this route you need to set the 
content type of the response using `response.type`. The structure will be something like this:

```
post("/", "application/json", (request, response) -> {
    response.type("application/json");
    
    // create and store the encoding as before
    // with the result in an instance of ShurlyURL called `result'
 
    return new Gson().toJson(result);
});
```

Note that the two `post` routes will share a lot of code, so you can create a helper method to remove 
duplication.