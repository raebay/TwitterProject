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
        int dbCount = (int)postRepository.count();
        Post post = postRepository.findById(dbCount).get();
        return post;
    }
```

<b> Sample from a controller class where scheduled tasks are run to execute the CRUD commands every 12 hours</b>

```java
//Gets most recent post from specified subreddit and saves it to database
    @Scheduled(cron = "0 0 */12 * * * ")
    public void addPost() throws IOException {
        Post post = getPost(id);
        String url = "http://localhost:8080/addPostToDB";
        restTemplate.postForObject(url, post, Post.class);
        System.out.println("Post saved to db " + post.getTitle());

    }

//Posts most recent Reddit post saved to the database on to Twitter
    @Scheduled(cron = "0 0 */12 * * * ")
    public void postToTwitter() throws Exception {
        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr, consumerSecretStr);
        oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
        Post post = restTemplate.getForObject("http://localhost:8080/getMostRecentPost", Post.class);
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

//Take first post from specific subreddit and parses JSON to java object
    public Post getPost(int id) throws IOException {
        URL url = new URL("https://api.pushshift.io/reddit/search/submission/?subreddit="+ subRed + "&after=1d&sort=desc&sort_type=num_comments&is_video=false&size=1");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String fullStr = "";
        while ((line = inputReader.readLine()) != null) {
            fullStr += line;
        }
        JSONObject json = new JSONObject(fullStr);
        String postURL = json.getJSONArray("data").getJSONObject(0).getString("url");
        String title = json.getJSONArray("data").getJSONObject(0).getString("title");
        String text = json.getJSONArray("data").getJSONObject(0).getString("selftext");

        if(text.length() > 235){
            text = text.substring(0, 235);
        }
        if(postURL.length() > 235){
            postURL = null;
        }
            Post post = new Post(id, title, text, postURL);
            inputStream.close();
            inputReader.close();
            return post;
    }
    ```

