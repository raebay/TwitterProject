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

    @RequestMapping(value = "/addPostToDB", method = RequestMethod.POST)
    public Post addPost(@RequestBody Post newPost) {
        postRepository.save(newPost);
        return newPost;
    }
    
    @RequestMapping(value = "/getRandPost", method = RequestMethod.GET)
    public Post getPost() {
        int id = (int) postRepository.count();
        Random rand = new Random();
        Post post = new Post();

        while(post.getTitle() == null){
                int num = rand.nextInt(id +1);
                post = postRepository.findById(num).get();
        }
        return post;

    }

    @RequestMapping(value = "/getPost/{id}", method = RequestMethod.GET)
    public Post getPost(@PathVariable("id") int id) {
        Post post = postRepository.findById(id).get();
        return post;
    }
```

<b> Sample from a controller class where scheduled tasks are run to execute the CRUD commands once per hour</b>

```java
    @Scheduled(cron = "0 0 * * * * ")
    public void addPost() throws IOException {
        Post post = getPost(id++);
        if(post.getTitle() != null){
            String url = "http://localhost:8080/addPostToDB";
            restTemplate.postForObject(url, post, Post.class);
            System.out.println("Post saved to db " + post.getTitle());
        }
    }


    @Scheduled(cron = "0 0 * * * * ")
    public static void postToTwitter() throws Exception {
        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr, consumerSecretStr);
        oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
        Post post = restTemplate.getForObject("http://localhost:8080/getRandPost", Post.class);
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

