package rae.vroman.random;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.commons.io.IOUtils;


@RestController
public class ScheduledTasks {

    private static String consumerKeyStr = "5QmjQqdKqYNklbdZXccylu7dF";
    private static String consumerSecretStr = "OltKkbgCV5HQPoqfYYUu7JWCzrlTb2D48YK6C6M2hUjyHtbgxT";
    private static String accessTokenStr = "719527259556724736-S4IZ4hue8ndqiTYQyYTvg0ZQgTYg2kv";
    private static String accessTokenSecretStr = "ghVYM4o4P5Dz0cjVJt1q09u161Mib3w47N09C0PYoUfiQ";

    int id = 0;
    static RestTemplate restTemplate = new RestTemplate();
    String subRed = "dogpictures";

    //@Scheduled(cron = "*/30 * * * * *")
    @Scheduled(cron = "0 0 * * * * ")
    public void addPost() throws IOException {
        Post post = getPost(id++);
        String url = "http://localhost:8080/addPostToDB";
        restTemplate.postForObject(url, post, Post.class);
        System.out.println("Post saved to db " + post.getTitle());

    }


   // @Scheduled(cron = "*/30 * * * * *")
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
}


