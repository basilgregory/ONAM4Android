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

You need to call init() function in your launcher Activity onCreate().

If @DB annonation is defined in Activity Class file.
```
    Entity.init(this);
```
Incase @DB annonation is defined in separate Class file.

```
    Entity.init(this, objectOfClass);
```

For information on how to activate logs [Activate Logs using log()](https://github.com/basilgregory/ONAM4Android/wiki/Logs)


You need to specify the Entity classes using @DB annotation along with name and version of your database, in the same launcher Activity.

```
@DB(name = "blog_db",
        tables = {Post.class,Comment.class,User.class
}, version = 1)
```


Types of mappings that are needed,
Post has ManyToOne mapping with User (as owner of post)  
Post has OneToMany mapping with Comment (as comments of post)  
Post has ManyToMany mapping with User (as followers of post)  

Comment has ManyToOne mapping with Post (as comments of post)  
Comment has ManyToOne mapping with User (as owner of comment)  

User has OneToMany mapping with Post (as owner of post)  
User has OneToMany mapping with Comments (as owner of comment)  
User has ManyToMany mapping with Post (as followed posts)  

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

All fields will be converted to database columns. All fields with transient modifier *( in this case* private transient String transientPost *)* will be ommited out. You may use such fields to do your bidding at freewill.

Now generate getter and setters for all fields ( This is mandatory for all fields except transient fields, *your choice* ).

```
    .....
    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
```

No changes are needed in setter functions. 
For getter functions that returns entity/List<Enitity> (that has some mapping relation), like **User**, **Comment**, you need to replace the return statement. 

In this case, for the following functions.
```
public List<User> getFollowers()
public List<Comment> getComments() 
public User getUser()
```
Their complementary getters are,
In **Comment** Entity
```
public Post getPost() 
```
and in **User** Entity
```
public List<Post> getPosts() 
public List<Post> getFollowedPosts()
```

Replace return statement in getter functions of entities with 

```
    replace,
        return followers;
    with,
        return fetch(this.followers,new User(){});
```
Finally, getter functions for related entities would be,
```
    In Post Entity,
    public List<User> getFollowers() {
        return fetch(this.followers,new User(){});
    }
    In User Entity
    public List<Post> getFollowedPosts() {
        return fetch(this.followedPosts,new Post(){});
    }

    In Post Entity,
    public List<Comment> getComments() {
        return fetch(this.comments,new Comment(){});
    }
    In Comment Entity,
    public Post getPost() {
        return fetch(this.post,new Post(){});
    }

    In Post Entity,
    public User getUser() {
        return fetch(this.user,new User(){});
    }
    In User Entity,
    public List<Post> getPosts() {
        return fetch(this.posts,new Post(){});
    }

```
Now, specify the mapping for each of the getter functions,

Recall that Post has 3 types of mappings  
Post has ManyToMany mapping with User (as followers of post)  
Post has OneToMany mapping with Comment (as comments of post)  
Post has ManyToOne mapping with User (as owner of post)  

**Post** has OneToMany mapping with **Comment**  
For OneToMany mapping a foreignkey for **Post** entity is needed in **Comment** table, you may suggest a foreign key column name.
```
    @OneToMany(referencedColumnName = "post_id", targetEntity = Comment.class)
    public List<Comment> getComments() {
        return fetch(this.comments,new Comment(){});
    }
```
And In **Comments** entity, 
Comment has ManyToOne mapping with Post (as comments of post)
```
    @ManyToOne
    @Column(name = "post_id")
    public Post getPost() {
        return fetch(this.post,new Post(){});
    }
```
Here a foreign key column named *post_id* will be created in **Comment** table.  
If you are providing a column name for ManyToOne mapping, then correspondingly  
the same name as to be provided for the OneToMany mapping in related entity as  
referencedColumnName, here *post_id*.

**Post** has ManyToOne mapping with **User**  
For ManyToOne mapping a foreignkey for **User** entity is needed at **Post** table, you may suggest a foreign key column name.
```
    @ManyToOne
    public User getUser() {
        return fetch(this.user,new User(){});
    }
```
Here a foreign key column named *user_id* will be created automatically, as no explicit name is provided in **Post** table.  
Similarly, **User** entity has
```
    @OneToMany(targetEntity = Post.class)
    public List<Post> getPosts() {
        return fetch(this.posts,new Post(){});
    }
```
In short, if you are providing a @Column name to @ManyToOne mapping then the same column name has to be provided in the corresponding @OneToMany mapping as referencedColumnName. 


**Post** has ManyToMany mapping with **User** entity (as followers of post), we need a mapping table with name '*user_followers*' and the mapping to be done with **User** enitity 

```
    @ManyToMany(tableName = "post_followers", 
            targetEntity = User.class)
    public List<User> getFollowers() {
        return fetch(this.followers,new User(){});
    }
```
Correspondingly in **User** entity, the tableName should be same.
```
    @ManyToMany(tableName = "post_followers", 
            targetEntity = Post.class)
    public List<Post> getFollowedPosts() {
        return fetch(this.followedPosts,new Post(){});
    }
```

**@ManyToMany** mapping should *mandatorily* have tableName and enitity class to be mapped with.



## JSON parser

We have added support to convert Entity objects/delegate objects to JSONObject/JSONArray.

```
    JSONObject postObject = Entity.toJSON(post);
```
were 'post' is object of **Post** Entity, and this will return a JSONObject with the values mapped to corresponding fields.  
For detailed information on how to specify the fields to be marked for JSON convertion [JSONParser Docs](https://github.com/basilgregory/ONAM4Android/wiki/JSON-Parser)


## Contributing

Please read [CONTRIBUTING.md](https://github.com/basilgregory/ONAM4Android/blob/master/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/basilgregory/ONAM4Android/tags). 

## Authors

* **Robin Alex Panicker** - [BasilGregory Software Labs Pvt Ltd](https://github.com/rp-bg)
* **Don Peter** - [BasilGregory Software Labs Pvt Ltd](https://github.com/dp-bg)

## License

This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/basilgregory/ONAM4Android/blob/master/LICENSE) file for details

