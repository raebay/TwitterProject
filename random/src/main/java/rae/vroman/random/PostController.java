package rae.vroman.random;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Random;

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
        int num = rand.nextInt(id + 1);
        Post post = postRepository.findById(num).get();
        if(post != null){
            return post;
        }
        else{
            int num2 = rand.nextInt(id + 1);
            post = postRepository.findById(num2).get();
            return post;
        }
    }

    @RequestMapping(value = "/getPost/{id}", method = RequestMethod.GET)
    public Post getPost(@PathVariable("id") int id) {
        Post post = postRepository.findById(id).get();
        return post;
    }

    @RequestMapping(value = "/getDatabaseCount", method = RequestMethod.GET)
    public long getDatabaseCount(){
        return postRepository.count();
    }

    @RequestMapping(value = "/updatePost", method = RequestMethod.PUT)
    public Post updatePost(@RequestBody Post newPost) {

        for(Post po : postRepository.findAll()){
            if(po.getId() == newPost.getId()){
                po = newPost;
                postRepository.save(po);
                break;
            }
        }
        return newPost;
    }

    @RequestMapping(value = "/deletePost/{id}", method = RequestMethod.DELETE)
    public String deletePost(@PathVariable("id") int id) {
        for(Post po : postRepository.findAll()){
            if(po.getId() == id){
                postRepository.delete(po);
            }
        }
        return id + " Has been deleted";
    }
}
