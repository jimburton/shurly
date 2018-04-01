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

## Things to look out for

+ The use of the config file, `src/main/resources/application.conf`,
and the way it is loaded in the `Shurly` class.
+ The use of the lightweight data access framework `Sql2o` -- read
the `Shurly` class to see how it is set up, then the `Sql2oModel` class
to see it being used.
+ The use of the template, `src/main/resources/templates/index.vm`,
and the way it is setup in the `main` method of `Shurly`.
