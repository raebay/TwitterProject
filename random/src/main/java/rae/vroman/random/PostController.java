package rae.vroman.random;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;
import java.util.Random;

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
    @RequestMapping(value = "/getRandPost", method = RequestMethod.GET)
    public Post getPost() {
        int id = (int) postRepository.count();
        Random rand = new Random();
        Post post = new Post();
        int num = -1;
        while(!postRepository.existsById(num)){
            num = rand.nextInt(id +1);
            post = postRepository.findById(num).get();
        }
        return post;
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
