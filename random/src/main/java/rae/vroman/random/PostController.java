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

        Post post = new Post();
        for(Post po : postRepository.findAll()) {
            if (po.getId() == num)
            {
                post = po;
            }
        }
        return post;
    }

    @RequestMapping(value = "/getPost/{id}", method = RequestMethod.GET)
    public Post getPost(@PathVariable("id") int id) {
        Post post = new Post();
        for(Post po : postRepository.findAll()) {
            if (po.getId() == id)
            {
                post = po;
            }
        }
        return post;
    }

    @RequestMapping(value = "/getDatabaseCount", method = RequestMethod.GET)
    public long getDatabaseCount(){
        //long count = postRepository.count();
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
