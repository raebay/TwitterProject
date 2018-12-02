package rae.vroman.random;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Random;

@Component
@RestController
public class ScheduledTasks {

    private static String consumerKeyStr = "LDJ4eFCbZVNdQjqsfrRu2DikF ";
    private static String consumerSecretStr = "dGaned10ImpqbYaevUyG5jEYAXj5lnXujpXsyhfeLWj1zZZ1DC ";
    private static String accessTokenStr = "719527259556724736-KFnW0pmN3pIoRhZJ0DP320yFAaqQfvM";
    private static String accessTokenSecretStr = "U6CtKUwgErYnC7XgbmLK1Qfsmm2OiWgyRIlK5X8IlaMPw ";

    int id = 0;
    static RestTemplate restTemplate = new RestTemplate();
    String[] subReds = {"cleanjokes", "ShittyLifeProTips", "Showerthoughts", "fortunecookies", "dogpictures"};
    Random rand = new Random();


    @Scheduled(cron = "*/5 * * * * *")
    public void addPost() throws IOException {
        int s = rand.nextInt(subReds.length);
        Post post = getPost(id++, Arrays.asList(subReds).get(s));
        String url = "http://localhost:8080/addPostToDB";
        restTemplate.postForObject(url, post, Post.class);
        System.out.println("Post saved to db");
    }


    @Scheduled(cron = "*/5 * * * * *")
    public static void postToTwitter() throws Exception {
        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr, consumerSecretStr);
        oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
        Post post = restTemplate.getForObject("http://localhost:8080/getRandPost", Post.class);
        System.out.println("Post retrieved from DB");
        String str = URLEncoder.encode(post.getTitle() +"\n" + post.getText()+"\n" + post.getUrl(), "UTF-8");
        HttpPost httpPost = new HttpPost("https://api.twitter.com/1.1/statuses/update.json?status=" + str );
        oAuthConsumer.sign(httpPost);
        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
        org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPost);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        System.out.println(statusCode + ':' + httpResponse.getStatusLine().getReasonPhrase());
    }

    public Post getPost(int id, String subRed) throws IOException {
        URL url = new URL("https://api.pushshift.io/reddit/search/submission/?subreddit="+ subRed + "&after=1d&sort=desc&sort_type=num_comments&is_video=false&size=1");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String fullStr = "";
        while ((line = inputReader.readLine()) != null) {
            fullStr += line;
        }
        JSONObject json = new JSONObject(fullStr);
        String postURL = json.getJSONArray("data").getJSONObject(0).getString("url");
        String title = json.getJSONArray("data").getJSONObject(0).getString("title");
        String text = json.getJSONArray("data").getJSONObject(0).getString("selftext");


        Post post = new Post(id, title, text, postURL);
        inputStream.close();
        inputReader.close();
        return post;
    }

}
