package com.oycbest.springbootelasticsearch.controller;

import com.oycbest.springbootelasticsearch.document.Blog;
import com.oycbest.springbootelasticsearch.document.EsBlog;
import com.oycbest.springbootelasticsearch.repository.BlogRepository;
import com.oycbest.springbootelasticsearch.service.EsBlogService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: blog控制类
 * @Author oyc
 * @Date 2020/5/10 10:35 下午
 */
@Controller
@RequestMapping("blog")
public class EsBLogController {

    @Resource
    private BlogRepository blogRepository;

    @Resource
    private EsBlogService searchService;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate ;

    @Resource
    private RestHighLevelClient restHighLevelClient ;

    @Resource
    private  ElasticsearchOperations elasticsearchOperations;


    public EsBLogController() {

    }

 /*   public EsBLogController(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }*/



    /*@GetMapping("/my/findById")
    public EsBlog findById(String id) {
        EsBlog esBlog = elasticsearchOperations
                .queryForObject(GetQuery.getById(id.toString()), EsBlog.class);
        return esBlog;
    }*/





    @GetMapping
    public String blog(HttpServletRequest request, Model model) {

        Pageable pageable = getPageByRequest(request);
        Page<EsBlog> esBlogPage = searchService.getByKeyWord(null, pageable);
        model.addAttribute("blogContent", "blog/include");
        model.addAttribute("esBlogPage", esBlogPage);
        return "blog/index";
    }
    /**
     * @param key 关键字
     * @return
     */
    @RequestMapping("search")
    public String getByKey(HttpServletRequest request, String key, Model model) {
        Pageable pageable = getPageByRequest(request);
        Page<EsBlog> esBlogPage = searchService.queryForPage(key, pageable);
        model.addAttribute("blogContent", "blog/include");
        model.addAttribute("esBlogPage", esBlogPage);
        return "blog/index::blogContent";
    }

    @GetMapping("init")
    @ResponseBody
    private String initBlog() {
        List<Blog> blogs = blogRepository.findAll();
        List<EsBlog> esBlogs = new ArrayList<>();
        blogs.forEach(blog -> {
                    esBlogs.add(new EsBlog(blog.getId(), blog.getTitle(), blog.getContent()));
                }
        );
        searchService.save(esBlogs);
        return "init Success";
    }

    /**
     * @param blog 博客文档
     * @return
     */
    @PostMapping("save")
    @ResponseBody
    public void save(EsBlog blog) {
        searchService.save(blog);
    }

    /**
     * @param id 文档id
     * @return
     */
    @GetMapping("getById")
    @ResponseBody
    public Object getById(int id) {
        return searchService.getById(id);
    }

    /**
     * @param key 关键字
     * @return
     */
    @GetMapping("keyWord")
    @ResponseBody
    public Page<EsBlog> getByKeyWord(HttpServletRequest request, String key) {
        Pageable pageable = getPageByRequest(request);
        return searchService.getByKeyWord(key, pageable);
    }

    private Pageable getPageByRequest(HttpServletRequest request) {
        int page = StringUtils.isEmpty(request.getParameter("page")) ? 1 : Integer.parseInt(request.getParameter("page"));
        int size = StringUtils.isEmpty(request.getParameter("size")) ? 10 : Integer.parseInt(request.getParameter("size"));
        Pageable pageable = PageRequest.of(page - 1, size);
        return pageable;
    }


    /**
     *
     * @param request
     * @param title
     * @return
     */
    @GetMapping("getByTitle")
    @ResponseBody
    public Object getByTitle(HttpServletRequest request,String title) {
        Pageable pageable = getPageByRequest(request);
        return searchService.findByTitleLikeForPage(title,pageable);
    }

    /**
     *
     * @param request
     * @param content
     * @return
     */
    @GetMapping("getByContent")
    @ResponseBody
    public Object getByContent(HttpServletRequest request,String content) {

       // System.out.println(elasticsearchTemplate);
        System.out.println(restHighLevelClient);
        System.out.println(elasticsearchOperations);

        Pageable pageable = getPageByRequest(request);
        return searchService.findByContentLikeForPage(content,pageable);
    }


    @GetMapping("elasticsearchOperationsSave")
    @ResponseBody
    public void save2(){

        EsBlog esBlog = new EsBlog();
        esBlog.setId(8);
        esBlog.setTitle("title8");
        esBlog.setContent("content8");

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(String.valueOf(esBlog.getId()))
                .withObject(esBlog)
                .build();

        String documentId = elasticsearchOperations.index(indexQuery);
    }

    @GetMapping("/esBlog/{id}")
    @ResponseBody
    public EsBlog findById(@PathVariable("id")  Integer id) {
        EsBlog esBlog = elasticsearchOperations
                .queryForObject(GetQuery.getById(String.valueOf(id)), EsBlog.class);
        return esBlog;
    }


}
