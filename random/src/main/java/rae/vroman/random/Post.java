package rae.vroman.random;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.io.Serializable;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Entity
public class Post {


    @Id@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    String title, text, url;

    public Post(int id, String title, String text, String url) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.url = url;
    }

    public Post() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
