# ONAM4Android

ONAM (Object Nested Access Management) is a lightweight ORM & Persistence API for Sqlite. ONAM4Android is designed to work seamlessly with android projects for ORM and data persistence.

### Prerequisites

Requires a Gradle based project in Android Studio with SQLite. Minimum supported SDK version is 14.

### Installing

Add Jitpack to gradle -- preferably in app.gradle

```
repositories {
    .....
    maven {
        url "https://jitpack.io"
    }
}
```

And
```
dependencies {
    compile 'com.github.basilgregory:ONAM4Android:2.0'
}
```


## How to integrate with your project - Based on demo application in code base

Lets consider the below database requirements.  
Database Name: **blog_db**  
Tables: **post, comment, user**.  


### @DB 

```
@DB(name = "blog_db",
        tables = {Post.class,Comment.class,User.class
}, version = 1)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    ...
}
```

### Entity.init()

You need to call init() function in same class where @DB annotation is defined.

If @DB annonation is defined in Activity Class file.
```
@DB(name = "blog_db",
        tables = {Post.class,Comment.class,User.class
}, version = 1)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Entity.init(this);
    }
    ...
}
```
Incase @DB annonation is defined in separate Class file.

```
@DB(name = "blog_db",
        tables = {Post.class,Comment.class,User.class
}, version = 1)
public class InitClass {

    public InitClass(Activity activity) {
        Entity.init(activity, this);
    }
    ...
}
```

For information on how to activate logs [Activate Logs](https://github.com/basilgregory/ONAM4Android/wiki/Logs) using log()

## Tables

**ONAM** provides 2 kinds of interfaces to take care of you all your ORM requirements. One an abstract class named **Entity** and a series of annotations that you need to integrate in your code.

The current **blog_db** database has 3 entities (or tables). So lets create 3 classes extending **Entity** class, and add the annotation **@Table** on each of them.

```
@Table
public class Post extends Entity {
    .....    
}

@Table
public class Comment extends Entity {
    .....    
}

@Table
public class User extends Entity {
    .....    
}
```

Now add columns as class fields.

```
@Table
public class Post extends Entity {
    private String title;
    private String post;
    private long createdAt;
    private List<User> followers;
    private List<Comment> comments;
    private User user;
    
    private transient String transientPost;

    .....
}
```

## Transient fields

All fields will be converted to database columns. All fields with transient modifier *( in this case* private transient String transientPost *)* will be ommited out. You may use such fields to do your bidding at freewill.

Now generate getter and setters for all fields ( This is mandatory for all fields except transient fields).


## Mappings

### Types of mappings that are needed,
Post has ManyToOne mapping with User (as owner of post)  
Post has OneToMany mapping with Comment (as comments of post)  
Post has ManyToMany mapping with User (as followers of post)  

Comment has ManyToOne mapping with Post (as comments of post)  
Comment has ManyToOne mapping with User (as owner of comment)  

User has OneToMany mapping with Post (as owner of post)  
User has OneToMany mapping with Comments (as owner of comment)  
User has ManyToMany mapping with Post (as followed posts)  


For more on entity mappings, see [wiki on Mappings](https://github.com/basilgregory/ONAM4Android/wiki/Entity-Mappings)


## JSON parser

We have added support to convert Entity objects/delegate objects to JSONObject/JSONArray.

```
    JSONObject postObject = Entity.toJSON(post);
```
were 'post' is object of **Post** Entity, and this will return a JSONObject with the values mapped to corresponding fields.  
For detailed information on how to specify the fields to be marked for JSON convertion [JSONParser Docs](https://github.com/basilgregory/ONAM4Android/wiki/JSON-Parser)


### Contributing

Please read [CONTRIBUTING.md](https://github.com/basilgregory/ONAM4Android/blob/master/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/basilgregory/ONAM4Android/tags). 

### Authors

* **Robin Alex Panicker** - [BasilGregory Software Labs Pvt Ltd](https://github.com/rp-bg)
* **Don Peter** - [BasilGregory Software Labs Pvt Ltd](https://github.com/dp-bg)

### License

This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/basilgregory/ONAM4Android/blob/master/LICENSE) file for details

