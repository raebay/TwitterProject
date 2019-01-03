# TwitterProject

A Spring Boot RESTful API that takes pictures and videos about dogs from Reddit using the pushshift API and posts them on Twitter for your enjoyment.

https://twitter.com/Raebay99

<b> Sample from a controller class using a CRUD repository to read and write data to the MySQL database</b>
```java
@RestController
@Controller
@Service
public class PostController {

    @Autowired
    public PostRepository postRepository;

    @Async
    @RequestMapping(value = "/addPostToDB", method = RequestMethod.POST)
    public Post addPost(@RequestBody Post newPost) {
        postRepository.save(newPost);
        return newPost;
    }
    
    @Async
    @RequestMapping(value = "/getPost/{id}", method = RequestMethod.GET)
    public Post getPost(@PathVariable("id") Integer id) {
        Post post = postRepository.findById(id).get();
        return post;
    }

    @Async
    @RequestMapping(value = "/getMostRecentPost", method = RequestMethod.GET)
    public Post getMostRecentPost() {
        int dbCount = (int)postRepository.count() + 1;
        Post post = postRepository.findById(dbCount).get();
        return post;
    }
```

<b> Sample from a controller class where scheduled tasks are run to execute the CRUD commands once per hour</b>

```java
   @Scheduled(cron = "0 0 * * * * ")
    public void addPost() throws IOException {
        Post post = getPost(id++);
        String url = "http://localhost:8080/addPostToDB";
        restTemplate.postForObject(url, post, Post.class);
        System.out.println("Post saved to db " + post.getTitle());

    }

   @Scheduled(cron = "0 0 * * * * ")
    public void postToTwitter() throws Exception {
        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr, consumerSecretStr);
        oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
        Post post = restTemplate.getForObject("http://localhost:8080/getPost/" + id , Post.class);
        System.out.println("Post retrieved from DB" + post.getTitle() + " " + post.getId());
        if(post.getTitle() != null){
            String str = URLEncoder.encode(post.getTitle() +"\n" + post.getText()+"\n" + post.getUrl(), "UTF-8");
            HttpPost httpPost = new HttpPost("https://api.twitter.com/1.1/statuses/update.json?status=" + str );
            oAuthConsumer.sign(httpPost);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println(statusCode + ':' + httpResponse.getStatusLine().getReasonPhrase());
            System.out.println(IOUtils.toString(httpResponse.getEntity().getContent()));
        }
        else{
            System.out.println("Post object was null");
        }

    }
    ```

