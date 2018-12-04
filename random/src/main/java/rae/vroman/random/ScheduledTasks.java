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
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.io.IOUtils;


@RestController
public class ScheduledTasks {

    private static String consumerKeyStr = "5QmjQqdKqYNklbdZXccylu7dF";
    private static String consumerSecretStr = "OltKkbgCV5HQPoqfYYUu7JWCzrlTb2D48YK6C6M2hUjyHtbgxT";
    private static String accessTokenStr = "719527259556724736-S4IZ4hue8ndqiTYQyYTvg0ZQgTYg2kv";
    private static String accessTokenSecretStr = "ghVYM4o4P5Dz0cjVJt1q09u161Mib3w47N09C0PYoUfiQ";

    int id = 0;
    static RestTemplate restTemplate = new RestTemplate();
    String[] subReds = {"cleanjokes", "ShittyLifeProTips", "Showerthoughts", "dogpictures"};
    Random rand = new Random();


    //@Scheduled(cron = "0 0 * * * * ")
    @Scheduled(cron = "*/30 * * * * *")
    public void addPost() throws IOException {
        int s = rand.nextInt(subReds.length);
       // Post post = getPost(id++, Arrays.asList(subReds).get(s));
        Post post = getPost(id++);
        if(post.getTitle() != null){
            String url = "http://localhost:8080/addPostToDB";
            restTemplate.postForObject(url, post, Post.class);
            System.out.println("Post saved to db" + post.getTitle());
        }
    }


    //@Scheduled(cron = "0 0 * * * * ")
    @Scheduled(cron = "*/45 * * * * *")
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
            System.out.println("Post object was null, DO NOT POST");
        }

    }

    public Post getPost(int id) throws IOException {
        Post post = new Post();
        URL url = new URL("https://api.pushshift.io/reddit/search/submission/?q=dogs");
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
        String over18 = "";
        try{
            over18 = json.getJSONArray("data").getJSONObject(0).getString("over_18");

        }
        catch(Exception e){
            System.out.println("There is no over_18 for this reddit submission");
        }
        finally{
            if(!over18.equals("true")){
                if(text.length() > 235){
                    String subtext = text.substring(0, 235);
                }

                post = new Post(id, title, text, postURL);
                inputStream.close();
                inputReader.close();
                System.out.println("Over 18 is not true, this is in the if finally statement" + post.getTitle());
                return post;
            }
            else{
                System.out.println("Over 18 is true, this is in the else finally statement" + post.getTitle());
                return post;
            }
        }

    }

/*    public Post getPost(int id, String subRed) throws IOException {
        Post post = new Post();
        URL url = new URL("https://api.pushshift.io/reddit/search/submission/?subreddit="+ subRed + "&after=1d&sort=desc&sort_type=num_comments&is_video=false&size=1");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        S
        tring line = "";
        String fullStr = "";
        while ((line = inputReader.readLine()) != null) {
            fullStr += line;
        }
        JSONObject json = new JSONObject(fullStr);
        String postURL = json.getJSONArray("data").getJSONObject(0).getString("url");
        String title = json.getJSONArray("data").getJSONObject(0).getString("title");
        String text = json.getJSONArray("data").getJSONObject(0).getString("selftext");
        String over18 = json.getJSONArray("data").getJSONObject(0).getString("over_18");

                if(over18.equals("false")){

                    post = new Post(id, title, text, postURL);
                    inputStream.close();
                    inputReader.close();
                }
        return post;

    }*/

}
